package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Inventarization;
import uz.uzcard.genesis.hibernate.entity._InventarizationLog;
import uz.uzcard.genesis.hibernate.entity._ProductItem;
import uz.uzcard.genesis.hibernate.entity._Warehouse;

public interface InventarizationDao extends Dao<_Inventarization> {
    _Inventarization getByWarehouse(_Warehouse warehouse);

    _InventarizationLog getByInventarizationAndProductItem(_Inventarization inventarization, _ProductItem productItem);
}