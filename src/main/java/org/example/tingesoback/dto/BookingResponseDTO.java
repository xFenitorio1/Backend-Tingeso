package org.example.tingesoback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BookingResponseDTO {
    private Long id;
    private String packageName;
    private String destination;
    private LocalDate startDate;
    private Double finalAmount;
    private BookingStatus status;
}
