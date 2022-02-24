package uz.uzcard.genesis.controller.order;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.config.properties.ProjectConfig;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.order.OrderFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderRequest;
import uz.uzcard.genesis.dto.api.resp.ItemResponse;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.OrderResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.OrderItemDao;
import uz.uzcard.genesis.hibernate.entity._Order;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.hibernate.enums.TableType;
import uz.uzcard.genesis.service.ColumnSettingsService;
import uz.uzcard.genesis.service.OrderService;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Order controller", description = "Buyurtmalar")
@RestController
@RequestMapping(value = "/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StateService stateService;
    @Autowired
    private ColumnSettingsService columnSettingsService;
    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ProjectConfig projectConfig;

    /*
    Подразделения, OMTK. OZL, Default
     */
    @ApiOperation(value = "Get All orders for Department")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-department", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).DEPARTMENT_ORDER_READ)")
    public ListResponse listDepartment(OrderFilterRequest request) {
        request.setOrdertype(OrderClassification.DEPARTMENT);
        return list(request);
    }

    @ApiOperation(value = "Get All orders for OZL")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-ozl", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).OZL_ORDER_READ)")
    public ListResponse listOzl(OrderFilterRequest request) {
        request.setOrdertype(OrderClassification.OZL);
        return list(request);
    }

    @ApiOperation(value = "Get All orders for OMTK")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-omtk", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).OMTK_ORDER_READ)")
    public ListResponse listOMTK(OrderFilterRequest request) {
        request.setOrdertype(OrderClassification.OMTK);
        return list(request);
    }

    @ApiOperation(value = "Add new order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_CREATE)")
    public SingleResponse add(OrderRequest request, @RequestPart(value = "file") MultipartFile file) {

        /**
         *  ORDER ATTACHMENT REQUIRED  CONFIGURED
         */
        if (projectConfig.orderAttachmentRequired() && ServerUtils.isEmpty(file)) {
            throw new RpcException("FILE_REQUIRED");
        }

        return SingleResponse.of(orderService.add(request, Collections.singletonList(file)), (order, map) -> {
            stateService.wrapStatus(map, order.getState());
            return map;
        });
    }

    @Transactional
    @ApiOperation(value = "Delete order")
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_DELETE)")
    public SingleResponse delete(Long id) {
        orderService.delete(id);
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Send order")
    @Transactional
    @PostMapping(value = "/send-order", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, T(uz.uzcard.genesis.hibernate.enums.Permissions).ORDER_SEND_TO_OMTK)")
    public SingleResponse sendToOMTK(@RequestBody Long orderId) {
        return orderService.send(orderId);
    }

    private ListResponse list(OrderFilterRequest request) {
        PageStream<_Order> pageStream = orderService.list(request);
        List<String> blackList = columnSettingsService.blackList(TableType._Order.name());
        return ListResponse.of(pageStream.stream().map(order -> {
            CoreMap map = order.getMap();
            stateService.wrapStatus(map, order.getState());
            blackList.forEach(map::remove);

            getAttachment(order, map);

            if (OrderClassification.DEPARTMENT.equals(request.getOrdertype())) {
                if (order.getAuditInfo().getCreatedByUser().getId().equals(SessionUtils.getInstance().getUserId()))
                    map.add("owner", "true");
                else
                    map.add("owner", "false");
            }

            if (order.getAuditInfo() != null) {
                if (order.getAuditInfo().getCreatedByUser() != null) {
                    map.add("initiator", order.getAuditInfo().getCreatedByUser().getFirstName() + " " + order.getAuditInfo().getCreatedByUser().getLastName());
                    if (order.getAuditInfo().getCreatedByUser().getDepartment() != null) {
                        map.add("department", order.getAuditInfo().getCreatedByUser().getDepartment().getNameByLanguage());
                    }
                }

                if (order.getStatusChangedUser() != null) {
                    map.add("updateUser", order.getStatusChangedUser().getShortName());
                }
            }

            OrderResponse response = new OrderResponse(map.getInstance());
            if (request.isFiltered()) {
                orderItemDao.search(new FilterParameters() {{
                    setSize(Integer.MAX_VALUE);
                    addString("order_id", "" + order.getId());
                    addString("positionState", request.getPositionState());
                    addString("groupId", "" + request.getGroupId());
                    addString("typeId", "" + request.getTypeId());
                    addString("name", request.getName());
                    addString("contractNumber", request.getContractNumber());
                    addString("statusChangedUser", "" + request.getStatusChangedUser());
                    if (request.isHasContract()) {
                        addString("isHasContract", "" + request.isHasContract());
                    }
                    if (OrderClassification.OZL.equals(request.getOrdertype())) {
                        addBoolean("fromOzl", true);
                    }
                }}).stream().forEach(orderItem -> {
                    response.add(new OrderResponse(getDetails(orderItem, request.getOrdertype())));
                });
            }
            return response;
        }).collect(Collectors.toList()), pageStream.getSize());
    }

    private ItemResponse getDetails(_OrderItem orderItem, OrderClassification ordertype) {
        ItemResponse response = new ItemResponse();
        CoreMap map = orderItem.getMap();
        stateService.wrapStatus(map, orderItem.getState());
        getAllResourceFile(orderItem, map);
        if (orderItem.getUnitType() != null) {
            map.add("unitTypeId", orderItem.getUnitType().getId());
            map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
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
            if (orderItem.getContractItem().getParent() != null) {
                map.add("contractLink", AttachmentUtils.getLink(orderItem.getContractItem().getParent().getProductResource().getName()));
                map.add("contractFileName", orderItem.getContractItem().getParent().getProductResource().getOriginalName());
            }
        } else {
            map.add("contractNumber", "");
        }
        if (_State.OZL_TO_DEPARTMENT.equals(orderItem.getState())) {
            if (orderItem.getOfferProduct() != null) {
                map.add("offerProductId", orderItem.getOfferProduct().getId());
                map.add("offerProductName", orderItem.getOfferProduct().getName());
            }
            if (orderItem.getOfferUnitType() != null) {
                map.add("offerUnitTypeId", orderItem.getOfferUnitType().getId());
                map.add("offer_unit_type_name_en", orderItem.getOfferUnitType().getNameEn());
                map.add("offer_unit_type_name_ru", orderItem.getOfferUnitType().getNameRu());
                map.add("offer_unit_type_name_uz", orderItem.getOfferUnitType().getNameUz());
            }
            if (orderItem.getOfferCount() != null)
                map.add("offerCount", "" + orderItem.getOfferCount());
        }
        response.setMaps(map.getInstance());
        return response;
    }

    private void getAttachment(_Order order, CoreMap map) {
        /*if (order.getAttachment() != null) {
            map.add("attachmentLink", AttachmentUtils.getLink(order.getAttachment().getName()));
            map.add("attachmentFileName", order.getAttachment().getOriginalName());
        }*/
    }

    private CoreMap getAllResourceFile(_OrderItem orderItem, CoreMap map) {
        if (orderItem.getRejectResource() != null) {
            map.add("rejectionFileLink", AttachmentUtils.getLink(orderItem.getRejectResource().getName()));
            map.add("rejectionFileName", orderItem.getRejectResource().getOriginalName());
        }
        return map;
    }
}