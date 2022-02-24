package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

public interface DashBoardService {

    ListResponse boardByTMSExpiredDate(DashboardFilter filterRequest);

    ListResponse warehouseFullness(DashboardFilter request);

    ListResponse supplierBlackList(DashboardFilter filterRequest);

    ListResponse orderSendDateList(DashboardFilter filterRequest);

    ListResponse productRejectedList(DashboardFilter request);

    ListResponse getOrderItemsByProductCategory(DashboardFilter request);

    ListResponse givenProductPriceForDepartment(DashboardFilter filterRequest);

    ListResponse productRemainsAndLimitCountDiff(DashboardFilter filterRequest);

    ListResponse defineOrderStatusCount(DashboardFilter filterRequest);

    ListResponse departmentOrderItems(DashboardFilter filterRequest);

    SingleResponse getContractStatus(DashboardFilter filterRequest);

    ListResponse request(DashboardFilter filterRequest);

    ListResponse givenProductPriceForOneDepartment(DashboardFilter filterRequest);
}