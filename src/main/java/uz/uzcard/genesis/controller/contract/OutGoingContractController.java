package uz.uzcard.genesis.controller.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.service.OutGoingContractService;
import uz.uzcard.genesis.uitls.AttachmentUtils;

import javax.validation.Valid;

@Api
@RestController
@RequestMapping("/api/outgoing-contract")
public class OutGoingContractController {

    @Autowired
    private OutGoingContractService outGoingContractService;

    @ApiOperation(value = "OutGoing Contract list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(OutGoingContractFilterRequest request) {
        return ListResponse.of(outGoingContractService.list(request), (outGoingContract, map) -> {
            if (outGoingContract.getCustomer() != null) {
                map.add("customerId", outGoingContract.getCustomer().getId());
                map.add("customerName", outGoingContract.getCustomer().getName());
            }
            if (outGoingContract.getProduct() != null) {
                map.add("productId", outGoingContract.getProduct().getId());
                map.add("productName", outGoingContract.getProduct().getName());
                if (outGoingContract.getProduct().getAttr() != null)
                    map.addStrings("productAttrs", outGoingContract.getProduct().getAttr());
            }
            if (outGoingContract.getProductResource() != null) {
                map.add("productResourceLink", AttachmentUtils.getLink(outGoingContract.getProductResource().getName()));
                map.add("productResourceName", outGoingContract.getProductResource().getOriginalName());
            }
            if (outGoingContract.getUnitType() != null) {
                map.add("uniTypeId", outGoingContract.getUnitType().getId());
                map.add("unit_type_name_en", outGoingContract.getUnitType().getNameEn());
                map.add("unit_type_name_ru", outGoingContract.getUnitType().getNameRu());
                map.add("unit_type_name_uz", outGoingContract.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", outGoingContract.getUnitType().getNameCyrl());
            }
            if (outGoingContract.getCustomer() != null) {
                map.add("customerId", outGoingContract.getCustomer().getId());
                map.add("customerName", outGoingContract.getCustomer().getName());
            }
            map.add("closeDate", outGoingContract.getCloseDate().toString());
            map.add("closeContractDate", outGoingContract.getCloseContractDate().toString());
            return map;
        });
    }

    @ApiOperation(value = "Save outgoing contract")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse save(@Valid OutGoingContractRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(outGoingContractService.save(request, file), (outGoingContract, map) -> map);
    }
}
