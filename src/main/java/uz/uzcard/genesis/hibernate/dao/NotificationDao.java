package uz.uzcard.genesis.hibernate.dao;

import org.springframework.stereotype.Repository;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Notification;

/**
 * Created by norboboyev_h  on 24.12.2020  11:58
 */
@Repository
public interface NotificationDao extends Dao<_Notification> {
    int getUnreadMessageCount();
}
