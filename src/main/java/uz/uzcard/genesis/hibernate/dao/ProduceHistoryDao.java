package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._ProduceHistory;

public interface ProduceHistoryDao extends Dao<_ProduceHistory> {
    double getCountByOrderItem(_OrderItem orderItem);

    double getCountByOrderItemAndReqDone(_OrderItem orderItem);
}
