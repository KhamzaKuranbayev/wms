package uz.uzcard.genesis.service;


import uz.uzcard.genesis.dto.api.req.order.OrderRequirementRequest;

public interface ProjectConfigService {

    void saveOrderRequirement(OrderRequirementRequest request);
}
