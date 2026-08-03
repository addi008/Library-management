package com.library.model;

import java.time.LocalDate;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private String phone;
    private LocalDate membershipDate;
    private String membershipType;

    public Member() {
    }

    public Member(int memberId, String name, String email, String phone, LocalDate membershipDate, String membershipType) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipDate = membershipDate;
        this.membershipType = membershipType;
    }

    public Member(String name, String email, String phone, LocalDate membershipDate, String membershipType) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.membershipDate = membershipDate;
        this.membershipType = membershipType;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public void setMembershipDate(LocalDate membershipDate) {
        this.membershipDate = membershipDate;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    @Override
    public String toString() {
        return String.format("Member [ID=%d, Name='%s', Email='%s', Phone='%s', Type='%s', Joined=%s]",
                memberId, name, email, phone, membershipType, membershipDate);
    }
}
