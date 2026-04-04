package org.example.tingesoback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingAdminDTO {
    private Long id;
    private String customerFullName;
    private String customerEmail;
    private String packageName;
    private Integer passengerCount;
    private Double basePrice;
    private Double totalDiscount;
    private Double finalAmount;
    private String status;
    private LocalDateTime createdAt;
}