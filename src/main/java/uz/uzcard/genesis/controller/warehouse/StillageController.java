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
import uz.uzcard.genesis.dto.api.req.warehouse.*;
import uz.uzcard.genesis.dto.api.resp.BoardResponse;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.hibernate.entity._WarehouseY;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.StillageService;
import uz.uzcard.genesis.service.WarehouseService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Stelaj controller", description = "Stelajlar")
@RestController
@RequestMapping("/api/stillage")
public class StillageController {

    @Autowired
    private StillageService stillageService;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private WarehouseService warehouseService;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get all stillage items by warehouse")
    @GetMapping(value = "/get-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(StillageListFilterRequest request) {
        PageStream<_Stillage> pageStream = stillageService.list(request);
        return ListResponse.of(pageStream.stream().map(stillage -> new SelectItem(stillage.getId(), stillage.getName(), "" + stillage.getId())).collect(Collectors.toList()));
    }

    @Transactional
    @ApiOperation(value = "Save stillage")
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody StillageRequest request) {
        return SingleResponse.of(stillageService.save(request), (stillage, map) -> map);
    }

    @Transactional
    @ApiOperation(value = "Ustunlarga bo`lish")
    @PostMapping(value = "/split-column", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(StillageSplitRequest request) {
        stillageService.splitColumn(request.getStillage_id(), request.getColumnCount());
        return SingleResponse.empty();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get all stillage by warehouse - ombordagi shaxmat doska")
    @GetMapping(value = "/board", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse board(StillageFilterRequest request) {
        _Stillage stillage = stillageService.get(request);
        if (stillage == null)
            stillage = stillageService.getByCell(request.getCellId());
        if (ServerUtils.isEmpty(stillage)) {
            throw new ValidatorException("Стиллаж йўқ");
        }
        List<Long> foundList = new ArrayList<>();
        if (!(request.getInventarizationDate() == null && StringUtils.isEmpty(request.getTerm()))) {
            _Stillage finalStillage = stillage;
            foundList.addAll(carriageService.searchByProduct(
                    new WarehouseFilterRequest() {{
                        setId(finalStillage.getWarehouse().getId());
                        setTerm(request.getTerm());
                        setInventarizationDate(request.getInventarizationDate());
                    }}));
        }
        List<BoardResponse> data = wrap(stillage, foundList);
        return ListResponse.of(data);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional
    @ApiOperation(value = "Setup Stillage")
    @PostMapping(value = "/setup", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse setup(@RequestBody StillageSetupRequest request) {
        _Stillage stillage = stillageService.setup(request);
        if (stillage == null)
            return SingleResponse.empty();
        return SingleResponse.of(stillage.getId());
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Setup Stillage")
    @PostMapping(value = "/setup-preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse setupPreview(@RequestBody StillageSetupRequest request) {
        request.setPreview(true);
        _Stillage stillage = stillageService.setup(request);
        return ListResponse.of(wrap(stillage, Collections.emptyList()));
    }

    @ApiOperation(value = "Delete [x;y]")
    @Transactional
    @DeleteMapping(value = "/delete-cell", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse deleteCell(Long id) {
        carriageService.delete(id);
        return SingleResponse.empty();
    }

    private List<BoardResponse> wrap(_Stillage stillage, List<Long> foundList) {
        List<BoardResponse> data = new LinkedList<>();
        if (stillage != null)
            data.addAll(stillage.getColumns().stream().map(stillageColumn -> {
                BoardResponse stillageResponse = new BoardResponse(stillageColumn.getMap().getInstance());
                stillageColumn.getCarriages().forEach(carriage -> {
                    CoreMap map = carriage.getMap();
                    map.addBool("alarm", foundList.contains(map.getId()));
                    stillageResponse.add(new BoardResponse(map.getInstance()));
                });
                return stillageResponse;
            }).collect(Collectors.toList()));
        return data;
    }

    @ApiOperation(value = "Get info of stillage and warehouse by warehouse field")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/info-by-cell")
    public SingleResponse searchEngine(@RequestParam Long cellId) {
        return SingleResponse.of(stillageService.getByCell(cellId), (stillage, map) -> {
            if (stillage.getWarehouse() != null)
                map.add("warehouseName", stillage.getWarehouse().getNameByLanguage());
            _WarehouseY warehouseY = warehouseService.getCellInfo(cellId);
            if (warehouseY != null) {
                map.add("placeType", GlobalizationExtentions.getName(warehouseY.getPlaceType()));
                map.add("row", "" + warehouseY.getRow());
                if (warehouseY.getColumn() != null)
                    map.add("column", "" + warehouseY.getColumn().getColumn());
            }
            return map;
        });
    }
}