package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;

public interface RejectProductService {
    ListResponse productRejectedList(DashboardFilter request);
}