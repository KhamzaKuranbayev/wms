package uz.uzcard.genesis.service.impl;

import org.hibernate.search.FullTextSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.partition.PartitionFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionProductRequest;
import uz.uzcard.genesis.dto.api.req.partition.PartitionRequest;
import uz.uzcard.genesis.dto.api.req.product.ProductItemRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PartitionServiceImpl implements PartitionService {
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GivenProductsService givenProductsService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private LotService lotService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private ProduceHistoryService produceHistoryService;
    @Autowired
    private ProduceHistoryDao produceHistoryDao;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OrderDao orderDao;

    @Override
    public PageStream<_Partition> list(PartitionFilterRequest request) {
        return partitionDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getProductId()))
                addString("productId", "" + request.getProductId());
            if (!ServerUtils.isEmpty(request.getWarehouseNameSearch()))
                addString("warehouseNameSearch", "" + request.getWarehouseNameSearch());
            add("warehouseId", request.getWarehouseId());
        }});
    }

    @Override
    public _Partition save(PartitionRequest request, Long lotId, boolean used) {
        if (request.getCount() <= 0 || request.getPackageCount() <= 0)
            throw new ValidatorException("0 ёки ундан кичик қийматда қабул қилиш мумкин эмас");
        LocalDate date = request.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        _Partition partition = get(request.getContractItemId(), request.getWarehouseId(), date);
        if (partition == null)
            partition = new _Partition();
        if (ServerUtils.isEmpty(request.getContractItemId()))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_REQUIRED"));

        if (!ServerUtils.isEmpty(request.getContractItemId())) {
            _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
            if (ServerUtils.isEmpty(contractItem))
                throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
            partition.setContractItem(contractItem);
            partition.setProduct(contractItem.getProduct());
            if (!contractItem.getPartitions().contains(partition))
                contractItem.getPartitions().add(partition);
        }
        partition.setExpiration(request.getExpirationDate());
        _Warehouse warehouse = warehouseDao.get(request.getWarehouseId());
        if (warehouse == null)
            throw new ValidatorException("Омборхонани танланг");
        partition.setWarehouse(warehouse);
        partition.setCount(request.getCount());
        partition.setDate(date);
        partition.setRemains(request.getCount());
        Long partitionId = partitionDao.save(partition).getId();
        Long unitTypeId = partition.getContractItem().getUnitType() == null ? null
                : partition.getContractItem().getUnitType().getId();

        productItemService.save(ProductItemRequest.builder().partitionId(partitionId).
                count(request.getCount()).lotId(lotId).packageCount(request.getPackageCount()).
                unitTypeId(unitTypeId).used(used).build());
        return partition;
    }

    @Override
    public SingleResponse produce(PartitionProductRequest request) {
        _Partition partition = partitionDao.get(request.getPartitionId());
        if (partition == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PARTITION_NOT_FOUND"));
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));

        if (partition.getRemains() < request.getCount())
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_IS_NOT_ENOUGH"));

        // todo maxsulot qimmatroq bo'lsa 0.5 kg berishi ham mumkinmi.
        if (request.getCount() < 1)
            throw new ValidatorException("Камида 1 та беришингиз керак");
        if (orderItem.getUnitType() != null && orderItem.getUnitType().isCountable() && (request.getCount() != Math.floor(request.getCount())))
            throw new CriticException(GlobalizationExtentions.localication("ORDER_ITEM_COUNT_COUNTABLE"));
        if (request.getProduceHistoryId() != null) {
            _ProduceHistory produceHistory = produceHistoryDao.get(request.getProduceHistoryId());
            if (produceHistory == null)
                throw new ValidatorException("ProduceHistory is null");
            if (produceHistory.getRemain() < request.getCount())
                throw new ValidatorException("Berib yuborilayotgan mahsulot miqdori inisiator so'ragan miqdordan ko'p");
            produceHistoryService.makeStateDone(produceHistory, request.getCount(), request.getNotificationId());
        }
        productItemService.checkProductCount(partition, request.getCount());
        givenProductsService.save(orderItem, request.getCount(), partition);

        Double remainedOrderItemCount = orderItemsService.provide(orderItem, request.getCount());

        reCalculate(partition);
        if (partition.getProduct().getCount() < 0)
            throw new CriticException(GlobalizationExtentions.localication("PRODUCT_IS_NOT_ENOUGH"));
        notificationService.pushAboutProduced(orderItem, request.getCount());
        _ContractItem contractItem = orderItem.getContractItem();
        if (contractItem != null) {
            contractItemDao.save(contractItem);
            if (contractItem.getParent() != null)
                contractDao.save(contractItem.getParent());
        }
        if (orderItem.getParent() != null) {
            orderDao.save(orderItem.getParent());
        }

        return SingleResponse.of(partition, ((partition1, map) -> {
            map.put("remainedOrderItemCount", "" + remainedOrderItemCount);
            map.put("wareHouseName", partition1.getWarehouse().getNameByLanguage());
            map.put("wareHouseAdress", partition1.getWarehouse().getAddress());
            map.put("remains", "" + partition1.getRemains());
            if (partition1.getProduct().getGroup() != null)
                map.put("productCategory", partition1.getProduct().getGroup().getName());
            if (partition1.getProduct().getType() != null)
                map.put("productType", partition1.getProduct().getType().getName());
            map.add("productName", partition1.getProduct().getName());
            if (partition1.getContractItem() != null && partition1.getContractItem().getParent() != null)
                map.add("contractCode", partition1.getContractItem().getParent().getCode());
            return map;
        }));
    }

    @Override
    public void reCalculate(_Partition partition) {
        Double count = productItemDao.findCountByPartition(partition, false);
        partition.setRemains(count == null ? 0 : count - givenProductsDao.getRemainsPartition(partition));
        partition.setCount(productItemDao.findCountByPartition(partition, true));
        partitionDao.save(partition);
        partition.getLots().forEach(lot ->
                lotService.reCalculate(lot));
        productDao.save(partition.getProduct());
        if (partition.getWarehouse() != null) {
            warehouseDao.save(partition.getWarehouse());
        }
    }

    @Override
    public void reCalculateProducts() {
        accountService.reloadCache();
        partitionDao.call(new _Partition(), "recalculateall", Types.BOOLEAN);
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(partitionDao.getSession());
        try {
            fullTextSession.createIndexer(_Product.class, _Partition.class, _Lot.class, _ProductItem.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public _Partition get(Long contractItemId, Long warehouseId, LocalDate date) {
        return partitionDao.get(contractItemId, warehouseId, date);
    }

    @Override
    public List<String> getExpiringPartitions(Long id, Long depId) {
        if (id == null) return new ArrayList<>();
        return partitionDao.getExpiringPartitions(id, depId);
    }

    @Override
    public PageStream<_Partition> dashboard(DashboardFilter filter) {
        return partitionDao.search(new FilterParameters() {{
            Date[] period = filter.getPeriod();
            if (period != null) {
                setFromDate(period[0]);
                setToDate(period[1]);
            }
            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
            add("filterStatus", _State.NEW);
        }});
    }
}