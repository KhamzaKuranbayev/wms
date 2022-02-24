package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.product.InventarizationFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.InventarizationService;

/**
 * Created by norboboyev_h  on 08.09.2020  13:53
 */
@Api(value = "Inventarization controller", description = "Inventarization")
@RestController
@RequestMapping(value = "/api/inventarization")
public class InventarizationController {

    @Autowired
    private InventarizationService inventarizationService;

    @ApiOperation(value = "Get Inventarization list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(InventarizationFilter request) {
        return ListResponse.of(inventarizationService.list(request), (inventarization, map) -> {
            map.add("auditor", inventarization.getAuditInfo().getCreatedByUser().getFirstName() + " " + inventarization.getAuditInfo().getCreatedByUser().getLastName());
            return map;
        });
    }

    @ApiOperation(value = "Inventarization by warehouse")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-by-warehouse", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getByWarehouse(Long warehouseId) {
        return SingleResponse.of(inventarizationService.getByWarehouse(warehouseId), (inventarization, map) -> map);
    }

    @ApiOperation(value = "Start Inventarization")
    @Transactional
    @PostMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(Long warehouseid) {
        return SingleResponse.of(inventarizationService.start(warehouseid), (inventarization, map) -> map);
    }

    @ApiOperation(value = "End Inventarization")
    @Transactional
    @PostMapping(value = "/end", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse end(Long warehouseid) {
        return SingleResponse.of(inventarizationService.end(warehouseid), (inventarization, map) -> map);
    }
}