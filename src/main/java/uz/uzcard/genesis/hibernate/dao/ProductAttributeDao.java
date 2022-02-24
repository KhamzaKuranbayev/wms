package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._ProductAttribute;

public interface ProductAttributeDao extends Dao<_ProductAttribute> {
    _ProductAttribute getByProductAndAttribute(_Product product, Long attributeId);
}
