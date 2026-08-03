package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidTransactionException;
import com.library.exception.MaxBooksBorrowedException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction issueBook(int memberId, int bookId)
            throws MemberNotFoundException, BookNotFoundException, BookNotAvailableException, MaxBooksBorrowedException;

    Transaction returnBook(int transactionId)
            throws InvalidTransactionException, BookNotFoundException;

    List<Transaction> getOverdueBooks();
    List<Transaction> getTransactionsByMember(int memberId);
    List<Transaction> getAllTransactions();
}
