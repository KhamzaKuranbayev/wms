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
import uz.uzcard.genesis.dto.api.req.contract.ContractItemChangeStatusRequest;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemFilterRequst;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemRequest;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.ParentChildResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Supplier;
import uz.uzcard.genesis.service.ContractItemService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Contract Item controller", description = "Kontrakt itemlar")
@RestController
@RequestMapping(value = "/api/contract")
public class ContractItemController {

    @Autowired
    private ContractItemService contractItemService;

    @ApiOperation(value = "Contract Item save")
    @Transactional
    @PostMapping(value = "/save-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_CREATE)")
    public SingleResponse saveItem(@Valid ContractItemRequest request) {
        return SingleResponse.of(contractItemService.save(request), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item save")
    @Transactional
    @PostMapping(value = "/save-item/ozl", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_CREATE)")
    public SingleResponse saveItemOzl(@Valid ContractItemRequest request) {
        return SingleResponse.of(contractItemService.saveOzl(request), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Update contract item count")
    @Transactional
    @PostMapping(value = "/update-item/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse updateOrderItemCount(@Valid ItemCountRequest request) {
        return SingleResponse.of(contractItemService.updateItemCount(request), (orderItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item status accept")
    @Transactional
    @PostMapping(value = "/accept-item", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_ACCEPT)")
    public SingleResponse acceptItem(@RequestBody Long id) {
        return SingleResponse.of(contractItemService.accept(id), (contractItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item status reject")
    @Transactional
    @PostMapping(value = "/reject-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_REJECT)")
    public SingleResponse rejectItem(ContractItemChangeStatusRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return SingleResponse.of(contractItemService.changeStatus(request, files, _State.CONTRACT_ITEM_REJECT), (contractItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item add akt file")
    @Transactional
    @PostMapping(value = "/attach-akt-file", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_ATTACH_AKT_FILES)")
    public SingleResponse attachAktFile(Long orderItemId, @RequestPart(value = "file") MultipartFile file) {
        return SingleResponse.of(contractItemService.attachAktFile(orderItemId, file).getInstance());
    }

    @ApiOperation(value = "Contract Item status partition accept")
    @Transactional
    @PostMapping(value = "/partition-accept-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_PART_ACCEPT)")
    public SingleResponse partitionAcceptItem(ContractItemChangeStatusRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return SingleResponse.of(contractItemService.changeStatus(request, files, _State.CONTRACT_ITEM_PARTITION_ACCEPTED), (contractItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item status partition")
    @Transactional
    @PostMapping(value = "/part-accept-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_PART_ACCEPT)")
    public SingleResponse partAcceptItem(ContractItemChangeStatusRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return SingleResponse.of(contractItemService.changeStatus(request, files, _State.CONTRACT_ITEM_PART_ACCEPTED), (contractItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "Contract Item list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse listItems(ContractItemFilterRequst request) {
        PageStream<_ContractItem> pageStream = contractItemService.list(request);
        List<ParentChildResponse> list = pageStream.stream().map(contractItem -> {
            CoreMap map = contractItem.getMap();
            if (contractItem.getUnitType() != null) {
                map.add("unit_type_name_en", contractItem.getUnitType().getNameEn());
                map.add("unit_type_name_ru", contractItem.getUnitType().getNameRu());
                map.add("unit_type_name_uz", contractItem.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", contractItem.getUnitType().getNameCyrl());
                map.add("unitTypeId", contractItem.getUnitType().getId());
            }
            if (contractItem.getParent() != null) {
                map.add("contractId", contractItem.getParent().getId().toString());
                map.add("contractNumber", contractItem.getParent().getCode());
                _Supplier supplier = contractItem.getParent().getSupplier();
                if (supplier != null) {
                    map.add("supplierId", supplier.getId());
                    map.add("supplierName", supplier.getName());
                }
            }
            if (contractItem.getProduct() != null) {
                map.add("productId", contractItem.getProduct().getId().toString());
                map.add("productName", contractItem.getProduct().getName());
                map.add("productExpiration", contractItem.getProduct().getExpiration());
                map.addStrings("attrs", contractItem.getProduct().getAttr());
            }
            if (contractItem.getProductGroup() != null) {
                map.add("productGroupId", contractItem.getProductGroup().getId().toString());
                map.add("productGroupName", contractItem.getProductGroup().getName());
            }
            if (contractItem.getProductType() != null) {
                map.add("productTypeId", contractItem.getProductType().getId().toString());
                map.add("productTypeName", contractItem.getProductType().getName());
            }
            if (contractItem.getAuditInfo() != null) {
                if (contractItem.getAuditInfo().getUpdatedByUser() != null) {
                    map.add("updateUser", contractItem.getAuditInfo().getUpdatedByUser().getFirstName() + " " + contractItem.getAuditInfo().getUpdatedByUser().getLastName());
                } else if (contractItem.getAuditInfo().getCreatedByUser() != null) {
                    map.add("updateUser", contractItem.getAuditInfo().getCreatedByUser().getFirstName() + " " + contractItem.getAuditInfo().getCreatedByUser().getLastName());
                }
            }
            if (!contractItem.getOrderItems().isEmpty()) {
                map.add("orderNumb",
                        contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getParent().getNumb()).collect(Collectors.joining(", "))
                );
                map.add("orderItemNumb",
                        contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getItemNumb()).collect(Collectors.joining(", "))
                );

//                List<String> orderResources = new ArrayList<>();
//                contractItem.getOrderItems().forEach(orderItem -> {
//                    if (orderItem.getAttachment() != null) {
//                        orderResources.add(AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//                    }
//                });
//                map.addStrings("orderResources", orderResources);
            }
            if (contractItem.getWarehouseReceivedType() != null)
                map.add("warehouseReceivedType", contractItem.getWarehouseReceivedType().name());
            map.remove("hashESign");

            ParentChildResponse response = ParentChildResponse.of(map);

            if (request.isForPrintQrCode()) {
                Boolean forQRStatus = request.getForQRStatus();
                contractItem.getPartitions().forEach(partition -> {
                    CoreMap partitionMap = partition.getMap();
                    if (partition.getAuditInfo().getCreatedByUser() != null) {
                        partitionMap.add("acceptedById", partition.getAuditInfo().getCreatedByUser().getId());
                        partitionMap.add("acceptedByName", partition.getAuditInfo().getCreatedByUser().getShortName());
                    }
                    ParentChildResponse partitionResponse = ParentChildResponse.of(partitionMap);
                    List<Boolean> collect = partition.getLots().stream().map(lot -> {
                        if (forQRStatus == null) {
                            ParentChildResponse lotResponse = ParentChildResponse.of(lot.getMap());
                            partitionResponse.add(lotResponse);
                        } else {
                            if ((forQRStatus == lot.isQrPrinted())) {
                                ParentChildResponse lotResponse = ParentChildResponse.of(lot.getMap());
                                partitionResponse.add(lotResponse);
                            }
                        }
                        return lot.isQrPrinted();
                    }).collect(Collectors.toList());
                    if (forQRStatus == null || collect.contains(forQRStatus))
                        response.add(partitionResponse);
                });
            }
            return response;
        }).collect(Collectors.toList());
        return ListResponse.of(list, pageStream.getSize());
    }

    @ApiOperation(value = "Contract Item Single")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-single", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getSingle(ContractItemFilterRequst request) {
        return SingleResponse.of(contractItemService.getSingle(request), (contractItem1, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @Transactional
    @ApiOperation(value = "Delete User Agreement")
    @DeleteMapping(value = "/delete-item", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).CONTRACT_ITEM_DELETE)")
    public SingleResponse deleteItem(@RequestBody DeleteRequest request) {
        contractItemService.deleteItem(request);
        return SingleResponse.of(true);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Qr code")
    @GetMapping(value = "/{contractItemId}/qr-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(@PathVariable Long contractItemId) {
        return contractItemService.generateQrCode(contractItemId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Qr code")
    @GetMapping(value = "/{contractItemId}/qr-code/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(@PathVariable Long contractItemId, HttpServletResponse response) {
        contractItemService.generateQrCode(contractItemId, response);
        return SingleResponse.of(true);
    }

    @Transactional
    @ApiOperation(value = "Check Hash ESign")
    @PostMapping(value = "item/check-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(Long id) {
        return contractItemService.checkEDS(id);
    }

    @Transactional
    @ApiOperation(value = "Set Hash ESign")
    @PostMapping(value = "item/set-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(@RequestBody HashESignRequest request) {
        return contractItemService.setHashESign(request);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Contract item akt files")
    @GetMapping(value = "/item/akt-files", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getAktFiles(Long orderItemId) {
        return contractItemService.getAktFiles(orderItemId);
    }
}