# Flight Ticket Booking API

A small REST API for booking flight tickets, built with Spring Boot and Java 17.
In-memory storage only — no database, no auth, no flight search. Clients are
assumed to already know the flight number for every operation.

## Tech Stack

- Java 17
- Spring Boot 3.2.5 (Spring Web, Spring Validation)
- Maven
- In-memory storage (`ConcurrentHashMap`), no external database

## How to Run

**Prerequisites:** Java 17+ and Maven installed.

```bash
git clone <your-repo-url>
cd flight-booking-api
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

To run tests (if any are present):
```bash
mvn test
```

## API Overview

There is no flight search endpoint by design — a flight is registered once
(acting as seed/setup data) and afterward is only referenced by its
`flightNumber`.

| Method | Endpoint                          | Description                          |
|--------|------------------------------------|---------------------------------------|
| POST   | `/api/flights`                    | Register a new flight                |
| GET    | `/api/flights/{flightNumber}`     | Check seat availability for a flight |
| POST   | `/api/bookings`                   | Book seat(s) on a flight              |
| DELETE | `/api/bookings/{id}`              | Cancel a booking                      |

## Example Requests

### 1. Register a flight
```bash
curl -X POST http://localhost:8080/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AI202",
    "totalSeats": 5
  }'
```
**Response — `201 Created`**
```json
{
  "flightNumber": "AI202",
  "totalSeats": 5,
  "availableSeats": 5
}
```
Registering the same `flightNumber` twice returns `409 Conflict`.

### 2. Check flight availability
```bash
curl http://localhost:8080/api/flights/AI202
```
**Response — `200 OK`**
```json
{
  "flightNumber": "AI202",
  "totalSeats": 5,
  "availableSeats": 5
}
```
Unknown flight number returns `404 Not Found`.

### 3. Book a ticket
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AI202",
    "passengerName": "Samprita Rao",
    "seatsRequested": 2
  }'
```
**Response — `201 Created`**
```json
{
  "id": "3f29c1e2-8b1a-4e3d-9a2b-6c7d8e9f0a1b",
  "flightNumber": "AI202",
  "passengerName": "Samprita Rao",
  "seatsBooked": 2,
  "status": "CONFIRMED",
  "createdAt": "2026-07-07T10:15:30.123Z"
}
```
- Unknown flight → `404 Not Found`
- Not enough seats remaining → `409 Conflict`
- Missing/invalid fields (blank name, `seatsRequested < 1`) → `400 Bad Request`

Seat decrement is done inside a per-flight synchronized block, so concurrent
booking requests against the same flight cannot oversell it.

### 4. Cancel a booking
```bash
curl -X DELETE http://localhost:8080/api/bookings/3f29c1e2-8b1a-4e3d-9a2b-6c7d8e9f0a1b
```
**Response — `204 No Content`**
*(Empty Response Body)*

Cancelling releases the seats back to the flight. Unknown booking ID →
`404 Not Found`; cancelling an already-cancelled booking → `409 Conflict`.

## Design Decisions

- **No booking-retrieval API** — intentionally omitted per the task spec.
- **No flight search** — a flight lookup by exact `flightNumber` exists only
  to support the booking flow (checking availability), not as a search
  feature.
- **Overbooking prevention** — each `Flight` object is used as a lock; seat
  checks and decrements happen inside a single `synchronized` block per
  flight, so two simultaneous booking requests for the same flight can't
  both succeed when only one seat remains.
- **In-memory storage** — `ConcurrentHashMap` for both flights and bookings,
  as required. State is lost on restart; this is acceptable per the task's
  "single instance, no DB required" constraint.
- **Cancellation** — soft delete (status flips to `CANCELLED`), seats are
  returned to the flight's pool. Bookings are never physically removed, to
  preserve a basic audit trail. (Note on HTTP semantics: while `DELETE` is used here out of convention for cancellation, it performs a soft-delete rather than a resource removal; in some strict REST APIs this might be modelled as `PATCH /api/bookings/{id}/status` or `POST /api/bookings/{id}/cancel`.)


## Manual fixes after reviewing AI-generated solution

Fixed: removed client-supplied `availableSeats` from the flight creation
request — only `totalSeats` is accepted, preventing clients from breaking
the totalSeats/availableSeats invariant. Wrapped the availableSeats read in
GET /api/flights/{flightNumber} inside the same per-flight synchronized
block used for writes, to avoid stale reads under the Java Memory Model.
Switched error response timestamp from LocalDateTime to Instant for
unambiguous UTC timestamps in API responses.

## Known issues not fixed due to time: 
no idempotency key on POST /bookings
(retried requests create duplicate bookings); error responses use
human-readable messages rather than machine-readable error codes; DELETE
is used for what is technically a soft-delete/status-change operation
rather than POST .../cancel; no automated tests.
## How This Was Built

Per the task instructions, the initial implementation was generated using
AI assistance (Claude), with each iteration committed separately — commit
messages contain the exact prompt used for that step. This was followed by
a manual review-and-fix pass (single commit) to correct concurrency
handling, wire up validation properly, and adjust response codes. See the
git history for details of what was changed and why.

## What I'd Improve With More Time

- **Persistence** — swap the in-memory maps for a real datastore (e.g.
  Postgres via Spring Data JPA) so state survives restarts and can scale
  beyond a single instance.
- **Idempotency** — add an idempotency key (e.g. client-supplied header) on
  `POST /api/bookings` so retried/duplicate requests don't create duplicate
  bookings.
- **Concurrency at scale** — the current per-flight `synchronized` lock is
  correct but won't scale across multiple app instances; a real DB with
  row-level locking or optimistic concurrency (version column) would be
  needed for a distributed deployment.
- **Partial seat operations** — support partially cancelling a multi-seat
  booking rather than only all-or-nothing cancellation.
- **Automated tests** — unit tests for the service layer (especially
  concurrent booking scenarios) and integration tests for the controllers.
- **Booking retrieval** — even though excluded from scope here, a
  `GET /api/bookings/{id}` would be a natural next addition.
- **Better validation messages** and a more structured error response
  schema (error codes, not just messages).
- **API documentation** — OpenAPI/Swagger spec generated from the
  controllers.
