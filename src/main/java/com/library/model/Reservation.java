package com.library.model;

import java.time.LocalDateTime;

public class Reservation {

    public enum ReservationStatus {
        PENDING, FULFILLED, CANCELLED
    }

    private int reservationId;
    private int bookId;
    private int memberId;
    private LocalDateTime reservationDate;
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(int reservationId, int bookId, int memberId, LocalDateTime reservationDate, ReservationStatus status) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public Reservation(int bookId, int memberId, ReservationStatus status) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservationDate = LocalDateTime.now();
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Reservation [ID=%d, BookID=%d, MemberID=%d, Date=%s, Status=%s]",
                reservationId, bookId, memberId, reservationDate, status);
    }
}
