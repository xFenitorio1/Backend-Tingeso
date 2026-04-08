package org.example.tingesoback.service;

import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TravelPackageService {

    private final TravelPackageRepository travelPackageRepository;

    @Autowired
    public TravelPackageService(TravelPackageRepository travelPackageRepository) {
        this.travelPackageRepository = travelPackageRepository;
    }

    public TravelPackage createPackage(TravelPackage pkg) {
        // En creación, los cupos disponibles son iguales a la capacidad total
        pkg.setAvailableSpots(pkg.getTotalCapacity());
        validateBusinessRules(pkg);
        return travelPackageRepository.save(pkg);
    }

    public List<TravelPackage> getAllPackages() {
        return travelPackageRepository.findAll();
    }

    public Optional<TravelPackage> getPackageById(Long id) {
        return travelPackageRepository.findById(id);
    }

    @Transactional
    public TravelPackage updatePackage(Long id, TravelPackage details) {
        // 1. We look for the current package in the database
        TravelPackage existingPkg = travelPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TravelPackage not found"));

        // 2. We validate the business rules by comparing the new with the current
        validateUpdateRules(details, existingPkg);

        // 3. We update the fields
        existingPkg.setName(details.getName());
        existingPkg.setDestination(details.getDestination());
        existingPkg.setDescription(details.getDescription());
        existingPkg.setStartDate(details.getStartDate());
        existingPkg.setEndDate(details.getEndDate());
        existingPkg.setPrice(details.getPrice());

        // Quota adjustment: If the total capacity changes, we must recalculate available spaces
        int diff = details.getTotalCapacity() - existingPkg.getTotalCapacity();
        existingPkg.setTotalCapacity(details.getTotalCapacity());
        existingPkg.setAvailableSpots(existingPkg.getAvailableSpots() + diff);

        existingPkg.setStatus(details.getStatus());

        return travelPackageRepository.save(existingPkg);
    }

    public void deletePackage(Long id) {
        travelPackageRepository.deleteById(id);
    }

    // Validation for creation
    private void validateBusinessRules(TravelPackage pkg) {
        if (pkg.getPrice() != null && pkg.getPrice() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (pkg.getStartDate() != null && pkg.getEndDate() != null && !pkg.getEndDate().isAfter(pkg.getStartDate())) {
            throw new IllegalArgumentException("La fecha de termino debe ser después de la fecha de inicio");
        }
        if (pkg.getTotalCapacity() != null && pkg.getTotalCapacity() <= 0) {
            throw new IllegalArgumentException("Los cupos deben ser mayor a 0");
        }
    }

    // Specific validation for update (Consistency with reservations)
    private void validateUpdateRules(TravelPackage newData, TravelPackage currentData) {
        // Run general validations first
        validateBusinessRules(newData);

        // Calculate how many slots have already been reserved (Total Capacity - Available)
        int reservedSpots = currentData.getTotalCapacity() - currentData.getAvailableSpots();

        // Do not reduce total capacity below what has already been reserved
        if (newData.getTotalCapacity() < reservedSpots) {
            throw new IllegalArgumentException("No se puede reducir la capacidad a " + newData.getTotalCapacity() +
                    " porque ya existen " + reservedSpots + " cupos reservados.");
        }

        // RULE: If there are reservations, do not allow changes to the base (Start) dates.
        if (reservedSpots > 0) {
            if (!newData.getStartDate().equals(currentData.getStartDate())) {
                throw new IllegalArgumentException("No se puede modificar la fecha de inicio de un paquete que ya tiene reservas registradas.");
            }
        }
    }
}