package uz.uzcard.genesis.controller.warehouse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.WarehouseSearchEngineFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.WarehouseRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseSetupRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseYRequest;
import uz.uzcard.genesis.dto.api.resp.BoardResponse;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.service.WarehouseService;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Warehouse controller", description = "Omborxonalar")
@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get all warehouses")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(WarehouseFilterRequest request) {
        return ListResponse.of(warehouseService.list(request), (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            if (warehouse.getDepartment() != null) {
                map.add("departmentId", warehouse.getDepartment().getId());
                map.add("departmentName", warehouse.getDepartment().getNameByLanguage());
            }
            return map;
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get own warehouses")
    @GetMapping(value = "/own-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse ownWarehouses() {
        String language = SessionUtils.getInstance().getLanguage();
        return ListResponse.of(warehouseService.ownList(), (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            if (warehouse.getDepartment() != null) {
                map.add("departmentId", warehouse.getDepartment().getId());
                map.add("departmentName", warehouse.getDepartment().getNameByLanguage());
            }
            return map;
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get warehouses list for dropdown")
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(@RequestParam(required = false) String name) {
        return ListResponse.of(warehouseService.getItems(name));
    }

    @Transactional
    @ApiOperation(value = "Save warehouse")
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody WarehouseRequest request) {
        return SingleResponse.of(warehouseService.save(request), (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            return map;
        });
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional
    @ApiOperation(value = "Delete warehouse")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        warehouseService.delete(id);
        return SingleResponse.empty();
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional
    @ApiOperation(value = "Setup Warehouse")
    @PostMapping(value = "/setup", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse setup(@RequestBody WarehouseSetupRequest request) {
        warehouseService.setup(request);
        return SingleResponse.empty();
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @ApiOperation(value = "Setup Warehouse")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @PostMapping(value = "/setup-preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse setupPreview(@RequestBody WarehouseSetupRequest request) {
        request.setPreview(true);
        setup(request);
        return board(new WarehouseFilterRequest(request.getId()));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get Warehouse Board  - ombordagi shaxmat doska")
    @GetMapping(value = "/board", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse board(WarehouseFilterRequest request) {
        _Warehouse warehouse = warehouseService.get(request);
        List<Long> foundList = new ArrayList<>();
        if (!(request.getInventarizationDate() == null && StringUtils.isEmpty(request.getTerm()))) {
            foundList.addAll(warehouseService.searchByProduct(request));
        }

        List<BoardResponse> data = new LinkedList<>();
        if (warehouse != null)
            data.addAll(warehouse.getColumns().stream().map(column -> {
                BoardResponse response = new BoardResponse(column.getMap().getInstance());
                column.getRows().forEach(row -> {
                    CoreMap map = row.getMap();
                    if (row.getPlaceType() != null)
                        map.add("placeType", row.getPlaceType().name());
                    map.addBool("alarm", foundList.contains(map.getId()));
                    response.add(new BoardResponse(map.getInstance()));
                });
                return response;
            }).collect(Collectors.toList()));

        return ListResponse.of(data);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get Warehouse Board  - ombordagi shaxmat doska")
    @GetMapping(value = "/get-cells", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getCells(WarehouseFilterRequest request) {
        _Warehouse warehouse = warehouseService.get(request);
        if (ServerUtils.isEmpty(warehouse)) {
            throw new ValidatorException("WARE_HOUSE_IS_NULL");
        }
        LinkedList<SelectItem> items = new LinkedList<>();
        warehouse.getColumns().forEach(column -> {
            column.getRows().forEach(row -> {
                items.add(new SelectItem(row.getId(), String.format("%s %s", column.getColumn(), row.getRow()), "" + row.getId(), column.getColumn()));
            });
        });
        return ListResponse.of(items);
    }

    @ApiOperation(value = "Get warehouse")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-item")
    public SingleResponse getItem(@RequestParam(value = "id") Long id) {
        return SingleResponse.of(warehouseService.get(new WarehouseFilterRequest(id)), (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            return map;
        });
    }

    @ApiOperation(value = "Search engine")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/search-engine")
    public ListResponse searchEngine(WarehouseSearchEngineFilterRequest request) {
        return ListResponse.of(warehouseService.searchEngine(request), (warehouse, map) -> {
            map.add("name", warehouse.getNameByLanguage());
            map.add("term", request.getName());
            return map;
        });
    }

    @ApiOperation(value = "Delete [x;y]")
    @Transactional
    @DeleteMapping(value = "/delete-cell", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse deleteCell(Long id) {
        warehouseService.deleteCell(id);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "Delete [x;y]")
    @Transactional
    @PostMapping(value = "/update-cell", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse full(@RequestBody WarehouseYRequest request) {
        return SingleResponse.of(warehouseService.updateCell(request), (warehouseY, map) -> map);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional
    @ApiOperation(value = "Setup Warehouse")
    @PostMapping(value = "/setup/occupancy-percentage-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse setup() {
        warehouseService.setPercentageAll();
        return SingleResponse.empty();
    }
}