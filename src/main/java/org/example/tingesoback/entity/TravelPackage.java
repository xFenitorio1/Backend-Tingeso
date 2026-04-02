package org.example.tingesoback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tingesoback.dto.PackageStatus;
import java.time.LocalDate;

@Entity
@Table(name = "travel_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String destination;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Double price;
    
    private Integer totalCapacity;
    private Integer availableSpots;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;
}
