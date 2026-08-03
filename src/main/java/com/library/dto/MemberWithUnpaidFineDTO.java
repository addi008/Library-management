package com.library.dto;

import java.math.BigDecimal;

public class MemberWithUnpaidFineDTO {
    private int memberId;
    private String name;
    private String email;
    private BigDecimal totalUnpaidFine;

    public MemberWithUnpaidFineDTO(int memberId, String name, String email, BigDecimal totalUnpaidFine) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.totalUnpaidFine = totalUnpaidFine;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getTotalUnpaidFine() {
        return totalUnpaidFine;
    }

    @Override
    public String toString() {
        return String.format("Member [ID=%d, Name='%s', Email='%s'] -> Total Unpaid Fines: $%.2f",
                memberId, name, email, totalUnpaidFine);
    }
}
