package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fine {
    private int fineId;
    private int transactionId;
    private BigDecimal amount;
    private boolean paid;
    private LocalDate paidDate;
    private String reason = "Late Return";

    public Fine() {
    }

    public Fine(int fineId, int transactionId, BigDecimal amount, boolean paid, LocalDate paidDate) {
        this.fineId = fineId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paid = paid;
        this.paidDate = paidDate;
    }

    public Fine(int transactionId, BigDecimal amount, boolean paid) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.paid = paid;
    }

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("Fine [ID=%d, TransactionID=%d, Amount=$%.2f, Paid=%s, PaidDate=%s]",
                fineId, transactionId, amount, paid, paidDate);
    }
}
