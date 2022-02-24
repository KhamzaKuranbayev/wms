package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.order.GivenProductOrderItemFilter;
import uz.uzcard.genesis.dto.api.req.order.GivenProductProductItemFilter;
import uz.uzcard.genesis.dto.api.req.setting.GivenProductsFilter;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.GivenProductsService;

/**
 * Created by norboboyev_h  on 26.09.2020  16:02
 */
@Api(value = "Given products controller")
@RestController
@RequestMapping(value = "/api/given-products")
public class GivenProductsController {

    @Autowired
    private GivenProductsService givenProductsService;

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(GivenProductsFilter filter) {
        return ListResponse.of(givenProductsService.list(filter), (givenProducts, map) -> {
            if (givenProducts.getWarehouse() != null) {
                map.add("warehouseId", givenProducts.getWarehouse().getId());
                map.add("warehouseName", givenProducts.getWarehouse().getNameByLanguage());
            }
            if (givenProducts.getOrderItem() != null && givenProducts.getOrderItem().getParent() != null) {
                map.add("orderNumb", "" + givenProducts.getOrderItem().getParent().getNumb());
            }
            if (givenProducts.getContractItem() != null && givenProducts.getContractItem().getParent() != null) {
                map.add("contractCode", givenProducts.getContractItem().getParent().getCode());
            }
            if (givenProducts.getPartition() != null && givenProducts.getPartition().getProduct() != null) {
                map.add("productName", givenProducts.getPartition().getProduct().getName());
            }
            return map;
        });
    }

    @ApiOperation(value = "List order items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-order-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(GivenProductOrderItemFilter filter) {
        return givenProductsService.listOrderItemsForDepartment(filter);
    }

    @ApiOperation(value = "Accepting Products")
    @Transactional
    @PostMapping(value = "/accepting-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse taking(TakenProductRequest request) {
        return givenProductsService.takingAway(request);
    }

    @Transactional
    @ApiOperation(value = "Check Before Sign")
    @PostMapping(value = "/check-before-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkBeforeSigning(TakenProductRequest request) {
        return SingleResponse.of(givenProductsService.takingAwayCheck(request));
    }

    // выдачи, прием ТМЦ
    @ApiOperation(value = "List product items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-product-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(GivenProductProductItemFilter filter) {
        return givenProductsService.listProductItemsForTmsGiven(filter);
    }

}
