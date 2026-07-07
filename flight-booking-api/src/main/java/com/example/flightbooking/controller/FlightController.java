package com.example.flightbooking.controller;

import com.example.flightbooking.dto.FlightRequest;
import com.example.flightbooking.exception.DuplicateFlightException;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;

    public FlightController(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @PostMapping
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody FlightRequest request) {
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getTotalSeats());
        
        return flightRepository.save(flight)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .orElseThrow(() -> new DuplicateFlightException("Flight already exists"));
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<Flight> getFlight(@PathVariable String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found"));
    }
}
