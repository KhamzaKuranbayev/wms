package uz.uzcard.genesis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.order.*;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemsRequest;
import uz.uzcard.genesis.dto.api.req.setting.OpportunityRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.OrderResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Order;
import uz.uzcard.genesis.hibernate.entity._OrderItem;

import java.util.List;
import java.util.stream.Stream;

public interface OrderItemsService {
    _OrderItem save(OrderItemRequest request);

    _OrderItem updateItemCount(ItemCountRequest request);

    ListResponse listByOrder(ItemsRequest request);

    CoreMap get(Long id);

    void delete(Long order_item_id);

    void changeStatus(_OrderItem orderItem, String state);

    SingleResponse accept(Long orderId);

    _OrderItem specification(Long id);

    _OrderItem tender(Long orderItemId);

    _OrderItem reject(OrderItemRejectRequest request, MultipartFile file);

    boolean rejectOzl(OrderItemsRejectRequest request, MultipartFile file) throws JsonProcessingException;

    void opportunity(OpportunityRequest request, MultipartFile file);

    PageStream<_OrderItem> list(OrderFilterRequest request);

    List<SelectItem> getStatusChangedUsers(String name);

    CoreMap getOneItem(Long orderId, Long productId);

    SingleResponse checkEDS(Long orderItemId);

    SingleResponse setHashESign(HashESignRequest request);

    Double provide(_OrderItem orderItem, Double count);

    void bron(_OrderItem orderItem, Double count);

    SingleResponse getSingle(Long id);

    SingleResponse ozlOffer(OrderItemOfferRequest request, MultipartFile file);

    void updateOzlOffer(OrderItemOfferRequest request);

    void updateTakenAwayCount();

    ListResponse getOrderItemsByProductCategory(DashboardFilter request);

    ListResponse defineOrderStatusCount(DashboardFilter filterRequest);

    ListResponse departmentOrderItems(DashboardFilter filterRequest);

    ListResponse latencyRequests(DashboardFilter request);

    _OrderItem changeState(OrderItemStateChangeRequest request);

    Stream<_OrderItem> findAll(_Order order);

    void search(OrderFilterRequest request, Long orderId, OrderResponse response);
}