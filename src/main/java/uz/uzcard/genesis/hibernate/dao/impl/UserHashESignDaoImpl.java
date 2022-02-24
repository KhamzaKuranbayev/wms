package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.UserHashESignDao;
import uz.uzcard.genesis.hibernate.entity._UserHashESign;
import uz.uzcard.genesis.hibernate.entity._UserSession;

import java.util.Date;

@Component(value = "userHashESignDao")
public class UserHashESignDaoImpl extends DaoImpl<_UserHashESign> implements UserHashESignDao {

    public UserHashESignDaoImpl() {
        super(_UserHashESign.class);
    }

    @Override
    public _UserHashESign getLastOne(Long userId, Date now) {
        return (_UserHashESign) findSingle("select t from _UserHashESign t where t.user.id = :userId and t.toHashESignDate > :nowDate order by id limit 1 ",
                preparing(new Entry("userId", userId), new Entry("nowDate", now)));
    }
}
