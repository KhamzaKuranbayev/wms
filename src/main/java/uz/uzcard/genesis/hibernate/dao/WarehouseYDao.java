package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._WarehouseY;

import java.util.List;
import java.util.stream.Stream;

public interface WarehouseYDao extends Dao<_WarehouseY> {
    Stream<_WarehouseY> findByIds(List<Long> ids);

    List<Long> findCellsByCarriage(List<Long> carriages);
}