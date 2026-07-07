package com.example.flightbooking.service;

import com.example.flightbooking.exception.BookingAlreadyCancelledException;
import com.example.flightbooking.exception.BookingNotFoundException;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.NoSeatsAvailableException;
import com.example.flightbooking.model.Booking;
import com.example.flightbooking.model.BookingStatus;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
                .orElseThrow(() -> new FlightNotFoundException("Flight not found"));

        // Synchronize on the flight instance to prevent concurrent overbooking
        // Since FlightRepository returns the exact same instance from the ConcurrentHashMap, this is safe.
        synchronized (flight) {
            if (flight.getAvailableSeats() < seatsRequested) {
                throw new NoSeatsAvailableException("Not enough seats available");
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
                Instant.now()
        );
        
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException("Booking not found");
        }

        synchronized (booking) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new BookingAlreadyCancelledException("Booking is already cancelled");
            }

            Flight flight = flightRepository.findByFlightNumber(booking.getFlightNumber())
                    .orElseThrow(() -> new FlightNotFoundException("Flight not found for booking"));

            synchronized (flight) {
                flight.setAvailableSeats(flight.getAvailableSeats() + booking.getSeatsBooked());
            }

            booking.setStatus(BookingStatus.CANCELLED);
        }
    }
}
