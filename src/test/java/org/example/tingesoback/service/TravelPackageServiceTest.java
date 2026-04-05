package org.example.tingesoback.service;

import org.example.tingesoback.entity.TravelPackage;
import org.example.tingesoback.repository.TravelPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelPackageServiceTest {

    @Mock
    private TravelPackageRepository travelPackageRepository;

    @InjectMocks
    private TravelPackageService travelPackageService;

    private TravelPackage mockPkg;

    @BeforeEach
    void setUp() {
        mockPkg = new TravelPackage();
        mockPkg.setId(1L);
        mockPkg.setName("Tour Torres del Paine");
        mockPkg.setPrice(500.0);
        mockPkg.setTotalCapacity(10);
        mockPkg.setAvailableSpots(10);
        mockPkg.setStartDate(LocalDate.now().plusDays(10));
        mockPkg.setEndDate(LocalDate.now().plusDays(15));
    }

    // --- TESTS DE CREACIÓN Y REGLAS BÁSICAS ---

    @Test
    void createPackage_Success() {
        when(travelPackageRepository.save(any())).thenReturn(mockPkg);

        TravelPackage result = travelPackageService.createPackage(mockPkg);

        assertNotNull(result);
        assertEquals(mockPkg.getTotalCapacity(), result.getAvailableSpots());
        verify(travelPackageRepository).save(mockPkg);
    }

    @Test
    void createPackage_InvalidPrice_ThrowsException() {
        mockPkg.setPrice(-10.0);
        assertThrows(IllegalArgumentException.class, () -> travelPackageService.createPackage(mockPkg));
    }

    @Test
    void createPackage_InvalidDates_ThrowsException() {
        // Fecha fin antes que inicio
        mockPkg.setEndDate(mockPkg.getStartDate().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> travelPackageService.createPackage(mockPkg));
    }

    // --- TESTS DE ACTUALIZACIÓN (REGLAS ESPECÍFICAS) ---

    @Test
    void updatePackage_Success_RecalculateSpots() {
        TravelPackage details = new TravelPackage();
        details.setName("Nuevo Nombre");
        details.setTotalCapacity(20); // Aumentamos capacidad de 10 a 20
        details.setPrice(600.0);
        details.setStartDate(mockPkg.getStartDate());

        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(mockPkg));
        when(travelPackageRepository.save(any())).thenReturn(mockPkg);

        TravelPackage result = travelPackageService.updatePackage(1L, details);

        assertEquals(20, result.getTotalCapacity());
        assertEquals(20, result.getAvailableSpots()); // 10 originales + (20-10) diff = 20
        verify(travelPackageRepository).save(mockPkg);
    }

    @Test
    void updatePackage_Fail_ReduceCapacityBelowReservations() {
        // Simulamos que ya hay 8 cupos reservados (10 total - 2 disponibles)
        mockPkg.setTotalCapacity(10);
        mockPkg.setAvailableSpots(2);

        TravelPackage newData = new TravelPackage();
        newData.setTotalCapacity(5); // Intentamos bajar a 5, pero ya hay 8 reservados

        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(mockPkg));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> travelPackageService.updatePackage(1L, newData));

        assertTrue(ex.getMessage().contains("ya existen 8 cupos reservados"));
    }

    @Test
    void updatePackage_Fail_ChangeStartDateWithReservations() {
        // Ya hay reservas (10 total - 5 disponibles = 5 reservados)
        mockPkg.setAvailableSpots(5);

        TravelPackage newData = new TravelPackage();
        newData.setTotalCapacity(10);
        newData.setStartDate(mockPkg.getStartDate().plusDays(1)); // Intentamos cambiar fecha

        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(mockPkg));

        assertThrows(IllegalArgumentException.class,
                () -> travelPackageService.updatePackage(1L, newData));
    }

    // --- CONSULTAS Y OTROS ---

    @Test
    void getAllPackages_Success() {
        when(travelPackageRepository.findAll()).thenReturn(List.of(mockPkg));
        assertEquals(1, travelPackageService.getAllPackages().size());
    }

    @Test
    void getPackageById_Found() {
        // 1. Configuramos el mock para que encuentre el paquete
        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(mockPkg));

        // 2. Ejecutamos el método del Service
        Optional<TravelPackage> result = travelPackageService.getPackageById(1L);

        // 3. Verificaciones
        assertTrue(result.isPresent(), "El resultado debería estar presente");
        assertEquals(1L, result.get().getId());
        assertEquals("Tour Torres del Paine", result.get().getName());

        // Verificamos que se llamó al repositorio exactamente una vez
        verify(travelPackageRepository, times(1)).findById(1L);
    }

    @Test
    void getPackageById_NotFound() {
        // 1. Configuramos el mock para que devuelva un Optional vacío
        when(travelPackageRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Ejecutamos
        Optional<TravelPackage> result = travelPackageService.getPackageById(99L);

        // 3. Verificación
        assertTrue(result.isEmpty(), "El resultado debería estar vacío");

        verify(travelPackageRepository, times(1)).findById(99L);
    }

    @Test
    void deletePackage_Success() {
        travelPackageService.deletePackage(1L);
        verify(travelPackageRepository).deleteById(1L);
    }
}