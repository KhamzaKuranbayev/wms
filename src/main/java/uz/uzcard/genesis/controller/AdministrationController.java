package uz.uzcard.genesis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.order.OrderRequirementRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.ProjectConfigService;

@Api(value = "administration controller")
@RestController
@RequestMapping(value = "/api/administration")
@PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
public class AdministrationController {

    @Autowired
    private ProjectConfigService projectConfigService;

    @ApiOperation(value = "Save attributes the product")
    @Transactional
    @PostMapping(value = "/orderAttachmentRequired", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse saveOrderRequirement(OrderRequirementRequest request) {
        projectConfigService.saveOrderRequirement(request);
        return SingleResponse.of(true);
    }




}
