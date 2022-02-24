package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._UnitType;

import java.util.List;
import java.util.stream.Stream;

public interface UnitTypeDao extends Dao<_UnitType> {
    Stream<_UnitType> findAllByIds(List<Long> ids);

    _UnitType getById(Long id);
}
