package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.ProductAttributeDao;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._ProductAttribute;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "productAttributeDao")
public class ProductAttributeDaoImpl extends DaoImpl<_ProductAttribute> implements ProductAttributeDao {
    public ProductAttributeDaoImpl() {
        super(_ProductAttribute.class);
    }

    @Override
    public _ProductAttribute getByProductAndAttribute(_Product product, Long attributeId) {
        return (_ProductAttribute) findSingle("select t from _ProductAttribute t join t.attribute a where t.state != :deleted and t.product = :product and a.id = :attributeId ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("product", product), new Entry("attributeId", attributeId)));
    }
}
