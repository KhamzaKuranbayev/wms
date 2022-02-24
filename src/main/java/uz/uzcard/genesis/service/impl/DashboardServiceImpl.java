package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._UnitType;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.service.*;

import java.util.stream.Collectors;

@Service(value = "dashboardService")
public class DashboardServiceImpl implements DashBoardService {

    @Autowired
    private PartitionService partitionService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RejectProductService rejectProductService;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductItemSummByDepartmentService productItemSummByDepartmentService;

    @Override
    public ListResponse boardByTMSExpiredDate(DashboardFilter filterRequest) {
        PageStream<_Partition> dashboard = partitionService.dashboard(filterRequest);
        return ListResponse.of(dashboard, (partition, map) -> {
            map = new CoreMap();
            map.add("productName", partition.getProduct().getName());
            map.add("expired", partition.getExpiration().toString());
            if (partition.getContractItem() != null)
                map.add("contractNumb", partition.getContractItem().getParent().getCode());
            return map;
        });
    }

    @Override
    public ListResponse warehouseFullness(DashboardFilter request) {
        PageStream<_Warehouse> pageStream = warehouseService.fullNess(request);
        return ListResponse.of(pageStream, (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            return map;
        });
    }

    @Override
    public ListResponse supplierBlackList(DashboardFilter filterRequest) {
        return contractService.supplierBlackListByContract(filterRequest);
    }

    @Override
    public ListResponse orderSendDateList(DashboardFilter filterRequest) {
        return orderService.getOrderBySendDateFacet(filterRequest);
    }

    @Override
    public ListResponse productRejectedList(DashboardFilter request) {
        return rejectProductService.productRejectedList(request);
    }

    @Override
    public ListResponse getOrderItemsByProductCategory(DashboardFilter request) {
        return orderItemsService.getOrderItemsByProductCategory(request);
    }

    @Override
    public ListResponse givenProductPriceForDepartment(DashboardFilter filterRequest) {
        return productItemSummByDepartmentService.getByMonth(filterRequest);
    }

    @Override
    public ListResponse givenProductPriceForOneDepartment(DashboardFilter filterRequest) {
        return productItemSummByDepartmentService.getByAllHistories(filterRequest);
    }

    @Override
    public ListResponse productRemainsAndLimitCountDiff(DashboardFilter filterRequest) {
        PageStream<_Product> productPageStream = productService.productRemainsAndLimitCountDiff(filterRequest);
        return ListResponse.of(productPageStream, (product, map) -> {
            if (product.getUnitTypes() != null)
                map.addStrings("unittypes", product.getUnitTypes().stream().map(_UnitType::getNameUz).collect(Collectors.toList()));
            return map;
        });
    }

    @Override
    public ListResponse defineOrderStatusCount(DashboardFilter filterRequest) {
        return orderItemsService.defineOrderStatusCount(filterRequest);
    }

    @Override
    public ListResponse departmentOrderItems(DashboardFilter filterRequest) {
        return orderItemsService.departmentOrderItems(filterRequest);
    }

    @Override
    public SingleResponse getContractStatus(DashboardFilter filterRequest) {
        return contractService.getContractStatus(filterRequest);
    }

    @Override
    public ListResponse request(DashboardFilter request) {
        return orderItemsService.latencyRequests(request);
    }
}