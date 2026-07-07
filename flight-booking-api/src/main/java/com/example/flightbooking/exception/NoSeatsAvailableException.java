package com.example.flightbooking.exception;

public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
