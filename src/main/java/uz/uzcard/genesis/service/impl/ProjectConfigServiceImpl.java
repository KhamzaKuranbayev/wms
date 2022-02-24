package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.config.properties.ProjectConfigImpl;
import uz.uzcard.genesis.dto.api.req.order.OrderRequirementRequest;
import uz.uzcard.genesis.service.ProjectConfigService;


@Service
public class ProjectConfigServiceImpl implements ProjectConfigService {

    @Autowired
    ProjectConfigImpl projectConfig;

    @Override
    public void saveOrderRequirement(OrderRequirementRequest request) {
        projectConfig.setValue(request.getKey(), request.getValue());
    }
}
