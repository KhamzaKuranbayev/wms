package uz.uzcard.genesis.service.impl;

import org.hibernate.internal.build.AllowPrintStacktrace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeStatusRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OrderItemDao;
import uz.uzcard.genesis.hibernate.dao.OrderItemPickUpTimeDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._OrderItemPickUpTime;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.OrderItemPickUpTimeService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.stream.Stream;

/**
 * Madaminov Javohir {02.12.2020}.
 */
@Service
public class OrderItemPickUpTimeServiceImpl implements OrderItemPickUpTimeService {

    @Autowired
    private OrderItemPickUpTimeDao orderItemPickUpTimeDao;
    @Autowired
    private OrderItemDao orderItemDao;

    @Override
    public ListResponse list(OrderItemPickUpTimeFilterRequest request) {
        PageStream<_OrderItemPickUpTime> pickUpTimes = orderItemPickUpTimeDao.search(new FilterParameters() {{
            if (request.getOrderItemId() != null)
                add("orderItemId", request.getOrderItemId().toString());
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
        }});
        return ListResponse.of(pickUpTimes, (orderItemPickUpTime, map) -> {
            if (orderItemPickUpTime.getOrderItem() != null) {
                map.add("orderItemId", orderItemPickUpTime.getOrderItem().getId());
                if (orderItemPickUpTime.getOrderItem().getContractItem() != null) {
                    if (orderItemPickUpTime.getOrderItem().getContractItem().getParent() != null) {
                        map.add("contractCode", orderItemPickUpTime.getOrderItem().getContractItem().getParent().getCode());
                    }
                    if (orderItemPickUpTime.getOrderItem().getProduct() != null)
                        map.add("productName", orderItemPickUpTime.getOrderItem().getProduct().getName());
                    if (orderItemPickUpTime.getOrderItem().getUnitType() != null) {
                        map.add("unitTypeNameUz", orderItemPickUpTime.getOrderItem().getUnitType().getNameUz());
                        map.add("unitTypeNameEn", orderItemPickUpTime.getOrderItem().getUnitType().getNameEn());
                        map.add("unitTypeNameRu", orderItemPickUpTime.getOrderItem().getUnitType().getNameRu());
                    }

                }
            }
            return map;
        });
    }

    @Override
    public SingleResponse save(OrderItemPickUpTimeRequest request) {
        if (request.getOrderItemId() == null)
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_REQUIRED"));

        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (orderItem == null)
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));

        _OrderItemPickUpTime orderItemPickUpTime = new _OrderItemPickUpTime();
        orderItemPickUpTime.setPickUpTime(request.getPickUpTime());
        orderItemPickUpTime.setOrderItem(orderItem);
        orderItemPickUpTime = orderItemPickUpTimeDao.save(orderItemPickUpTime);
        return SingleResponse.of(orderItemPickUpTime, (orderItemPickUpTime1, map) -> map);
    }

    @Override
    public SingleResponse updateStatus(OrderItemPickUpTimeStatusRequest request) {
        _OrderItemPickUpTime orderItemPickUpTime = orderItemPickUpTimeDao.get(request.getId());
        if (ServerUtils.isEmpty(orderItemPickUpTime))
            throw new ValidatorException("ORDER_ITEM_PICK_UP_TIME_IS_NULL");
        orderItemPickUpTime.setState(_State.DELETED);
        orderItemPickUpTimeDao.save(orderItemPickUpTime);
        return SingleResponse.of(true);
    }
}
