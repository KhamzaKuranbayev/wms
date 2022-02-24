package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.contract.InventarizationLogFilter;
import uz.uzcard.genesis.dto.api.req.product.InventarizationLogRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.InventarizationLogService;

/**
 * Created by norboboyev_h  on 08.09.2020  14:00
 */
@Api(value = "Inventarization Log controller", description = "Inventarization Log")
@RestController
@RequestMapping(value = "/api/inventarization-log")
public class InventarizationLogController {

    @Autowired
    private InventarizationLogService inventarizationLogService;

    @ApiOperation(value = "Get Inventarization Log list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(InventarizationLogFilter request) {
        return ListResponse.of(inventarizationLogService.list(request), (inventarizationLog, map) -> {
            map.add("auditor", inventarizationLog.getInventarization().getAuditInfo().getCreatedByUser().getFirstName() + " " +
                    inventarizationLog.getInventarization().getAuditInfo().getCreatedByUser().getFirstName());
            if (inventarizationLog.getProductItem() != null) {
                map.add("product", inventarizationLog.getProductItem().getName());
                if (inventarizationLog.getProductItem().getProduct() != null) {
                    if (inventarizationLog.getProductItem().getProduct().getGroup() != null)
                        map.add("productGroupName", inventarizationLog.getProductItem().getProduct().getGroup().getName());
                    if (inventarizationLog.getProductItem().getProduct().getType() != null)
                        map.add("productTypeName", inventarizationLog.getProductItem().getProduct().getType().getName());
                }
            }
            map.add("createdAt", inventarizationLog.getAuditInfo().getCreationDate().toString());
            return map;
        });
    }

    @ApiOperation(value = "Save Inventarization Log")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody InventarizationLogRequest request) {
        inventarizationLogService.save(request);
        return SingleResponse.of(true);
    }
}
