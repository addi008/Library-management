package com.library.dao.impl;

import com.library.dao.TransactionDAO;
import com.library.model.Transaction;
import com.library.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAOImpl implements TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionDAOImpl.class.getName());

    @Override
    public int createTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, transaction.getBookId());
            stmt.setInt(2, transaction.getMemberId());
            stmt.setDate(3, Date.valueOf(transaction.getIssueDate()));
            stmt.setDate(4, Date.valueOf(transaction.getDueDate()));
            if (transaction.getReturnDate() != null) {
                stmt.setDate(5, Date.valueOf(transaction.getReturnDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, transaction.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        transaction.setTransactionId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating transaction", e);
        }
        return -1;
    }

    @Override
    public boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET book_id = ?, member_id = ?, issue_date = ?, due_date = ?, return_date = ?, status = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transaction.getBookId());
            stmt.setInt(2, transaction.getMemberId());
            stmt.setDate(3, Date.valueOf(transaction.getIssueDate()));
            stmt.setDate(4, Date.valueOf(transaction.getDueDate()));
            if (transaction.getReturnDate() != null) {
                stmt.setDate(5, Date.valueOf(transaction.getReturnDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, transaction.getStatus().name());
            stmt.setInt(7, transaction.getTransactionId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating transaction ID: " + transaction.getTransactionId(), e);
        }
        return false;
    }

    @Override
    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving transaction ID: " + transactionId, e);
        }
        return null;
    }

    @Override
    public List<Transaction> getActiveTransactionsByMember(int memberId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE member_id = ? AND status = 'ISSUED' ORDER BY issue_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching active transactions for member: " + memberId, e);
        }
        return list;
    }

    @Override
    public List<Transaction> getOverdueTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE due_date < ? AND status = 'ISSUED' ORDER BY due_date ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching overdue transactions", e);
        }
        return list;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all transactions", e);
        }
        return list;
    }

    @Override
    public List<Transaction> getTransactionsByMember(int memberId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE member_id = ? ORDER BY transaction_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching transactions for member: " + memberId, e);
        }
        return list;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setBookId(rs.getInt("book_id"));
        t.setMemberId(rs.getInt("member_id"));
        Date issueDate = rs.getDate("issue_date");
        if (issueDate != null) t.setIssueDate(issueDate.toLocalDate());
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) t.setDueDate(dueDate.toLocalDate());
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) t.setReturnDate(returnDate.toLocalDate());
        t.setStatus(Transaction.TransactionStatus.valueOf(rs.getString("status")));
        return t;
    }
}
