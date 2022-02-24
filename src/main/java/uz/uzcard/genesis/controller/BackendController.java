package uz.uzcard.genesis.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.order.OrderItemStateChangeRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.backend.PreviousChangesRequest;
import uz.uzcard.genesis.service.*;

@RestController
@RequestMapping("/api/backend")
@PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
public class BackendController {

    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private ContractItemService contractItemService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private HistoryService historyService;

    @Transactional
    @ApiOperation(value = "Update Order item state")
    @PostMapping(value = "/order-item-change-state")
    public SingleResponse orderChangeState(OrderItemStateChangeRequest request) {
        return SingleResponse.of(orderItemsService.changeState(request), (orderItem, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Update Contract item state")
    @PostMapping(value = "/contract-item-change-state")
    public SingleResponse contractChangeState(OrderItemStateChangeRequest request) {
        return SingleResponse.of(contractItemService.changeState(request), (orderItem, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Reload chache ")
    @GetMapping(value = "/reset-cache", produces = MediaType.APPLICATION_JSON_VALUE)
    public void clearCache() {
        accountService.reloadCache();
    }

    @ApiOperation(value = "Recalculete Produce")
    @Transactional
    @PostMapping(value = "/recalculate-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse recalculateProducts() {
        partitionService.reCalculateProducts();
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Recalculate Warehouse")
    @Transactional
    @PostMapping(value = "/recalculate-warehouse", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse recalculateWarehouse(@RequestParam(value = "warehouseId") Long warehouseId) {
        return SingleResponse.of(warehouseService.recalculate(warehouseId), (warehouse, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Delete warehouse-ATTENTION!!!")
    @DeleteMapping(value = "/warehouse-delete-permanently", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse permanentlyDelete(Long id) {
        warehouseService.permanentlyDelete(id);
        return SingleResponse.empty();
    }

    @Transactional
    @ApiOperation(value = "Delete contract")
    @DeleteMapping(value = "/contract-delete-permanently", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse contractDelete(Long id) {
        contractService.delete(id);
        return SingleResponse.of(true);
    }

    @Transactional
    @ApiOperation(value = "Delete order")
    @DeleteMapping(value = "/order-delete-permanently", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        orderService.delete(id);
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Revert previous change Contract")
    @Transactional
    @PostMapping(value = "/previous-changes-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse previousChangesContract(@RequestBody PreviousChangesRequest request) {
        historyService.previousChangesContract(request);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Revert previous change Contract item")
    @Transactional
    @PostMapping(value = "/previous-changes-contract-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse previousChangesContractItem(@RequestBody PreviousChangesRequest request) {
        historyService.previousChangesContractItem(request);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Revert previous change Order")
    @Transactional
    @PostMapping(value = "/previous-changes-order", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse previousChangesOrder(@RequestBody PreviousChangesRequest request) {
        historyService.previousChangesOrder(request);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Revert previous change Order item")
    @Transactional
    @PostMapping(value = "/previous-changes-order-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse previousChangesOrderItem(@RequestBody PreviousChangesRequest request) {
        historyService.previousChangesOrderItem(request);
        return SingleResponse.empty();
    }
}