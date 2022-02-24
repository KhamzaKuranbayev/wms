package uz.uzcard.genesis.controller.contract;

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
import uz.uzcard.genesis.dto.api.req.contract.RealizationFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.RealizationRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.RealizationService;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.validation.Valid;

@Api
@RestController
@RequestMapping("/api/realization")
public class RealizationController {

    @Autowired
    private RealizationService realizationService;

    @ApiOperation(value = "Realization list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(RealizationFilterRequest request) {
        return ListResponse.of(realizationService.list(request), (realization, map) -> {
            if (realization.getProduct() != null) {
                map.add("productId", realization.getProduct().getId());
                map.add("productName", realization.getProduct().getName());
                if (realization.getProduct().getAttr() != null)
                    map.addStrings("productAttrs", realization.getProduct().getAttr());
            }
            if (realization.getAuditInfo() != null) {
                map.add("realizatorId", realization.getAuditInfo().getCreatedByUser().getId());
                map.add("realizatorFullName", realization.getAuditInfo().getCreatedByUser().getFullName());

                if (realization.getDepartment() != null) {
                    map.add("departmentId", realization.getDepartment().getId());
                    map.add("departmentName", realization.getDepartment().getNameByLanguage());
                }
            }
            if (realization.getUnitType() != null) {
                map.add("uniTypeId", realization.getUnitType().getId());
                map.add("unit_type_name_en", realization.getUnitType().getNameEn());
                map.add("unit_type_name_ru", realization.getUnitType().getNameRu());
                map.add("unit_type_name_uz", realization.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", realization.getUnitType().getNameCyrl());
            }
            map.add("realizationDate", realization.getRealizationDate().toString());
            return map;
        });
    }

    @ApiOperation(value = "Save realization")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse save(@Valid RealizationRequest request) {
        return SingleResponse.of(realizationService.save(request), (realization, map) -> map);
    }
}
