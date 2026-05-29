package com.localbanana.pantry.service;

import com.localbanana.pantry.domain.entity.Household;
import com.localbanana.pantry.domain.repository.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HouseholdService {

    private final HouseholdRepository householdRepository;

    public HouseholdService(HouseholdRepository householdRepository) {
        this.householdRepository = householdRepository;
    }

    public Household createHousehold(String name) {
        Household household = new Household();
        household.setName(name);
        household.setCreatedAt(LocalDateTime.now());
        return householdRepository.save(household);
    }

    @Transactional(readOnly = true)
    public Household getHouseholdById(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Household not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }
}