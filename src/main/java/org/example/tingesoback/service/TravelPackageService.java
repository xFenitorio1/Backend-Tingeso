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
        validateBusinessRules(pkg);
        return travelPackageRepository.save(pkg);
    }

    public List<TravelPackage> getAllPackages() {
        return travelPackageRepository.findAll();
    }

    public Optional<TravelPackage> getPackageById(Long id) {
        return travelPackageRepository.findById(id);
    }

    public TravelPackage updatePackage(Long id, TravelPackage details) {
        validateBusinessRules(details);
        return travelPackageRepository.findById(id).map(pkg -> {
            pkg.setName(details.getName());
            pkg.setDestination(details.getDestination());
            pkg.setDescription(details.getDescription());
            pkg.setStartDate(details.getStartDate());
            pkg.setEndDate(details.getEndDate());
            pkg.setPrice(details.getPrice());
            pkg.setTotalCapacity(details.getTotalCapacity());
            pkg.setAvailableSpots(details.getAvailableSpots());
            pkg.setStatus(details.getStatus());
            return travelPackageRepository.save(pkg);
        }).orElseThrow(() -> new RuntimeException("TravelPackage not found"));
    }

    public void deletePackage(Long id) {
        travelPackageRepository.deleteById(id);
    }

    private void validateBusinessRules(TravelPackage pkg) {
        if (pkg.getPrice() != null && pkg.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (pkg.getStartDate() != null && pkg.getEndDate() != null && !pkg.getEndDate().isAfter(pkg.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (pkg.getTotalCapacity() != null && pkg.getTotalCapacity() <= 0) {
            throw new IllegalArgumentException("Total capacity must be greater than 0");
        }
    }
}
