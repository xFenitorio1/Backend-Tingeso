package org.example.tingesoback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageRankingDTO {
    private String packageName;
    private Long totalReservations;
    private Long totalPassengers;
    private Double totalRevenue;
}