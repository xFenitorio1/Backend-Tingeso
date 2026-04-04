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
        // 1. Buscamos el paquete actual en la BD
        TravelPackage existingPkg = travelPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TravelPackage not found"));

        // 2. Validamos las reglas de negocio comparando lo nuevo con lo actual
        validateUpdateRules(details, existingPkg);

        // 3. Actualizamos los campos
        existingPkg.setName(details.getName());
        existingPkg.setDestination(details.getDestination());
        existingPkg.setDescription(details.getDescription());
        existingPkg.setStartDate(details.getStartDate());
        existingPkg.setEndDate(details.getEndDate());
        existingPkg.setPrice(details.getPrice());

        // Ajuste de cupos: Si se cambia la capacidad total, debemos recalcular disponibles
        int diff = details.getTotalCapacity() - existingPkg.getTotalCapacity();
        existingPkg.setTotalCapacity(details.getTotalCapacity());
        existingPkg.setAvailableSpots(existingPkg.getAvailableSpots() + diff);

        existingPkg.setStatus(details.getStatus());

        return travelPackageRepository.save(existingPkg);
    }

    public void deletePackage(Long id) {
        travelPackageRepository.deleteById(id);
    }

    // Validación para creación (Reglas generales)
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

    // Validación específica para actualización (Consistencia con reservas)
    private void validateUpdateRules(TravelPackage newData, TravelPackage currentData) {
        // Ejecutar validaciones generales primero
        validateBusinessRules(newData);

        // Calcular cuántos cupos ya han sido reservados (Capacidad total - Disponibles)
        int reservedSpots = currentData.getTotalCapacity() - currentData.getAvailableSpots();

        // REGLA: No reducir capacidad total por debajo de lo ya reservado
        if (newData.getTotalCapacity() < reservedSpots) {
            throw new IllegalArgumentException("No se puede reducir la capacidad a " + newData.getTotalCapacity() +
                    " porque ya existen " + reservedSpots + " cupos reservados.");
        }

        // REGLA: Si hay reservas, no permitir cambios de fechas base (Inicio)
        if (reservedSpots > 0) {
            if (!newData.getStartDate().equals(currentData.getStartDate())) {
                throw new IllegalArgumentException("No se puede modificar la fecha de inicio de un paquete que ya tiene reservas registradas.");
            }
        }
    }
}