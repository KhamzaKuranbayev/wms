package uz.uzcard.genesis.controller.warehouse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.warehouse.*;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.CarriageDao;
import uz.uzcard.genesis.hibernate.entity._Carriage;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.service.CarriageService;

/**
 * Created by norboboyev_h  on 19.08.2020  18:51
 */
@Api(value = "Carriage controller")
@RestController
@RequestMapping("/api/carriage")
public class CarriageController {

    @Autowired
    private CarriageService carriageService;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get all Carriages")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(CarriageFilterRequest request) {
        PageStream<_Carriage> pageStream = carriageService.list(request);
        return ListResponse.of(carriageService.list(request), (carriage, map) -> {
            map.put("wareHouseName", carriage.getStillageColumn().getStillage().getName());
            map.put("stillageName", carriage.getStillageColumn().getStillage().getName());
            map.put("stillageAdress", carriage.getStillageColumn().getStillage().getAddress());
            map.put("stillageColumnCode", carriage.getStillageColumn().getCode());
            return map;
        });
    }

    @ApiOperation(value = "Put To Carriage")
    @Transactional
    @PostMapping(value = "/put", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse put(@RequestBody PutToCarriageRequest request) {
        carriageService.put(request);
        return SingleResponse.empty();
    }

    @ApiOperation(value = "For marking as full or not")
    @Transactional(readOnly = true)
    @GetMapping(value = "/for-marking-full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse forMarkingAsFullOrNot(CarriagesForMarkAsFullRequest request) {
        return ListResponse.of(carriageService.getForMarkingAsFullOrNot(request.getIds()), (carriage, map) -> {
            if (carriage.getStillageColumn() != null) {
                map.add("stillageColumnCode", carriage.getStillageColumn().getCode());
                map.add("stillageColumnId", carriage.getStillageColumn().getId());
                if (carriage.getStillageColumn().getStillage() != null) {
                    map.add("stillageName", carriage.getStillageColumn().getStillage().getName());
                }
            }
            return map;
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Single Carriage")
    @GetMapping(value = "single/{carriageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse list(@PathVariable Long carriageId) {
        return SingleResponse.of(carriageService.getSingle(carriageId), (carriage, map) -> {
            if (carriage.getStillageColumn() != null) {
                map.put("stillageColumnCode", carriage.getStillageColumn().getCode());
                if (carriage.getStillageColumn().getStillage() != null) {
                    map.put("wareHouseName", carriage.getStillageColumn().getStillage().getName());
                    map.put("stillageName", carriage.getStillageColumn().getStillage().getName());
                    map.put("stillageAdress", carriage.getStillageColumn().getStillage().getAddress());
                }
            }
            return map;
        });
    }

    @ApiOperation(value = "Mark as full or empty")
    @Transactional
    @PostMapping(value = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse full(@RequestBody CarriageIsFullRequest request) {
        return SingleResponse.of(carriageService.full(request));
    }

    @ApiOperation(value = "Delete carriage")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse full(Long id) {
        carriageService.delete(id);
        return SingleResponse.empty();
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @Transactional
    @ApiOperation(value = "Setup Carriage with and height")
    @PostMapping(value = "/setup-size", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse setupSize(@RequestBody CarriageSizeRequest request) {
        boolean setup = carriageService.setupSize(request);
        return SingleResponse.of(setup);
    }
}
