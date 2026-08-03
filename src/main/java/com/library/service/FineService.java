package com.library.service;

import com.library.exception.FineNotFoundException;
import com.library.model.Fine;

import java.util.List;

public interface FineService {
    boolean payFine(int fineId) throws FineNotFoundException;
    List<Fine> getUnpaidFines(int memberId);
    List<Fine> getFinesByMember(int memberId);
    List<Fine> getAllFines();
}
