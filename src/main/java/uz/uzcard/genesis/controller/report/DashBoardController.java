package uz.uzcard.genesis.controller.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.DashBoardService;

@Api("Dashboard")
@RestController
@RequestMapping("/api/dashboards")
public class DashBoardController {
    @Autowired
    private DashBoardService dashBoardService;

    /**
     * Warehouse
     */
    @ApiOperation(value = "заполненность складов")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/warehouse-fullness", method = RequestMethod.GET)
    public ListResponse warehouseFullness(DashboardFilter filterRequest) {
        return dashBoardService.warehouseFullness(filterRequest);
    }

    @ApiOperation(value = "отчет по истечению сроков ТМЦ")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/tms-expired", method = RequestMethod.GET)
    public ListResponse getAllProducts(DashboardFilter filterRequest) {
        return dashBoardService.boardByTMSExpiredDate(filterRequest);
    }

    @ApiOperation(value = "отчет по просрочкам")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/supplier-black-list", method = RequestMethod.GET)
    public ListResponse getAllSupplierBlackList(DashboardFilter filterRequest) {
        return dashBoardService.supplierBlackList(filterRequest);
    }

    @ApiOperation(value = "отчет по бракам")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/rejected-products", method = RequestMethod.GET)
    public ListResponse productRejectedList(DashboardFilter filterRequest) {
        return dashBoardService.productRejectedList(filterRequest);
    }

    @ApiOperation(value = "часто используемые ТМЦ")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/given-product-price", method = RequestMethod.GET)
    public ListResponse givenProductPriceForDepartment(DashboardFilter filterRequest) {
        return dashBoardService.givenProductPriceForDepartment(filterRequest);
    }

    @ApiOperation(value = "Задержки запросов")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/latency-requests", method = RequestMethod.GET)
    public ListResponse latencyRequests(DashboardFilter filterRequest) {
        return dashBoardService.request(filterRequest);
    }

    @ApiOperation(value = "часто используемые ТМЦ (на филиал)")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/given-product-price-one", method = RequestMethod.GET)
    public ListResponse givenProductPriceForOneDepartment(DashboardFilter filterRequest) {
        return dashBoardService.givenProductPriceForOneDepartment(filterRequest);
    }

    @ApiOperation(value = "ТМЦ которые кончаются")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/product-limit-count", method = RequestMethod.GET)
    public ListResponse productRemainsAndLimitCountDiff(DashboardFilter filterRequest) {
        return dashBoardService.productRemainsAndLimitCountDiff(filterRequest);
    }

    /**
     * Orders
     */
    @ApiOperation(value = "по времени")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/order-send-list", method = RequestMethod.GET)
    public ListResponse getAllOrderSendList(DashboardFilter filterRequest) {
        return dashBoardService.orderSendDateList(filterRequest);
    }

    @ApiOperation(value = "по категориям")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/order-items-by-product-category", method = RequestMethod.GET)
    public ListResponse orderItemsByCategory(DashboardFilter filterRequest) {
        return dashBoardService.getOrderItemsByProductCategory(filterRequest);
    }

    @ApiOperation(value = "по статусам позиций запросов")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/define-order-status-count", method = RequestMethod.GET)
    public ListResponse defineOrderStatusCount(DashboardFilter filterRequest) {
        return dashBoardService.defineOrderStatusCount(filterRequest);
    }

    @ApiOperation(value = "по подразделениям")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/define-orderitem-by-department", method = RequestMethod.GET)
    public ListResponse departmentOrderItems(DashboardFilter filterRequest) {
        return dashBoardService.departmentOrderItems(filterRequest);
    }

    /**
     * Contracts
     */
    @ApiOperation(value = "заключено и принято")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/contract-status", method = RequestMethod.GET)
    public SingleResponse getContractStatus(DashboardFilter filterRequest) {
        return dashBoardService.getContractStatus(filterRequest);
    }
}
