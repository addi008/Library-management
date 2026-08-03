package com.library.model;

import java.time.LocalDate;

public class Transaction {

    public enum TransactionStatus {
        ISSUED, RETURNED, OVERDUE
    }

    private int transactionId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private TransactionStatus status;

    public Transaction() {
    }

    public Transaction(int transactionId, int bookId, int memberId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, TransactionStatus status) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public Transaction(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate, TransactionStatus status) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Transaction [ID=%d, BookID=%d, MemberID=%d, IssueDate=%s, DueDate=%s, ReturnDate=%s, Status=%s]",
                transactionId, bookId, memberId, issueDate, dueDate, returnDate, status);
    }
}
