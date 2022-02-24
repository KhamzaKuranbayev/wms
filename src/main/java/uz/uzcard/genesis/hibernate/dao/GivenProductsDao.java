package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.dto.api.req.order.GivenProductProductItemFilter;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._GivenProducts;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._ProductItem;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 26.09.2020  16:07
 */

public interface GivenProductsDao extends Dao<_GivenProducts> {

    Double getRemainsPartition(_Partition partition);

    Double getRemainsOrderItemByState(_OrderItem orderItem, String state);

    Double getRemainsOrderItem(_OrderItem orderItem);

    _GivenProducts getByPartitionAndOrderItem(_Partition partition, _OrderItem orderItem);

    _GivenProducts getNewByPartitionAndOrderItem(_Partition partition, _OrderItem orderItem);

    Stream<_ProductItem> getByGivenProductParamsForProductItem(GivenProductProductItemFilter filter);

    Long getByGivenProductParamsForProductItemCount(GivenProductProductItemFilter filter);

    List<String> getWarehouseList(_OrderItem orderItem);
}