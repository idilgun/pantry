package com.localbanana.pantry.controller;

import com.localbanana.pantry.domain.entity.Household;
import com.localbanana.pantry.dto.HouseholdDto;
import com.localbanana.pantry.service.HouseholdService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/households")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping
    public List<HouseholdDto> getAllHouseholds() {
        return householdService.getAllHouseholds()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public HouseholdDto getHouseholdById(@PathVariable Long id) {
        return toDto(householdService.getHouseholdById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdDto createHousehold(@RequestBody CreateHouseholdRequest request) {
        return toDto(householdService.createHousehold(request.name()));
    }

    private HouseholdDto toDto(Household household) {
        return new HouseholdDto(
                household.getId(),
                household.getName(),
                household.getCreatedAt()
        );
    }

    public record CreateHouseholdRequest(String name) {}
}