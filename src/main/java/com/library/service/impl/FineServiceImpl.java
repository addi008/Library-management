package com.library.service.impl;

import com.library.dao.FineDAO;
import com.library.dao.impl.FineDAOImpl;
import com.library.exception.FineNotFoundException;
import com.library.model.Fine;
import com.library.service.FineService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class FineServiceImpl implements FineService {

    private static final Logger LOGGER = Logger.getLogger(FineServiceImpl.class.getName());
    private final FineDAO fineDAO;

    public FineServiceImpl() {
        this.fineDAO = new FineDAOImpl();
    }

    public FineServiceImpl(FineDAO fineDAO) {
        this.fineDAO = fineDAO;
    }

    @Override
    public boolean payFine(int fineId) throws FineNotFoundException {
        Fine fine = fineDAO.getFineById(fineId);
        if (fine == null) {
            throw new FineNotFoundException("Fine record not found with ID: " + fineId);
        }

        if (fine.isPaid()) {
            LOGGER.info("Fine ID " + fineId + " is already paid.");
            return true;
        }

        fine.setPaid(true);
        fine.setPaidDate(LocalDate.now());

        boolean updated = fineDAO.updateFine(fine);
        if (updated) {
            LOGGER.info("Successfully processed fine payment of $" + fine.getAmount() + " for Fine ID: " + fineId);
        }
        return updated;
    }

    @Override
    public List<Fine> getUnpaidFines(int memberId) {
        return fineDAO.getUnpaidFinesByMember(memberId);
    }

    @Override
    public List<Fine> getFinesByMember(int memberId) {
        return fineDAO.getFinesByMember(memberId);
    }

    @Override
    public List<Fine> getAllFines() {
        return fineDAO.getAllFines();
    }
}
