package uz.uzcard.genesis.service.impl;

import org.hibernate.envers.*;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.config.Constants;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.history.ContractHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.OrderHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.ProductItemRequest;
import uz.uzcard.genesis.dto.backend.PreviousChangesRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.base._AuditInfo;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.HistoryService;
import uz.uzcard.genesis.service.ProductItemService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HistoryServiceImpl implements HistoryService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private UserAgreementDao userAgreementDao;
    @Autowired
    private LotDao lotDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private AttachmentDao attachmentDao;

    @Override
    public Stream<HashMap<String, Object>> orders(OrderHistoryRequest request) {
        AuditQuery query = AuditReaderFactory.get(orderDao.getSession())
                .createQuery()
                .forRevisionsOfEntityWithChanges(_Order.class, true);
        if (request.getState() != null)
            query.add(AuditEntity.property("state").eq(request.getState()));
        if (request.getNumb() != null)
            query.add(AuditEntity.property("numb").eq(request.getNumb()));

        if (request.getFromDate() != null)
            query.add(AuditEntity.property("updated_date").ge(request.getFromDate()));
        if (request.getToDate() != null)
            query.add(AuditEntity.property("updated_date").le(request.getToDate()));
        query.addOrder(AuditEntity.revisionNumber().desc());

        List<Object[]> changes = query.setCacheable(true).setCacheRegion(Constants.Cache.QUERY_ORDER).getResultList();

        Stream<HashMap<String, Object>> list = changes.stream().map(objects -> {
            _Order order = (_Order) objects[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];
            Set<String> changedColumns = ((Set<String>) objects[3]);

            CoreMap map = order.getMap(true);

            return wrap(order, revisionEntity, revisionType, changedColumns, map);
        });
        return list;
    }

    @Override
    public Stream<HashMap<String, Object>> orderItems(OrderHistoryRequest request) {
        AuditQuery query = AuditReaderFactory.get(orderItemDao.getSession())
                .createQuery()
                .forRevisionsOfEntityWithChanges(_OrderItem.class, true);
        if (request.getState() != null)
            query.add(AuditEntity.property("state").eq(request.getState()));
        _Order order = null;
        if (request.getNumb() != null) {
            order = orderDao.getByNumber(request.getNumb());
            if (order == null)
                throw new ValidatorException("Заявка топилмади");
        } else if (request.getOrderId() != null) {
            order = orderDao.get(request.getOrderId());
            if (order == null)
                throw new ValidatorException("Заявка топилмади");
        }
        if (order == null)
            throw new ValidatorException("Заявка топилмади");
        if (request.getItemNumb() != null) {
            query.add(AuditEntity.property("itemNumb").eq(request.getItemNumb()));
        }
        query.add(AuditEntity.property("parent").eq(order));

        if (request.getFromDate() != null)
            query.add(AuditEntity.property("updated_date").ge(request.getFromDate()));
        if (request.getToDate() != null)
            query.add(AuditEntity.property("updated_date").le(request.getToDate()));
        query.addOrder(AuditEntity.revisionNumber().desc());

        List<Object[]> changes = query.setCacheable(true).setCacheRegion(Constants.Cache.QUERY_ORDER).getResultList();

        Stream<HashMap<String, Object>> list = changes.stream().map(objects -> {
            _OrderItem orderItem = (_OrderItem) objects[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];
            Set<String> changedColumns = ((Set<String>) objects[3]);

            CoreMap map = orderItem.getMap(true);
            if (orderItem.getParent() != null)
                map.add("orderNum", "" + orderItem.getParent().getNumb());
            if (orderItem.getStatusChangedUser() != null)
                map.add("statusChangedUser", orderItem.getStatusChangedUser().getShortName());

            return wrap(orderItem, revisionEntity, revisionType, changedColumns, map);
        });
        return list;
    }

    @Override
    public Stream<HashMap<String, Object>> contracts(ContractHistoryRequest request) {
        AuditQuery query = AuditReaderFactory.get(contractDao.getSession())
                .createQuery()
                .forRevisionsOfEntityWithChanges(_Contract.class, true);
        if (request.getState() != null)
            query.add(AuditEntity.property("state").eq(request.getState()));
        if (request.getCode() != null)
            query.add(AuditEntity.property("code").eq(request.getCode()));

        /*if (request.getFromDate() != null)
            query.add(AuditEntity.property("updated_date").ge(request.getFromDate()));
        if (request.getToDate() != null)
            query.add(AuditEntity.property("updated_date").le(request.getToDate()));*/
        query.addOrder(AuditEntity.revisionNumber().desc());

        List<Object[]> changes = query.setCacheable(true).setCacheRegion(Constants.Cache.QUERY_CONTRACT).getResultList();

        Stream<HashMap<String, Object>> list = changes.stream().map(objects -> {
            _Contract contract = (_Contract) objects[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];
            Set<String> changedColumns = ((Set<String>) objects[3]);

            CoreMap map = contract.getMap(true);

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
            if (contract.getSupplyType() != null)
                map.put("supplyType", contract.getSupplyType().name());
            return wrap(contract, revisionEntity, revisionType, changedColumns, map);
        });
        return list;
    }

    @Override
    public Stream<HashMap<String, Object>> contractItems(ContractHistoryRequest request) {
        AuditQuery query = AuditReaderFactory.get(contractItemDao.getSession())
                .createQuery()
                .forRevisionsOfEntityWithChanges(_ContractItem.class, true);
        if (request.getState() != null)
            query.add(AuditEntity.property("state").eq(request.getState()));
        if (request.getCode() != null) {
            _Contract contract = contractDao.getByCode(request.getCode());
            query.add(AuditEntity.property("parent").eq(contract));
        }
        if (request.getItemNumb() != null) {
            query.add(AuditEntity.property("numb").eq(request.getItemNumb()));
        }

//        if (request.getFromDate() != null)
//            query.add(AuditEntity.property("updated_date").ge(request.getFromDate()));
//        if (request.getToDate() != null)
//            query.add(AuditEntity.property("updated_date").le(request.getToDate()));
        query.addOrder(AuditEntity.revisionNumber().desc());

        List<Object[]> changes = query.setCacheable(true).setCacheRegion(Constants.Cache.QUERY_CONTRACT).getResultList();

        Stream<HashMap<String, Object>> list = changes.stream().map(objects -> {
            _ContractItem contractItem = (_ContractItem) objects[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];
            Set<String> changedColumns = ((Set<String>) objects[3]);

            CoreMap map = contractItem.getMap(true);

            if (contractItem.getUnitType() != null) {
                map.add("unit_type_name_en", contractItem.getUnitType().getNameEn());
                map.add("unit_type_name_ru", contractItem.getUnitType().getNameRu());
                map.add("unit_type_name_uz", contractItem.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", contractItem.getUnitType().getNameCyrl());
            }
            if (contractItem.getParent() != null)
                map.put("contractCode", contractItem.getParent().getCode());
            if (contractItem.getProduct() != null)
                map.put("productName", contractItem.getProduct().getName());
            return wrap(contractItem, revisionEntity, revisionType, changedColumns, map);
        });
        return list;
    }

    @Override
    public Stream<HashMap<String, Object>> getProductItemHistory(ProductItemRequest request) {
        AuditQuery query = AuditReaderFactory.get(productItemDao.getSession())
                .createQuery()
                .forRevisionsOfEntityWithChanges(_ProductItem.class, true);
        if (request.getState() != null)
            query.add(AuditEntity.property("state").eq(request.getState()));
        if (request.getOrderId() != null) {
            _Order order = orderDao.get(request.getOrderId());
            if (order != null)
                query.add(AuditEntity.property("orderItem.parent").eq(order));
        }
        if (request.getOrderNumb() != null) {
            _Order order = orderDao.getByNumber(request.getOrderNumb());
            if (order != null)
                query.add(AuditEntity.property("orderItem.parent").eq(order));
        }
        if (request.getId() != null)
            query.add(AuditEntity.id().eq(request.getId()));
        if (request.getQrCode() != null)
            query.add(AuditEntity.property("qrcode").eq(request.getQrCode()));
        if (!StringUtils.isEmpty(request.getAccountingCode()))
            query.add(AuditEntity.property("accountingCode").eq(request.getAccountingCode()));

        /*if (request.getFromDate() != null)
            query.add(AuditEntity.property("updated_date").ge(request.getFromDate()));
        if (request.getToDate() != null)
            query.add(AuditEntity.property("updated_date").le(request.getToDate()));*/

        query.addOrder(AuditEntity.revisionNumber().desc());

        List<Object[]> changes = query.setCacheable(true).setCacheRegion(Constants.Cache.QUERY_PRODUCT).getResultList();

        Stream<HashMap<String, Object>> list = changes.stream().map(objects -> {
            _ProductItem productItem = (_ProductItem) objects[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) objects[1];
            RevisionType revisionType = (RevisionType) objects[2];
            Set<String> changedColumns = ((Set<String>) objects[3]);

            CoreMap map = productItem.getMap(true);
            map.addDouble("count", productItem.getCount());
            map.add("qrcode", "" + productItem.getQrcode());

            if (productItem.getUnitType() != null) {
                map.add("unit_type_name_en", productItem.getUnitType().getNameEn());
                map.add("unit_type_name_ru", productItem.getUnitType().getNameRu());
                map.add("unit_type_name_uz", productItem.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", productItem.getUnitType().getNameCyrl());
            }
            productItemService.putPosition(productItem, map);
            if (productItem.getPlacementType() != null) {
                map.add("placementTypeName", GlobalizationExtentions.getName(productItem.getPlacementType()));
                if (productItem.getPlacedBy() != null)
                    map.add("placementByUser", productItem.getPlacedBy().getShortName());
            }
            if (productItem.getPartition() != null && productItem.getPartition().getContractItem() != null) {
                map.add("contractItemId", productItem.getPartition().getContractItem().getId());
            }
            return wrap(productItem, revisionEntity, revisionType, changedColumns, map);
        });
        return list;
    }

    @Override
    public void previousChangesContract(PreviousChangesRequest request) {
        Long contractId = request.getObjectId();
        Integer revision = request.getRevision();

        _Contract contract = contractDao.get(contractId);

        contractDao.save(contract = revert(contractDao, contract, revision));

        _Contract finalContract = contract;
        contract.getItems().forEach(contractItem -> {
            revertChangesForContractItem(revision, contractItem, finalContract);
        });
        contractDao.save(contract);
    }

    @Override
    public void previousChangesContractItem(PreviousChangesRequest request) {
        Long contractItemId = request.getObjectId();
        Integer revision = request.getRevision();

        _ContractItem contractItem = contractItemDao.get(contractItemId);
        _Contract contract = contractItem.getParent();

        contract = revert(contractDao, contract, revision);
        contractDao.save(contract);
        revertChangesForContractItem(revision, contractItem, contract);
    }

    @Autowired
    private ProductDao productDao;

    private void revertChangesForContractItem(Integer revision, _ContractItem contractItem, _Contract contract) {
        _ContractItem dbVersion = contractItemDao.get(contractItem.getId());

        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());
        _ContractItem oldVersion = revert(contractItemDao, dbVersion, revision);
        oldVersion.setParent(contract);
        contractItemDao.save(oldVersion);

        List<_UserAgreement> temp = oldVersion.getUserAgreements();
        mergeCollection(dbVersion.getUserAgreements(), temp);
        oldVersion.setUserAgreements(temp);

        for (_UserAgreement userAgreement : oldVersion.getUserAgreements()) {
            userAgreementDao.save(userAgreement = revert(userAgreementDao, userAgreement, revision));
        }
        partitionDao.findAllByContractItem(oldVersion).forEach(partition -> {
            partition = revert(partitionDao, partition, revision);
            partition.setContractItem(contractItem);
            partition.setProduct(productDao.get(partition.getProduct().getId()));

            List<_Lot> lots = new ArrayList<>();
            for (_Lot lot : partition.getLots()) {
                lot = revert(lotDao, lot, revision);
                lot.setPartition(partition);
                lotDao.save(lot);
                lots.add(lot);
            }
            partition.setLots(lots);
            partition.getGivens().forEach(givenProduct -> {
                givenProductsDao.save(givenProduct = revert(givenProductsDao, givenProduct, revision));
            });

            for (final _Lot lot : partition.getLots()) {
                _Partition finalPartition = partition;
                productItemDao.findAllByLot(lot).forEach(productItem -> {
                    productItem = revert(productItemDao, productItem, revision);
                    productItem.setPartition(finalPartition);
                    productItem.setLot(lot);
                    productItemDao.save(productItem);
                });
            }
        });
        orderItemDao.findAllByContractItem(contractItem).forEach(orderItem -> {
            previousChangesOrderItem(new PreviousChangesRequest() {{
                setObjectId(orderItem.getId());
                setRevision(revision);
            }});
        });
    }

    @Override
    public void previousChangesOrder(PreviousChangesRequest request) {
        Long orderId = request.getObjectId();
        Integer revision = request.getRevision();

        _Order order = orderDao.get(orderId);

        orderDao.save(order = revert(orderDao, order, revision));

        for (_OrderItem orderItem : order.getItems()) {
            revertOrderItem(revision, order, orderItem);
        }
        orderDao.save(order);
    }

    @Override
    public void previousChangesOrderItem(PreviousChangesRequest request) {
        Long orderId = request.getObjectId();
        Integer revision = request.getRevision();

        _OrderItem orderItem = orderItemDao.get(orderId);
        _Order order = orderItem.getParent();

        revertOrderItem(revision, order, orderItem);

        orderDao.save(order);
    }

    private void revertOrderItem(Integer revision, _Order order, _OrderItem orderItem) {
        orderItem = revert(orderItemDao, orderItem, revision);
        orderItem.setParent(order);

        if (orderItem.getOfferAttachment() != null) {
            _Attachment offerAttachment = attachmentDao.get(orderItem.getOfferAttachment().getId());

            orderItem.setOfferAttachment(attachmentDao.getById(offerAttachment.getId()));
        }
        orderItemDao.save(orderItem);
    }

    private HashMap<String, Object> wrap(_Entity entity, DefaultRevisionEntity revisionEntity, RevisionType revisionType, Set<String> changedColumns, CoreMap map) {
        map.remove("hashESign");
        map.add("revisionType", revisionType.name());
        map.add("revision", "" + revisionEntity.getId());
        List<Field> fields = _Entity.getFields(entity.getClass());
        Optional<Field> optional = fields.stream().filter(x -> _AuditInfo.class.equals(x.getType())).findFirst();
        if (optional.isPresent()) {
            Field field = optional.get();
            field.setAccessible(true);
            try {
                _AuditInfo auditInfo = (_AuditInfo) field.get(entity);
                if (auditInfo.getCreatedByUser() != null)
                    map.add("createdByUser", auditInfo.getCreatedByUser().getShortName());
                if (auditInfo.getCreationDate() != null)
                    map.addDate("creationDate", auditInfo.getCreationDate());
                if (auditInfo.getUpdatedByUser() != null)
                    map.add("updatedByUser", auditInfo.getUpdatedByUser().getShortName());
                if (auditInfo.getUpdatedDate() != null)
                    map.addDate("updatedDate", auditInfo.getUpdatedDate());
                if (!map.has("updatedByUser"))
                    map.add("updatedByUser", map.getString("createdByUser"));
                if (!map.has("updatedDate"))
                    map.add("updatedDate", map.getString("creationDate"));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        map.addStrings("changes", new ArrayList<>(changedColumns));
        LinkedHashMap<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.putAll(map.getInstance());
        objectMap.putAll(map.getInstance2());
        return objectMap;
    }

    private <T extends _Entity> T revert(Dao dao, T entity, Integer revision) {
        T dbVersion = (T) dao.get(entity.getId());
        T oldVersion = getPreviousVersion(dbVersion, revision);
        if (oldVersion == null) {
            ((_Entity) dbVersion).setState(_State.DELETED);
            orderDao.getSession().saveOrUpdate((_Entity) dbVersion);
            return dbVersion;
        }
        dbVersion.setMap(oldVersion.getMap());

        for (Field field : _Entity.getFields(oldVersion.getClass())) {
            try {
                if (field.get(oldVersion) == null)
                    continue;

                if (_Entity.class.isAssignableFrom(field.getType())) {
                    Object temp = field.getType().newInstance();
                    BeanUtils.copyProperties(field.get(oldVersion), temp);
                    field.set(dbVersion, temp);

                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    List oldFieldData = List.copyOf((List) field.get(oldVersion));
                    List dbFieldData = (List) field.get(dbVersion);

                    mergeCollection(oldFieldData, dbFieldData);
                    field.set(dbVersion, oldFieldData);

                } else if (field.getType().isArray() || Map.class.isAssignableFrom(field.getType())) {
                    Object temp = field.getType().newInstance();
                    BeanUtils.copyProperties(field.get(oldVersion), temp);
                    field.set(dbVersion, temp);

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return dbVersion;
    }

    private void mergeCollection(List oldFieldData, List dbFieldData) {
        for (Object item : dbFieldData) {
            if (!(item instanceof _Entity))
                break;
            if (oldFieldData.contains(item))
                continue;
            ((_Entity) item).setState(_State.DELETED);
            orderDao.getSession().saveOrUpdate((_Entity) item);
        }
        for (Object item : oldFieldData) {
            if (!(item instanceof _Entity) || item.getClass().isAnnotationPresent(Audited.class))
                break;
            if (_State.DELETED.equals(((_Entity) item).getState()))
                ((_Entity) item).setState(_State.NEW);
        }
    }

    private <T extends _Entity> int getCurrentRevision(T entity) {
        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());

        Object[] singleResult = (Object[]) reader.createQuery().forRevisionsOfEntity(entity.getClass(), false, true)
                .add(AuditEntity.id().eq(entity.getId()))
                .add(AuditEntity.revisionNumber().maximize())
                .getSingleResult();
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) singleResult[1];
        return revisionEntity.getId();
    }

    public <T extends _Entity> T getPreviousVersion(T entity, Integer current_rev) {
        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());

//        Number prior_revision = (Number) reader.createQuery()
//                .forRevisionsOfEntity(entity.getClass(), false, true)
//                .addProjection(AuditEntity.revisionNumber().max())
//                .add(AuditEntity.id().eq(entity.getId()))
//                .add(AuditEntity.revisionNumber().lt(current_rev))
//                .getSingleResult();

//        if (prior_revision != null)
        T data = (T) reader.find(entity.getClass(), entity.getId(), current_rev);
//        if (data == null)
//            throw new ValidatorException("Ортга, бу қадамгача қайтиб бўлмайди.");
        return data;
    }
}