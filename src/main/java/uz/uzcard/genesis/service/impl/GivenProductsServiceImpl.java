package uz.uzcard.genesis.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.order.GivenProductOrderItemFilter;
import uz.uzcard.genesis.dto.api.req.order.GivenProductProductItemFilter;
import uz.uzcard.genesis.dto.api.req.setting.GivenProductsFilter;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.GivenProductsService;
import uz.uzcard.genesis.service.PartitionService;
import uz.uzcard.genesis.service.ProductItemService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 26.09.2020  16:03
 */
@Service
public class GivenProductsServiceImpl implements GivenProductsService {

    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private LotDao lotDao;

    @Override
    public PageStream<_GivenProducts> list(GivenProductsFilter request) {
        FilterParameters filter = new FilterParameters() {{
            add("productName", request.getProductName());
            addLong("orderNumb", request.getOrderNumb());
            add("contractCode", request.getContractCode());
            addLong("warehouseId", request.getWarehouseId());
        }};
        return givenProductsDao.search(filter);
    }

    @Override
    public ListResponse listOrderItemsForDepartment(GivenProductOrderItemFilter filter) {
        return ListResponse.of(orderItemDao.search(new FilterParameters() {{
            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
            addString("orderNumber", "" + filter.getNumbSearch());
            addBool("hasGiven", true);
        }}), (orderItem, map) -> {
            if (orderItem.getParent() != null)
                map.add("parentNumb", "" + orderItem.getParent().getNumb());
            if (orderItem.getProduct() != null)
                map.add("productName", "" + orderItem.getProduct().getName());
            return map;
        });
    }

    @Override
    public _GivenProducts save(_OrderItem orderItem, Double count, _Partition partition) {
        _GivenProducts givenProducts = givenProductsDao.getNewByPartitionAndOrderItem(partition, orderItem);
        if ((orderItem.getGiven() + count) > orderItem.getCount())
            throw new ValidatorException("Заказда кўрсатилганидан кўпроқ маҳсулот бериляпти!");
        if (ServerUtils.isEmpty(givenProducts)) {
            givenProducts = new _GivenProducts();
            givenProducts.setPartition(partition);
            givenProducts.setOrderItem(orderItem);
            givenProducts.setContractItem(orderItem.getContractItem());
            givenProducts.setCount(count);
            givenProducts.setRemains(count);
            givenProducts.setWarehouse(partition.getWarehouse());
            givenProductsDao.save(givenProducts);
            orderItem.getGivens().remove(givenProducts);
            partition.getGivens().remove(givenProducts);
            orderItem.getGivens().add(givenProducts);
            partition.getGivens().add(givenProducts);
        } else {
            givenProducts.setCount(givenProducts.getCount() + count);
            givenProducts.setRemains(givenProducts.getRemains() + count);
            givenProductsDao.save(givenProducts);
        }
        orderItem.setGiven(givenProductsDao.getRemainsOrderItem(orderItem));
        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());
        return givenProducts;
    }

    @Override
    public SingleResponse takingAway(TakenProductRequest request) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());

        if (ServerUtils.isEmpty(request.getProductItemIds()))
            throw new ValidatorException("Продукт иды не найдено");

        if (ServerUtils.isEmpty(orderItem))
            throw new ValidatorException("По идентификатору заказа не найдено");
        List<_Partition> partitions = productItemDao.findByQrCodes(request.getProductItemIds())
                .map(_ProductItem::getPartition)
                .distinct().collect(Collectors.toList());
        if (partitions.isEmpty())
            throw new ValidatorException("Партия не найден");
        if (partitions.stream().anyMatch(partition -> partition == null))
            throw new ValidatorException("Танланганлар орасида омборхонадан чиқарилган махсулот бор. Махсулотларни қайта танланг.");
        if (partitions.size() > 1)
            throw new ValidatorException("Невозможно забрать по двух партиям сразу. Нужно брать по одному.");
        _GivenProducts givenProducts = givenProductsDao.getNewByPartitionAndOrderItem(partitions.get(0), orderItem);
        if (ServerUtils.isEmpty(givenProducts))
            throw new ValidatorException("По этому заказу и партию товар не отдан");
        if (givenProducts.getRemains() == 0.0 || givenProducts.getState().equals(_State.PRODUCT_ACCEPTED))
            throw new ValidatorException("Бу заказ ва бу партия бўйича берилган махсулотлар олиб кетилган");
        if (request.getProductItemIds().size() > 1 &&
                givenProducts.getCount() < productItemDao.findCountByIds(request.getProductItemIds()))
            throw new ValidatorException("Вы даете больше продуктов, чем выдано");
        if (request.getWarehouseId() == null)
            throw new ValidatorException("WAREHOUSE_IS_REQUIRED");
        _Warehouse warehouse = warehouseDao.get(request.getWarehouseId());
        if (warehouse == null)
            throw new RpcException(GlobalizationExtentions.localication("WAREHOUSE_NOT_FOUND"));

        productItemDao.findByQrCodes(request.getProductItemIds()).forEach(producedProduct -> {
            _Warehouse omtkWarehouse = producedProduct.getWarehouse();
            List<Long> carriagesId = ServerUtils.isEmpty(producedProduct.getCarriages_id()) ? new ArrayList<>() : producedProduct.getCarriages_id();
            checkGivenProducts(orderItem, producedProduct);

            List<Long> reindexProdId = request.getProductItemIds();
            if (producedProduct.getCount() - givenProducts.getRemains() > 0) {
                producedProduct.setCount(producedProduct.getCount() - givenProducts.getRemains());

//                //agar productni upakovkasidan hammasi berilmasa qoldig'ini yangi product sifatida saqlash
//                _ProductItem remainedProduct = new _ProductItem();
//                BeanUtils.copyProperties(producedProduct, remainedProduct, "id", "map", "carriages_id", "cells_id");
//                remainedProduct.setCells_id(new ArrayList<>());
//                remainedProduct.setCarriages_id(new ArrayList<>());
//                remainedProduct.setCount(producedProduct.getCount() - givenProducts.getRemains());
//                productItemDao.save(remainedProduct);
//                remainedProduct.getCarriages_id().addAll(producedProduct.getCarriages_id());
//                remainedProduct.getCells_id().addAll(producedProduct.getCells_id());
//                productItemDao.save(remainedProduct);
//                producedProduct.setCount(givenProducts.getRemains());
//                reindexProdId.add(remainedProduct.getId());

                _ProductItem newProductItem = new _ProductItem();
                BeanUtils.copyProperties(producedProduct, newProductItem, "id", "map", "carriages_id", "cells_id", "placementType");
                newProductItem.setCount(givenProducts.getRemains());
                newProductItem.setGivenUser(givenProducts.getAuditInfo().getCreatedByUser());
                newProductItem.setTakenAwayUser(SessionUtils.getInstance().getUser());
                newProductItem.setTakenAwayDate(new Date());
                newProductItem.setOrderItem(orderItem);
                newProductItem.setState(_State.PRODUCT_PRODUCED);
                if (orderItem.getParent().getDepartment() == null)
                    throw new RpcException("Заявка ёзган фойдаланувчи бўлимга бириктирилмаган");
                if (orderItem.getParent().getDepartment().getWarehouses() == null)
                    throw new RpcException("Заявка ёзган фойдаланувчига обморхона бириктирилмаган");
                newProductItem.setWarehouse(warehouse);
                newProductItem.setCells_id(new ArrayList<>());
                newProductItem.setCarriages_id(new ArrayList<>());

                if (request.isReplace() && !(newProductItem.getPartition().getRemains() == 0 && givenProducts.getRemains() == 0)) {
                    newProductItem.setState(_State.NEW);
                    newProductItem.setQrcode(null);
                    _Partition partition = newProductItem.getPartition();
                    _Partition newPartition = new _Partition();
                    BeanUtils.copyProperties(partition, newPartition, "id", "map", "warehouse", "givens", "lots", "addresses");
                    if (orderDao.getUser().getDepartment() == null)
                        throw new ValidatorException("DEPARTMENT_NOT_FOUND");

                    newPartition.setWarehouse(warehouse);
                    newPartition.setRemains(newProductItem.getCount());
                    newPartition.setCount(newProductItem.getCount());

                    _Lot newLot = new _Lot();
                    BeanUtils.copyProperties(newProductItem.getLot(), newLot, "id", "map", "name", "remains", "count", "addresses");
                    newLot.setPartition(newPartition);
                    newLot.setName(orderDao.getUser().getDepartment().getNameUz());
                    newLot.setRemains(newProductItem.getCount());
                    newLot.setCount(newProductItem.getCount());
                    partitionDao.save(newPartition);
                    lotDao.save(newLot);
                    newPartition.getLots().add(newLot);
                    partitionDao.save(newPartition);

                    newProductItem.setLot(newLot);
                    newProductItem.setPartition(newPartition);
                } else {
                    newProductItem.setWarehouse(warehouse);
                }
                productItemDao.save(newProductItem);
                reindexProdId.add(newProductItem.getId());


                givenProducts.setRemains(0d);
            } else {
                //hammasini bervorish
                givenProducts.setRemains(givenProducts.getRemains() - producedProduct.getCount());
                producedProduct.setState(_State.PRODUCT_PRODUCED);
                producedProduct.setWarehouse(warehouse);
                producedProduct.setTakenAwayUser(SessionUtils.getInstance().getUser());
                producedProduct.setTakenAwayDate(new Date());
                producedProduct.setGivenUser(givenProducts.getAuditInfo().getCreatedByUser());
                producedProduct.setOrderItem(orderItem);
                producedProduct.setCells_id(new ArrayList<>());
                producedProduct.setCarriages_id(new ArrayList<>());
            }

            if (givenProducts.getRemains() < 0)
                throw new ValidatorException("Вы даете больше продуктов, чем выдано");
            if (givenProducts.getRemains() == 0.0)
                givenProducts.setState(_State.PRODUCT_ACCEPTED);
            givenProductsDao.save(givenProducts);
            //productni boshqa skladga o'tqazish
//            producedProduct.setState(_State.PRODUCT_PRODUCED);
//            if (orderItem.getParent().getDepartment() == null)
//                throw new RpcException("Заявка ёзган фойдаланувчи бўлимга бириктирилмаган");
//            if (orderItem.getParent().getDepartment().getWarehouse() == null)
//                throw new RpcException("Заявка ёзган фойдаланувчига обморхона бириктирилмаган");
//            producedProduct.setWarehouse(orderItem.getParent().getDepartment().getWarehouse());
            //karetkani bo'shatish

//            producedProduct.getCells_id().clear();
//            producedProduct.getCarriages_id().clear();
//            producedProduct.setGivenUser(givenProducts.getAuditInfo().getCreatedByUser());
//            producedProduct.setTakenAwayUser(SessionUtils.getInstance().getUser());
//            producedProduct.setTakenAwayDate(new Date());
//            producedProduct.setOrderItem(orderItem);

//            if (/*request.isReplace() &&*/ !(producedProduct.getPartition().getRemains() == 0 && givenProducts.getRemains() == 0)) {
//                producedProduct.setQrcode(null);
//                _Partition partition = producedProduct.getPartition();
//                _Partition newPartition = new _Partition();
//                BeanUtils.copyProperties(partition, newPartition, "id", "map", "warehouse", "givens", "lots", "addresses");
//                if (orderDao.getUser().getDepartment() == null)
//                    throw new ValidatorException("DEPARTMENT_NOT_FOUND");
//                if (orderDao.getUser().getDepartment().getWarehouse() == null)
//                    throw new ValidatorException("WAREHOUSE_NOT_FOUND");
//                newPartition.setWarehouse(orderDao.getUser().getDepartment().getWarehouse());
//                newPartition.setRemains(producedProduct.getCount());
//                newPartition.setCount(producedProduct.getCount());
//
//                _Lot newLot = new _Lot();
//                BeanUtils.copyProperties(producedProduct.getLot(), newLot, "id", "map", "name", "remains", "count", "addresses");
//                newLot.setPartition(newPartition);
//                newLot.setName(orderDao.getUser().getDepartment().getName());
//                newLot.setRemains(producedProduct.getCount());
//                newLot.setCount(producedProduct.getCount());
//                partitionDao.save(newPartition);
//                lotDao.save(newLot);
//                newPartition.getLots().add(newLot);
//                partitionDao.save(newPartition);
//
//                producedProduct.setLot(newLot);
//                producedProduct.setPartition(newPartition);
//                productItemDao.save(producedProduct);
//            } else {
//                producedProduct.setWarehouse(orderDao.getUser().getDepartment().getWarehouse());
//            }
            productItemDao.save(producedProduct);
            carriageService.checkToHasProduct(carriageService.findByIds(carriagesId).collect(Collectors.toList()));
            if (omtkWarehouse != null) {
                omtkWarehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(omtkWarehouse));
                warehouseDao.save(omtkWarehouse);
            }
            partitionService.reCalculate(givenProducts.getPartition());
        });
        Double givenProductSum = givenProductsDao.getRemainsOrderItemByState(orderItem, _State.PRODUCT_ACCEPTED);
        orderItem.setTakenAway(givenProductSum);
        if (givenProductSum.equals(orderItem.getCount())) {
            orderItem.setState(_State.RECEIVED);
            orderItemDao.save(orderItem);
            checkFinishedOrder(orderItem);
        }

        productItemDao.reindex(request.getProductItemIds());

        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());

        return SingleResponse.of(true);
    }

    private void checkFinishedOrder(_OrderItem orderItem) {
        if (orderItemDao.getTotalCount(orderItem.getParent()) == orderItemDao.getAcceptCount(orderItem.getParent())) {
            _Order parent = orderItem.getParent();
            parent.setState(_State.ACCEPTED_ORDER);
            orderDao.save(parent);
        }
    }

    private void checkGivenProducts(_OrderItem orderItem, _ProductItem producedProduct) {
        if (ServerUtils.isEmpty(producedProduct))
            throw new ValidatorException("По идентификатору продукт не найдено");
        if (_State.PRODUCT_PRODUCED.equals(producedProduct.getState()))
            throw new ValidatorException("Продукт уже забрали");
        if (!SessionUtils.getInstance().getUser().getDepartment().equals(orderItem.getAuditInfo().getCreatedByUser().getDepartment()))
            throw new ValidatorException("Пользователь не имеет права получить продукт по данному заказу.");
    }

    @Override
    public SingleResponse takingAwayCheck(TakenProductRequest request) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());

        if (ServerUtils.isEmpty(orderItem))
            throw new ValidatorException("По идентификатору заказа не найдено");

        productItemDao.findByQrCodes(request.getProductItemIds()).forEach(productItem -> {
            checkGivenProducts(orderItem, productItem);
            _GivenProducts givenProducts = givenProductsDao.getByPartitionAndOrderItem(productItem.getPartition(), orderItem);
            if (ServerUtils.isEmpty(givenProducts))
                throw new ValidatorException("По этому заказу и партию товар не отдан");
            if (givenProducts.getRemains() == 0.0 || givenProducts.getState().equals(_State.PRODUCT_ACCEPTED))
                throw new ValidatorException("Бу заказ ва бу партия бўйича берилган махсулотлар олиб кетилган");
        });
        return SingleResponse.of(true);
    }

    @Override
    public ListResponse listProductItemsForTmsGiven(GivenProductProductItemFilter filter) {
        Stream<_ProductItem> productItemStream = givenProductsDao.getByGivenProductParamsForProductItem(filter);
        Long count = givenProductsDao.getByGivenProductParamsForProductItemCount(filter);
        return ListResponse.of(productItemStream, count.intValue(), (productItem, map) -> map);
    }

    @Override
    public List<String> getWarehouseList(_OrderItem orderItem) {
        return givenProductsDao.getWarehouseList(orderItem);
    }
}
