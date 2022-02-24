package uz.uzcard.genesis.listener.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.resp.ItemResponse;
import uz.uzcard.genesis.dto.api.resp.OrderResponse;
import uz.uzcard.genesis.dto.event.order.item.OrderItemCreateEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemDeleteEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemUpdateCountEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemUpdateStatusEvent;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.AttachmentUtils;

/**
 * Created by 'Madaminov Javohir' on 27.10.2020
 */
@Component
public class OrderItemEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private StateService stateService;

    @EventListener
    public void saveHandle(OrderItemCreateEvent event) {
        LOGGER.info("Order item create event received: " + event);
        if (event.getOrderId() != null && event.getOrderItem() != null) {
            CoreMap map = new CoreMap();
            switch (event.getEventType()) {
                case _State.ORDER_ITEM_CREATE_OMTK_TENDER_EVENT: {
                    map = event.getOrderItem().getParent().getMap();
                    map.put("eventType", event.getEventType());
                    OrderResponse response = new OrderResponse(map.getInstance());
                    response.add(new OrderResponse(getDetails(event.getOrderItem())));

                    for (String userName : event.getUserNames()) {
                        if (userName != null || userName != "") {
                            messagingTemplate.convertAndSendToUser(
                                    userName, "/order/reply", response);
                        }
                    }
                }
                break;
                default: {
                    map = event.getOrderItem().getMap();
                    details(map, event.getOrderItem());
                    map.add("statusColour", stateService.getColour(event.getOrderItem().getState()));
                    map.put("orderId", "" + event.getOrderId());
                    map.put("eventType", event.getEventType());

                    for (String userName : event.getUserNames()) {
                        if (userName != null || userName != "") {
                            messagingTemplate.convertAndSendToUser(
                                    userName, "/order/reply", map.getInstance());
                        }
                    }
                }
            }
        }
    }

    @EventListener
    public void updateCountHandle(OrderItemUpdateCountEvent event) {
        LOGGER.info("Order item update count event received: " + event);
        if (event.getOrderId() != null && event.getOrderItemId() != null && event.getCount() != null && !event.getUserNames().isEmpty()) {
            CoreMap map = new CoreMap();
            map.add("orderId", event.getOrderId());
            map.add("orderItemId", event.getOrderItemId());
            map.add("count", "" + event.getCount());
            map.add("eventType", _State.ORDER_ITEM_UPDATE_COUNT_EVENT);

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/order/reply", map.getInstance());
                }
            }
        }
    }

    @EventListener
    public void deleteHandle(OrderItemDeleteEvent event) {
        LOGGER.info("Order item delete event received: " + event);
        if (event.getOrderId() != null && event.getOrderItemId() != null && !event.getUserNames().isEmpty()) {
            CoreMap map = new CoreMap();
            map.add("orderId", event.getOrderId());
            map.add("orderItemId", event.getOrderItemId());
            map.add("eventType", _State.ORDER_ITEM_DELETE_EVENT);

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/order/reply", map.getInstance());
                }
            }
        }
    }

    @EventListener
    public void changeStatusHandle(OrderItemUpdateStatusEvent event) {
        LOGGER.info("Order item delete event received: " + event);
        if (event.getOrderId() != null && event.getOrderItem() != null && !event.getUserNames().isEmpty()) {
            CoreMap map = new CoreMap();
            map.add("orderId", event.getOrderId());
            map.add("orderItemId", event.getOrderItem().getId());
            map.add("state", event.getStatus());
            map.add("statusColour", stateService.getColour(event.getOrderItem().getState()));
            if (_State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT.equals(event.getEventType())) {
                map.add("rejectionReason", event.getOrderItem().getRejectionReason());
                if (event.getOrderItem().getRejectResource() != null) {
                    map.add("rejectionFileLink", AttachmentUtils.getLink(event.getOrderItem().getRejectResource().getName()));
                    map.add("rejectionFileName", event.getOrderItem().getRejectResource().getOriginalName());
                }
            }
            map.add("eventType", event.getEventType());

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/order/reply", map.getInstance());
                }
            }
        }
    }

    private void details(CoreMap map, _OrderItem orderItem) {
        if (orderItem.getProduct() != null) {
            map.add("productName", orderItem.getProduct().getName());
            if (orderItem.getProduct().getAttr() != null)
                map.put("productAttrs", orderItem.getProduct().getAttr());
        }
        if (orderItem.getProductGroup() != null)
            map.add("productGroupName", orderItem.getProductGroup().getName());
        if (orderItem.getProductType() != null)
            map.add("productTypeName", orderItem.getProductType().getName());
        if (orderItem.getUnitType() != null) {
            map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", orderItem.getUnitType().getNameCyrl());
        }
//        if (orderItem.getAttachment() != null) {
//            map.add("attachmentLink", AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//            map.add("attachmentName", orderItem.getAttachment().getOriginalName());
//        }
        if (orderItem.getRejectResource() != null) {
            map.add("rejectionFileLink", AttachmentUtils.getLink(orderItem.getRejectResource().getName()));
            map.add("rejectionFileName", orderItem.getRejectResource().getOriginalName());
        }
    }

    private ItemResponse getDetails(_OrderItem orderItem) {
        ItemResponse response = new ItemResponse();
        CoreMap map = orderItem.getMap();
        stateService.wrapStatus(map, orderItem.getState());
        if (orderItem.getUnitType() != null) {
            map.add("unitTypeId", orderItem.getUnitType().getId());
            map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", orderItem.getUnitType().getNameCyrl());
        }
        if (orderItem.getParent() != null)
            map.add("order_id", orderItem.getParent().getId().toString());
        if (orderItem.getProduct() != null) {
            map.add("productId", orderItem.getProduct().getId());
            map.add("productName", orderItem.getProduct().getName());
            map.addDouble("hasProductCount", orderItem.getProduct().getCount());
            if (orderItem.getProduct().getAttr() != null)
                response.getListMaps().put("productAttrs", orderItem.getProduct().getAttr());
        }
        if (orderItem.getProductGroup() != null) {
            map.add("productGroupName", orderItem.getProductGroup().getName());
            map.add("productGroupId", orderItem.getProductGroup().getId().toString());
        }
        if (orderItem.getProductType() != null) {
            map.add("productTypeName", orderItem.getProductType().getName());
            map.add("productTypeId", orderItem.getProductType().getId().toString());
        }
        if (orderItem.getContractItem() != null) {
            if (orderItem.getContractItem().getParent() != null) {
                map.add("contractNumber", orderItem.getContractItem().getParent().getCode());
            } else {
                map.add("contractNumber", "");
            }
        } else {
            map.add("contractNumber", "");
        }
//        if (orderItem.getAttachment() != null) {
//            map.add("attachmentLink", AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//            map.add("attachmentName", orderItem.getAttachment().getOriginalName());
//        }
        if (orderItem.getRejectResource() != null) {
            map.add("rejectionFileLink", AttachmentUtils.getLink(orderItem.getRejectResource().getName()));
            map.add("rejectionFileName", orderItem.getRejectResource().getOriginalName());
        }
        response.setMaps(map.getInstance());
        return response;
    }
}
