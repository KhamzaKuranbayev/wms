package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.product.ProductGroupRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ProductGroup;
import uz.uzcard.genesis.service.ProductGroupService;

import java.util.stream.Collectors;

@Api(value = "Product Group Controller")
@RestController
@RequestMapping("/api/product-group")
public class ProductGroupController {

    @Autowired
    private ProductGroupService productGroupService;

    @ApiOperation(value = "Product group items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(String name, Integer page, Integer limit) {
        page = page == null ? 0 : page;
        limit = limit == null ? 10 : limit;
        PageStream<_ProductGroup> pageStream = productGroupService.items(name, page, limit);
        return ListResponse.of(pageStream.stream()
                .map(productGroup -> new SelectItem(productGroup.getId(), productGroup.getName())).collect(Collectors.toList()), pageStream.getSize());
    }

    @ApiOperation(value = "Get Product group")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/getBy-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getById(Long id) {
        _ProductGroup productGroup = productGroupService.getById(id);
        if (productGroup == null) return SingleResponse.empty();
        return SingleResponse.of(productGroup.getMap(true).getInstance());
    }

    @ApiOperation(value = "Save product group")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody ProductGroupRequest request) {
        return SingleResponse.of(productGroupService.save(request));
    }

    @Transactional
    @ApiOperation(value = "Delete product group")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        productGroupService.delete(id);
        return SingleResponse.empty();
    }
}