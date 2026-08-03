package com.library.service.impl;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReservationDAO;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.ReservationDAOImpl;
import com.library.exception.BookNotFoundException;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ReservationException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ReservationServiceImpl implements ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationServiceImpl.class.getName());

    private final ReservationDAO reservationDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;

    public ReservationServiceImpl() {
        this.reservationDAO = new ReservationDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.memberDAO = new MemberDAOImpl();
    }

    public ReservationServiceImpl(ReservationDAO reservationDAO, BookDAO bookDAO, MemberDAO memberDAO) {
        this.reservationDAO = reservationDAO;
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
    }

    @Override
    public Reservation reserveBook(int memberId, int bookId)
            throws MemberNotFoundException, BookNotFoundException, ReservationException {

        Member member = memberDAO.getMemberById(memberId);
        Book book = bookDAO.getBookById(bookId);

        if (book.getAvailableCopies() > 0) {
            throw new ReservationException("Book '" + book.getTitle() + "' currently has " +
                    book.getAvailableCopies() + " copy/copies available for immediate borrowing. Reservation is not required.");
        }

        List<Reservation> existing = reservationDAO.getPendingReservationsByMember(memberId, bookId);
        if (!existing.isEmpty()) {
            throw new ReservationException("Member '" + member.getName() + "' already has a pending reservation for '" + book.getTitle() + "'.");
        }

        Reservation reservation = new Reservation(bookId, memberId, Reservation.ReservationStatus.PENDING);
        reservation.setReservationDate(LocalDateTime.now());

        int resId = reservationDAO.createReservation(reservation);
        if (resId <= 0) {
            throw new RuntimeException("Failed to save reservation in database.");
        }

        LOGGER.info("Reservation created successfully for Book ID: " + bookId + " by Member ID: " + memberId);
        System.out.println("[RESERVATION CONFIRMED] Reservation ID " + resId + " placed for '" + book.getTitle() + "' by " + member.getName());
        return reservation;
    }

    @Override
    public void notifyNextPendingReservation(int bookId) {
        Reservation earliest = reservationDAO.getEarliestPendingReservation(bookId);
        if (earliest != null) {
            try {
                Book book = bookDAO.getBookById(bookId);
                Member member = memberDAO.getMemberById(earliest.getMemberId());

                System.out.println("\n********************************************************************************");
                System.out.println("  [AUTO NOTIFICATION] Reserved Book Available!");
                System.out.println("  Book Title : " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
                System.out.println("  Notifying  : " + member.getName() + " (Email: " + member.getEmail() + ")");
                System.out.println("  Reservation: ID " + earliest.getReservationId() + " placed on " + earliest.getReservationDate());
                System.out.println("********************************************************************************\n");

                earliest.setStatus(Reservation.ReservationStatus.FULFILLED);
                reservationDAO.updateReservation(earliest);

            } catch (Exception e) {
                LOGGER.warning("Error notifying reservation recipient: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Reservation> getReservationsByMember(int memberId) {
        return reservationDAO.getReservationsByMember(memberId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }
}
