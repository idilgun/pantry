package com.localbanana.pantry.controller;

import com.localbanana.pantry.domain.entity.ShoppingListItem;
import com.localbanana.pantry.dto.ShoppingListItemDto;
import com.localbanana.pantry.service.ShoppingListService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/households/{householdId}/shopping-list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GetMapping
    public List<ShoppingListItemDto> getActiveItems(@PathVariable Long householdId) {
        return shoppingListService.getActiveItems(householdId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingListItemDto addItem(@PathVariable Long householdId,
                                       @RequestBody AddItemRequest request) {
        boolean hasExisting = request.ingredientId() != null;
        boolean hasNew = request.newIngredient() != null;

        if (hasExisting == hasNew) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide either ingredientId or newIngredient, not both or neither");
        }

        ShoppingListService.NewIngredientRequest newIngredient = null;
        if (hasNew) {
            AddItemRequest.NewIngredientPayload p = request.newIngredient();
            newIngredient = new ShoppingListService.NewIngredientRequest(
                    p.name(), p.categoryId(), p.trackingType(), p.location(), p.canonicalUnit());
        }

        return toDto(shoppingListService.addItem(
                householdId,
                request.ingredientId(),
                newIngredient,
                request.addedById(),
                request.reason()));
    }

    @PatchMapping("/{itemId}/move-to-basket")
    public ShoppingListItemDto moveToBasket(@PathVariable Long householdId,
                                            @PathVariable Long itemId) {
        return toDto(shoppingListService.moveToBasket(itemId));
    }

    @PatchMapping("/{itemId}/remove-from-basket")
    public ShoppingListItemDto removeFromBasket(@PathVariable Long householdId,
                                                @PathVariable Long itemId) {
        return toDto(shoppingListService.removeFromBasket(itemId));
    }

    @PostMapping("/complete-trip")
    public List<ShoppingListItemDto> completeShoppingTrip(
            @PathVariable Long householdId,
            @RequestBody CompleteTripRequest request) {
        List<ShoppingListService.ItemConfirmation> confirmations = request.items().stream()
                .map(i -> new ShoppingListService.ItemConfirmation(i.itemId(), i.confirmedQuantity(), i.confirmedUnit()))
                .toList();
        return shoppingListService.completeShoppingTrip(householdId, confirmations)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PatchMapping("/{itemId}/cancel")
    public ShoppingListItemDto cancelItem(@PathVariable Long householdId,
                                          @PathVariable Long itemId) {
        return toDto(shoppingListService.cancelItem(itemId));
    }

    private ShoppingListItemDto toDto(ShoppingListItem item) {
        return new ShoppingListItemDto(
                item.getId(),
                item.getIngredient().getId(),
                item.getIngredient().getName(),
                item.getIngredient().getTrackingType(),
                item.getAddedBy() != null ? item.getAddedBy().getId() : null,
                item.getReason(),
                item.getStatus(),
                item.getAddedAt(),
                item.getBoughtAt()
        );
    }

    public record AddItemRequest(
            Long ingredientId,
            NewIngredientPayload newIngredient,
            Long addedById,
            String reason
    ) {
        public record NewIngredientPayload(
                String name,
                Long categoryId,
                String trackingType,
                String location,
                String canonicalUnit
        ) {}
    }

    public record CompleteTripRequest(List<ItemConfirmation> items) {
        public record ItemConfirmation(Long itemId, BigDecimal confirmedQuantity, String confirmedUnit) {}
    }
}
