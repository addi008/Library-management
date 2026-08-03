package com.library.dao;

import com.library.model.Transaction;

import java.util.List;

public interface TransactionDAO {
    int createTransaction(Transaction transaction);
    boolean updateTransaction(Transaction transaction);
    Transaction getTransactionById(int transactionId);
    List<Transaction> getActiveTransactionsByMember(int memberId);
    List<Transaction> getOverdueTransactions();
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByMember(int memberId);
}
