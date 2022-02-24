package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._UserHashESign;

import java.util.Date;

public interface UserHashESignDao extends Dao<_UserHashESign> {

    _UserHashESign getLastOne(Long userId, Date now);
}
