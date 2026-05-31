package com.localbanana.pantry.service;

import com.localbanana.pantry.domain.entity.Category;
import com.localbanana.pantry.domain.entity.Household;
import com.localbanana.pantry.domain.entity.Ingredient;
import com.localbanana.pantry.domain.entity.IngredientConversion;
import com.localbanana.pantry.domain.entity.ShoppingListItem;
import com.localbanana.pantry.domain.entity.User;
import com.localbanana.pantry.domain.repository.CategoryRepository;
import com.localbanana.pantry.domain.repository.HouseholdRepository;
import com.localbanana.pantry.domain.repository.IngredientConversionRepository;
import com.localbanana.pantry.domain.repository.IngredientRepository;
import com.localbanana.pantry.domain.repository.ShoppingListItemRepository;
import com.localbanana.pantry.domain.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final HouseholdRepository householdRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientConversionRepository ingredientConversionRepository;
    private final UserRepository userRepository;

    public ShoppingListService(ShoppingListItemRepository shoppingListItemRepository,
                               HouseholdRepository householdRepository,
                               IngredientRepository ingredientRepository,
                               CategoryRepository categoryRepository,
                               IngredientConversionRepository ingredientConversionRepository,
                               UserRepository userRepository) {
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.householdRepository = householdRepository;
        this.ingredientRepository = ingredientRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientConversionRepository = ingredientConversionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ShoppingListItem> getActiveItems(Long householdId) {
        return shoppingListItemRepository.findByHouseholdIdAndStatusIn(
                householdId, List.of("pending", "in_basket"));
    }

    public ShoppingListItem addItem(Long householdId,
                                    Long ingredientId,
                                    NewIngredientRequest newIngredient,
                                    Long addedById,
                                    String reason) {
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found: " + householdId));

        Ingredient ingredient;
        if (ingredientId != null) {
            ingredient = ingredientRepository.findById(ingredientId)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + ingredientId));
        } else {
            ingredient = createIngredient(household, newIngredient);
        }

        User addedBy = null;
        if (addedById != null) {
            addedBy = userRepository.findById(addedById)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + addedById));
        }

        ShoppingListItem item = new ShoppingListItem();
        item.setHousehold(household);
        item.setIngredient(ingredient);
        item.setAddedBy(addedBy);
        item.setReason(reason);
        item.setStatus("pending");
        item.setAddedAt(LocalDateTime.now());
        return shoppingListItemRepository.save(item);
    }

    private Ingredient createIngredient(Household household, NewIngredientRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));

        Ingredient ingredient = new Ingredient();
        ingredient.setHousehold(household);
        ingredient.setCategory(category);
        ingredient.setName(req.name());
        ingredient.setTrackingType(req.trackingType());
        ingredient.setLocation(req.location());
        ingredient.setCanonicalUnit(req.canonicalUnit());
        ingredient.setQuantity(null);
        ingredient.setIsAvailable(false);
        ingredient.setIsFrozen(false);
        ingredient.setAddedAt(LocalDateTime.now());
        return ingredientRepository.save(ingredient);
    }

    public ShoppingListItem moveToBasket(Long itemId) {
        ShoppingListItem item = getItemById(itemId);
        if (!item.getStatus().equals("pending")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only pending items can be moved to basket");
        }
        item.setStatus("in_basket");
        return shoppingListItemRepository.save(item);
    }

    public ShoppingListItem removeFromBasket(Long itemId) {
        ShoppingListItem item = getItemById(itemId);
        if (!item.getStatus().equals("in_basket")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only in_basket items can be removed from basket");
        }
        item.setStatus("pending");
        return shoppingListItemRepository.save(item);
    }

    public List<ShoppingListItem> completeShoppingTrip(Long householdId,
                                                       List<ItemConfirmation> confirmations) {
        List<ShoppingListItem> inBasket = shoppingListItemRepository
                .findByHouseholdIdAndStatus(householdId, "in_basket");

        for (ItemConfirmation confirmation : confirmations) {
            boolean valid = inBasket.stream().anyMatch(i -> i.getId().equals(confirmation.itemId()));
            if (!valid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Item " + confirmation.itemId() + " is not in basket for this household");
            }
        }

        for (ShoppingListItem item : inBasket) {
            item.setStatus("bought");
            item.setBoughtAt(LocalDateTime.now());

            ItemConfirmation confirmation = confirmations.stream()
                    .filter(c -> c.itemId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);

            Ingredient ingredient = item.getIngredient();
            if ("stocked".equals(ingredient.getTrackingType())) {
                ingredient.setIsAvailable(true);
            } else if ("counted".equals(ingredient.getTrackingType())) {
                if (confirmation != null && confirmation.confirmedQuantity() != null) {
                    BigDecimal current = ingredient.getQuantity() != null ? ingredient.getQuantity() : BigDecimal.ZERO;
                    ingredient.setQuantity(current.add(confirmation.confirmedQuantity()));
                    ingredient.setIsAvailable(true);
                }
            } else if ("measured".equals(ingredient.getTrackingType())) {
                if (confirmation != null && confirmation.confirmedQuantity() != null) {
                    BigDecimal quantityInCanonical = resolveToCanonical(
                            ingredient, confirmation.confirmedQuantity(), confirmation.confirmedUnit());
                    BigDecimal current = ingredient.getQuantity() != null ? ingredient.getQuantity() : BigDecimal.ZERO;
                    ingredient.setQuantity(current.add(quantityInCanonical));
                    ingredient.setIsAvailable(true);
                }
            }

            ingredientRepository.save(ingredient);
            shoppingListItemRepository.save(item);
        }

        return inBasket;
    }

    public ShoppingListItem cancelItem(Long itemId) {
        ShoppingListItem item = getItemById(itemId);
        if (item.getStatus().equals("bought")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bought items cannot be cancelled");
        }
        item.setStatus("cancelled");
        return shoppingListItemRepository.save(item);
    }

    private BigDecimal resolveToCanonical(Ingredient ingredient, BigDecimal quantity, String unit) {
        if (unit == null || unit.equalsIgnoreCase(ingredient.getCanonicalUnit())) {
            return quantity;
        }
        IngredientConversion conversion = ingredientConversionRepository
                .findByIngredientIdAndFromUnit(ingredient.getId(), unit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No conversion found for " + unit + " for ingredient " + ingredient.getName()
                        + ". Please enter quantity in " + ingredient.getCanonicalUnit()));
        return quantity.multiply(conversion.getValueInCanonicalUnit());
    }

    private ShoppingListItem getItemById(Long itemId) {
        return shoppingListItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list item not found: " + itemId));
    }

    public record NewIngredientRequest(
            String name,
            Long categoryId,
            String trackingType,
            String location,
            String canonicalUnit
    ) {}

    public record ItemConfirmation(Long itemId, BigDecimal confirmedQuantity, String confirmedUnit) {}
}
