package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.ProductTypeRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ProductType;

public interface ProductTypeService {
    PageStream<_ProductType> items(String name, Long id);

    _ProductType save(ProductTypeRequest request);

    void delete(Long id);

    PageStream<_ProductType> list(Long parentId, String name, Boolean isParent);
}