package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Stillage;

public interface StillageDao extends Dao<_Stillage> {
    _Stillage getByCell(Long cellId);
}