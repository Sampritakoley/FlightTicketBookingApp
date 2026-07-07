package com.example.flightbooking.model;

import java.time.LocalDateTime;

public class Booking {
    private String id;
    private String flightNumber;
    private String passengerName;
    private Integer seatsBooked;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking() {
    }

    public Booking(String id, String flightNumber, String passengerName, Integer seatsBooked, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.seatsBooked = seatsBooked;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(Integer seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
