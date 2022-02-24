package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Attribute;

public interface AttributeDao extends Dao<_Attribute> {
    boolean checkByName(String name);

    boolean checkByNameAndOwn(Long id, String name);

    _Attribute getById(Long id);
}