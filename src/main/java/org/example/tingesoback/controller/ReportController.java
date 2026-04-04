package org.example.tingesoback.controller;

import lombok.RequiredArgsConstructor;
import org.example.tingesoback.dto.BookingSalesDTO;
import org.example.tingesoback.dto.PackageRankingDTO;
import org.example.tingesoback.repository.BookingRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final BookingRepository bookingRepository;

    @GetMapping("/sales")
    public ResponseEntity<List<BookingSalesDTO>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate end) {

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        return ResponseEntity.ok(bookingRepository.getSalesPeriodReport(startDateTime, endDateTime));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<PackageRankingDTO>> getRankingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate end) {

            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

        return ResponseEntity.ok(bookingRepository.getPackageRankingReport(startDateTime, endDateTime));
    }
}
