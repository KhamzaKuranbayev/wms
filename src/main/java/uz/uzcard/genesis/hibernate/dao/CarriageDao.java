package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Carriage;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;
import uz.uzcard.genesis.hibernate.entity._Warehouse;

import java.util.List;
import java.util.stream.Stream;

public interface CarriageDao extends Dao<_Carriage> {
    int getMaxOrderByColumn(_StillageColumn stillageColumn);

    Stream<_Carriage> findByIds(List<Long> ids);

    Integer totalCarriageCountyWarehouse(_Warehouse warehouse);

    Stream<_Carriage> findByPartition(_Partition partition);

    Stream<_Carriage> findByWarehouse(Long warehouseId);
}
