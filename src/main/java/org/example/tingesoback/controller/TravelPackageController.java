package org.example.tingesoback.controller;

import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.service.TravelPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin("*")
public class TravelPackageController {

    private final TravelPackageService travelPackageService;

    @Autowired
    public TravelPackageController(TravelPackageService travelPackageService) {
        this.travelPackageService = travelPackageService;
    }

    @PostMapping
    public ResponseEntity<?> createPackage(@RequestBody TravelPackage pkg) {
        try {
            return ResponseEntity.ok(travelPackageService.createPackage(pkg));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TravelPackage>> getAllPackages() {
        return ResponseEntity.ok(travelPackageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackage> getPackageById(@PathVariable Long id) {
        return travelPackageService.getPackageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable Long id, @RequestBody TravelPackage pkg) {
        try {
            return ResponseEntity.ok(travelPackageService.updatePackage(id, pkg));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        travelPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
