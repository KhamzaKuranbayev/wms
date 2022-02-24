package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.DepartmentService;

import java.util.List;

@Api(value = "Department controller", description = "Bo'limlar")
@RestController
@RequestMapping(value = "/api/deparments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "Items of Department", response = List.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(DepartmentFilterRequest request) {
        return departmentService.items(request);
    }

    @ApiOperation(value = "View a list of Department", response = List.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(DepartmentFilterRequest request) {
        return departmentService.list(request);
    }

    @ApiOperation(value = "Department save")
    @Transactional
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody DepartmentRequest request) {
        return departmentService.save(request);
    }

    @ApiOperation(value = "Department delete")
    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(@PathVariable Long id) {
        departmentService.delete(id);
        return SingleResponse.of(true);
    }

}
