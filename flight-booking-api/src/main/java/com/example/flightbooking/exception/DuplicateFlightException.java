package com.example.flightbooking.exception;

public class DuplicateFlightException extends RuntimeException {
    public DuplicateFlightException(String message) {
        super(message);
    }
}
