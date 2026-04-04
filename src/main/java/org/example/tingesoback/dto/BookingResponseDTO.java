package org.example.tingesoback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private Double finalAmount;
    private Double totalDiscount;
    private Integer passengerCount;
    private Double basePrice;
    private List<String> discountDetails;
}
