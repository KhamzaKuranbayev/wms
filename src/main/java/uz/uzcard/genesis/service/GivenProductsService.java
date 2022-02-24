package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.order.GivenProductOrderItemFilter;
import uz.uzcard.genesis.dto.api.req.order.GivenProductProductItemFilter;
import uz.uzcard.genesis.dto.api.req.setting.GivenProductsFilter;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._GivenProducts;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._Partition;

import java.util.List;

/**
 * Created by norboboyev_h  on 26.09.2020  16:03
 */

public interface GivenProductsService {
    PageStream<_GivenProducts> list(GivenProductsFilter request);

    ListResponse listOrderItemsForDepartment(GivenProductOrderItemFilter filter);

    _GivenProducts save(_OrderItem orderItem, Double count, _Partition partition);

    SingleResponse takingAway(TakenProductRequest request);

    SingleResponse takingAwayCheck(TakenProductRequest request);

    ListResponse listProductItemsForTmsGiven(GivenProductProductItemFilter filter);

    List<String> getWarehouseList(_OrderItem orderItem);
}