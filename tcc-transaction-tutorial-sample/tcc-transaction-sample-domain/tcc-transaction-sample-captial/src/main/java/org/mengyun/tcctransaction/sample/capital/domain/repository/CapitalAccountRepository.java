package org.mengyun.tcctransaction.sample.capital.domain.repository;

import org.mengyun.tcctransaction.sample.capital.domain.entity.CapitalAccount;
import org.mengyun.tcctransaction.sample.capital.infrastructure.dao.CapitalAccountDao;
import org.mengyun.tcctransaction.sample.exception.InsufficientBalanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/2/16.
 */
@Repository
public class CapitalAccountRepository {

    @Autowired
    CapitalAccountDao capitalAccountDao;

    public CapitalAccount findByUserId(long userId) {
        System.out.println("CapitalAccountRepository.findByUserId");
        return capitalAccountDao.findByUserId(userId);
    }

    public void save(CapitalAccount capitalAccount) {
        System.out.println("CapitalAccountRepository.save(capitalAccount)");
        int effectCount = capitalAccountDao.update(capitalAccount);
        if (effectCount < 1) {
            throw new InsufficientBalanceException();
        }
    }
}
