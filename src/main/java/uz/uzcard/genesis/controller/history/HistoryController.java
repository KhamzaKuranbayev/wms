package uz.uzcard.genesis.controller.history;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.history.ContractHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.OrderHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.ProductItemRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.service.HistoryService;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Javohir Elmurodov
 * @created 20/10/2020 - 4:24 PM
 * @project GTL
 */

@Api(value = "History Controller", description = "History")
@RestController
@RequestMapping(value = "/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @ApiOperation(value = "Contract history list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse contracts(ContractHistoryRequest request) {
        if (request.getCode() == null)
            return ListResponse.of(Collections.emptyList());
        return ListResponse.of(historyService.contracts(request).collect(Collectors.toList()));
    }

    @ApiOperation(value = "Contract Item history list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/contract-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse contractItems(ContractHistoryRequest request) {
        if (request.getCode() == null)
            return ListResponse.of(Collections.emptyList());
        return ListResponse.of(historyService.contractItems(request).collect(Collectors.toList()));
    }

    @ApiOperation(value = "Order history list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse orders(OrderHistoryRequest request) {
        if (request.getNumb() == null)
            return ListResponse.of(Collections.emptyList());
        return ListResponse.of(historyService.orders(request).collect(Collectors.toList()));
    }

    @ApiOperation(value = "Order Item history list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/order-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse orderItems(OrderHistoryRequest request) {
        if (request.getNumb() == null && request.getOrderId() == null)
            return ListResponse.of(Collections.emptyList());
        return ListResponse.of(historyService.orderItems(request).collect(Collectors.toList()));
    }

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/product-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getReplacementHistory(ProductItemRequest request) {
        if (request.getId() == null && request.getQrCode() == null && request.getOrderId() == null)
            return ListResponse.of(Collections.emptyList());
        return ListResponse.of(historyService.getProductItemHistory(request).collect(Collectors.toList()));
    }
}