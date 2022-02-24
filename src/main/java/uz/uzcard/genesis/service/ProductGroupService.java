package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.ProductGroupRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ProductGroup;

public interface ProductGroupService {
    PageStream<_ProductGroup> items(String name, int page, int limit);

    Long save(ProductGroupRequest request);

    _ProductGroup getById(Long id);

    void delete(Long id);
}
