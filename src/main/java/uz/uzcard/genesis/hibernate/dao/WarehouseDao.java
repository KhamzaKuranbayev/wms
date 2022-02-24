package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Warehouse;

import java.util.List;
import java.util.stream.Stream;

public interface WarehouseDao extends Dao<_Warehouse> {
    Stream<_Warehouse> findAll();

    Stream<_Warehouse> getByIds(List<Long> ids);

    Stream<_Warehouse> findAllWarehouseByDepartment(Long id);
}