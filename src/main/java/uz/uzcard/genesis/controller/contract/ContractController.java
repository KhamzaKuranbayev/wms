package uz.uzcard.genesis.controller.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.resp.*;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.service.ContractItemService;
import uz.uzcard.genesis.service.ContractService;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.AttachmentUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Api(value = "Contract controller", description = "Kontraktlar")
@RestController
@RequestMapping(value = "/api/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private StateService stateService;
    @Autowired
    private ContractItemService contractItemService;

    @ApiOperation(value = "Get Contract")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT)")
    public SingleResponse getItems(Long id) {
        return SingleResponse.of(contractService.get(id), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT)")
    public ListResponse getItems(ContractFilterItemsRequest request) {
        return ListResponse.of(contractService.getItems(request));
    }

    @ApiOperation(value = "Contract list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT)")
    public ListResponse list(ContractFilterRequest request) {
        PageStream<_Contract> pageStream = contractService.list(request);
        return ListResponse.of(pageStream.stream().map(contract -> {
            CoreMap map = contract.getMap();
            if (contract.getProductResource() != null) {
                map.add("productResourceLink", AttachmentUtils.getLink(contract.getProductResource().getName()));
                map.add("productResourceName", contract.getProductResource().getOriginalName());
            }
            if (contract.getRejectResource() != null) {
                map.add("rejectResourceLink", AttachmentUtils.getLink(contract.getRejectResource().getName()));
                map.add("rejectResourceName", contract.getRejectResource().getOriginalName());
            }

            if (contract.getSupplier() != null) {
                map.put("supplierId", contract.getSupplier().getId().toString());
                map.put("supplierName", contract.getSupplier().getName());
            }
            if (!contract.getAgreementResources().isEmpty()) {
                List<Map<String, String>> links = new ArrayList<>();
                contract.getAgreementResources().forEach(attachmentView -> {
                    Map<String, String> map1 = new HashMap<>();
                    map1.put("link", AttachmentUtils.getLink(attachmentView.getName()));
                    map1.put("name", attachmentView.getOriginalName());
                    links.add(map1);
                });
                map.addStrings("aggrementFiles", links);
            } else {
                map.addStrings("aggrementFiles", null);
            }
            if (contract.getSupplyType() != null)
                map.put("supplyType", contract.getSupplyType().name());
            map.remove("hashESign");
            stateService.wrapStatus(map, contract.getState());

            ItemResponse itemResponse = new ItemResponse(map.getInstance(), map.getInstance2());
            OrderResponse response = new OrderResponse(itemResponse);
            if (request.isFiltered() && !request.isForMobile()) {
                /*contract.getItems().forEach(contractItem -> {
                    response.add(new OrderResponse(getDetails(contractItem)));
                });*/
                ContractItemFilterRequst itemFilter = request.wrapIremRequst(contract.getId());
                itemFilter.setLimit(Integer.MAX_VALUE);
                contractItemService.list(itemFilter)
                        .stream().forEach(contractItem -> {
                    response.add(new OrderResponse(getDetails(contractItem)));
                });
            }
            return response;
        }).collect(Collectors.toList()), pageStream.getSize());
    }

    @ApiOperation(value = "Contract save")
    @Transactional
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_CREATE)")
    public SingleResponse save(@Valid ContractRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(contractService.save(request, file), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract update")
    @Transactional
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_CREATE)")
    public SingleResponse update(@Valid ContractUpdateRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(contractService.update(request, file), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract save in OZL (Ordersiz)")
    @Transactional
    @PostMapping(value = "/save-ozl", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_CREATE_OZL)")
    public SingleResponse saveOzl(@Valid ContractRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(contractService.saveOzl(request, file), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract status change")
    @Transactional
    @PostMapping(value = "/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ACCEPT)")
    public SingleResponse accept(@RequestBody Long id) {
        return SingleResponse.of(contractService.accept(id), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract status change")
    @Transactional
    @PostMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_REJECT)")
    public SingleResponse reject(ContractRejectRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(contractService.reject(request, file), (contract, map) -> map);
    }

    @ApiOperation(value = "Contract agreement file add")
    @Transactional
    @PostMapping(value = "/agreement-file", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_AGGREMENT_FILE_ADD)")
    public SingleResponse agreementFile(Long id, @RequestPart(value = "file") MultipartFile file) {
        return SingleResponse.of(contractService.agreementFile(id, file), ((attachmentView, map) -> map));
    }

    @ApiOperation(value = "Contract agreement file delete")
    @Transactional
    @DeleteMapping(value = "/agreement-file-delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_AGGREMENT_FILE_DELETED)")
    public SingleResponse agreementFileDelete(@RequestBody AgreementFileDeleteRequest request) {
        contractService.agreementFileDelete(request);
        return SingleResponse.of(true);
    }

    private ItemResponse getDetails(_ContractItem contractItem) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setMaps(contractItem.getMap().getInstance());
        itemResponse.getMaps().remove("hashESign");
        if (contractItem.getUnitType() != null) {
            itemResponse.getMaps().put("unitTypeId", contractItem.getUnitType().getId().toString());
            itemResponse.getMaps().put("unit_type_name_en", contractItem.getUnitType().getNameEn());
            itemResponse.getMaps().put("unit_type_name_ru", contractItem.getUnitType().getNameRu());
            itemResponse.getMaps().put("unit_type_name_uz", contractItem.getUnitType().getNameUz());
            itemResponse.getMaps().put("unit_type_name_cyrl", contractItem.getUnitType().getNameCyrl());
        }
        if (contractItem.getParent() != null)
            itemResponse.getMaps().put("contractId", contractItem.getParent().getId().toString());
        if (contractItem.getProduct() != null) {
            itemResponse.getMaps().put("productId", contractItem.getProduct().getId().toString());
            itemResponse.getMaps().put("productName", contractItem.getProduct().getName());
            if (contractItem.getProduct().getAttr() != null)
                itemResponse.getListMaps().put("productAttrs", contractItem.getProduct().getAttr());
        }
        if (contractItem.getProductGroup() != null) {
            itemResponse.getMaps().put("productGroupId", contractItem.getProductGroup().getId().toString());
            itemResponse.getMaps().put("productGroupName", contractItem.getProductGroup().getName());
        }
        if (contractItem.getProductType() != null) {
            itemResponse.getMaps().put("productTypeId", contractItem.getProductType().getId().toString());
            itemResponse.getMaps().put("productTypeName", contractItem.getProductType().getName());
        }
        if (contractItem.getAuditInfo() != null) {
            if (contractItem.getAuditInfo().getUpdatedByUser() != null)
                itemResponse.getMaps().put("updateUser", contractItem.getAuditInfo().getUpdatedByUser().getShortName());
            else
                itemResponse.getMaps().put("updateUser", contractItem.getAuditInfo().getCreatedByUser().getShortName());
        }
        if (!contractItem.getOrderItems().isEmpty()) {
            itemResponse.getMaps().put("orderNumb",
                    contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getParent().getNumb()).collect(Collectors.joining(", "))
            );
            itemResponse.getMaps().put("orderItemNumb",
                    contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getItemNumb()).collect(Collectors.joining(", "))
            );

        }
        return itemResponse;
    }

    @ApiOperation("For history")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/node")
    public IResponse getNode(@RequestParam(value = "id") Long id) {
        _Contract contract = contractService.get(id);
        if (contract == null)
            return SingleResponse.empty();
        Stream<_ContractItem> contractItems = contractItemService.findAll(contract);
        ParentChildResponse response = new ParentChildResponse(contract.getMap());
        contractItems.forEach(contractItem -> {
            CoreMap itemMap = contractItem.getMap();
            if (contractItem.getProduct() != null) {
                itemMap.add("productName", contractItem.getProduct().getName());
            }
            if (contractItem.getUnitType() != null) {
                itemMap.add("unitType", contractItem.getUnitType().getNameCyrl());
            }
            response.add(new ParentChildResponse(itemMap));
        });
        return response;
    }
}