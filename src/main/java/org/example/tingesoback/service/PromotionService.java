package org.example.tingesoback.service;

import org.example.tingesoback.entity.Promotion;
import org.example.tingesoback.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    public Promotion updatePromotion(Long id, Promotion details) {
        return promotionRepository.findById(id).map(promo -> {
            promo.setName(details.getName());
            promo.setDiscountPercentage(details.getDiscountPercentage());
            promo.setValidFrom(details.getValidFrom());
            promo.setValidTo(details.getValidTo());
            return promotionRepository.save(promo);
        }).orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
}
