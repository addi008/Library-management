package com.library.dto;

public class BookBorrowReportDTO {
    private int bookId;
    private String title;
    private String author;
    private int borrowCount;

    public BookBorrowReportDTO(int bookId, String title, String author, int borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.borrowCount = borrowCount;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    @Override
    public String toString() {
        return String.format("Book [ID=%d, Title='%s', Author='%s'] -> Total Times Borrowed: %d",
                bookId, title, author, borrowCount);
    }
}
