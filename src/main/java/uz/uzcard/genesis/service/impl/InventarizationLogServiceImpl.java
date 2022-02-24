package uz.uzcard.genesis.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.InventarizationLogFilter;
import uz.uzcard.genesis.dto.api.req.product.InventarizationLogRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.InventarizationLogService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by norboboyev_h  on 08.09.2020  12:12
 */
@Service
public class InventarizationLogServiceImpl implements InventarizationLogService {

    @Autowired
    private InventarizationLogDao inventarizationLogDao;

    @Autowired
    private InventarizationDao inventarizationDao;

    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private CarriageDao carriageDao;

    @Override
    public PageStream<_InventarizationLog> list(InventarizationLogFilter request) {
        return inventarizationLogDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            addLong("userId", request.getUserId());
            addLong("inventarizationId", request.getInventarizationId());
            addString("productId", "" + request.getProductId());
            addString("isValid", "" + request.getValid());
            addString("productGroupId", "" + request.getProductGroupId());
            addString("productTypeId", "" + request.getProductTypeId());
        }});
    }

    @Override
    public void save(InventarizationLogRequest request) {
        _Inventarization inventarization = inventarizationDao.get(request.getInventarizationId());
        if (inventarization == null)
            throw new ValidatorException("INVENTARIZATION_REQUIRED");
        _ProductItem productItem = productItemDao.get(request.getProductItemId());
        if (productItem == null)
            throw new ValidatorException("PRODUCT_ITEM_NOT_FOUND");

        // mobiledan soni bo'yicha inventarizatsiya qilish,
        _InventarizationLog log = inventarizationDao.getByInventarizationAndProductItem(inventarization, productItem);
        if (log == null)
            log = new _InventarizationLog();

        _ProductItem newProductItem = new _ProductItem();
        List<Long> carriagesId = new ArrayList<>();
        if (request.isValid()) {
            log.setProductItem(productItem);
        } else {
            if (productItem.getCount() >= request.getCount()) {
                if (productItem.getCount() == request.getCount()) {
                    log.setProductItem(productItem);
                    carriagesId.addAll(productItem.getCarriages_id());
                    productItem.setCells_id(new ArrayList<>());
                    productItem.setCarriages_id(new ArrayList<>());
                    productItem.setState(_State.DELETED);
                } else {
                    BeanUtils.copyProperties(productItem, newProductItem, "id", "map", "carriages_id", "cells_id", "placementType");
                    productItem.setCount(productItem.getCount() - request.getCount());
                    newProductItem.setCount(request.getCount());
                    carriagesId.addAll(newProductItem.getCarriages_id());
                    newProductItem.setCells_id(new ArrayList<>());
                    newProductItem.setCarriages_id(new ArrayList<>());
                    newProductItem.setState(_State.DELETED);
                    productItemDao.save(newProductItem);
                    productItemDao.save(productItem);
                    log.setProductItem(newProductItem);
                }
            } else {
                throw new RpcException(GlobalizationExtentions.localication("COUNT_ENOUGHT"));
            }
        }

        log.setValid(request.isValid());
        log.setCount(request.getCount());
        log.setInventarization(inventarization);
        inventarizationLogDao.save(log);

        productItem.setInventarizationLog(log);
        productItemDao.save(productItem);

        List<_Carriage> carriages = carriageDao.findByIds(carriagesId).collect(Collectors.toList());
        carriageService.checkToHasProduct(carriages);
        carriages.stream().map(carriage -> carriage.getStillageColumn().getStillage().getWarehouse())
                .distinct().forEach(warehouse -> {
            warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
            warehouseDao.save(warehouse);
        });
    }
}