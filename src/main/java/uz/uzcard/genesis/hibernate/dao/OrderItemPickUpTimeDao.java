package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._OrderItemPickUpTime;

/**
 * Madaminov Javohir {02.12.2020}.
 */
public interface OrderItemPickUpTimeDao extends Dao<_OrderItemPickUpTime> {

    _OrderItemPickUpTime getById(Long id);
}
