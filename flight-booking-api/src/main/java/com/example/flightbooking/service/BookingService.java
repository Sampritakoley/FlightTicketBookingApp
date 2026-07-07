package com.example.flightbooking.service;

import com.example.flightbooking.model.Booking;
import com.example.flightbooking.model.BookingStatus;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BookingService {

    private final FlightRepository flightRepository;
    private final ConcurrentMap<String, Booking> bookings = new ConcurrentHashMap<>();

    public BookingService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public Booking bookFlight(String flightNumber, String passengerName, int seatsRequested) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));

        // Synchronize on the flight instance to prevent concurrent overbooking
        // Since FlightRepository returns the exact same instance from the ConcurrentHashMap, this is safe.
        synchronized (flight) {
            if (flight.getAvailableSeats() < seatsRequested) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough seats available");
            }
            
            // Deduct seats
            flight.setAvailableSeats(flight.getAvailableSeats() - seatsRequested);
        }

        // Create the booking
        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                flightNumber,
                passengerName,
                seatsRequested,
                BookingStatus.CONFIRMED,
                LocalDateTime.now()
        );
        
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        synchronized (booking) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already cancelled");
            }

            Flight flight = flightRepository.findByFlightNumber(booking.getFlightNumber())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Flight not found for booking"));

            synchronized (flight) {
                flight.setAvailableSeats(flight.getAvailableSeats() + booking.getSeatsBooked());
            }

            booking.setStatus(BookingStatus.CANCELLED);
        }
    }
}
