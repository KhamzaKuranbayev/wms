package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._UserSession;

public interface UserSessionDao extends Dao<_UserSession> {
    _UserSession getBySessionId(String sessionId);
}
