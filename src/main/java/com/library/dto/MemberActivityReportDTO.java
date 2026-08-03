package com.library.dto;

public class MemberActivityReportDTO {
    private int memberId;
    private String name;
    private String email;
    private int transactionCount;

    public MemberActivityReportDTO(int memberId, String name, String email, int transactionCount) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.transactionCount = transactionCount;
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

    public int getTransactionCount() {
        return transactionCount;
    }

    @Override
    public String toString() {
        return String.format("Member [ID=%d, Name='%s', Email='%s'] -> Total Borrowing Transactions: %d",
                memberId, name, email, transactionCount);
    }
}
