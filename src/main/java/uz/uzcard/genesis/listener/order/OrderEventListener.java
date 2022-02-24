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
import uz.uzcard.genesis.dto.event.order.OrderCreateEvent;
import uz.uzcard.genesis.dto.event.order.OrderSendDepartmentEvent;
import uz.uzcard.genesis.dto.event.order.OrderSendOmtkEvent;
import uz.uzcard.genesis.hibernate.entity._Order;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 'Madaminov Javohir' on 20.10.2020
 */
@Component
public class OrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private StateService stateService;

    @EventListener
    public void createHandle(OrderCreateEvent event) {
        LOGGER.info("Order create event received: " + event);
        if (event.getOrder() != null) {

            _Order order = event.getOrder();
            CoreMap map = order.getMap();
            stateService.wrapStatus(map, order.getState());
            OrderResponse response = new OrderResponse(map.getInstance());
            response.put("owner", "false");

            if (order.getAuditInfo() != null) {
                if (order.getAuditInfo().getCreatedByUser() != null) {
                    response.put("initiator", order.getAuditInfo().getCreatedByUser().getFirstName() + " " + order.getAuditInfo().getCreatedByUser().getLastName());
                    if (order.getAuditInfo().getCreatedByUser().getDepartment() != null) {
                        response.put("department", order.getAuditInfo().getCreatedByUser().getDepartment().getNameByLanguage());
                    }
                }

                if (order.getStatusChangedUser() != null) {
                    response.put("updateUser", order.getStatusChangedUser().getShortName());
                }
            }

            order.getItems().forEach(orderItem -> {
                response.add(new OrderResponse(getDetails(orderItem)));
            });
            response.put("eventType", _State.ORDER_CREATE_EVENT);

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/order/reply", response);
                }
            }
        }
    }

    @EventListener
    public void sendHandleDepartment(OrderSendDepartmentEvent departmentEvent) {
        LOGGER.info("Order send event received: " + departmentEvent);

        _Order order = departmentEvent.getOrder();
        CoreMap map = order.getMap();
        stateService.wrapStatus(map, order.getState());

        OrderResponse response = new OrderResponse(map.getInstance());
        List<HashMap<String, String>> orderItems = order.getItems().stream().map(orderItem -> {
            HashMap<String, String> map1 = new HashMap<>();
            map1.put("id", "" + orderItem.getId());
            map1.put("state", orderItem.getState());
            return map1;
        }).collect(Collectors.toList());

        response.put("items", orderItems);
        response.put("eventType", _State.ORDER_SEND_EVENT);

        // send to Department users
        for (String userName : departmentEvent.getUserNameDepartment()) {
            if (userName != null || userName != "") {
                messagingTemplate.convertAndSendToUser(
                        userName, "/order/reply", response);
            }
        }
    }

    @EventListener
    public void sendHandleOMTK(OrderSendOmtkEvent omtkEvent) {
        LOGGER.info("Order send event received: " + omtkEvent);

        _Order order = omtkEvent.getOrder();
        CoreMap map = order.getMap();
        stateService.wrapStatus(map, order.getState());
        OrderResponse response = new OrderResponse(map.getInstance());

        order.getItems().forEach(orderItem -> {
            response.add(new OrderResponse(getDetails(orderItem)));
        });
        response.put("eventType", _State.ORDER_SEND_EVENT);

        // send to OMTK users
        for (String userName : omtkEvent.getUserNameOmtk()) {
            if (userName != null || userName != "") {
                messagingTemplate.convertAndSendToUser(
                        userName, "/order/reply", response);
            }
        }
    }

    private ItemResponse getDetails(_OrderItem orderItem) {
        ItemResponse response = new ItemResponse();
        CoreMap map = orderItem.getMap();
        getAllResourceFile(orderItem, map);
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
        response.setMaps(map.getInstance());
        return response;
    }

    private CoreMap getAllResourceFile(_OrderItem orderItem, CoreMap map) {
        if (orderItem.getRejectResource() != null) {
            map.add("rejectionFileLink", AttachmentUtils.getLink(orderItem.getRejectResource().getName()));
            map.add("rejectionFileName", orderItem.getRejectResource().getOriginalName());
        }
//        if (orderItem.getAttachment() != null) {
//            map.add("attachmentLink", AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//            map.add("attachmentFileName", orderItem.getAttachment().getOriginalName());
//        }
        return map;
    }
}
