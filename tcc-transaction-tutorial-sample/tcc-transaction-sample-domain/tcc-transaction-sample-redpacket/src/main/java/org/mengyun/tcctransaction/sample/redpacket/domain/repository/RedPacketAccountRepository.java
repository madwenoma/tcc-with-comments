package org.mengyun.tcctransaction.sample.redpacket.domain.repository;


import org.apache.log4j.spi.LoggerFactory;
import org.mengyun.tcctransaction.sample.exception.InsufficientBalanceException;
import org.mengyun.tcctransaction.sample.redpacket.domain.entity.RedPacketAccount;
import org.mengyun.tcctransaction.sample.redpacket.infrastructure.dao.RedPacketAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/2/16.
 */
@Repository
public class RedPacketAccountRepository {
    @Autowired
    RedPacketAccountDao redPacketAccountDao;

    public RedPacketAccount findByUserId(long userId) {
        System.out.println("RedPacketAccountRepository.findByUserId FROM red_red_packet_account");
        return redPacketAccountDao.findByUserId(userId);
    }

    public void save(RedPacketAccount redPacketAccount) {
        System.out.println("RedPacketAccountRepository.save(red_red_packet_account)");
        int effectCount = redPacketAccountDao.update(redPacketAccount);
        if (effectCount < 1) {
            throw new InsufficientBalanceException();
        }
    }
}
