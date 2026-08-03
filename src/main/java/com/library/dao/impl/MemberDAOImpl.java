package com.library.dao.impl;

import com.library.dao.MemberDAO;
import com.library.exception.MemberNotFoundException;
import com.library.model.Member;
import com.library.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemberDAOImpl implements MemberDAO {

    private static final Logger LOGGER = Logger.getLogger(MemberDAOImpl.class.getName());

    @Override
    public boolean addMember(Member member) {
        String sql = "INSERT INTO members (name, email, phone, membership_date, membership_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setDate(4, Date.valueOf(member.getMembershipDate() != null ? member.getMembershipDate() : LocalDate.now()));
            stmt.setString(5, member.getMembershipType() != null ? member.getMembershipType() : "STANDARD");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setMemberId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding member: " + member.getName(), e);
        }
        return false;
    }

    @Override
    public boolean updateMember(Member member) {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, membership_date = ?, membership_type = ? WHERE member_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setDate(4, Date.valueOf(member.getMembershipDate()));
            stmt.setString(5, member.getMembershipType());
            stmt.setInt(6, member.getMemberId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member ID: " + member.getMemberId(), e);
        }
        return false;
    }

    @Override
    public boolean deleteMember(int memberId) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting member ID: " + memberId, e);
        }
        return false;
    }

    @Override
    public Member getMemberById(int memberId) throws MemberNotFoundException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving member ID: " + memberId, e);
        }
        throw new MemberNotFoundException("Member not found with ID: " + memberId);
    }

    @Override
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY member_id ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all members", e);
        }
        return members;
    }

    @Override
    public Member getMemberByEmail(String email) throws MemberNotFoundException {
        String sql = "SELECT * FROM members WHERE email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching member by email: " + email, e);
        }
        throw new MemberNotFoundException("Member not found with email: " + email);
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setMemberId(rs.getInt("member_id"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        Date mDate = rs.getDate("membership_date");
        if (mDate != null) {
            member.setMembershipDate(mDate.toLocalDate());
        }
        member.setMembershipType(rs.getString("membership_type"));
        return member;
    }
}
