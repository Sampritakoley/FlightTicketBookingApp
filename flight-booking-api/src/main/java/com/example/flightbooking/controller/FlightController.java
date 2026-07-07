package com.example.flightbooking.controller;

import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;

    public FlightController(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @PostMapping
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody Flight flight) {
        if (flight.getAvailableSeats() == null) {
            flight.setAvailableSeats(flight.getTotalSeats());
        }
        
        return flightRepository.save(flight)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Flight already exists"));
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<Flight> getFlight(@PathVariable String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));
    }
}
