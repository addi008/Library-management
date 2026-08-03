package com.library.dao.impl;

import com.library.dao.ReportDAO;
import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;
import com.library.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportDAOImpl implements ReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAOImpl.class.getName());

    @Override
    public List<BookBorrowReportDTO> getMostBorrowedBooks(int limit) {
        List<BookBorrowReportDTO> list = new ArrayList<>();
        String sql = "SELECT b.book_id, b.title, b.author, COUNT(t.transaction_id) AS borrow_count " +
                     "FROM books b " +
                     "JOIN transactions t ON b.book_id = t.book_id " +
                     "GROUP BY b.book_id, b.title, b.author " +
                     "ORDER BY borrow_count DESC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookBorrowReportDTO(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("borrow_count")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing most borrowed books query", e);
        }
        return list;
    }

    @Override
    public List<MemberActivityReportDTO> getMostActiveMembers(int limit) {
        List<MemberActivityReportDTO> list = new ArrayList<>();
        String sql = "SELECT m.member_id, m.name, m.email, COUNT(t.transaction_id) AS tx_count " +
                     "FROM members m " +
                     "JOIN transactions t ON m.member_id = t.member_id " +
                     "GROUP BY m.member_id, m.name, m.email " +
                     "ORDER BY tx_count DESC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new MemberActivityReportDTO(
                            rs.getInt("member_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("tx_count")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing most active members query", e);
        }
        return list;
    }

    @Override
    public BigDecimal getTotalFinesCollectedThisMonth() {
        String sql = "SELECT SUM(amount) FROM fines WHERE paid = TRUE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                return sum != null ? sum : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating total fines collected this month", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<MemberWithUnpaidFineDTO> getMembersWithUnpaidFines() {
        List<MemberWithUnpaidFineDTO> list = new ArrayList<>();
        String sql = "SELECT m.member_id, m.name, m.email, SUM(f.amount) AS total_unpaid " +
                     "FROM members m " +
                     "JOIN transactions t ON m.member_id = t.member_id " +
                     "JOIN fines f ON t.transaction_id = f.transaction_id " +
                     "WHERE f.paid = FALSE " +
                     "GROUP BY m.member_id, m.name, m.email " +
                     "ORDER BY total_unpaid DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MemberWithUnpaidFineDTO(
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBigDecimal("total_unpaid")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying members with unpaid fines", e);
        }
        return list;
    }
}
