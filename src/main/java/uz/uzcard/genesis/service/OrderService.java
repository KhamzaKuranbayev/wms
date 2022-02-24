package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.order.MultipleOrderCreateRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Order;
import uz.uzcard.genesis.hibernate.entity._OrderItem;

import java.util.HashMap;
import java.util.List;

public interface OrderService {
    PageStream<_Order> list(OrderFilterRequest request);

    _Order add(OrderRequest request, List<MultipartFile> files);

    void delete(Long id);

    SingleResponse send(Long orderId);

    _OrderItem addDefaultOrder(OrderRequest request, List<MultipartFile> files);

    boolean teamLogicsFix(int start, int limit);

    _Order addMultiple(MultipleOrderCreateRequest request, List<MultipartFile> files);

    _Order addFiles(Long orderId, List<MultipartFile> files);

    ListResponse getOrderBySendDateFacet(DashboardFilter filterRequest);

    _Order get(Long id);
}