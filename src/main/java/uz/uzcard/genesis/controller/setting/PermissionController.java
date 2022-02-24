package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.PermissionRequest;
import uz.uzcard.genesis.dto.api.req.setting.RolePermissionRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.PermissionResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._Role;
import uz.uzcard.genesis.service.PermissionService;

import java.util.stream.Collectors;

//@PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.entity._Permission).PERMISSION)")
@Api(value = "Permission Controller")
@RestController
@RequestMapping(value = "/api/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @ApiOperation(value = "Permissionlar ro`yxati")
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListResponse list() {
        return ListResponse.of(permissionService.getParents().map(permission -> {
            PermissionResponse response = new PermissionResponse(permission.getMap().getInstance());
            response.remove("parent");
            response.addRoles(permission.getRoles().stream().map(_Role::getCode).collect(Collectors.toList()));
            permissionService.getChilds(permission).forEach(child -> {
                PermissionResponse childResponse = new PermissionResponse(child.getMap().getInstance());
                childResponse.addRoles(child.getRoles().stream().map(_Role::getCode).collect(Collectors.toList()));
                response.add(childResponse);
            });
            return response;
        }).collect(Collectors.toList()));
    }

    @ApiOperation(value = "Permissionni saqlash")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody PermissionRequest request) {
        return SingleResponse.of(permissionService.save(request), (state, map) -> map);
    }

    @ApiOperation(value = "Permissionni o`chirish")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        permissionService.delete(id);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Role & Permissionni saqlash")
    @Transactional
    @PostMapping(value = "/bindToRole", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody RolePermissionRequest request) {
        permissionService.bindToRole(request);
        return SingleResponse.empty();
    }
}