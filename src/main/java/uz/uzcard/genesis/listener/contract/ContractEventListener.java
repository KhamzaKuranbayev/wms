package uz.uzcard.genesis.listener.contract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.resp.ItemResponse;
import uz.uzcard.genesis.dto.api.resp.OrderResponse;
import uz.uzcard.genesis.dto.event.contract.ContractChangeStatusEvent;
import uz.uzcard.genesis.dto.event.contract.ContractCreateEvent;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.listener.order.OrderEventListener;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.AttachmentUtils;

import java.util.stream.Collectors;

/**
 * Created by 'Madaminov Javohir' on 02.11.2020
 */
@Component
public class ContractEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private StateService stateService;

    @EventListener
    public void createHandle(ContractCreateEvent event) {
        LOGGER.info("Contract create or update event received: " + event);
        if (event.getContract() != null && event.getUserNames() != null && !event.getUserNames().isEmpty()) {

            CoreMap map = event.getContract().getMap();
            _Contract contract = event.getContract();

            if (contract.getProductResource() != null) {
                map.add("productResourceLink", AttachmentUtils.getLink(contract.getProductResource().getName()));
                map.add("productResourceName", contract.getProductResource().getOriginalName());
            }
            if (contract.getRejectResource() != null) {
                map.add("rejectResourceLink", AttachmentUtils.getLink(contract.getRejectResource().getName()));
                map.add("rejectResourceName", contract.getRejectResource().getOriginalName());
            }

            if (contract.getSupplier() != null) {
                map.put("supplierId", contract.getSupplier().getId().toString());
                map.put("supplierName", contract.getSupplier().getName());
            }
            if (contract.getSupplyType() != null)
                map.put("supplyType", contract.getSupplyType().name());

            stateService.wrapStatus(map, contract.getState());

            OrderResponse response = new OrderResponse(map.getInstance());
            response.put("evetType", event.getEventType());
            contract.getItems().forEach(contractItem -> {
                response.add(new OrderResponse(getDetails(contractItem)));
            });

            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/contract/reply", response);
                }
            }
        }
    }

    @EventListener
    public void changeStatusHandle(ContractChangeStatusEvent event) {
        LOGGER.info("Contract update status event received: " + event);
        if (event.getContract() != null && event.getUserNames() != null && !event.getUserNames().isEmpty()) {
            CoreMap map = event.getContract().getMap();
            stateService.wrapStatus(map, event.getContract().getState());
            OrderResponse response = new OrderResponse(map.getInstance());
            if (event.getContract().getRejectResource() != null) {
                response.put("rejectResourceLink", AttachmentUtils.getLink(event.getContract().getRejectResource().getName()));
                response.put("rejectResourceName", event.getContract().getRejectResource().getOriginalName());
            }
            response.put("eventType", event.getEventType());

            event.getContract().getItems().forEach(contractItem -> {
                response.add(new OrderResponse(getDetails(contractItem)));
            });
            for (String userName : event.getUserNames()) {
                if (userName != null || userName != "") {
                    messagingTemplate.convertAndSendToUser(
                            userName, "/contract/reply", response);
                }
            }
        }
    }


    private ItemResponse getDetails(_ContractItem contractItem) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setMaps(contractItem.getMap().getInstance());

        if (contractItem.getUnitType() != null) {
            itemResponse.getMaps().put("unitTypeId", contractItem.getUnitType().getId().toString());
            itemResponse.getMaps().put("unit_type_name_en", contractItem.getUnitType().getNameEn());
            itemResponse.getMaps().put("unit_type_name_ru", contractItem.getUnitType().getNameRu());
            itemResponse.getMaps().put("unit_type_name_uz", contractItem.getUnitType().getNameUz());
            itemResponse.getMaps().put("unit_type_name_cyrl", contractItem.getUnitType().getNameCyrl());
        }
        if (contractItem.getParent() != null)
            itemResponse.getMaps().put("contractId", contractItem.getParent().getId().toString());
        if (contractItem.getProduct() != null) {
            itemResponse.getMaps().put("productId", contractItem.getProduct().getId().toString());
            itemResponse.getMaps().put("productName", contractItem.getProduct().getName());
            if (contractItem.getProduct().getAttr() != null)
                itemResponse.getListMaps().put("productAttrs", contractItem.getProduct().getAttr());
        }
        if (contractItem.getProductGroup() != null) {
            itemResponse.getMaps().put("productGroupId", contractItem.getProductGroup().getId().toString());
            itemResponse.getMaps().put("productGroupName", contractItem.getProductGroup().getName());
        }
        if (contractItem.getProductType() != null) {
            itemResponse.getMaps().put("productTypeId", contractItem.getProductType().getId().toString());
            itemResponse.getMaps().put("productTypeName", contractItem.getProductType().getName());
        }
        if (contractItem.getAuditInfo() != null) {
            if (contractItem.getAuditInfo().getUpdatedByUser() != null)
                itemResponse.getMaps().put("updateUser", contractItem.getAuditInfo().getUpdatedByUser().getShortName());
            else
                itemResponse.getMaps().put("updateUser", contractItem.getAuditInfo().getCreatedByUser().getShortName());
        }
        if (!contractItem.getOrderItems().isEmpty()) {
            itemResponse.getMaps().put("orderNumb",
                    contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getParent().getNumb()).collect(Collectors.joining(", "))
            );
            itemResponse.getMaps().put("orderItemNumb",
                    contractItem.getOrderItems().stream().map(orderItem -> "" + orderItem.getItemNumb()).collect(Collectors.joining(", "))
            );

//            List<String> orderResources = new ArrayList<>();
//            contractItem.getOrderItems().forEach(orderItem -> {
//                if (orderItem.getAttachment() != null) {
//                    orderResources.add(AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//                }
//            });
//            itemResponse.getListMaps().put("orderResources", orderResources);
        }
        return itemResponse;
    }
}
