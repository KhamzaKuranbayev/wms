package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.ProductAttributesRequest;
import uz.uzcard.genesis.hibernate.entity._Product;

public interface ProductAttributeService {
    _Product saveAttributes(ProductAttributesRequest request);
}
