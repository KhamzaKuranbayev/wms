package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.api.req.product.ProductAttributesRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.AttributeDao;
import uz.uzcard.genesis.hibernate.dao.ProductAttributeDao;
import uz.uzcard.genesis.hibernate.dao.ProductDao;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._ProductAttribute;
import uz.uzcard.genesis.service.ProductAttributeService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
public class ProductAttributeServiceImpl implements ProductAttributeService {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private AttributeDao attributeDao;
    @Autowired
    private ProductAttributeDao productAttributeDao;

    @Override
    public _Product saveAttributes(ProductAttributesRequest request) {
        _Product product = productDao.get(request.getProductId());
        if (product == null) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_NOT_FOUND"));
        }
        Iterator<_ProductAttribute> iterator = product.getAttributes().iterator();
        while (iterator.hasNext()) {
            _ProductAttribute productAttribute = iterator.next();
            if (productAttribute.getAttribute() == null)
                iterator.remove();

            Long attributeId = productAttribute.getAttribute().getId();
            request.getAttributes().stream().filter(attribute -> attributeId.equals(attribute.getId())).findFirst()
                    .ifPresentOrElse(attribute -> {
                        _ProductAttribute refreshAttr = productAttributeDao.getByProductAndAttribute(product, attribute.getId());
                        refreshAttr.setItems(attribute.getItems());
                        productAttributeDao.save(refreshAttr);
                        request.getAttributes().remove(attribute);
                    }, () -> {
                        productAttributeDao.delete(productAttribute);
                        iterator.remove();
                    });
        }
        request.getAttributes().forEach(attribute -> {
            _ProductAttribute productAttribute = new _ProductAttribute();
            productAttribute.setProduct(productDao.get(request.getProductId()));
            productAttribute.setAttribute(attributeDao.get(attribute.getId()));
            productAttribute.setItems(attribute.getItems());
            productAttributeDao.save(productAttribute);
            product.getAttributes().add(productAttribute);
        });
        List<String> attrs = new LinkedList<>();
        product.getAttributes().forEach(productAttribute1 -> {
            productAttribute1.getItems().forEach(s -> {
                attrs.add(s);
            });
        });
        product.getAttr().clear();
        product.setAttr(attrs);
        productDao.save(product);
        return product;
    }
}
