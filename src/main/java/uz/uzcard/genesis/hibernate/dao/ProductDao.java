package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Product;

public interface ProductDao extends Dao<_Product> {
    boolean checkByName(String name);

    _Product getByUniqueKey(String uniqueKey);

    _Product getByUniqueKeyWithoutId(String uniqueKey, Long id);
}