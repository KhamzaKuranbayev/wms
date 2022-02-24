package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.PackageTypeRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.PackageTypeService;

@Api(value = "Package type controller", description = "Package type")
@RestController
@RequestMapping(value = "/api/packageType")
public class PackageTypeController {

    @Autowired
    private PackageTypeService packageTypeService;

    @ApiOperation(value = "Save package type")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody PackageTypeRequest request) {
        return SingleResponse.of(packageTypeService.save(request));
    }

    @ApiOperation(value = "Delete package type")
    @Transactional
    @DeleteMapping(value = "/delete")
    public SingleResponse delete(Long id) {
        packageTypeService.delete(id);
        return SingleResponse.empty();
    }
}