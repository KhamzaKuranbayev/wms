package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.ProductAttributeSplitRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterItemRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity._Attribute;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._ProductAttribute;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.ProductService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private ProductTypeDao productTypeDao;
    @Autowired
    private ProductGroupDao productGroupDao;
    @Autowired
    private AttributeDao attributeDao;
    @Autowired
    private ProductAttributeDao productAttributeDao;
    @Autowired
    private UnitTypeDao unitTypeDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private AttachmentService attachmentService;

    @Override
    public PageStream<_Product> search(ProductFilterItemRequest request) {
        FilterParameters filter = new FilterParameters();
        if (!ServerUtils.isEmpty(request.getName())) {
            filter.add("name", request.getName());
        }
        if (!ServerUtils.isEmpty(request.getProductId())) {
            filter.add("productId", "" + request.getProductId());
        }
        if (!ServerUtils.isEmpty(request.getGroup_id())) {
            filter.add("group_id", "" + request.getGroup_id());
        }
        if (!ServerUtils.isEmpty(request.getType_id())) {
            filter.add("type_id", "" + request.getType_id());
        }
        if (request.isAttribute()) {
            filter.add("withOutAttribute", "" + request.isAttribute());
        }
        filter.setSize(request.getLimit());
        filter.setStart(request.getLimit() * request.getPage());
        return productDao.search(filter);
    }

    @Override
    public _Product save(ProductRequest request, MultipartFile file) {
        _Product product = productDao.get(request.getId());
        if (product == null) {
            product = new _Product();
        }
        product.setUniqueKey(request.getUniqueKey());
        product.setExpiration(request.getExpiration());
        product.setName(request.getName());
        product.setLimitCount(request.getLimitCount());
        if (!ServerUtils.isEmpty(file)) {
            product.setMsds(attachmentService.uploadPdf(file));
        }
        if (request.isFileDeleted())
            product.setMsds(null);
        if (request.getProduct_type_id() != null)
            product.setType(productTypeDao.get(request.getProduct_type_id()));
        product.setGroup(productGroupDao.get(request.getProduct_group_id()));
        product.setUnitTypes(unitTypeDao.findAllByIds(request.getUnitTypeIds()).collect(Collectors.toList()));
        unitTypeDao.reindex(request.getUnitTypeIds());
        return productDao.save(product);
    }

    @Override
    public _Product getById(Long id) {
        return productDao.get(id);
    }

    @Override
    public PageStream<_Product> search(ProductFilterRequest request) {
        return productDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setSortColumn(request.getSortBy());
            setSortType(request.getSortDirection());
            setName(request.getName());
        }});
    }

    @Override
    public void delete(Long id) {
        productDao.delete(id);
    }

    @Override
    public void updateBron(_OrderItem orderItem) {
        _Product product = orderItem.getProduct();
        product.setBron(orderItemDao.getAllBronByProduct(product));
        if (product.getCount() < 0)
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_IS_NOT_ENOUGH"));
        productDao.save(product);
    }

    @Override
    public PageStream<_Product> productRemainsAndLimitCountDiff(DashboardFilter request) {
        return productDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setSortColumn("remains_limit_count_diff");
            addBool("materialesThatEnd", true);
        }});
    }

    @Override
    public _Product splitAttribute(ProductAttributeSplitRequest request) {
        _Product product = productDao.get(request.getProductId());
        if (product == null)
            throw new ValidatorException("PRODUCT_NOT_FOUND");
        product.setName(request.getProductName());
        Optional<_ProductAttribute> productAttributeOptional = product.getAttributes().stream().filter(productAttribute -> productAttribute.getAttribute().isByDefault()).findFirst();
        productAttributeOptional.ifPresentOrElse(productAttribute -> {
            _Attribute attribute = productAttribute.getAttribute();
            attribute.setName(product.getName());
            attribute.getItems().addAll(request.getAttributes());
            productDao.save(product);

            productAttribute.getItems().addAll(request.getAttributes());
            productAttributeDao.save(productAttribute);

            product.getAttributes().remove(productAttribute);
            product.getAttributes().add(productAttribute);
            product.getAttr().addAll(request.getAttributes());
        }, () -> {
            _Attribute attribute = new _Attribute();
            attribute.setByDefault(true);
            attribute.setName(product.getName());
            attribute.setItems(request.getAttributes());
            attributeDao.save(attribute);

            _ProductAttribute productAttribute = new _ProductAttribute();
            productAttribute.setProduct(product);
            productAttribute.setAttribute(attribute);
            productAttribute.setItems(request.getAttributes());
            productAttributeDao.save(productAttribute);

            product.getAttributes().add(productAttribute);
        });
        productDao.save(product);
        return product;
    }
}
