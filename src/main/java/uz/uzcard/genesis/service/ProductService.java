package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.ProductAttributeSplitRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterItemRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._Product;

public interface ProductService {
    PageStream<_Product> search(ProductFilterItemRequest name);

    _Product save(ProductRequest request, MultipartFile file);

    _Product getById(Long id);

    PageStream<_Product> search(ProductFilterRequest request);

    void delete(Long id);

    void updateBron(_OrderItem orderItem);

    PageStream<_Product> productRemainsAndLimitCountDiff(DashboardFilter filterRequest);

    _Product splitAttribute(ProductAttributeSplitRequest request);
}