package uz.uzcard.genesis.controller.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.UnitTypeService;

@Api(value = "Unit Type controller", description = "O'lchamlar")
@RestController
@RequestMapping(value = "/api/unit-type")
public class UnitTypeController {

    @Autowired
    private UnitTypeService unitTypeService;

    @ApiOperation(value = "Save unit type")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody UnitTypeRequest request) {
        return SingleResponse.of(unitTypeService.save(request), (product, map) -> map);
    }

    @ApiOperation(value = "Get Unit type list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(UnitTypeFilterRequest request) {
        return ListResponse.of(unitTypeService.search(request), (unitType, map) -> {
            map.add("name_en", unitType.getNameEn());
            map.add("name_ru", unitType.getNameRu());
            map.add("name_uz", unitType.getNameUz());
            return map;
        });
    }

    @Transactional
    @ApiOperation(value = "Delete unitType")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        unitTypeService.delete(id);
        return SingleResponse.empty();
    }
}
