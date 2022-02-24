package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;

public interface ProductItemSummByDepartmentService {
    ListResponse getByMonth(DashboardFilter filterRequest);

    ListResponse getByAllHistories(DashboardFilter filterRequest);
}
