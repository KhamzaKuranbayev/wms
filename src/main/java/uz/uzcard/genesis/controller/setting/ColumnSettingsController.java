package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.ColumnSettingsSortRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsRequest;
import uz.uzcard.genesis.dto.api.req.setting.ColumnSettingsUpdateRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.enums.TableType;
import uz.uzcard.genesis.service.ColumnSettingsService;

import java.util.stream.Collectors;

@RestController
@Api(value = "Column settings controller")
@RequestMapping(value = "/api/column-settings")
public class ColumnSettingsController {

    @Autowired
    private ColumnSettingsService columnSettingsService;

    @ApiOperation(value = "Column settings list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(TableType table) {
        return ListResponse.of(columnSettingsService.listByTable(table), (columnSettings, map) -> {
            if (columnSettings.getRoles() != null && !columnSettings.getRoles().isEmpty())
                map.addStrings("roles", columnSettings.getRoles().stream().map(role -> role.toString()).collect(Collectors.toList()));
            map.addStrings("roleIds", columnSettings.getRoles().stream().map(role -> "" + role.getId()).collect(Collectors.toList()));
            return map;
        });
    }

    @ApiOperation(value = "Column settings save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody ColumnSettingsRequest request) {
        return SingleResponse.of(columnSettingsService.save(request), (t, map) -> map);
    }

    @ApiOperation(value = "Column settings list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/columns", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse columns(TableType table) {
        return ListResponse.of(columnSettingsService.findByTable(table));
    }

    @Transactional
    @ApiOperation(value = "Delete column setings")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        columnSettingsService.delete(id);
        return SingleResponse.empty();
    }

    @Transactional
    @ApiOperation(value = "Update column setings")
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse update(@RequestBody ColumnSettingsUpdateRequest request) {
        return SingleResponse.of(columnSettingsService.update(request), (columnSettings, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Update session user column setings")
    @PostMapping(value = "/current-user/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse updateCurrent(@RequestBody ColumnSettingsUpdateRequest request) {
        return SingleResponse.of(columnSettingsService.updateCurrent(request), (columnSettings, map) -> map);
    }

    @Transactional
    @PostMapping(value = "/sort", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse sort(@RequestBody ColumnSettingsSortRequest request) {
        columnSettingsService.sort(request);
        return SingleResponse.empty();
    }
}