package com.library.dao.impl;

import com.library.dao.ReservationDAO;
import com.library.model.Reservation;
import com.library.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationDAOImpl implements ReservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReservationDAOImpl.class.getName());

    @Override
    public int createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (book_id, member_id, reservation_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getBookId());
            stmt.setInt(2, reservation.getMemberId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getReservationDate() != null ? reservation.getReservationDate() : LocalDateTime.now()));
            stmt.setString(4, reservation.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        reservation.setReservationId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
        }
        return -1;
    }

    @Override
    public boolean updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET book_id = ?, member_id = ?, reservation_date = ?, status = ? WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservation.getBookId());
            stmt.setInt(2, reservation.getMemberId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getReservationDate()));
            stmt.setString(4, reservation.getStatus().name());
            stmt.setInt(5, reservation.getReservationId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation ID: " + reservation.getReservationId(), e);
        }
        return false;
    }

    @Override
    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservation ID: " + reservationId, e);
        }
        return null;
    }

    @Override
    public Reservation getEarliestPendingReservation(int bookId) {
        String sql = "SELECT * FROM reservations WHERE book_id = ? AND status = 'PENDING' ORDER BY reservation_date ASC LIMIT 1";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching earliest pending reservation for book ID: " + bookId, e);
        }
        return null;
    }

    @Override
    public List<Reservation> getReservationsByMember(int memberId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE member_id = ? ORDER BY reservation_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations for member ID: " + memberId, e);
        }
        return list;
    }

    @Override
    public List<Reservation> getPendingReservationsByMember(int memberId, int bookId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE member_id = ? AND book_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking member pending reservation", e);
        }
        return list;
    }

    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all reservations", e);
        }
        return list;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setMemberId(rs.getInt("member_id"));
        Timestamp ts = rs.getTimestamp("reservation_date");
        if (ts != null) r.setReservationDate(ts.toLocalDateTime());
        r.setStatus(Reservation.ReservationStatus.valueOf(rs.getString("status")));
        return r;
    }
}
