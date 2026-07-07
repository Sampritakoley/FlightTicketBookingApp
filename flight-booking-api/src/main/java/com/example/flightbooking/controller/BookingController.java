package com.example.flightbooking.controller;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.model.Booking;
import com.example.flightbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.bookFlight(
                request.getFlightNumber(),
                request.getPassengerName(),
                request.getSeatsRequested()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
