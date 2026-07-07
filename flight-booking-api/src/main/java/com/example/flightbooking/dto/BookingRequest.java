package com.example.flightbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotBlank(message = "Flight number cannot be blank")
    private String flightNumber;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @NotNull(message = "Seats requested cannot be null")
    @Min(value = 1, message = "Must request at least 1 seat")
    private Integer seatsRequested;

    public BookingRequest() {
    }

    public BookingRequest(String flightNumber, String passengerName, Integer seatsRequested) {
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.seatsRequested = seatsRequested;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Integer getSeatsRequested() {
        return seatsRequested;
    }

    public void setSeatsRequested(Integer seatsRequested) {
        this.seatsRequested = seatsRequested;
    }
}
