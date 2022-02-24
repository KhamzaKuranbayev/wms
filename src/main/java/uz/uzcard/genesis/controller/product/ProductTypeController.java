package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterItemRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductTypeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.ProductTypeResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ProductType;
import uz.uzcard.genesis.service.ProductService;
import uz.uzcard.genesis.service.ProductTypeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(value = "Product type controller", description = "Productlarni tipi")
@RestController
@RequestMapping(value = "/api/product-type")
public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;
    @Autowired
    private ProductService productService;

    @ApiOperation(value = "Get all product types", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getItem(Long id, String name) {
        PageStream<_ProductType> pageStream = productTypeService.items(name, id);
        return ListResponse.of(pageStream.stream()
                .map(productType -> new SelectItem(productType.getId(), productType.getName(), "" + productType.getId()))
                .collect(Collectors.toList()), pageStream.getSize());
    }

    @ApiOperation(value = "Save product types", response = ListResponse.class)
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody ProductTypeRequest request) {
        return SingleResponse.of(productTypeService.save(request), (productType, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Delete product type")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        productTypeService.delete(id);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Get product type list", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(Long parentId, String name) {
        Map<Long, ProductTypeResponse> typeMap = new HashMap<>();

        if (StringUtils.isEmpty(name) || parentId != null) {
            productTypeService.list(parentId, name, parentId == null).stream().forEach(productType -> {
                ProductTypeResponse response = new ProductTypeResponse(productType.getId(), productType.getName(), ProductTypeResponse.Type.Type, productType.getParent() == null ? null : productType.getParent().getId());
                response.setHasParent(productType.getParent() != null);
                typeMap.put(response.getId(), response);
            });
            if (parentId != null)
                productService.search(new ProductFilterItemRequest() {{
                    setName(name);
                    setLimit(Integer.MAX_VALUE);
                    setType_id(parentId);
                }}).stream().forEach(product -> {
                    if (product.getType() == null)
                        return;

                    List<CoreMap> unitTypes = product.getUnitTypes().stream().map(unitType -> unitType.getMap()).collect(Collectors.toList());
                    ProductTypeResponse response = new ProductTypeResponse(product.getId(), product.getName(), ProductTypeResponse.Type.Product, product.getType() == null ? null : product.getType().getId(), unitTypes, product.getAttr());
                    typeMap.put(-1 * product.getId(), response);
                });
            return ListResponse.of(typeMap.values().stream().collect(Collectors.toList()));
        } else {
            productTypeService.list(parentId, name, null).stream().forEach(productType -> {
                recursiveProductTypeParentSearch(typeMap, productType);
            });
            productService.search(new ProductFilterItemRequest() {{
                setName(name);
                setLimit(Integer.MAX_VALUE);
                setType_id(parentId);
            }}).stream().forEach(product -> {
                if (product.getType() == null)
                    return;

                List<CoreMap> unitTypes = product.getUnitTypes().stream().map(unitType -> unitType.getMap()).collect(Collectors.toList());
                ProductTypeResponse response = new ProductTypeResponse(product.getId(), product.getName(), ProductTypeResponse.Type.Product, product.getType() == null ? null : product.getType().getId(), unitTypes, product.getAttr());
                recursiveProductTypeParentSearch(typeMap, product.getType());

                if (typeMap.get(product.getType().getId()) == null)
                    throw new RpcException(String.format("Маҳсулот тури бўйича қидирувда %s маҳсулотнинг %s типи бўйича жойлаштиришда хатолик бўлмоқда",
                            product.getName(), product.getType().getName()));

                typeMap.get(product.getType().getId()).addChild(response);
            });
            return ListResponse.of(typeMap.values().stream().filter(response ->
                    ProductTypeResponse.Type.Type.equals(response.getType()) && (!response.isHasParent() || parentId != null))
                    .collect(Collectors.toList()));
        }
    }

    private void recursiveProductTypeParentSearch(Map<Long, ProductTypeResponse> typeMap, _ProductType productType) {
        if (productType == null)
            return;

        ProductTypeResponse response = new ProductTypeResponse(productType.getId(), productType.getName(), ProductTypeResponse.Type.Type, productType.getParent() == null ? null : productType.getParent().getId());
        response.setHasParent(productType.getParent() != null);
        if (typeMap.get(response.getId()) == null)
            typeMap.put(response.getId(), response);

        recursiveProductTypeParentSearch(typeMap, productType.getParent());

        if (productType.getParent() != null && typeMap.get(productType.getParent().getId()) != null) {
            typeMap.get(productType.getParent().getId()).addChild(response);
        }
    }
}