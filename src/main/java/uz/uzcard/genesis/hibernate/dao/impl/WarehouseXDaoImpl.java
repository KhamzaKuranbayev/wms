package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.WarehouseXDao;
import uz.uzcard.genesis.hibernate.entity._WarehouseX;

@Component
public class WarehouseXDaoImpl extends DaoImpl<_WarehouseX> implements WarehouseXDao {
    public WarehouseXDaoImpl() {
        super(_WarehouseX.class);
    }
}