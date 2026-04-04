package org.example.tingesoback.repository;

import org.example.tingesoback.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND :now BETWEEN p.validFrom AND p.validTo")
    List<Promotion> findActivePromotions(LocalDateTime now);
}
