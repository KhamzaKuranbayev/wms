package uz.uzcard.genesis.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.product.RentChangeStatusRequest;
import uz.uzcard.genesis.dto.api.req.product.RentFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.RentRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.ProductItemDao;
import uz.uzcard.genesis.hibernate.dao.RentDao;
import uz.uzcard.genesis.hibernate.dao.WarehouseDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.RentService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

@Service
public class RentServiceImpl implements RentService {

    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private RentDao rentDao;
    @Autowired
    private WarehouseDao warehouseDao;

    @Override
    @Transactional
    public _Rent save(RentRequest request) {
        if (request.getProductItemId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_ITEM_REQUIRED"));

        _ProductItem productItem = productItemDao.get(request.getProductItemId());
        if (productItem == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));

        if (request.getWarehouseId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("WAREHOUSE_IS_REQUIRED"));

        _Warehouse warehouse = warehouseDao.get(request.getWarehouseId());
        if (warehouse == null)
            throw new RpcException(GlobalizationExtentions.localication("WAREHOUSE_NOT_FOUND"));

        _ProductItem newProductItem = new _ProductItem();
        if (productItem.getCount() > request.getCount()) {
            BeanUtils.copyProperties(productItem, newProductItem, "id", "map", "bron", "carriages", "cells");

            newProductItem.setState(_State.PRODUCT_PRODUCED);
            newProductItem.setCount(request.getCount());
            newProductItem.setWarehouse(warehouse);
            newProductItem = productItemDao.save(newProductItem);

            productItem.setCount(productItem.getCount() - request.getCount());
            productItem = productItemDao.save(productItem);
        } else {
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_COUNT_NOT_ENOUGH"));
        }

        if (request.getCount() == null)
            throw new RpcException(GlobalizationExtentions.localication("COUNT_REQUIRED"));
        if (request.getExpireDate() == null)
            throw new RpcException(GlobalizationExtentions.localication("EXPIRED_DATE_REQUIRED"));

        _Rent rent = new _Rent();
        rent.setCount(request.getCount());
        rent.setRemains(request.getCount());
        rent.setDepartment(warehouse.getDepartment());
        rent.setProductItem(newProductItem);
        rent.setProductItemParent(productItem);
        rent.setExpireDate(request.getExpireDate());
        rent = rentDao.save(rent);
        return rent;
    }

    @Override
    public PageStream<_Rent> search(RentFilterRequest request) {
        return rentDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (request.getProductItemId() != null)
                add("productItemId", "" + request.getProductItemId());
            if (request.getProductId() != null)
                add("productId", "" + request.getProductId());
            if (request.getDepartmentId() != null)
                add("departmentId", "" + request.getDepartmentId());
            if (request.getFromDate() != null)
                addDate("fromDate", request.getFromDate());
            if (request.getToDate() != null)
                addDate("toDate", request.getToDate());
            if (request.getWithReturned())
                add("withReturned", "true");
        }});
    }

    @Override
    @Transactional
    public _Rent update(RentChangeStatusRequest request) {
        if (request.getRentId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("RENT_REQUIRED"));

        _Rent rent = rentDao.get(request.getRentId());
        if (rent == null)
            throw new RpcException(GlobalizationExtentions.localication("RENT_NOT_FOUND"));

        if (rent.getProductItem() == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));

        if (rent.getDepartment() == null)
            throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));

        _ProductItem productItem = rent.getProductItem();
        _ProductItem updateProductItem = productItemDao.getByQrCodeNewOnly(productItem.getQrcode());
        if (updateProductItem == null)
            throw new RpcException(GlobalizationExtentions.localication("PRODUCT_ITEM_NOT_FOUND"));

        if (productItem.getCount() == request.getCount()) {
            rent.setState(_State.RETURNED);
            updateProductItem.setCount(updateProductItem.getCount() + productItem.getCount());

            productItem.setCount(0);
            productItem.setState(_State.DELETED);
            productItemDao.save(productItem);
        } else {
            productItem.setCount(productItem.getCount() - request.getCount());
            productItemDao.save(productItem);

            updateProductItem.setCount(updateProductItem.getCount() + request.getCount());
        }
        productItemDao.save(updateProductItem);

        rent.setRemains(productItem.getCount());
        return rentDao.save(rent);
    }
}
