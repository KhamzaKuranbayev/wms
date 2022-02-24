package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.ProductAttributeSplitRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductAttributesRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterItemRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.ProductResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.api.resp.UnitTypeResponse;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductAttributeDao;
import uz.uzcard.genesis.hibernate.entity._Attribute;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.service.ProductAttributeService;
import uz.uzcard.genesis.service.ProductService;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Product controller", description = "Productlar")
@RestController
@RequestMapping(value = "/api/product")
public class ProductController {

    @Autowired
    public ProductAttributeDao productAttributeDao;
    @Autowired
    public ProductAttributeService productAttributeService;
    @Autowired
    private ProductService productService;

    static CoreMap wrapProduct(_Product product, CoreMap map) {
        if (!product.getUnitTypes().isEmpty()) {
            List<UnitTypeResponse> items = new ArrayList<>();
            product.getUnitTypes().stream().forEach(unitType -> {
                UnitTypeResponse unitTypeResponse = new UnitTypeResponse(unitType.getId(), unitType.getNameEn(), unitType.getNameUz(), unitType.getNameRu(), unitType.getNameCyrl());
                items.add(unitTypeResponse);
            });
            map.addStrings("unitTypes", items);
        }
        if (product.getAttr() != null)
            map.addStrings("attrs", product.getAttr());
        return map;
    }

    @ApiOperation(value = "Get items. Dropdown uchun")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-items", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT," +
            "T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT," +
            "T(uz.uzcard.genesis.hibernate.enums.Permissions).REALIZATION)")
    public ListResponse items(ProductFilterItemRequest request) {
        PageStream<_Product> pageStream = productService.search(request);
        return ListResponse.of(pageStream, ProductController::wrapProduct);
    }

    @ApiOperation(value = "Save product")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT_CREATE)")
    public SingleResponse save(ProductRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(productService.save(request, file), (product, map) -> map);
    }

    @ApiOperation(value = "Get Product")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/getBy-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT)")
    public SingleResponse getById(Long id) {
        _Product product = productService.getById(id);
        if (product == null)
            SingleResponse.empty();
        return SingleResponse.of(wrapResponse(product));
    }

    @ApiOperation(value = "Get Unit type by product")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-product-unittypes", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getProductUnittypes(Long id) {
        _Product product = productService.getById(id);
        if (product == null)
            SingleResponse.empty();
        return SingleResponse.of(product, (product1, map) -> wrapProduct(product1, map));
    }

    @ApiOperation(value = "Get Product list. Pagination uchun")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT)")
    public ListResponse list(ProductFilterRequest request) {
        return ListResponse.of(productService.search(request), (product, map) -> {
            if (product.getGroup() != null)
                map.add("group_name", product.getGroup().getName());
            if (product.getType() != null)
                map.add("type_name", product.getType().getName());
            if (product.getPercentRemainingToLimit() != null)
                map.add("percentRemainingToLimit", product.getPercentRemainingToLimit().toString());
            if (product.getMsds() != null) {
                map.add("msdsFileName", product.getMsds().getName());
                map.add("msdsFileOriginalName", product.getMsds().getOriginalName());
            }
            return wrapProduct(product, map);
        });
    }

    @Transactional
    @ApiOperation(value = "Delete product")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT_ITEM_SPLIT)")
    public SingleResponse delete(Long id) {
        productService.delete(id);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Save attributes the product")
    @Transactional
    @PostMapping(value = "/saveAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT)")
    public SingleResponse saveAttributes(@RequestBody ProductAttributesRequest request) {
        return SingleResponse.of(wrapResponse(productAttributeService.saveAttributes(request)));
    }

    @ApiOperation(value = "Product attribute split")
    @Transactional
    @PostMapping(value = "/split-attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT)")
    public SingleResponse saveAttributes(@RequestBody ProductAttributeSplitRequest request) {
        return SingleResponse.of(wrapResponse(productService.splitAttribute(request)));
    }

    private ProductResponse wrapResponse(_Product product) {
        ProductResponse response = new ProductResponse();
        if (ServerUtils.isEmpty(product)) {
            throw new ValidatorException("PRODUCT_IS_NULL");
        }
        response.putAll(product.getMap(true).getInstance());
//        if (product.getProductPackageType() != null)
//            response.put("productPackageType", product.getProductPackageType().name());
        List<ProductResponse.Attributes> attributes = product.getAttributes().stream().map(productAttribute -> {
            _Attribute attribute = productAttribute.getAttribute();
            return new ProductResponse.Attributes(attribute.getId(), attribute.getName(), productAttribute.getItems());
        }).collect(Collectors.toList());
        response.put("attributes", attributes);
        if (product.getType() != null)
            response.put("type_id", product.getType().getId());
        if (product.getGroup() != null)
            response.put("group_id", product.getGroup().getId());
        if (product.getMsds() != null)
            response.put("MSDSFilePath", product.getMsds().getName());
        return response;
    }
}