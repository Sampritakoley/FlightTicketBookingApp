package com.example.flightbooking.repository;

import com.example.flightbooking.model.Flight;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class FlightRepository {
    
    private final ConcurrentMap<String, Flight> flights = new ConcurrentHashMap<>();

    public Optional<Flight> save(Flight flight) {
        // putIfAbsent returns null if there was no existing mapping
        if (flights.putIfAbsent(flight.getFlightNumber(), flight) != null) {
            return Optional.empty(); // Flight already exists
        }
        return Optional.of(flight);
    }

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return Optional.ofNullable(flights.get(flightNumber));
    }
}
