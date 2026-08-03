package com.library.dao.impl;

import com.library.dao.FineDAO;
import com.library.exception.FineNotFoundException;
import com.library.model.Fine;
import com.library.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FineDAOImpl implements FineDAO {

    private static final Logger LOGGER = Logger.getLogger(FineDAOImpl.class.getName());

    @Override
    public int createFine(Fine fine) {
        String sql = "INSERT INTO fines (transaction_id, amount, paid, paid_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, fine.getTransactionId());
            stmt.setBigDecimal(2, fine.getAmount());
            stmt.setBoolean(3, fine.isPaid());
            if (fine.getPaidDate() != null) {
                stmt.setDate(4, Date.valueOf(fine.getPaidDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        fine.setFineId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating fine", e);
        }
        return -1;
    }

    @Override
    public boolean updateFine(Fine fine) {
        String sql = "UPDATE fines SET transaction_id = ?, amount = ?, paid = ?, paid_date = ? WHERE fine_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fine.getTransactionId());
            stmt.setBigDecimal(2, fine.getAmount());
            stmt.setBoolean(3, fine.isPaid());
            if (fine.getPaidDate() != null) {
                stmt.setDate(4, Date.valueOf(fine.getPaidDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setInt(5, fine.getFineId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating fine ID: " + fine.getFineId(), e);
        }
        return false;
    }

    @Override
    public Fine getFineById(int fineId) throws FineNotFoundException {
        String sql = "SELECT * FROM fines WHERE fine_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fineId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFine(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving fine ID: " + fineId, e);
        }
        throw new FineNotFoundException("Fine not found with ID: " + fineId);
    }

    @Override
    public List<Fine> getFinesByMember(int memberId) {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT f.* FROM fines f JOIN transactions t ON f.transaction_id = t.transaction_id WHERE t.member_id = ? ORDER BY f.fine_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving fines for member ID: " + memberId, e);
        }
        return list;
    }

    @Override
    public List<Fine> getUnpaidFinesByMember(int memberId) {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT f.* FROM fines f JOIN transactions t ON f.transaction_id = t.transaction_id WHERE t.member_id = ? AND f.paid = FALSE ORDER BY f.fine_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFine(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving unpaid fines for member ID: " + memberId, e);
        }
        return list;
    }

    @Override
    public List<Fine> getAllFines() {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT * FROM fines ORDER BY fine_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToFine(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all fines", e);
        }
        return list;
    }

    private Fine mapResultSetToFine(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setFineId(rs.getInt("fine_id"));
        fine.setTransactionId(rs.getInt("transaction_id"));
        fine.setAmount(rs.getBigDecimal("amount"));
        fine.setPaid(rs.getBoolean("paid"));
        Date paidDate = rs.getDate("paid_date");
        if (paidDate != null) fine.setPaidDate(paidDate.toLocalDate());
        return fine;
    }
}
