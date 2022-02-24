package uz.uzcard.genesis.controller.order;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeStatusRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.OrderItemPickUpTimeService;

import javax.validation.Valid;
import java.util.List;

/**
 * Madaminov Javohir {02.12.2020}.
 */
@Api(value = "Order Item pick up time controller")
@RestController
@RequestMapping(value = "/api/order-item/pick-up/time")
public class OrderItemPickUpTimeController {

    @Autowired
    private OrderItemPickUpTimeService orderItemPickUpTimeService;

    @ApiOperation(value = "View a list of Order Item pick up time", response = List.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(OrderItemPickUpTimeFilterRequest request) {
        return orderItemPickUpTimeService.list(request);
    }

    @ApiOperation(value = "Order Item pick up time save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody OrderItemPickUpTimeRequest request) {
        return orderItemPickUpTimeService.save(request);
    }

    @ApiOperation(value = "Order Item pick up time update status")
    @Transactional
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse updateStatus(@RequestBody OrderItemPickUpTimeStatusRequest request) {
        return orderItemPickUpTimeService.updateStatus(request);
    }
}
