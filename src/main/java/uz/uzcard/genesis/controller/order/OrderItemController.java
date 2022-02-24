package uz.uzcard.genesis.controller.order;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.order.OrderItemOfferRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemStateChangeRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemsRequest;
import uz.uzcard.genesis.dto.api.req.setting.OpportunityRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.service.OrderItemsService;
import uz.uzcard.genesis.service.StateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Api(value = "Order Item controller")
@RestController
@RequestMapping(value = "/api/orders")
public class OrderItemController {

    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private StateService stateService;

    @ApiOperation(value = "Add new Item to the order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @PostMapping(value = "/add-item", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_CREATE)")
    public SingleResponse addOrderItem(OrderItemRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return SingleResponse.of(orderItemsService.save(request), (orderItem, map) -> {
            stateService.wrapStatus(map, orderItem.getState());

            if (orderItem.getProduct() != null)
                map.add("productName", orderItem.getProduct().getName());
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
//            if (orderItem.getAttachment() != null) {
//                map.add("attachmentLink", AttachmentUtils.getLink(orderItem.getAttachment().getName()));
//                map.add("attachmentName", orderItem.getAttachment().getOriginalName());
//            }
            return map;
        });
    }

    @ApiOperation(value = "Update order item count")
    @Transactional
    @PostMapping(value = "/update-item/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_UPDATE_COUNT)")
    public SingleResponse updateOrderItemCount(@RequestBody ItemCountRequest request) {
        return SingleResponse.of(orderItemsService.updateItemCount(request), (orderItem, map) -> {
            stateService.wrapStatus(map, orderItem.getState());
            return map;
        });
    }

    @ApiOperation(value = "Get order items for Department", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items-department", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).DEPARTMENT_ORDER_ITEM_READ)")
    public ListResponse itemsForDepartment(ItemsRequest request) {
        request.setType(OrderClassification.DEPARTMENT);
        return orderItemsService.listByOrder(request);
    }

    @ApiOperation(value = "Get order items for OMTK", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items-omtk", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).OMTK_ORDER_ITEM_READ)")
    public ListResponse itemsForOMTK(ItemsRequest request) {
        request.setType(OrderClassification.OMTK);
        return orderItemsService.listByOrder(request);
    }

    @ApiOperation(value = "Get order item single for OMTK", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/item-single-omtk/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).OMTK_ORDER_ITEM_READ)")
    public SingleResponse itemForOmtk(@PathVariable Long id) {
        return orderItemsService.getSingle(id);
    }

    @ApiOperation(value = "Get order items for OZL", response = ListResponse.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/items-ozl", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).OZL_ORDER_ITEM_READ)")
    public ListResponse itemsForOzl(ItemsRequest request) {
        request.setType(OrderClassification.OZL);
        return orderItemsService.listByOrder(request);
    }

    @ApiOperation(value = "Get order item", response = Map.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-item", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM)")
    public SingleResponse getItem(Long order_item_id) {
        CoreMap data = orderItemsService.get(order_item_id);
        if (data == null) return SingleResponse.empty();
        data.remove("hashESign");
        return SingleResponse.of(data.getInstance());
    }

    @ApiOperation(value = "Get order item", response = Map.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-one", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM)")
    public SingleResponse getOneItem(Long orderId, Long productId) {
        CoreMap data = orderItemsService.getOneItem(orderId, productId);
        if (data == null) return SingleResponse.empty();
        data.remove("hashESign");
        return SingleResponse.of(data.getInstance());
    }

    @Transactional
    @ApiOperation(value = "Delete order item")
    @DeleteMapping(value = "/item-delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_DELETE)")
    public SingleResponse itemDelete(Long order_item_id) {
        orderItemsService.delete(order_item_id);
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Order Item given product")
    @Transactional
    @PostMapping(value = "/item/opportunity", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_ITEM_OPPORTUNITY)")
    public SingleResponse opportunity(OpportunityRequest request, @RequestPart(value = "file", required = false) MultipartFile file,
                                      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws UnsupportedEncodingException {
        servletRequest.setCharacterEncoding("UTF-8");
        servletResponse.setCharacterEncoding("UTF-8");
        try {
            orderItemsService.opportunity(request, file);
        } catch (Exception e) {
            throw new MultipartException(e.getMessage());
        }
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Order item status changed user items")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/statusChangedUsers", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse opportunity(String name) {
        return ListResponse.of(orderItemsService.getStatusChangedUsers(name));
    }

    @Transactional
    @ApiOperation(value = "Check Hash ESign")
    @PostMapping(value = "/check-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(Long id) {
        return orderItemsService.checkEDS(id);
    }

    @Transactional
    @ApiOperation(value = "Set Hash ESign")
    @PostMapping(value = "/set-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(@RequestBody HashESignRequest request) {
        return orderItemsService.setHashESign(request);
    }

    @Transactional
    @ApiOperation(value = "Ozl offer params for Department")
    @PostMapping(value = "/ozl-offer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse ozlOffer(OrderItemOfferRequest request, @RequestPart(value = "file", required = true) MultipartFile file) {
        return orderItemsService.ozlOffer(request, file);
    }

    @Transactional
    @ApiOperation(value = "Department update offer data")
    @PostMapping(value = "/update-ozl-offer", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse updateOzlOffer(@RequestBody OrderItemOfferRequest request) {
        orderItemsService.updateOzlOffer(request);
        return SingleResponse.of(true);
    }

    @Transactional
    @ApiOperation(value = "Update Taken Away Count")
    @PostMapping(value = "/update-taken-away")
    public SingleResponse updateTakenAwayCount() {
        orderItemsService.updateTakenAwayCount();
        return SingleResponse.of(true);
    }
}
