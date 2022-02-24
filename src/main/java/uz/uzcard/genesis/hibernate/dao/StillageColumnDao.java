package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;

import java.util.stream.Stream;

public interface StillageColumnDao extends Dao<_StillageColumn> {

    _StillageColumn getByCode(_Stillage stillage, String code);

    Stream<_StillageColumn> findByStillage(_Stillage stillage);

    int getMaxOrderByStillage(_Stillage stillage);
}