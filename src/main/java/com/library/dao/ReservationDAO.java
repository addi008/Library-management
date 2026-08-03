package com.library.dao;

import com.library.model.Reservation;

import java.util.List;

public interface ReservationDAO {
    int createReservation(Reservation reservation);
    boolean updateReservation(Reservation reservation);
    Reservation getReservationById(int reservationId);
    Reservation getEarliestPendingReservation(int bookId);
    List<Reservation> getReservationsByMember(int memberId);
    List<Reservation> getPendingReservationsByMember(int memberId, int bookId);
    List<Reservation> getAllReservations();
}
