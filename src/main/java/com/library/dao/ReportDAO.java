package com.library.dao;

import com.library.dto.BookBorrowReportDTO;
import com.library.dto.MemberActivityReportDTO;
import com.library.dto.MemberWithUnpaidFineDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ReportDAO {
    List<BookBorrowReportDTO> getMostBorrowedBooks(int limit);
    List<MemberActivityReportDTO> getMostActiveMembers(int limit);
    BigDecimal getTotalFinesCollectedThisMonth();
    List<MemberWithUnpaidFineDTO> getMembersWithUnpaidFines();
}
