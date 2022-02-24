package uz.uzcard.genesis.service.impl;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemRequest;
import uz.uzcard.genesis.dto.api.req.partition.LotAddRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionCarriageAddressDto;
import uz.uzcard.genesis.dto.api.req.patient.ProduceRequest;
import uz.uzcard.genesis.dto.api.req.product.*;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.CarriageIsFullRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.PutToCarriageRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.api.resp.UnitTypeResponse;
import uz.uzcard.genesis.dto.product.ProductImportExcel;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductItemServiceImpl implements ProductItemService {
    public static final String BOOLEAN_TRUE = "1";
    public static final String LIST_SEPARATOR = ",";
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private PackageTypeDao packageTypeDao;
    @Autowired
    private PackageDao packageDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private ContractItemService contractItemService;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private LotService lotService;
    @Autowired
    private LotDao lotDao;
    @Autowired
    private UnitTypeDao unitTypeDao;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private WarehouseYDao warehouseYDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private AttributeDao attributeDao;
    @Autowired
    private ProductAttributeDao productAttributeDao;

    @Override
    public List<SelectItem> getPackageTypesByProduct(Long product_id) {
        LinkedList<Long> usedItems = productItemDao.getPackageTypesByProduct(product_id);

        List<SelectItem> items = new ArrayList<>();
        usedItems.forEach(categoryId -> {
            _PackageType packageType = packageTypeDao.get(categoryId);
            items.add(new SelectItem(packageType.getId(), packageType.getName(), "" + packageType.getId()));
        });
        packageTypeDao.search(new FilterParameters()).stream().forEach(packageType -> {
            if (usedItems.contains(packageType.getId()))
                return;
            items.add(new SelectItem(packageType.getId(), packageType.getName(), "" + packageType.getId()));
        });
        return items;
    }

    @Override
    public List<Map<String, String>> getPackageByProjectAndPackageType(Long product_id, Long packageType_id) {
        LinkedList<Long> usedItems = productItemDao.getPackageByProjectAndPackageType(product_id, packageType_id);

        List<Map<String, String>> items = new ArrayList<>();
        usedItems.forEach(categoryId -> {
            _Package aPackage = packageDao.get(categoryId);
            items.add(aPackage.getMap().getInstance());
        });
        packageDao.search(new FilterParameters()).stream().forEach(aPackage -> {
            if (usedItems.contains(aPackage.getId()))
                return;
            items.add(aPackage.getMap().getInstance());
        });
        return items;
    }

    @Override
    public PageStream<_ProductItem> list(ProductFilterItemRequest request) {

        return productItemDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            add("name", request.getName());
            if (!ServerUtils.isEmpty(request.getWarehouseIds()))
                addLongs("warehouseIds", request.getWarehouseIds());
            if (request.isForPrintQrCode())
                addString("isForPrintQrCode", "" + true);
            if (!ServerUtils.isEmpty(request.getState()))
                addString("state", request.getState());
            if (!ServerUtils.isEmpty(request.getContractId()))
                addString("contractId", "" + request.getContractId());
            if (!ServerUtils.isEmpty(request.getTakenAwayUserId()))
                addString("takenAwayUserId", "" + request.getTakenAwayUserId());
            if (!ServerUtils.isEmpty(request.getProductId()))
                addString("productId", "" + request.getProductId());
            if (!StringUtils.isEmpty(request.getOrderNumb()))
                addString("orderNumb", request.getOrderNumb());
            if (!StringUtils.isEmpty(request.getOrderItemNumb()))
                addString("orderItemNumb", request.getOrderItemNumb());
            if (!StringUtils.isEmpty(request.getContractNumb()))
                addString("contractNumb", request.getContractNumb());
            if (request.getFromDate() != null)
                addDate("fromDate", request.getFromDate());
            if (request.getToDate() != null)
                addDate("toDate", request.getToDate());
            addString("accountingCode", request.getAccountingCode());
            addBoolean("used", request.getUsed());
        }});
    }

    @Override
    public void checkProductCount(_Partition partition, Double count) {
        Double available = productItemDao.findCountByPartition(partition, false);
        if (available == null)
            throw new CriticException(GlobalizationExtentions.localication("CHECK_DATABASE"));
        if ((available < count)) {
            throw new CriticException(GlobalizationExtentions.localication("PRODUCT_IS_NOT_ENOUGH_IN_WAREHOUSE"));
        }
    }

    @Override
    public Boolean income(ProductItemIncomeReq request) {
        if (ServerUtils.isEmpty(request)) {
            throw new ValidatorException("REQUEST_IS_NULL");
        }
        if (!ServerUtils.isEmpty(request.getQrCode())) {
            _ProductItem productItem = productItemDao.getByQrCodeNewOnly(request.getQrCode());
            if (ServerUtils.isEmpty(productItem)) {
                throw new ValidatorException("PRODUCT_ITEM_IS_NULL");
            }
            productItem.setCount(productItem.getCount() + request.getCount());
            productItem.setUsed(request.isUsed());
            productItemDao.save(productItem);
            partitionService.reCalculate(productItem.getPartition());
        } else {
            if (contractDao.getUser().getDepartment() == null)
                throw new ValidatorException("Сизни қайси бўлимда ишлашингиз киритилмаган. Администраторга мурожаат қилинг");
            _Contract contract = contractDao.getDefaultByDepartment(contractDao.getUser().getDepartment());
            if (contract == null) {
                contract = new _Contract();
                contract.setDefaultYearly(Calendar.getInstance().get(Calendar.YEAR));
                contract.setCode(contract.getDefaultYearly() + " " + contractDao.getUser().getDepartment().getNameUz());
                contractDao.save(contract);
            }
            _ContractItem contractItem = contractItemService.saveOzl(ContractItemRequest.builder().
                    count(request.getCount()).parentId(contract.getId()).
                    productId(request.getProductId()).unitTypeId(request.getUnitTypeId()).build());
            contractItemService.accept(contractItem.getId());
            _Warehouse warehouse = carriageDao.get(request.getCarriages().get(0).getCarriageId()).getStillageColumn().getStillage().getWarehouse();
            Long lotId = lotService.add(LotAddRequest.builder().contractItemId(contractItem.getId())
                    .count(request.getCount()).packageCount(request.getPackageCount())
                    .date(new Date()).warehouseId(warehouse.getId()).build(), WarehouseReceivedType.PARTITION, request.isUsed()).getId();
            if (!ServerUtils.isEmpty(request.getCarriages())) {
                List<Long> productItemIds = productItemDao.findByLot(lotId).map(_ProductItem::getId).collect(Collectors.toList());
                List<Long> carriageIds = request.getCarriages().stream().map(ProductItemIncomeReq.CarriagesReq::getCarriageId).collect(Collectors.toList());
                request.getCarriages().forEach(carriagesReq -> {
                    carriageService.full(CarriageIsFullRequest.builder().carriageId(carriagesReq.getCarriageId()).isFull(carriagesReq.isFull()).build());
                });
                carriageService.put(PutToCarriageRequest.builder().carriageIds(carriageIds).productItemIds(productItemIds).build());
            }
        }

        return true;
    }

    @Override
    public Boolean save(ProductItemRequest request) {
        _Partition partition = partitionDao.get(request.getPartitionId());

        if (!ServerUtils.isEmpty(partition)) {
            if (request.getPackageCount() == null || request.getPackageCount() < 1)
                request.setPackageCount(1);
            Map<String, String> data = new HashMap<>();
            if (partition.getContractItem() != null && partition.getContractItem().getUnitType() != null)
                data.put("unitTypeId", "" + partition.getContractItem().getUnitType().getId());
            data.put("partitionId", "" + partition.getId());
            data.put("lotId", "" + request.getLotId());
            data.put("count", "" + (request.getCount() / request.getPackageCount()));
            data.put("allCount", "" + residue(Long.parseLong(request.getCount().toString().split("\\.")[0]), request.getPackageCount()));
            data.put("packageCount", "" + request.getPackageCount());
            data.put("used", "" + request.isUsed());
            productItemDao.call(data, "createproductitem", Types.BOOLEAN);
        } else
            throw new ValidatorException(GlobalizationExtentions.localication("PARTITION_NOT_FOUND"));
        partitionService.reCalculate(partition);
        List<Long> productItems = productItemDao.findIdsByPartition(partition);
        productItemDao.findAllByIds(productItems).forEach(productItem -> productItemDao.save(productItem));
        productItemDao.reindex(productItems);
        return true;
    }

    @Override
    public SingleResponse generateQrCode(Long qrCode, HttpServletResponse response, boolean forOnlyGetQr) {
        _ProductItem productItem = productItemDao.getByQrCode(qrCode);
        Map<String, String> map = new HashMap<>();
        if (ServerUtils.isEmpty(productItem)) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        }
        map.put("id", productItem.getId().toString());
        String qr = ServerUtils.gson.toJson(map);
        if (!forOnlyGetQr) {
            productItem.setQrPrinted(true);
            productItemDao.save(productItem);
        }
        if (ServerUtils.isEmpty(response)) {
            throw new ValidatorException(GlobalizationExtentions.localication("RESPONSE_IS_NULL))"));
        }
        generateQrCode(response, qr);
        return SingleResponse.of(true);
    }

    @Override
    public SingleResponse getDetailsById(Long productItemId) {
        _ProductItem productItem = productItemDao.getByQrCode(productItemId);
        if (ServerUtils.isEmpty(productItem)) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        }
        return wrapForDetails(productItem);
    }

    @Override
    public SingleResponse getDetailsByQrCode(Long qrCode) {
        _ProductItem productItem = productItemDao.getByQrCode(qrCode);
        if (ServerUtils.isEmpty(productItem)) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        }
        return wrapForDetails(productItem);
    }

    private SingleResponse wrapForDetails(_ProductItem productItem) {
        return SingleResponse.of(productItem, (item, map) -> {
            map = new CoreMap();
            if (!ServerUtils.isEmpty(item.getProduct().getGroup()))
                map.add("productGroupName", "" + item.getProduct().getGroup().getName());
            if (!ServerUtils.isEmpty(item.getProduct().getType()))
                map.add("productTypeName", "" + item.getProduct().getType().getName());
            if (!ServerUtils.isEmpty(item.getPartition()))
                map.add("numb", "" + item.getPartition().getContractItem().getParent().getCode());
            map.add("productName", item.getName());
            map.add("id", item.getId());
            map.addDouble("count", item.getCount());
            List<UnitTypeResponse> items = new ArrayList<>();
            productItem.getProduct().getUnitTypes().forEach(unitType -> {
                UnitTypeResponse unitTypeResponse = new UnitTypeResponse(unitType.getId(), unitType.getNameEn(), unitType.getNameUz(), unitType.getNameRu(), unitType.getNameCyrl());
                items.add(unitTypeResponse);
            });
            map.addStrings("unitTypes", items);
            return map;
        });
    }

    @Override
    public SingleResponse getQrCodeFullInfo(Long qrCode) {
        _ProductItem productItem = productItemDao.getByQrCode(qrCode);
        if (ServerUtils.isEmpty(productItem)) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        }
        return SingleResponse.of(productItem, (item, map) -> {
            if (item.getProduct() != null) {
                if (item.getProduct().getMsds() != null)
                    map.add("msdsFileName", "" + item.getProduct().getMsds().getName());
                if (!ServerUtils.isEmpty(item.getProduct().getGroup()))
                    map.add("productGroupName", "" + item.getProduct().getGroup().getName());
                if (!ServerUtils.isEmpty(item.getProduct().getType()))
                    map.add("productTypeName", "" + item.getProduct().getType().getName());
                if (item.getProduct().getAttr() != null) {
                    map.addStrings("attrs", item.getProduct().getAttr());
                }
            }
            if (productItem.getPlacedBy() != null)
                map.add("placedBy", productItem.getPlacedBy().getShortName());
            if (!ServerUtils.isEmpty(item.getPartition()))
                map.add("contractCode", "" + item.getPartition().getContractItem().getParent().getCode());
            if (productItem.getPartition() != null && productItem.getPartition().getExpiration() != null)
                map.addDate("expirationDate", productItem.getPartition().getExpiration());
            if (productItem.getPlacedDate() != null)
                map.addDate("placedDate", productItem.getPlacedDate());
            if (productItem.getOrderItem() != null && productItem.getOrderItem().getParent() != null) {
                map.addStrings("orderNumb", Arrays.asList("" + productItem.getOrderItem().getParent().getNumb()));
            } else {
                if (productItem.getPartition() == null)
                    throw new ValidatorException("PARTITION_NOT_FOUND");
                if (productItem.getPartition().getContractItem() == null)
                    throw new RpcException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
                List<_OrderItem> byContractItem = orderItemDao.findByContractItem(productItem.getPartition().getContractItem());
                if (byContractItem != null) {
                    List<String> orderNumbs = new ArrayList<>();
                    for (_OrderItem orderItem : byContractItem) {
                        if (orderItem.getParent() != null)
                            orderNumbs.add("" + orderItem.getParent().getNumb());
                    }
                    map.addStrings("orderNumb", orderNumbs);
                }
            }
            map.add("productName", item.getName());
            map.add("id", item.getId());
            map.addDouble("count", item.getCount());
            if (productItem.getLot() != null && !ServerUtils.isEmpty(productItem.getLot().getName()))
                map.add("lotName", productItem.getLot().getName());
            if (productItem.getLot() != null)
                map.addDate("deliveredDate", productItem.getAuditInfo().getCreationDate());
            if (productItem.getUnitType() != null) {
                map.add("unitTypeId", productItem.getUnitType().getId());
                map.add("unit_type_name_en", productItem.getUnitType().getNameEn());
                map.add("unit_type_name_ru", productItem.getUnitType().getNameRu());
                map.add("unit_type_name_uz", productItem.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", productItem.getUnitType().getNameCyrl());
            }
            if (productItem.getOrderItem() != null && productItem.getOrderItem().getParent() != null)
                map.add("orderNumb", "" + productItem.getOrderItem().getParent().getNumb());
            putPosition(productItem, map);
            return map;
        });
    }

    @Override
    public void putPosition(_ProductItem productItem, CoreMap map) {
        if (productItem.getWarehouse() != null)
            map.add("warehouseName", productItem.getWarehouse().getNameByLanguage());
        if (!ServerUtils.isEmpty(productItem.getCarriages_id())) {
            Stream<_Carriage> carriages = carriageDao.findByIds(productItem.getCarriages_id());
            List<String> carriagesAddresses = carriages.map(carriage -> (carriage.getStillageColumn().getCode() + "" + carriage.getSortOrder())).collect(Collectors.toList());
            map.addStrings("carriageAddresses", carriagesAddresses);
            if (!ServerUtils.isEmpty(productItem.getCells_id())) {
                List<String> stillageAddress = warehouseYDao.findByIds(productItem.getCells_id()).map(warehouseY -> {
                    if (warehouseY.getColumn() != null)
                        return warehouseY.getColumn().getColumn() + warehouseY.getRow();
                    else return null;
                }).collect(Collectors.toList());
                if (!ServerUtils.isEmpty(stillageAddress))
                    map.addStrings("stillageAddress", stillageAddress);
            }
        }
    }

    @Override
    public PageStream<_ProductItem> getByCarriage(ProductItemByCarriageFilterRequest request) {
        return productItemDao.search(new FilterParameters() {{
            add("name", request.getName());
            add("carriageId", "" + request.getCarriageId());
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
        }});
    }

    @Override
    public void print(ProductFilterItemRequest request) {
        list(request).stream().forEach(productItem -> {
            productItem.setQrPrinted(true);
            productItemDao.save(productItem);
        });
    }

    @Override
    public _ProductItem getByAccountingCode(String accountingCode) {
        return productItemDao.getByAccountingCode(accountingCode);
    }

    @Override
    public _Product getProductByAccountingCode(String accountingCode) {
        _ProductItem productItem = getByAccountingCode(accountingCode);
        if (productItem == null)
            return null;
        return productItem.getProduct();
    }

    @Override
    public _ProductItem saveAccounting(SaveAccountingRequest request) {
        _ProductItem productItem = productItemDao.get(request.getId());
        if (productItem == null)
            throw new ValidatorException("Продукт танланмаган");
        productItem.setAccountingCode(request.getAccountingCode());
        if (request.getPrice() != null)
            productItem.setPrice(request.getPrice());
        productItemDao.save(productItem);
        return productItem;
    }

    @Override
    public ListResponse getDetailsQrCodeList(ProductItemQrCodeList request) {

        List<_ProductItem> productItems = productItemDao.findByQrCodes(request.getProductItemIds()).collect(Collectors.toList());
        if (productItems == null || productItems.isEmpty()) {
            productItems = productItemDao.findByLot(request.getLotId()).collect(Collectors.toList());
        }
        return ListResponse.of(new PageStream<_ProductItem>(productItems.stream(), productItems.size()), (productItem, map) -> {
            map = new CoreMap();
            if (!ServerUtils.isEmpty(productItem.getProduct().getGroup()))
                map.add("productGroupName", "" + productItem.getProduct().getGroup().getName());
            if (!ServerUtils.isEmpty(productItem.getProduct().getType()))
                map.add("productTypeName", "" + productItem.getProduct().getType().getName());
            if (!ServerUtils.isEmpty(productItem.getPartition())) {
                if (productItem.getPartition().getContractItem() != null && productItem.getPartition().getContractItem().getParent() != null)
                    map.add("numb", "" + productItem.getPartition().getContractItem().getParent().getCode());
            }
            map.add("productName", productItem.getName());
            map.add("id", productItem.getId());
            map.addDouble("count", productItem.getCount());
            return map;
        });
    }

    public void generateQrCode(HttpServletResponse response, String qrCode) {
        OutputStream outputStream = null;
        try {
            response.setContentType("image/png");
            outputStream = response.getOutputStream();
            outputStream.write(ServerUtils.getQRCodeImage(qrCode, 200, 200));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ListResponse getDetailsByIds(TakenProductRequest request) {
        List<_ProductItem> productItems = productItemDao.findByQrCodes(request.getProductItemIds()).collect(Collectors.toList());

        final AtomicDouble requestCount = new AtomicDouble(0);
        if (productItems != null && !productItems.isEmpty()) {
            _Partition partition = productItems.get(0).getPartition();
            _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
            _GivenProducts givenProduct = givenProductsDao.getNewByPartitionAndOrderItem(partition, orderItem);
            if (givenProduct != null)
                requestCount.set(givenProduct.getRemains());
        }

        return ListResponse.of(new PageStream<_ProductItem>(productItems.stream(), productItems.size()), (productItem1, map) -> {
            CoreMap data = wrapDetailQrCodeForTakingAway(request, productItem1);
            if (requestCount.get() > 0) {
                data.addDouble("requestedCount", requestCount.get());
            }
            return data;
        });
    }

    @Override
    public PageStream<_ProductItem> givenPatient(ProductFilterItemRequest request) {
        _User user = SessionUtils.getInstance().getUser();
        if (user == null)
            throw new RpcException("USER_NOT_FOUND");
        _Department department = user.getDepartment();
        if (department == null)
            throw new RpcException("DEPARTMENT_NOT_FOUND");
        Set<_Warehouse> warehouses = department.getWarehouses();
        if (warehouses.isEmpty() || warehouses == null)
            throw new RpcException("WAREHOUSE_NOT_FOUND");

        return productItemDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }

            if (request.getName() != null)
                add("name", request.getName().trim());
            addLongs("warehouseIds", warehouses.stream().map(_Warehouse::getId).collect(Collectors.toList()));
            if (request.isForPrintQrCode())
                addString("isForPrintQrCode", "" + true);
            if (!ServerUtils.isEmpty(request.getState()))
                addString("state", request.getState());
            if (!ServerUtils.isEmpty(request.getContractId()))
                addString("contractId", "" + request.getContractId());
            if (!ServerUtils.isEmpty(request.getTakenAwayUserId()))
                addString("takenAwayUserId", "" + request.getTakenAwayUserId());
            if (!ServerUtils.isEmpty(request.getProductId()))
                addString("productId", "" + request.getProductId());
            if (!StringUtils.isEmpty(request.getOrderNumb()))
                addString("orderNumb", request.getOrderNumb());
            if (!StringUtils.isEmpty(request.getOrderItemNumb()))
                addString("orderItemNumb", request.getOrderItemNumb());
            if (!StringUtils.isEmpty(request.getContractNumb()))
                addString("contractNumb", request.getContractNumb());
            addString("accountingCode", request.getAccountingCode());
        }});
    }

    @Override
    public _ProductItem get(Long id) {
        if (id == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_REQUIRED"));
        _ProductItem productItem = productItemDao.get(id);
        if (productItem == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        return productItem;
    }

    @Override
    public PageStream<_ProductItem> getByCell(ProductItemByCellFilterRequest request) {
        return productItemDao.search(new FilterParameters() {{
            add("cellId", "" + request.getCellId());
            add("name", request.getName());
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
        }});
    }

    private CoreMap wrapDetailQrCodeForTakingAway(TakenProductRequest request, _ProductItem productItem1) {
        CoreMap map = new CoreMap();
        map.add("count", "" + productItem1.getCount());
        if (!ServerUtils.isEmpty(productItem1.getProduct().getGroup()))
            map.add("productGroupName", "" + productItem1.getProduct().getGroup().getName());
        if (!ServerUtils.isEmpty(productItem1.getProduct().getType()))
            map.add("productTypeName", "" + productItem1.getProduct().getType().getName());
        if (!ServerUtils.isEmpty(productItem1.getPartition()))
            map.add("numb", "" + productItem1.getPartition().getContractItem().getParent().getCode());
        map.add("productName", productItem1.getName());
        map.add("id", productItem1.getId());
        return map;
    }

    @Override
    public Map<String, Long> excelImport(MultipartFile file) {
        Long productSave = 0L;
//        Long productItemSave = 0L;
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        Map<String, _UnitType> unitTypes = new HashMap<>();
        unitTypeDao.list().forEach(unitType -> {
            unitTypes.put(unitType.getNameEn(), unitType);
            unitTypes.put(unitType.getNameEn().toLowerCase(), unitType);
            unitTypes.put(unitType.getNameEn().toUpperCase(), unitType);
            unitTypes.put(unitType.getNameCyrl(), unitType);
            unitTypes.put(unitType.getNameCyrl().toLowerCase(), unitType);
            unitTypes.put(unitType.getNameCyrl().toUpperCase(), unitType);
            unitTypes.put(unitType.getNameRu(), unitType);
            unitTypes.put(unitType.getNameRu().toLowerCase(), unitType);
            unitTypes.put(unitType.getNameRu().toUpperCase(), unitType);
            unitTypes.put(unitType.getNameUz(), unitType);
            unitTypes.put(unitType.getNameUz().toLowerCase(), unitType);
            unitTypes.put(unitType.getNameUz().toUpperCase(), unitType);
        });

        for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j++) {

//            boolean isFull = Boolean.FALSE;
            Row row = sheet.getRow(j);
            if (row == null)
                continue;
            ProductImportExcel result = new ProductImportExcel();
            Field[] fields = result.getClass().getDeclaredFields();

//            if (row.getPhysicalNumberOfCells() > 7)
//                isFull = Boolean.TRUE;
            for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {

                Cell cell = row.getCell(k, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
//                if (Boolean.FALSE == isFull && k == 4)
//                    break;
                if (cell != null) {

                    Field field = fields[k];
                    field.setAccessible(true);
                    try {
                        if (field != null) {
                            Object value = null;
                            if (field.getType().equals(Long.class)) {
                                value = Long.valueOf(formatter.formatCellValue(cell));
                            } else if (field.getType().equals(String.class)) {
                                value = formatter.formatCellValue(cell);
                            } else if (field.getType().equals(Integer.class)) {
                                value = Integer.valueOf(formatter.formatCellValue(cell));
                            } else if (field.getType().equals(Double.class)) {
                                value = Double.valueOf(formatter.formatCellValue(cell));
                            } else if (field.getType().equals(LocalDate.class)) {
                                value = cell.getLocalDateTimeCellValue().toLocalDate();
                            } else if (field.getType().equals(LocalDateTime.class)) {
                                value = cell.getLocalDateTimeCellValue();
                            } else if (field.getType().equals(Boolean.class)) {
                                value = BOOLEAN_TRUE.equals(formatter.formatCellValue(cell));
                            } else if (field.getType().equals(BigDecimal.class)) {
                                value = new BigDecimal(formatter.formatCellValue(cell));
                            } else if (field.getType().equals(List.class)) {
                                value = Arrays.asList(formatter.formatCellValue(cell).trim().split("\\s*" + LIST_SEPARATOR + "\\s*"));
                            }
                            field.set(result, value);
                        }
                    } catch (Exception e) {
                        throw new RpcException(String.format("%s саҳифа %s устун %s сатрда %s қиймати формати мос тушмади",
                                sheet.getSheetName(), sheet.getRow(0).getCell(k).getStringCellValue(), j, cell.toString()));
                    }
                }
            }
            if (StringUtils.isEmpty(result.getName()) || StringUtils.isEmpty(result.getName().trim()))
                continue;

            if (unitTypes.keySet().contains(result.getUnittype())) {
                _UnitType unitType = unitTypes.get(result.getUnittype());

                _Product product = new _Product();
                product.setName(result.getName());
                product.setUnitTypes(Arrays.asList(unitType));
                product = productDao.save(product);

                _Attribute attribute = new _Attribute();
                attribute.setByDefault(true);
                attribute.setName(product.getName());
                attribute.getItems().add(result.getAttribute());
                attributeDao.save(attribute);

                _ProductAttribute productAttribute = new _ProductAttribute();
                productAttribute.setProduct(product);
                productAttribute.setAttribute(attribute);
                productAttribute.getItems().add(result.getAttribute());
                productAttributeDao.save(productAttribute);

                product.getAttributes().add(productAttribute);
                product = productDao.save(product);
                productSave++;

//                if (isFull) {
//                    _ProductItem productItem = new _ProductItem();
//                    productItem.setProduct(product);
//                    productItem.setName(result.getName());
//                    productItem.setUnitType(unitType);
//                    productItem.setCount(result.getCount());
//                    productItem.setPrice(result.getPrice());
//                    productItem.setAccountingCode(result.getAccountingCode());
//                    productItem.setQrcode(result.getQrCode());
//                    productItemDao.save(productItem);
//                    productItemSave++;
//                }

            } else {
                throw new ValidatorException(String.format("Бирлик тури тўғри келмади. {%s}", j));
            }
        }

        Map<String, Long> returns = new HashMap<>();
        returns.put("product", productSave);
//        returns.put("productWithItem", productItemSave);
        return returns;
    }

    @Override
    public Boolean produce(ProduceRequest request) {
        _Department department = departmentDao.get(request.getDepartmentId());
        if (department == null)
            throw new ValidatorException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
        if (ServerUtils.isEmpty(request.getProduceMedicine()))
            throw new ValidatorException("MEDICINES_EMPTY");
        request.getProduceMedicine().forEach(produceMedicineReq -> {
            _ProductItem producedProduct = productItemDao.get(produceMedicineReq.getMedicineId());
            if (producedProduct == null)
                throw new ValidatorException("PRODUCT_ITEM_NOT_FOUND");
//            if (producedProduct.getState().equals(_State.PRODUCT_PRODUCED))
//                throw new ValidatorException("Dori berib yuborilgan");
            if (producedProduct.getCount() - produceMedicineReq.getCount() < 0)
                throw new ValidatorException(String.format("%s bo'yicha so'ralgan miqdor mavjud bo'lganidan ko'p, mavjud miqdor: %s", producedProduct.getName(), producedProduct.getCount()));
            producedProduct.setCount(producedProduct.getCount() - produceMedicineReq.getCount());
            if (department.getWarehouses().isEmpty())
                throw new ValidatorException("Bu departmentda sklad mavjud emas");
            if (producedProduct.getCount() != 0.0) {
                _ProductItem remainedProduct = new _ProductItem();
                BeanUtils.copyProperties(producedProduct, remainedProduct, "id", "map", "carriages_id", "cells_id");
                remainedProduct.setCells_id(new ArrayList<>());
                remainedProduct.setCarriages_id(new ArrayList<>());
                remainedProduct.setCount(producedProduct.getCount());
                remainedProduct.getCarriages_id().addAll(producedProduct.getCarriages_id());
                remainedProduct.getCells_id().addAll(producedProduct.getCells_id());
                productItemDao.save(remainedProduct);
                producedProduct.setCount(produceMedicineReq.getCount());
                productItemDao.save(remainedProduct);
            }
            producedProduct.setGivenUser(SessionUtils.getInstance().getUser());
            producedProduct.setTakenAwayDate(new Date());
            producedProduct.setWarehouse(department.getWarehouses().stream().findFirst().orElse(null));
            producedProduct.setState(_State.PRODUCT_PRODUCED);
            productItemDao.save(producedProduct);

        });
        return true;
    }

    @Override
    public Boolean returnProduct() {
        _User user = SessionUtils.getInstance().getUser();
        if (user.getDepartment() != null && !user.getDepartment().getWarehouses().isEmpty()) {

            productItemDao.getForReturning(user.getDepartment().getWarehouses()).forEach(productItem -> {
                if (productItem.getGivenUser() != null
                        && productItem.getGivenUser().getDepartment() != null
                        && !productItem.getGivenUser().getDepartment().getWarehouses().isEmpty()) {
                    productItem.setWarehouse(productItem.getGivenUser().getDepartment().getWarehouses().stream().findFirst().orElse(null));
                }
                productItemDao.save(productItem);
            });
        }
        return true;
    }

    @Override
    public List<String> getProductItemAddress(Long partitionId) {
        _Partition partition = partitionDao.get(partitionId);
        if (partition == null)
            throw new RpcException(GlobalizationExtentions.localication("PARTITION_NOT_FOUND"));
        List<_ProductItem> productItems = productItemDao.search(new FilterParameters() {{
//            add("productId", "" + productId);
            add("partitionId", "" + partition.getId());
        }}).stream().collect(Collectors.toList());

        if (productItems == null)
            return null;
        List<String> addressGson = new ArrayList<>();
        for (_ProductItem productItem : productItems) {
            List<Long> carriages = productItem.getCarriages_id();
            carriages.forEach(aLong -> {
                PartitionCarriageAddressDto dto = getAddress(carriageDao.get(aLong));
                addressGson.add(ServerUtils.gson.toJson(dto));
            });
        }
        return addressGson;
    }

    @Override
    public boolean deleteByWarehouse(Long warehouseId, int offset, int limit) {
        AtomicBoolean has = new AtomicBoolean(false);
        productItemDao.list(new FilterParameters() {{
            addLong("warehouseId", warehouseId);
            setStart(offset);
            setSize(limit);
        }}).forEach(productItem -> {
            has.set(true);
            productItemDao.delete(productItem);
        });
        return has.get();
    }

    @Override
    public boolean hasAnyByWarehouse(Long warehouseId) {
        return productItemDao.hasAnyByWarehouse(warehouseId);
    }

    @Override
    public _ProductItem split(SplitProductItemRequest request) {
        _ProductItem fromProduct = productItemDao.getByQrCodeNewOnly(request.getQrCode());
        if (fromProduct == null)
            throw new ValidatorException("Маҳсулот топилмади");
        if (request.isNew()) {
            _ProductItem newProductItem = new _ProductItem();
            BeanUtils.copyProperties(fromProduct, newProductItem, "id", "map", "carriages_id", "cells_id", "qrcode");
            newProductItem.setCells_id(fromProduct.getCells_id());
            newProductItem.setCarriages_id(fromProduct.getCarriages_id());
            newProductItem.setCount(request.getCount());
            fromProduct.setCount(fromProduct.getCount() - request.getCount());
            if (fromProduct.getCount() <= 0)
                throw new ValidatorException("Маҳсулот етарли эмас");
            productItemDao.save(newProductItem);
            productItemDao.save(fromProduct);
            return newProductItem;
        } else {
            _ProductItem toProductItem = productItemDao.getByQrCodeNewOnly(request.getQrCode2());
            if (toProductItem == null)
                throw new ValidatorException("Иккинчи продукт топилмади");
            if (!fromProduct.getLot().equals(toProductItem.getLot()))
                throw new ValidatorException("Битта партияда келмаган продуктларни қўшиб бўлмайди");
            fromProduct.setCount(fromProduct.getCount() - request.getCount());
            if (fromProduct.getCount() <= 0)
                throw new ValidatorException("Маҳсулот етарли эмас");
            toProductItem.setCount(toProductItem.getCount() + request.getCount());
            productItemDao.save(fromProduct);
            productItemDao.save(toProductItem);
            return toProductItem;
        }
    }

    @Override
    public _ProductItem useItem(UseProductItemRequest request, MultipartFile file) {
        if (request.getProductItemId() == null) {
            throw new ValidatorException("PRODUCT_ITEM_REQUIRED");
        }
        _ProductItem productItem = productItemDao.get(request.getProductItemId());
        if (productItem == null) {
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));
        }
        if (request.getCount() == null || request.getCount() == 0) {
            throw new ValidatorException("COUNT_REQUIRED");
        }
        if (request.getCount() > productItem.getCount()) {
            throw new RpcException(String.format(GlobalizationExtentions.localication("PRODUCT_COUNT_NOT_DOT_MATCH"), request.getCount()));
        }
        _AttachmentView attachment = attachmentService.uploadPdf(file);
        if (request.getCount() == productItem.getCount()) {
            productItem.setComment(request.getComment());
            productItem.setResourceForUsed(attachment);
            productItem.setState(_State.PRODUCT_USED);
            productItemDao.save(productItem);
        } else {
            _ProductItem newProductItem = new _ProductItem();
            BeanUtils.copyProperties(productItem, newProductItem, "id", "map", "carriages_id", "cells_id", "placementType");
            productItem.setCount(productItem.getCount() - request.getCount());
            newProductItem.setCount(request.getCount());
            newProductItem.setState(_State.PRODUCT_USED);
            newProductItem.setComment(request.getComment());
            newProductItem.setResourceForUsed(attachment);
            productItemDao.save(newProductItem);
            productItemDao.save(productItem);
        }
        return productItem;
    }

    private PartitionCarriageAddressDto getAddress(_Carriage carriage) {
        PartitionCarriageAddressDto addressDto = new PartitionCarriageAddressDto();
        _StillageColumn column = carriage.getStillageColumn();
        if (!ServerUtils.isEmpty(column)) {
            _Stillage stillage = column.getStillage();
            if (!ServerUtils.isEmpty(stillage)) {
                addressDto.setCarriage(carriage.getId());
                addressDto.setCarriagePosition(carriage.getSortOrder());
                addressDto.setStillageColumn(column.getId());
                addressDto.setStillageColumnName(column.getCode());
                addressDto.setStillage(stillage.getId());
                addressDto.setStillageName(stillage.getName());

                String builder = stillage.getName() + " " +
                        column.getCode() +
                        carriage.getSortOrder();
                addressDto.setAddresse(builder);
                return addressDto;
            }
        }
        return null;
    }

    private List<Long> residue(Long residue, Integer all) {
        List<Long> residues = new ArrayList<>();

        if (residue % all != 0) {
            Long butun = Math.floorDiv(residue, all);
            Long qoldiq = Long.valueOf(String.valueOf(residue % all));

            List<Long> qoldiqlar = residueInside(qoldiq, all);

            for (int i = 1; i <= all; i++) {
                residues.add(butun + qoldiqlar.get(i - 1));
            }
        } else {
            for (int i = 1; i <= all; i++) {
                residues.add(residue / all);
            }
        }
        return residues;
    }

    private List<Long> residueInside(Long residue, Integer all) {
        List<Long> residues = new ArrayList<>();
        Long butun = Math.floorDiv(residue, all);
        Long qoldiq1 = Long.valueOf(String.valueOf(residue % all));
        if (butun == 0) {
            for (int i = 1; i <= all; i++) {
                if (qoldiq1 > 0) {
                    residues.add(butun + 1);
                    qoldiq1 = qoldiq1 - 1;
                } else {
                    residues.add(butun);
                }
            }
        } else {
            for (int i = 1; i <= all; i++) {
                if (i == all)
                    residues.add(butun + qoldiq1);
                else
                    residues.add(butun);
            }
        }
        return residues;
    }
}