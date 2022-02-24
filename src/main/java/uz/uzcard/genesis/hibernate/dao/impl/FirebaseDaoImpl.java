package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.FirebaseTokenDao;
import uz.uzcard.genesis.hibernate.entity._FirebaseToken;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 07.07.2020  17:43
 */
@Component(value = "firebaseDao")
public class FirebaseDaoImpl extends DaoImpl<_FirebaseToken> implements FirebaseTokenDao {

    public FirebaseDaoImpl() {
        super(_FirebaseToken.class);
    }

    @Override
    public _FirebaseToken findByDeviceAndUser(String deviceId) {
        return ((_FirebaseToken) findSingle("select t from  _FirebaseToken t where userId = :userId and " +
                        "deviceId= :deviceId and t.state <> :deleted",
                preparing(new Entry("userId", SessionUtils.getInstance().getUser().getId()),
                        new Entry("deviceId", deviceId),
                        new Entry("deleted", _State.DELETED))));
    }

    @Override
    public Stream<_FirebaseToken> findByUser(Long userId) {
        return findInterval("select distinct t from _FirebaseToken t where t.state <> :deleted and " +
                        " userId = :user order by t.auditInfo.updatedDate desc",
                preparing(new Entry("deleted", _State.DELETED), new Entry("user", userId)), 0, 3);
    }

}
