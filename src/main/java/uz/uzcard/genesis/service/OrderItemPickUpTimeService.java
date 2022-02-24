package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemPickUpTimeStatusRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

/**
 * Madaminov Javohir {02.12.2020}.
 */
public interface OrderItemPickUpTimeService {

    ListResponse list(OrderItemPickUpTimeFilterRequest request);

    SingleResponse save(OrderItemPickUpTimeRequest request);

    SingleResponse updateStatus(OrderItemPickUpTimeStatusRequest request);
}
