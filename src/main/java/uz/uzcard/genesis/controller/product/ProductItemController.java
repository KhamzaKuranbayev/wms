package uz.uzcard.genesis.controller.product;

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
import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.dto.api.req.patient.ProduceRequest;
import uz.uzcard.genesis.dto.api.req.product.*;
import uz.uzcard.genesis.dto.api.req.setting.PackageByProductAndPackageTypeRequest;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.ProductItemService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Product item controller", description = "Product items")
@RestController
@RequestMapping(value = "/api/product-item")
public class ProductItemController {

    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private CarriageService carriageService;

    @ApiOperation(value = "Get package types by project")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/getPackageTypesBy-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getPackageTypesByProduct(Long product_id) {
        return ListResponse.of(productItemService.getPackageTypesByProduct(product_id));
    }

    @ApiOperation(value = "Get package by project and packageType")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/getPackageBy-project-and-packageType", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getPackageByProjectAndPackageType(PackageByProductAndPackageTypeRequest request) {
        return ListResponse.of(productItemService.getPackageByProjectAndPackageType(request.getProduct_id(), request.getPackageType_id()));
    }

    @ApiOperation(value = "Inventarization")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(ProductFilterItemRequest request) {
        return ListResponse.of(productItemService.list(request), (productItem, map) -> wrapList(request, productItem, map));
    }

    private CoreMap wrapList(ProductFilterItemRequest request, uz.uzcard.genesis.hibernate.entity._ProductItem productItem, CoreMap map) {
        if (!ServerUtils.isEmpty(productItem.getProduct())) {
            map.put("productName", productItem.getProduct().getName());
//                map.add("packageType", productItem.getProduct().getProductPackageType().name());
            if (!ServerUtils.isEmpty(productItem.getProduct().getType()))
                map.add("productTypeName", productItem.getProduct().getType().getName());
            if (!ServerUtils.isEmpty(productItem.getProduct().getGroup()))
                map.add("productGroup", productItem.getProduct().getGroup().getName());
            if (productItem.getProduct().getAttr() != null)
                map.addStrings("productAttrs", productItem.getProduct().getAttr());
            if (!(productItem.getCarriages_id() == null || productItem.getCarriages_id().isEmpty())) {
                List<PartitionCarriageAddressDto> adresses = carriageService.getAddresses(productItem.getCarriages_id());
                map.addStrings("address", adresses.stream().map(addressDto ->
                        ServerUtils.gson.toJson(addressDto)).collect(Collectors.toList()));
            }
        }
        if (productItem.getUnitType() != null) {
            map.add("unitTypeId", productItem.getUnitType().getId());
            map.add("unit_type_name_en", productItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", productItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", productItem.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", productItem.getUnitType().getNameCyrl());
        }
        if (request.isForPrintQrCode()) {
            if (productItem.getPartition() != null) {
                if (productItem.getPartition().getContractItem() != null) {
                    _ContractItem contractItem = productItem.getPartition().getContractItem();
                    map.put("contractItemNumb", "" + contractItem.getNumb());
                    map.put("itemGuessReceiveDate", contractItem.getItemGuessReceiveDate() != null ? "" + contractItem.getItemGuessReceiveDate() : null);
                    if (!ServerUtils.isEmpty(contractItem.getAcceptedUser()))
                        map.put("initiator", contractItem.getAcceptedUser().getShortName());

                    map.put("contractItemAcceptedDate", contractItem.getAcceptedDate() != null ? "" + contractItem.getAcceptedDate() : null);

                    if (contractItem.getParent() != null) {
                        map.put("contractId", contractItem.getParent().getId().toString());
                        map.put("contractCode", contractItem.getParent().getCode());
                        if (!ServerUtils.isEmpty(contractItem.getParent().getSupplier()))
                            map.put("supplier", contractItem.getParent().getSupplier().getName());
                    }
                }
            }
        }

        if (productItem.getPartition() != null) {
            if (productItem.getPartition().getContractItem() != null) {
                if (productItem.getPartition().getContractItem().getParent() != null) {
                    if (productItem.getPartition().getContractItem().getParent().getProductResource() != null) {
                        map.add("productResourceLink", AttachmentUtils.getLink(productItem.getPartition().getContractItem().getParent().getProductResource().getName()));
                        map.add("productResourceName", productItem.getPartition().getContractItem().getParent().getProductResource().getOriginalName());
                    }
                }
            }
        }
        return map;
    }

    @ApiOperation(value = "SSV given products for Patient")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/given-patient", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse givenPatients(ProductFilterItemRequest request) {
        return ListResponse.of(productItemService.givenPatient(request), (productItem, map) -> wrapList(request, productItem, map));
    }

    @ApiOperation(value = "SSV given product for Patient")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse get(Long id) {
        return SingleResponse.of(productItemService.get(id), (productItem, map) -> wrapList(new ProductFilterItemRequest(), productItem, map));
    }

    @ApiOperation(value = "Printer")
    @Transactional
    @GetMapping(value = "/print", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse print(ProductFilterItemRequest request) {
        productItemService.print(request);
        return list(request);
    }

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/own-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse ownProducts(ProductFilterItemRequest request) {
        _User user = SessionUtils.getInstance().getUser();
        if (user != null) {
            if (user.getDepartment() != null && !user.getDepartment().getWarehouses().isEmpty()) {
                Long sessionUserId = user.getId();
                request.setWarehouseIds(user.getDepartment().getWarehouses().stream().map(_Warehouse::getId).collect(Collectors.toList()));
                request.setTakenAwayUserId(sessionUserId);
//                request.setState(_State.PRODUCT_PRODUCED);

                return ListResponse.of(productItemService.list(request), (productItem, map) -> {

                    if (!ServerUtils.isEmpty(productItem.getTakenAwayUser()))
                        map.put("takenAwayUser", productItem.getTakenAwayUser().getShortName());
                    if (!ServerUtils.isEmpty(productItem.getTakenAwayDate()))
                        map.put("takenAwayDate", "" + productItem.getTakenAwayDate());
                    if (productItem.getGivenUser() != null) {
                        map.add("givenUser", productItem.getGivenUser().getShortName());
                    }
                    if (productItem.getOrderItem() != null && productItem.getOrderItem().getParent() != null) {
                        map.add("orderNumb", "" + productItem.getOrderItem().getParent().getNumb());
                    }
                    if (productItem.getUnitType() != null) {
                        map.add("unit_type_name_en", productItem.getUnitType().getNameEn());
                        map.add("unit_type_name_ru", productItem.getUnitType().getNameRu());
                        map.add("unit_type_name_uz", productItem.getUnitType().getNameUz());
                        map.add("unit_type_name_cyrl", productItem.getUnitType().getNameCyrl());
                    }
                    if (_State.PRODUCT_USED.equals(productItem.getState())) {
                        map.add("comment", productItem.getComment());
                        map.add("usedFileLink", AttachmentUtils.getLink(productItem.getResourceForUsed().getName()));
                        map.add("usedFileName", productItem.getResourceForUsed().getOriginalName());
                        map.addDate("writeOffDate", productItem.getAuditInfo().getUpdatedDate());
                        map.add("writeOffByUser", productItem.getAuditInfo().getUpdatedByUser().getShortName());
                    }
                    if (!ServerUtils.isEmpty(productItem.getProduct())) {
                        map.put("productName", productItem.getProduct().getName());
//                        map.add("packageType", productItem.getProduct().getProductPackageType().name());
                        if (!ServerUtils.isEmpty(productItem.getProduct().getType()))
                            map.add("productTypeName", productItem.getProduct().getType().getName());
                        if (!ServerUtils.isEmpty(productItem.getProduct().getGroup()))
                            map.add("productCategoryName", productItem.getProduct().getGroup().getName());
                        if (productItem.getProduct().getAttr() != null)
                            map.put("productAttrs", productItem.getProduct().getAttr());
                    }
                    if (productItem.getPartition() != null) {
                        if (productItem.getPartition().getContractItem() != null) {
                            _ContractItem contractItem = productItem.getPartition().getContractItem();
                            if (!ServerUtils.isEmpty(contractItem.getAcceptedUser()))
                                map.put("initiator", contractItem.getAcceptedUser().getShortName());

                            if (contractItem.getParent() != null) {
                                map.put("contractId", contractItem.getParent().getId().toString());
                                map.put("contractCode", contractItem.getParent().getCode());
                                if (!ServerUtils.isEmpty(contractItem.getParent().getSupplier()))
                                    map.put("supplier", contractItem.getParent().getSupplier().getName());
                            }
                        }
                    }
                    return map;
                });
            }
        }
        return null;
    }

    @ApiOperation(value = "QrCode mobile")
    @Transactional
    @GetMapping(value = "/qr-code/mobile/{productItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(@PathVariable Long productItemId, HttpServletResponse response) {
        return productItemService.generateQrCode(productItemId, response, true);
    }

    @ApiOperation(value = "QrCode mobile")
    @Transactional
    @GetMapping(value = "/detail-by-qr-code/{qrCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getDetails(@PathVariable Long qrCode) {
        return productItemService.getDetailsByQrCode(qrCode);
    }


    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/qr-code/detail/{productItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getQrDetails(@PathVariable Long productItemId) {
        return productItemService.getDetailsById(productItemId);
    }

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/qr-code/detail-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getQrDetailsList(ProductItemQrCodeList request) {
        return productItemService.getDetailsQrCodeList(request);
    }

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/qr-code/full-info/{qrCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getProductItemInfo(@PathVariable Long qrCode) {
        return productItemService.getQrCodeFullInfo(qrCode);
    }

    @ApiOperation(value = "List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/qr-code/detail/taken-away", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getQrDetails(TakenProductRequest request) {
        return productItemService.getDetailsByIds(request);
    }

    @ApiOperation(value = "shaxmat doskaning cell idagi productlarni chiqarish")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-by-carriage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getByCarriage(ProductItemByCarriageFilterRequest request) {
        return ListResponse.of(productItemService.getByCarriage(request), (productItem, map) -> {
            if (productItem.getProduct() != null) {
                map.add("productName", productItem.getProduct().getName());
                if (productItem.getProduct().getMsds() != null)
                    map.add("msdsFilePath", productItem.getProduct().getMsds().getName());
            }
            _Partition partition = productItem.getPartition();
            if (partition != null && partition.getDate() != null) {
                map.addString("partitionDate", partition.getDate().format(ServerUtils.dateFormat));
                if (partition.getContractItem() != null && partition.getContractItem().getParent() != null) {
                    map.add("contractNumber", partition.getContractItem().getParent().getCode());
                }
            }
            if (productItem.getPlacedBy() != null)
                map.add("placedByName", productItem.getPlacedBy().getShortName());
            if (productItem.getUnitType() != null) {
                map.put("unitTypeId", productItem.getUnitType().getId().toString());
                map.put("unit_type_name_en", productItem.getUnitType().getNameEn());
                map.put("unit_type_name_ru", productItem.getUnitType().getNameRu());
                map.put("unit_type_name_uz", productItem.getUnitType().getNameUz());
                map.put("unit_type_name_cyrl", productItem.getUnitType().getNameCyrl());
            }
            if (productItem.getLot() != null && !ServerUtils.isEmpty(productItem.getLot().getName()))
                map.add("lotName", productItem.getLot().getName());
            inventarizationDetails(productItem, map);
            return map;
        });
    }

    private void inventarizationDetails(_ProductItem productItem, CoreMap map) {
        _InventarizationLog inventarizationLog = productItem.getInventarizationLog();
        if (inventarizationLog != null) {
            map.addBool("valid", inventarizationLog.isValid());
            map.addDate("inventarizationDate", productItem.getInventarizationDate());
//            _Inventarization inventarization = inventarizationLog.getInventarization();
//            if (inventarization != null)
//                map.addDate("inventarizationDate", inventarization.getEndedAt());
//            if (!map.has("inventarizationDate")) {
//                map.addDate("inventarizationDate", inventarization.getAuditInfo().getCreationDate());
//            }
        }
    }

    @ApiOperation(value = "shaxmat doskaning cell idagi productlarni chiqarish")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-by-cell", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getByCarriage(ProductItemByCellFilterRequest request) {
        return ListResponse.of(productItemService.getByCell(request), (productItem, map) -> {
            if (productItem.getProduct() != null) {
                map.add("productName", productItem.getProduct().getName());
                if (productItem.getProduct().getMsds() != null)
                    map.add("msdsFilePath", productItem.getProduct().getMsds().getName());
            }
            _Partition partition = productItem.getPartition();
            if (partition != null && partition.getDate() != null) {
                map.addString("partitionDate", partition.getDate().format(ServerUtils.dateFormat));
                if (partition.getContractItem() != null && partition.getContractItem().getParent() != null) {
                    map.add("contractNumber", partition.getContractItem().getParent().getCode());
                }
            }
            if (productItem.getPlacedBy() != null)
                map.add("placedByName", productItem.getPlacedBy().getShortName());
            inventarizationDetails(productItem, map);
            return map;
        });
    }

    @ApiOperation(value = "Get by kod ucheta")
    @Transactional
    @GetMapping(value = "/get-product-by-accounting-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(String accountingCode) {
        return SingleResponse.of(productItemService.getProductByAccountingCode(accountingCode), ProductController::wrapProduct);
    }

    @ApiOperation(value = "Kod ucheta")
    @GetMapping(value = "/accounting")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ListResponse accounting(ProductFilterItemRequest request) {
        request.setSortBy("idSort");
        return ListResponse.of(productItemService.list(request), (productItem, map) -> wrapList(request, productItem, map));
    }

    @ApiOperation(value = "Save kod ucheta")
    @Transactional
    @PostMapping(value = "/save-accounting-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse generateQrCode(@RequestBody SaveAccountingRequest request) {
        return SingleResponse.of(productItemService.saveAccounting(request), (productItem, map) -> map);
    }

    @ApiOperation(value = "Income")
    @Transactional
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).WAREHOUSE_INCOME)")
    @PostMapping(value = "/income", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse income(@RequestBody ProductItemIncomeReq request) {
        return SingleResponse.of(productItemService.income(request));
    }

    @ApiOperation(value = "Produce")
    @Transactional
    @PostMapping(value = "/produce", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse produce(@RequestBody ProduceRequest request) {
        return SingleResponse.of(productItemService.produce(request));
    }

    //fixme tekshirilmagan
    @ApiOperation(value = "Vozvrat")
    @Transactional
    @PostMapping(value = "/return", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).PRODUCT_CREATE)")
    public SingleResponse returnProduct() {
        return SingleResponse.of(productItemService.returnProduct());
    }

    @ApiOperation(value = "Income")
    @Transactional
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).WAREHOUSE_INCOME)")
    @PostMapping(value = "/income-used", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse incomeUsed(@RequestBody ProductItemIncomeReq request) {
        if (request == null)
            throw new ValidatorException("Малумотларни тшғри тўлдиринг");
        request.setUsed(true);
        return SingleResponse.of(productItemService.income(request));
    }

    @ApiOperation(value = "Product import from excel")
    @Transactional
    @PostMapping(value = "/import-excel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse excelImport(@RequestPart(value = "file") MultipartFile file) {
        return SingleResponse.of(productItemService.excelImport(file));
    }

    @ApiOperation(value = "Split product")
    @Transactional
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).WAREHOUSE_INCOME)")
    @PostMapping(value = "/split-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse splitProduct(@RequestBody SplitProductItemRequest request) {
        return SingleResponse.of(productItemService.split(request), (productItem, map) -> map);
    }

    @ApiOperation(value = "Department use product item")
    @Transactional
    @PostMapping(value = "/use-product-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse useProductItem(UseProductItemRequest request, @RequestPart(value = "file") MultipartFile file) {
        return SingleResponse.of(productItemService.useItem(request, file), (productItem, map) -> map);
    }
}