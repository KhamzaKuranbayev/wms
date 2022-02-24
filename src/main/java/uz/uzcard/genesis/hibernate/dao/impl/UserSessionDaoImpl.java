package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.UserSessionDao;
import uz.uzcard.genesis.hibernate.entity._UserSession;

@Component(value = "userSessionDao")
public class UserSessionDaoImpl extends DaoImpl<_UserSession> implements UserSessionDao {
    public UserSessionDaoImpl() {
        super(_UserSession.class);
    }

    @Override
    public _UserSession getBySessionId(String sessionId) {
        return (_UserSession) findSingle("select t from _UserSession t where t.token = :sessionId",
                preparing(new Entry("sessionId", sessionId)));
    }
}
