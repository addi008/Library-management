package com.library.service;

import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;
import com.library.model.Book;

import java.math.BigDecimal;
import java.util.List;

public interface SearchReportService {
    List<Book> searchBooks(String keyword, String category);
    List<Book> getBooksIssuedToMember(int memberId);

    List<BookBorrowReportDTO> getMostBorrowedBooks(int limit);
    List<MemberActivityReportDTO> getMostActiveMembers(int limit);
    BigDecimal getTotalFinesCollectedThisMonth();
    List<MemberWithUnpaidFineDTO> getMembersWithUnpaidFines();
}
