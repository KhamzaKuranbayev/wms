package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._OTPMessage;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.Date;

public interface OtpMessageDao extends Dao<_OTPMessage> {
    _OTPMessage findByUserName(String userName);

    Long countByUser(_User user);

    Date lastSentCode(_User user);

    Long getAllMessagesByUserName(Long userId, Date before, Date current);
}