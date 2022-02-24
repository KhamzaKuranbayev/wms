package uz.uzcard.genesis.controller.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.order.OrderItemAcceptRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemRejectRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemTenderRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemsRejectRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.service.OrderItemsService;

import javax.validation.Valid;

@Api(value = "Order status controller")
@RestController
@RequestMapping(value = "/api/order-status")
public class OrderItemStatusController {

    @Autowired
    private OrderItemsService orderItemsService;

    @ApiOperation(value = "Order item status change")
    @Transactional
    @PostMapping(value = "/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_STATUS_ACCEPT)")
    public SingleResponse accept(@RequestBody OrderItemAcceptRequest request) {
        return orderItemsService.accept(request.getOrderItemId());
    }

    @ApiOperation(value = "Order item status change")
    @Transactional
    @PostMapping(value = "/specification", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_STATUS_ACCEPT)")
    public SingleResponse specification(@RequestBody OrderItemTenderRequest request) {
        return SingleResponse.of(orderItemsService.specification(request.getOrderItemId()), (orderItem, map) -> map);
    }

    @ApiOperation(value = "Order item status change")
    @Transactional
    @PostMapping(value = "/tender", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_STATUS_TENDER)")
    public SingleResponse tender(@RequestBody OrderItemTenderRequest request) {
        return SingleResponse.of(orderItemsService.tender(request.getOrderItemId()), (orderItem, map) -> map);
    }

    @ApiOperation(value = "Order item status change")
    @Transactional
    @PostMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_STATUS_REJECT)")
    public SingleResponse reject(@Valid OrderItemRejectRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(orderItemsService.reject(request, file), (orderItem, map) -> map);
    }

    @ApiOperation(value = "Order item status change")
    @Transactional
    @PostMapping(value = "/reject-ozl", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_STATUS_REJECT_OZL)")
    public SingleResponse rejectOzl(@Valid OrderItemsRejectRequest request, @RequestPart(value = "file", required = true) MultipartFile file) throws JsonProcessingException {
        if (request == null) {
            throw new ValidatorException("Малумот келмади!!!");
        }
        return SingleResponse.of(orderItemsService.rejectOzl(request, file));
    }
}