package org.example.tingesoback.service;

import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion mockPromotion;

    @BeforeEach
    void setUp() {
        mockPromotion = new Promotion();
        mockPromotion.setId(1L);
        mockPromotion.setName("Promo Test");
        mockPromotion.setDiscountPercentage(0.10);
        mockPromotion.setActive(true);
        mockPromotion.setValidFrom(LocalDateTime.now());
        mockPromotion.setValidTo(LocalDateTime.now().plusDays(10));
    }

    @Test
    void createPromotion_Success() {
        when(promotionRepository.save(any(Promotion.class))).thenReturn(mockPromotion);
        Promotion result = promotionService.createPromotion(new Promotion());
        assertNotNull(result);
        assertEquals("Promo Test", result.getName());
    }

    @Test
    void getAllPromotions_Success() {
        when(promotionRepository.findAll()).thenReturn(List.of(mockPromotion));
        List<Promotion> result = promotionService.getAllPromotions();
        assertEquals(1, result.size());
    }

    @Test
    void getPromotionById_Found() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(mockPromotion));
        Optional<Promotion> result = promotionService.getPromotionById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void updatePromotion_Success() {
        Promotion details = new Promotion();
        details.setName("Updated Name");
        details.setDiscountPercentage(0.20);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(mockPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(mockPromotion);

        Promotion result = promotionService.updatePromotion(1L, details);

        assertEquals("Updated Name", result.getName());
        assertEquals(0.20, result.getDiscountPercentage());
        verify(promotionRepository).save(mockPromotion);
    }

    @Test
    void updatePromotion_NotFound() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promotionService.updatePromotion(1L, new Promotion()));
    }

    @Test
    void togglePromotionStatus_Success() {
        // Estaba en true, debería pasar a false
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(mockPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(mockPromotion);

        Promotion result = promotionService.togglePromotionStatus(1L);

        assertFalse(result.isActive());
        verify(promotionRepository).save(mockPromotion);
    }

    @Test
    void togglePromotionStatus_NotFound() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promotionService.togglePromotionStatus(1L));
    }

    @Test
    void deletePromotion_Success() {
        promotionService.deletePromotion(1L);
        verify(promotionRepository, times(1)).deleteById(1L);
    }
}