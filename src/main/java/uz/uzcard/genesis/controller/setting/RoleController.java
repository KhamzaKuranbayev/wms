package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.dto.api.req.setting.RoleFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.RoleRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.RoleService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.entity._Permission).ROLE)")
@Api(value = "Role controller")
@RestController
@RequestMapping(value = "/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Role list. Dropdown uchun", response = List.class)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items() {
        return ListResponse.of(roleService.list(new RoleFilterRequest() {{
            setLimit(Integer.MAX_VALUE);
        }}).stream().map(role -> new SelectItem(role.getId(), role.getName(), "" + role.getId())).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Role list. Pagination uchun", response = List.class)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(RoleFilterRequest request) {
        return ListResponse.of(roleService.list(request), (role, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Save Role", response = Map.class)
    @PostMapping
    public SingleResponse post(@RequestBody RoleRequest request) {
        return SingleResponse.of(roleService.save(request), (role, map) -> map);
    }

    @ApiOperation(value = "Roleni o`chirish")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        roleService.delete(id);
        return SingleResponse.empty();
    }

    /*@Transactional
    @ApiOperation(value = "Update Role", response = Long.class)
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "appName", value = "应用的名字", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "vq", value = "vq", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "device", value = "设备的名字", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "test", value = "test", required = true, dataType = "array", allowMultiple = true)
    })
    public AbstractMap put(HttpServletRequest request) {
        AbstractMap map = new SelectItem();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getValue() == null) return map;
            if (entry.getValue().length < 2)
                map.addString(entry.getKey(), entry.getValue()[0]);
            else
                map.addStrings(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return map;
    }*/

}
