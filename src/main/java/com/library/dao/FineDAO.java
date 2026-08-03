package com.library.dao;

import com.library.exception.FineNotFoundException;
import com.library.model.Fine;

import java.util.List;

public interface FineDAO {
    int createFine(Fine fine);
    boolean updateFine(Fine fine);
    Fine getFineById(int fineId) throws FineNotFoundException;
    List<Fine> getFinesByMember(int memberId);
    List<Fine> getUnpaidFinesByMember(int memberId);
    List<Fine> getAllFines();
}
