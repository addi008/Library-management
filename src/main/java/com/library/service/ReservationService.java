package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ReservationException;
import com.library.model.Reservation;

import java.util.List;

public interface ReservationService {
    Reservation reserveBook(int memberId, int bookId)
            throws MemberNotFoundException, BookNotFoundException, ReservationException;

    void notifyNextPendingReservation(int bookId);

    List<Reservation> getReservationsByMember(int memberId);
    List<Reservation> getAllReservations();
}
