package uz.uzcard.genesis.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.order.*;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemsRequest;
import uz.uzcard.genesis.dto.api.req.setting.OpportunityRequest;
import uz.uzcard.genesis.dto.api.resp.ItemResponse;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.OrderResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.event.order.item.OrderItemCreateEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemDeleteEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemUpdateCountEvent;
import uz.uzcard.genesis.dto.event.order.item.OrderItemUpdateStatusEvent;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.*;

import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderItemsServiceImpl implements OrderItemsService {
    private final ObjectMapper objectMapper;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private StateService stateService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ProductService productService;
    @Autowired
    private UnitTypeDao unitTypeDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private GivenProductsService givenProductsService;

    @Autowired
    public OrderItemsServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
    }

    @Override
    public _OrderItem save(OrderItemRequest request) {
        _Order order = orderDao.get(request.getOrder_id());
        if (order == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_NOT_FOUND"));
        _OrderItem orderItem = orderItemDao.get(request.getId());
        if (orderItem == null) {
            orderItem = new _OrderItem();
            orderItem.setParent(order);
            Integer orderItemNumb = orderItemDao.findOrderItemMaxNumb(order.getId());
            orderItem.setItemNumb(1 + (orderItemNumb == null ? 0 : orderItemNumb));
            order.getItems().add(orderItem);
        }
        _Product product = productDao.get(request.getProduct_id());
        if (product == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_REQUIRED"));
        orderItem.setProduct(product);
        orderItem.setProductGroup(product.getGroup());
        orderItem.setProductType(product.getType());
        orderItem.setCount(request.getCount());
        if (request.getUnitTypeId() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("UNITTYPE_REQUIRED"));
        _UnitType unitType = unitTypeDao.get(request.getUnitTypeId());
        if (orderItem.getUnitType() != null && !orderItem.getUnitType().equals(unitType)) {
            throw new ValidatorException(GlobalizationExtentions.localication("UNIT_TYPE_DOES_NOT_MATCH"));
        }
        orderItem.setUnitType(unitTypeDao.get(request.getUnitTypeId()));
        if (request.getTimeToBeEntered() != null)
            orderItem.setTimeToBeEntered(request.getTimeToBeEntered());
        else
            orderItem.setTimeToBeEntered(new Date());

        if (orderItem.getDepartment() == null) {
            _User user = SessionUtils.getInstance().getUser();
            if (user == null)
                throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
            if (user.getDepartment() == null)
                throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
            orderItem.setDepartment(user.getDepartment());
        }

        orderItem = orderItemDao.save(orderItem);
        List<String> userNameDepartment = socketSendUsers(null);

        final OrderItemCreateEvent event = new OrderItemCreateEvent(order.getId(), orderItem, userNameDepartment, request.getId() == null ? _State.ORDER_ITEM_CREATE_EVENT : _State.ORDER_ITEM_UPDATE_EVENT);
        eventPublisher.publishEvent(event);

        orderItemDao.reindex(Arrays.asList(orderItem.getId()));
        orderDao.save(order);
        return orderItem;
    }

    @Override
    public _OrderItem updateItemCount(ItemCountRequest request) {
        if (ServerUtils.isEmpty(request.getItemId())) {
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_REQUIRED"));
        }
        _OrderItem orderItem = orderItemDao.get(request.getItemId());
        if (orderItem == null)
            throw new ValidatorException("Заявка топилмади");
        if (!ServerUtils.isEmpty(request.getCount())) {
            orderItem.setCount(request.getCount());
        }
        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());

        List<String> userNameDepartment = socketSendUsers(null);
        final OrderItemUpdateCountEvent event = new OrderItemUpdateCountEvent(orderItem.getParent().getId(), orderItem.getId(), request.getCount(), userNameDepartment);
        eventPublisher.publishEvent(event);

        return orderItem;
    }

    @Override
    public ListResponse listByOrder(ItemsRequest request) {
        if (request.getType() == null)
            throw new ValidatorException("Сўров турини киритинг");
        switch (request.getType()) {
            case DEPARTMENT: {
                return forDepartment(request);
            }
            case OMTK:
            case OZL: {
                return forOmtkOrOzl(request);
            }
        }
        return null;
    }

    @Override
    public CoreMap get(Long id) {
        return orderItemDao.getMap(id, (orderItem, map) -> {
            if (orderItem.getUnitType() != null) {
                map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
                map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
                map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
                map.add("unit_type_name_cyrl", orderItem.getUnitType().getNameCyrl());
            }
            stateService.wrapStatus(map, orderItem.getState());
            return map;
        });
    }

    @Override
    public void delete(Long order_item_id) {
        if (ServerUtils.isEmpty(order_item_id)) {
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_REQUIRED"));
        }
        _OrderItem orderItem = orderItemDao.get(order_item_id);
        if (ServerUtils.isEmpty(orderItem)) {
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        }
        orderItemDao.delete(orderItem);
        _Order order = orderItem.getParent();
        order.getItems().remove(orderItem);
        orderDao.save(order);
        if (order.getItems().size() < 1) {
            orderDao.delete(orderItem.getParent());
        }
        refreshOrder(orderItem);

        List<String> userNameDepartment = socketSendUsers(null);
        final OrderItemDeleteEvent event = new OrderItemDeleteEvent(order.getId(), orderItem.getId(), userNameDepartment);
        eventPublisher.publishEvent(event);

    }

    @Override
    public void changeStatus(_OrderItem orderItem, String state) {
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        if (!StateMap.match(orderItem.getState(), state)) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("CONTRACT_ITEM_CHANGE_STATUS"),
                    GlobalizationExtentions.localication(orderItem.getState()),
                    GlobalizationExtentions.localication(state)));
        }
        orderItem.setState(state);
        orderItem.setStatusChangedUser(SessionUtils.getInstance().getUser());
        orderItemDao.save(orderItem);
        refreshOrder(orderItem);
    }

    @Override
    public SingleResponse accept(Long orderId) {
        _OrderItem orderItem = orderItemDao.get(orderId);
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));


//        orderItem.setCount(value);
        changeStatus(orderItem, _State.YES_PRODUCT);
        _Product product = productDao.get(orderItem.getProduct().getId());
        if (product == null) {
            throw new ValidatorException(GlobalizationExtentions.localication("PRODUCT_NOT_FOUND"));
        }
        if (product.getCount() < orderItem.getCount()) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("AMOUNT_ENTERED_MORE_THEN_AMOUNT_LEFT_IN_WAREHOUSE"), orderItem.getCount()));
        }

//        product.setRemains(product.getRemains() - orderItem.getCount());
//        productDao.save(product);

        // socket message send to departments
        sendDepartment(orderItem, _State.READY_TO_PRODUCE, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

        // socket message send to omtks
        sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.READY_TO_PRODUCE, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

        // socket message send to ozl
        sendSocketMessageOZL(orderItem.getParent().getId(), orderItem, _State.READY_TO_PRODUCE, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

        return SingleResponse.of(orderItem, (orderItem1, map) -> {
            stateService.wrapStatus(map, orderItem1.getState());
            return map;
        });
    }

    @Override
    public _OrderItem specification(Long id) {
        _OrderItem orderItem = orderItemDao.get(id);
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        orderItem.setItemSendDate(new Date());
        changeStatus(orderItem, _State.PAPER_EXPECTED_SPECIFICATION);
        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());

        // socket message send to departments
        sendDepartment(orderItem, _State.PAPER_EXPECTED_SPECIFICATION, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

        // socket message send to omtks
        sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.PAPER_EXPECTED_SPECIFICATION, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

        // socket message send to ozl
        _Department ozl = departmentDao.getOneByType(OrderClassification.OZL);
        List<String> userNameOzl = socketSendUsers(ozl.getId());

        final OrderItemCreateEvent eventOzl = new OrderItemCreateEvent(orderItem.getParent().getId(), orderItem, userNameOzl, _State.ORDER_ITEM_CREATE_OMTK_TENDER_EVENT);
        eventPublisher.publishEvent(eventOzl);

        return orderItem;
    }

    @Override
    public boolean rejectOzl(OrderItemsRejectRequest request, MultipartFile file) {
        if (!request.getItemIds().isEmpty()) {
            if (request.getReasen() == null || request.getReasen() == "")
                throw new ValidatorException(GlobalizationExtentions.localication("REJECT_REASON_REQUIRED"));
            _AttachmentView attachment = null;
            if (!ServerUtils.isEmpty(file))
                attachment = attachmentService.uploadPdf(file);
            for (Long id : request.getItemIds()) {
                _OrderItem orderItem = orderItemDao.get(id);

                if (orderItem == null)
                    throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));

                orderItem.setRejectionReason(request.getReasen());
                if (attachment != null) {
                    orderItem.setRejectResource(attachment);
                }
                orderItem.setItemConclusionDate(orderItem.getPendingPurchaseDate());
                changeStatus(orderItem, _State.APPLICATION_REJECTED_OZL);
                orderItemDao.save(orderItem);
                orderDao.save(orderItem.getParent());

                // socket message send to departments
                sendDepartment(orderItem, _State.APPLICATION_REJECTED_OZL, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

                // socket message send to omtks
                sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.APPLICATION_REJECTED_OZL, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

                // socket message send to ozl
                sendSocketMessageOZL(orderItem.getParent().getId(), orderItem, _State.APPLICATION_REJECTED_OZL, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);
            }
            return true;
        } else {
            throw new RpcException(GlobalizationExtentions.localication("ORDER_ITEM_REQUIRED"));
        }
    }

    @Override
    public _OrderItem tender(Long orderItemId) {
        _OrderItem orderItem = orderItemDao.get(orderItemId);
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        orderItem.setPendingPurchaseDate(new Date());
        changeStatus(orderItem, _State.PENDING_PURCHASE);
        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());

        // socket message send to departments
        sendDepartment(orderItem, _State.PENDING_PURCHASE, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

        // socket message send to omtks
        sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.PENDING_PURCHASE, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

        // socket message send to ozl
        sendSocketMessageOZL(orderItem.getParent().getId(), orderItem, _State.PENDING_PURCHASE, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);
        return orderItem;
    }

    @Override
    public _OrderItem reject(OrderItemRejectRequest request, MultipartFile file) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        if (request.getReasen() == null || request.getReasen() == "") {
            throw new ValidatorException(GlobalizationExtentions.localication("REJECT_REASON_REQUIRED"));
        }
        orderItem.setRejectionReason(request.getReasen());
        if (!ServerUtils.isEmpty(file)) {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            orderItem.setRejectResource(attachment);
        }
        if (orderItem.getTimeToBeEntered() != null)
            orderItem.setItemConclusionDate(orderItem.getTimeToBeEntered());
        else
            orderItem.setItemConclusionDate(new Date());
        changeStatus(orderItem, _State.REJECT_ORDER_ITEM);
        orderItemDao.save(orderItem);

        // socket message send to departments
        sendDepartment(orderItem, _State.REJECT_ORDER_ITEM, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

        // socket message send to omtks
        sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.REJECT_ORDER_ITEM, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

        orderDao.save(orderItem.getParent());
        return orderItem;
    }

    @Override
    public void opportunity(OpportunityRequest request, MultipartFile file) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (ServerUtils.isEmpty(orderItem)) {
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        }
        if (ServerUtils.isEmpty(request.getProvideProductCount()) && ServerUtils.isEmpty(request.getRejectProductCount()) && ServerUtils.isEmpty(request.getContractProductCount())) {
            throw new ValidatorException("Вы не ввели число продукта!!!");
        }

        if (orderItem.getCount() != request.getProvideProductCount() + request.getContractProductCount() + request.getRejectProductCount()) {
            throw new RpcException(String.format("Количество предоставленных продуктов не соответствует количеству запрошенных продуктов %s, %s, %s",
                    request.getProvideProductCount(), request.getRejectProductCount(), request.getContractProductCount()));
        }
        Double hasProductCount = orderItem.getProduct().getCount();

        if (!ServerUtils.isEmpty(request.getProvideProductCount())) {
            if (hasProductCount < request.getProvideProductCount()) {
                throw new RpcException(String.format("Сўралган маҳсулотдан кўп маҳсулотни %s бекор қилдингиз!!!!!!", request.getProvideProductCount()));
            }
        }

        if (request.getRejectProductCount() != null && request.getRejectProductCount() != 0) {
            if (request.getReason() == null || request.getReason() == "")
                throw new RpcException(GlobalizationExtentions.localication("REJECT_REASON_REQUIRED"));
            if (file == null)
                throw new RpcException(GlobalizationExtentions.localication("FILE_REQUIRED"));
        }

        //  Provider product
        readyToProduce(request, orderItem);

        //  Reject product
        rejectProduct(request, file, orderItem);

        // Ozl product
        tenderProduct(request, orderItem);
        refreshOrder(orderItem);
    }

    private void readyToProduce(OpportunityRequest request, _OrderItem orderItem) {
        if (!ServerUtils.isEmpty(request.getProvideProductCount()) && request.getProvideProductCount() != 0) {
            changeStatus(orderItem, _State.YES_PRODUCT);
            orderItem.setCount(Double.valueOf(request.getProvideProductCount()));
            orderItem.setItemConclusionDate(orderItem.getTimeToBeEntered());
            bron(orderItem, request.getProvideProductCount().doubleValue());

            // socket message send to departments
            sendDepartment(orderItem, _State.READY_TO_PRODUCE, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

            // socket message send to omtks
            sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.READY_TO_PRODUCE, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);
        }
    }

    private void refreshOrder(_OrderItem orderItem) {
        /*if (!orderItemDao.findOrderItemsByOrder(orderItem.getParent(), _State.YES_PRODUCT, _State.REJECT_ORDER_ITEM, _State.APPLICATION_REJECTED_OZL, _State.CONTRACT_CONCLUTION)) {
            _Order order = orderItem.getParent();
            order.setState(_State.ACCEPTED_ORDER);
            orderDao.save(order);
        }*/
        orderDao.save(orderItem.getParent());
        orderItemDao.save(orderItem);
    }

    private void tenderProduct(OpportunityRequest request, _OrderItem orderItem) {
        if (!ServerUtils.isEmpty(request.getContractProductCount()) && request.getContractProductCount() != 0) {
            if ((!ServerUtils.isEmpty(request.getProvideProductCount()) && request.getProvideProductCount() != 0) ||
                    (!ServerUtils.isEmpty(request.getRejectProductCount()) && request.getRejectProductCount() != 0)) {
                _OrderItem newOrderItem = new _OrderItem();

                BeanUtils.copyProperties(orderItem, newOrderItem, "id", "map", "bron");
                newOrderItem.setGivens(new ArrayList<>());
                newOrderItem.setCount(Double.valueOf(request.getContractProductCount()));
                newOrderItem.setStatusChangedUser(SessionUtils.getInstance().getUser());
                newOrderItem.setState(_State.PAPER_EXPECTED_SPECIFICATION);
                newOrderItem.setItemSendDate(new Date());

                Integer orderItemNumb = orderItemDao.findOrderItemMaxNumb(orderItem.getParent().getId());

                newOrderItem.setItemNumb(1 + (orderItemNumb == null ? 0 : orderItemNumb));
                orderItemDao.save(newOrderItem);
                _Order order = orderItem.getParent();
                order.getItems().add(newOrderItem);
                orderDao.save(order);

                // socket message send to departments
                createItemSendDepartment(orderItem, newOrderItem);

                // socket message send to omtk
                _Department omtk = departmentDao.getOneByType(OrderClassification.OMTK);
                List<String> userNameOmtk = socketSendUsers(omtk.getId());

                final OrderItemCreateEvent eventOmtk = new OrderItemCreateEvent(newOrderItem.getParent().getId(), newOrderItem, userNameOmtk, _State.ORDER_ITEM_CREATE_EVENT);
                eventPublisher.publishEvent(eventOmtk);

                // socket message send to ozl
                _Department ozl = departmentDao.getOneByType(OrderClassification.OZL);
                List<String> userNameOzl = socketSendUsers(ozl.getId());

                final OrderItemCreateEvent eventOzl = new OrderItemCreateEvent(newOrderItem.getParent().getId(), newOrderItem, userNameOzl, _State.ORDER_ITEM_CREATE_OMTK_TENDER_EVENT);
                eventPublisher.publishEvent(eventOzl);

            } else {
                orderItem.setItemSendDate(new Date());
                changeStatus(orderItem, _State.PAPER_EXPECTED_SPECIFICATION);

                // socket message send to departments
                sendDepartment(orderItem, _State.PAPER_EXPECTED_SPECIFICATION, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

                // socket message send to omtks
                sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.PAPER_EXPECTED_SPECIFICATION, _State.ORDER_ITEM_CHANGE_STATUS_EVENT);

                // socket message send to ozl
                _Department ozl = departmentDao.getOneByType(OrderClassification.OZL);
                List<String> userNameOzl = socketSendUsers(ozl.getId());

                final OrderItemCreateEvent eventOzl = new OrderItemCreateEvent(orderItem.getParent().getId(), orderItem, userNameOzl, _State.ORDER_ITEM_CREATE_OMTK_TENDER_EVENT);
                eventPublisher.publishEvent(eventOzl);
            }
        }
    }

    private void createItemSendDepartment(_OrderItem orderItem, _OrderItem newOrderItem) {
        Long departmentId = null;
        if (orderItem.getAuditInfo() != null) {
            if (orderItem.getAuditInfo().getCreatedByUser() != null) {
                if (orderItem.getAuditInfo().getCreatedByUser().getDepartment() != null) {
                    departmentId = orderItem.getAuditInfo().getCreatedByUser().getDepartment().getId();
                    if (departmentId != null) {
                        List<String> userNameDepartment = socketSendUsers(departmentId);
                        final OrderItemCreateEvent eventDepartment = new OrderItemCreateEvent(newOrderItem.getParent().getId(), newOrderItem, userNameDepartment, _State.ORDER_ITEM_CREATE_EVENT);
                        eventPublisher.publishEvent(eventDepartment);
                    }
                }
            }
        }
    }

    private void rejectProduct(OpportunityRequest request, MultipartFile file, _OrderItem orderItem) {
        if (!ServerUtils.isEmpty(request.getRejectProductCount()) && request.getRejectProductCount() != 0) {
            if (!ServerUtils.isEmpty(request.getProvideProductCount()) && request.getProvideProductCount() != 0) {
                _OrderItem newOrderItem = new _OrderItem();

                BeanUtils.copyProperties(orderItem, newOrderItem, "id", "map", "bron");
                newOrderItem.setGivens(new ArrayList<>());
                newOrderItem.setCount(Double.valueOf(request.getRejectProductCount()));
                newOrderItem.setStatusChangedUser(SessionUtils.getInstance().getUser());
                newOrderItem.setState(_State.REJECT_ORDER_ITEM);

                if (orderItem.getTimeToBeEntered() != null)
                    newOrderItem.setItemConclusionDate(orderItem.getTimeToBeEntered());

                Integer orderItemNumb = orderItemDao.findOrderItemMaxNumb(orderItem.getParent().getId());

                newOrderItem.setItemNumb(1 + (orderItemNumb == null ? 0 : orderItemNumb));
                newOrderItem.setRejectionReason(request.getReason());
                if (file != null) {
                    _AttachmentView attachment = attachmentService.uploadPdf(file);
                    newOrderItem.setRejectResource(attachment);
                }
                orderItemDao.save(newOrderItem);
                _Order order = newOrderItem.getParent();
                order.getItems().add(newOrderItem);
                orderDao.save(order);

                // socket message send to departments
                createItemSendDepartment(orderItem, newOrderItem);

                // socket message send to omtk
                _Department omtk = departmentDao.getOneByType(OrderClassification.OMTK);
                List<String> userNameOmtk = socketSendUsers(omtk.getId());

                final OrderItemCreateEvent eventOmtk = new OrderItemCreateEvent(newOrderItem.getParent().getId(), newOrderItem, userNameOmtk, _State.ORDER_ITEM_CREATE_EVENT);
                eventPublisher.publishEvent(eventOmtk);

            } else {
                orderItem.setCount(Double.valueOf(request.getRejectProductCount()));
                if (orderItem.getTimeToBeEntered() != null)
                    orderItem.setItemConclusionDate(orderItem.getTimeToBeEntered());
                changeStatus(orderItem, _State.REJECT_ORDER_ITEM);

                // socket message send to departments
                sendDepartment(orderItem, _State.REJECT_ORDER_ITEM, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

                // socket message send to omtks
                sendSocketMessageOMTK(orderItem.getParent().getId(), orderItem, _State.REJECT_ORDER_ITEM, _State.ORDER_ITEM_CHANGE_STATUS_REJECT_EVENT);

            }
        }
    }

    @Override
    public PageStream<_OrderItem> list(OrderFilterRequest request) {
        return orderItemDao.search(new FilterParameters() {{
            setSize(request.getLimit());
            setStart(request.getPage() * request.getLimit());
            add("orderNumber", request.getOrderNumber());
            addLong("groupId", request.getGroupId());
            addLong("typeId", request.getTypeId());
            addLong("deparatmentId", request.getDepartmentId());
            add("name", request.getName());
            addDate("fromDate", request.getFromDate());
            addDate("toDate", request.getToDate());
            add("ordertype", request.getOrdertype().name());
            add("contractNumber", request.getContractNumber());
            add("positionState", request.getPositionState());
            add("requestState", request.getRequestState());
            addLong("statusChangedUser", request.getStatusChangedUser());
            setSortColumn(request.getSortBy());
            setSortType(request.getSortDirection());
            switch (request.getOrdertype()) {
                case DEPARTMENT: {
                    addBool("sendDateIsNotNull", false);
                }
                break;
                case OMTK: {
                    addBool("sendDateIsNotNull", true);
                    if (getSortColumn() == null) {
                        setSortColumn("sendDate");
                        setSortType(true);
                    }
                }
                break;
                case OZL: {
                    addBool("sendDateIsNotNull", true);
                }
                break;
            }
        }});
    }

    @Override
    public List<SelectItem> getStatusChangedUsers(String name) {
        return orderItemDao.getStatusChangedUsers(name).map(userId -> {
            _User user = userDao.get(userId);
            if (user == null)
                return new SelectItem();
            return new SelectItem(userId, user.getShortName());
        }).collect(Collectors.toList());
    }

    private ListResponse forOmtkOrOzl(ItemsRequest request) {
        String status;
        if (request.isForProducing()) {
            status = String.join(" , ", Arrays.asList(_State.READY_TO_PRODUCE, _State.YES_PRODUCT));
        } else if (OrderClassification.OMTK.equals(request.getType())) {
            List<String> status1 = new ArrayList<String>(Arrays.asList(_State.READY_TO_PRODUCE, _State.TO_REVIEW_WAREHOUSE, _State.PAPER_EXPECTED_SPECIFICATION, _State.YES_PRODUCT,
                    _State.REJECT_ORDER_ITEM, _State.PENDING_PURCHASE, _State.APPLICATION_REJECTED_OZL, _State.DELIVERY_EXPECTED, _State.ISSUED_ORDER_ITEM, _State.RECEIVED));
            if (request.getRemoveStatus() != null) {
                status1.removeAll(request.getRemoveStatus());
            }
            status = String.join(" , ", status1);
        } else {
            status = String.join(" , ", Arrays.asList(_State.PAPER_EXPECTED_SPECIFICATION, _State.PENDING_PURCHASE, _State.APPLICATION_REJECTED_OZL, _State.READY_TO_PRODUCE, _State.DELIVERY_EXPECTED,
                    _State.ISSUED_ORDER_ITEM, _State.YES_PRODUCT, _State.RECEIVED));
        }
        PageStream<_OrderItem> orderItemPageStream = orderItemDao.search(
                new FilterParameters() {{
                    if (!ServerUtils.isEmpty(request.getAllSearch()))
                        addString("allSearch", request.getAllSearch());
                    setStart(request.getPage() * request.getLimit());
                    setSize(request.getLimit());
                    add("order_id", "" + request.getObjectId());
                    add("states", status);
                    add("numbSearch", request.getParentNumbSearch());
                    add("name", request.getProductName());
                    if (request.getContractId() != null)
                        add("contractId", "" + request.getContractId());
                    if (request.isForProducing())
                        addBool("forProducing", true);
                }}
        );
        return ListResponse.of(orderItemPageStream, (orderItem, map) -> wrapForOmtkAndOzl(orderItem, map));
    }

    private ListResponse forDepartment(ItemsRequest request) {
        FilterParameters filter = new FilterParameters();
        if (request.getObjectId() != null)
            filter.add("order_id", "" + request.getObjectId());
        if (request.getPositionState() != null)
            filter.add("positionState", request.getPositionState());
        if (request.getProductName() != null)
            filter.add("productName", request.getProductName());
        if (request.getAllSearch() != null)
            filter.add("allSearch", request.getAllSearch());
        PageStream<_OrderItem> orderItemPageStream = orderItemDao.search(filter);
        return ListResponse.of(orderItemPageStream, (orderItem, map) -> wrapForDepartment(orderItem, map));
    }

    @Override
    public CoreMap getOneItem(Long orderId, Long productId) {
        CoreMap coreMap = null;
        _OrderItem byProductInOrder = orderItemDao.getByProductInOrder(orderId, productId);
        if (byProductInOrder != null) {
            coreMap = byProductInOrder.getMap(true);
            return coreMap;
        }
        return coreMap;
    }

    private CoreMap getAllResourceFile(_OrderItem orderItem, CoreMap map) {
        if (orderItem.getRejectResource() != null) {
            map.add("rejectionFileLink", AttachmentUtils.getLink(orderItem.getRejectResource().getName()));
            map.add("rejectionFileName", orderItem.getRejectResource().getOriginalName());
        }
        return map;
    }

    @Override
    public SingleResponse checkEDS(Long orderItemId) {
        _OrderItem orderItem = orderItemDao.get(orderItemId);
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        HashMap<String, Object> map = new HashMap<>();
        if (!ServerUtils.isEmpty(orderItem.getHashESign())) {
            map.put("hasSignedBefore", false);
        } else {
            map.put("hasSignedBefore", true);
            map.put("hashESign", orderItem.getHashESign());
        }
        return SingleResponse.of(map);
    }

    @Override
    public SingleResponse setHashESign(HashESignRequest request) {
        _OrderItem orderItem = orderItemDao.get(request.getId());
        if (orderItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
//        ServerUtils.checkESign(request.getHashESign());
        orderItem.setHashESign(request.getHashESign());
        orderItemDao.save(orderItem);
        return SingleResponse.of(true);
    }

    @Override
    public Double provide(_OrderItem orderItem, Double count) {
        if ((orderItem.getProduct().getCount() - (count - orderItem.getBron()) < 0)) {
            throw new CriticException("Bu mahsulot bo'yicha maksimum " + (orderItem.getProduct().getCount() + orderItem.getBron())
                    + " olishingiz mumkin, qolganlari bron qilingan");
        }
        if (orderItem.getBron() - count <= 0)
            orderItem.setBron(0);
        else orderItem.setBron(orderItem.getBron() - count);
        orderItemDao.save(orderItem);
        productService.updateBron(orderItem);
        return (orderItem.getCount() - orderItem.getGiven());
    }

    @Override
    public void bron(_OrderItem orderItem, Double count) {
        if (orderItem.getCount() - orderItem.getBron() > 0) {
            if (orderItem.getProduct().getCount() - count < 0)
                throw new ValidatorException("Омборда маҳсулот етарли эмас");
            if (orderItem.getCount() - (orderItem.getBron() + count) < 0)
                throw new ValidatorException("Заказда кўрсатилганидан кўпроқ маҳсулот бериляпти!");
            orderItem.setBron(orderItem.getBron() + count);
            orderItemDao.save(orderItem);
        }
        productService.updateBron(orderItem);
    }

    @Override
    public SingleResponse getSingle(Long id) {
        _OrderItem orderItem = orderItemDao.get(id);
        if (ServerUtils.isEmpty(orderItem))
            throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        return SingleResponse.of(orderItem, (orderItem1, map) -> wrapForSingle(orderItem1, map));
    }

    @Override
    public SingleResponse ozlOffer(OrderItemOfferRequest request, MultipartFile file) {
        if (request.getId() == null)
            throw new ValidatorException("ORDER_ITEM_REQUIRED");

        if (file == null || file.isEmpty())
            throw new ValidatorException("FILE_REQUIRED");

        _OrderItem orderItem = orderItemDao.get(request.getId());
        if (orderItem == null)
            throw new RpcException("ORDER_ITEM_NOT_FOUND");

        if (request.getProduct_id() == null)
            throw new ValidatorException("PRODUCT_REQUIRED");

        _Product product = productDao.get(request.getProduct_id());
        if (product == null)
            throw new RpcException("PRODUCT_NOT_FOUND");

        if (request.getUnitTypeId() == null)
            throw new ValidatorException("UNITTYPE_REQUIRED");

        _UnitType unitType = unitTypeDao.getById(request.getUnitTypeId());
        if (unitType == null)
            throw new RpcException("UNITTYPE_NOT_FOUND");

        if (request.getCount() == null)
            throw new ValidatorException("COUNT_REQUIRED");
        if (ServerUtils.isEmpty(request.getComment()))
            throw new ValidatorException("COMMENT_REQUIRED");
        _AttachmentView attachmentView = attachmentService.uploadPdf(file);
        orderItem.setOfferComment(request.getComment());
        orderItem.setOfferAttachment(attachmentView);
        orderItem.setOfferProduct(product);
        orderItem.setOfferUnitType(unitType);
        orderItem.setOfferCount(request.getCount());
        changeStatus(orderItem, _State.OZL_TO_DEPARTMENT);
        return SingleResponse.of(orderItem, (orderItem1, map) -> wrapForSingle(orderItem1, map));
    }

    @Override
    public void updateOzlOffer(OrderItemOfferRequest request) {
        if (request.getId() == null)
            throw new ValidatorException("ORDER_ITEM_REQUIRED");
        _OrderItem orderItem = orderItemDao.get(request.getId());
        if (orderItem == null)
            throw new RpcException("ORDER_ITEM_NOT_FOUND");

        if (request.getProduct_id() == null)
            throw new ValidatorException("PRODUCT_REQUIRED");

        _Product product = productDao.get(request.getProduct_id());
        if (product == null)
            throw new RpcException("PRODUCT_NOT_FOUND");

        if (request.getUnitTypeId() == null)
            throw new ValidatorException("UNITTYPE_REQUIRED");

        _UnitType unitType = unitTypeDao.getById(request.getUnitTypeId());
        if (unitType == null)
            throw new RpcException("UNITTYPE_NOT_FOUND");

        if (request.getCount() == null)
            throw new ValidatorException("COUNT_REQUIRED");

        orderItem.setProduct(product);
        orderItem.setUnitType(unitType);
        orderItem.setCount(request.getCount());
        changeStatus(orderItem, _State.PAPER_EXPECTED_SPECIFICATION);
    }

    @Override
    public void updateTakenAwayCount() {
        orderItemDao.callVoid("updateOrdersTakeAway", orderItemDao.getSession(), Types.BOOLEAN);
    }

    @Override
    public ListResponse getOrderItemsByProductCategory(DashboardFilter request) {
        List<Facet> facets = orderItemDao.getOrderItemsByProductCategory(new FilterParameters() {{
            Date[] period = request.getPeriod();
            if (period != null) {
                setFromDate(period[0]);
                setToDate(period[1]);
            }
            setStart(request.getPage() * request.getLimit());
        }});
        int total = facets.size();
        List<SelectItem> list = facets.stream().skip(request.getPage() * request.getLimit()).limit(request.getLimit())
                .map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(list, total);
    }

    @Override
    public ListResponse defineOrderStatusCount(DashboardFilter filterRequest) {
        List<Facet> facets = orderItemDao.getDefineOrderStatusCount(new FilterParameters());
        List<SelectItem> collect = facets.stream().map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(collect);
    }

    @Override
    public ListResponse departmentOrderItems(DashboardFilter filterRequest) {
        List<Facet> facets = orderItemDao.departmentOrderItems(new FilterParameters());
        List<SelectItem> collect = facets.stream().map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(collect);
    }

    @Override
    public ListResponse latencyRequests(DashboardFilter request) {
        return null;
    }

    @Override
    public _OrderItem changeState(OrderItemStateChangeRequest request) {
        _OrderItem orderItem = orderItemDao.get(request.getId());
        if (orderItem == null)
            throw new ValidatorException("ORDER_ITEM_NOT_FOUND");
        orderItem.setState(request.getState());
        orderItemDao.save(orderItem);
        return orderItem;
    }

    @Override
    public Stream<_OrderItem> findAll(_Order order) {
        return orderItemDao.findAll(order);
    }

    @Override
    public void search(OrderFilterRequest request, Long orderId, OrderResponse response) {
        orderItemDao.search(new FilterParameters() {{
            setSize(Integer.MAX_VALUE);
            addString("order_id", "" + orderId);
            addString("positionState", request.getPositionState());
            addString("groupId", "" + request.getGroupId());
            addString("typeId", "" + request.getTypeId());
            addString("productName", request.getName());
            addString("contractNumber", request.getContractNumber());
            addString("statusChangedUser", "" + request.getStatusChangedUser());
            if (request.isHasContract()) {
                addString("isHasContract", "" + request.isHasContract());
            }
            if (request.getSupplierId() != null) {
                addString("supplierId", "" + request.getSupplierId());
            }
            if (OrderClassification.OZL.equals(request.getOrdertype())) {
                addBoolean("fromOzl", true);
            }
        }}).stream().forEach(orderItem -> {
            ItemResponse itemResponse = getDetails(orderItem);
            if (OrderClassification.DEPARTMENT.equals(request.getOrdertype())) {
                if (_State.READY_TO_PRODUCE.equals(orderItem.getState())) {
                    itemResponse.getListMaps().put("warehouses", givenProductsService.getWarehouseList(orderItem));
                }
            }
            response.add(new OrderResponse(itemResponse));
        });
    }

    private List<String> socketSendUsers(Long departmentId) {
        if (departmentId == null)
            departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
        String username = SessionUtils.getInstance().getUser().getUsername();
        if (username == null) return null;
        List<String> usernames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(user -> user.getUsername()).collect(Collectors.toList());
        usernames.remove(username);
        return usernames;
    }

    private void sendSocketMessageDepartments(Long orderId, _OrderItem orderItem, String state, String eventType, Long departmentId) {
        List<String> userNameDepartment = socketSendUsers(departmentId);
        if (userNameDepartment.isEmpty()) return;
        final OrderItemUpdateStatusEvent event = new OrderItemUpdateStatusEvent(orderId, orderItem, state, eventType, userNameDepartment);
        eventPublisher.publishEvent(event);
    }

    private void sendSocketMessageOMTK(Long orderId, _OrderItem orderItem, String state, String eventType) {
        _Department omtk = departmentDao.getOneByType(OrderClassification.OMTK);
        if (omtk == null) return;
        List<String> userNameOmtk = socketSendUsers(omtk.getId());
        if (userNameOmtk == null) return;
        final OrderItemUpdateStatusEvent event = new OrderItemUpdateStatusEvent(orderId, orderItem, state, eventType, userNameOmtk);
        eventPublisher.publishEvent(event);
    }

    private void sendSocketMessageOZL(Long orderId, _OrderItem orderItem, String state, String eventType) {
        _Department ozl = departmentDao.getOneByType(OrderClassification.OZL);
        if (ozl == null) return;
        List<String> userNameOzl = socketSendUsers(ozl.getId());
        if (userNameOzl.isEmpty()) return;
        final OrderItemUpdateStatusEvent event = new OrderItemUpdateStatusEvent(orderId, orderItem, state, eventType, userNameOzl);
        eventPublisher.publishEvent(event);
    }


    private void sendDepartment(_OrderItem orderItem, String state, String eventType) {
        Long departmentId = null;
        if (orderItem.getAuditInfo() != null) {
            if (orderItem.getAuditInfo().getCreatedByUser() != null) {
                if (orderItem.getAuditInfo().getCreatedByUser().getDepartment() != null) {
                    departmentId = orderItem.getAuditInfo().getCreatedByUser().getDepartment().getId();
                    if (departmentId != null)
                        sendSocketMessageDepartments(orderItem.getParent().getId(), orderItem, state, eventType, departmentId);
                }
            }
        }
    }

    private CoreMap wrapForDepartment(_OrderItem orderItem, CoreMap map) {
        if (orderItem.getParent() != null)
            map.add("order_id", orderItem.getParent().getId());
        if (orderItem.getProduct() != null) {
            map.add("productId", orderItem.getProduct().getId());
            map.add("productName", orderItem.getProduct().getName());
            map.addDouble("hasProductCount", orderItem.getProduct().getCount());
        }
        if (orderItem.getProductGroup() != null) {
            map.add("productGroupName", orderItem.getProductGroup().getName());
            map.add("productGroupId", orderItem.getProductGroup().getId());
        }
        if (orderItem.getProductType() != null) {
            map.add("productTypeName", orderItem.getProductType().getName());
            map.add("productTypeId", orderItem.getProductType().getId());
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
        if (orderItem.getAuditInfo() != null) {
            if (orderItem.getAuditInfo().getUpdatedByUser() != null) {
                map.add("updateUser", orderItem.getAuditInfo().getUpdatedByUser().getFirstName() + " " + orderItem.getAuditInfo().getUpdatedByUser().getLastName());
            } else if (orderItem.getAuditInfo().getCreatedByUser() != null) {
                map.add("updateUser", orderItem.getAuditInfo().getCreatedByUser().getFirstName() + " " + orderItem.getAuditInfo().getCreatedByUser().getLastName());
            }
        }
        if (orderItem.getUnitType() != null) {
            map.add("unitTypeId", orderItem.getUnitType().getId());
            map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", orderItem.getUnitType().getNameCyrl());
        }
        getAllResourceFile(orderItem, map);
        if (StateConstants.YES_PRODUCT.equals(orderItem.getState())) {
            if (orderItem.getAuditInfo().getCreatedByUser() != null &&
                    orderItem.getAuditInfo().getCreatedByUser().getDepartment() != null) {
                _Warehouse warehouse = orderItem.getAuditInfo().getCreatedByUser().getDepartment().getWarehouses().stream().findFirst().orElse(null);
                if (warehouse != null) {
                    map.add("warehouseId", warehouse.getId());
                    map.put("warehouseName", warehouse.getNameByLanguage());
                }
            }
        }
        map.remove("hashESign");
        stateService.wrapStatus(map, orderItem.getState());
        return map;
    }

    private CoreMap wrapForOmtkAndOzl(_OrderItem orderItem, CoreMap map) {
        if (orderItem.getParent() != null) {
            map.add("order_id", orderItem.getParent().getId());
            map.add("parentNumb", "" + orderItem.getParent().getNumb());
        }
        if (orderItem.getProduct() != null) {
            map.add("productId", orderItem.getProduct().getId());
            map.add("productName", orderItem.getProduct().getName());
            map.addDouble("hasProductCount", orderItem.getProduct().getCount());
            map.addDouble("totalProductBron", orderItem.getProduct().getBron());
        }
        if (orderItem.getProductGroup() != null)
            map.add("productGroupName", orderItem.getProductGroup().getName());
        if (orderItem.getProductType() != null)
            map.add("productTypeName", orderItem.getProductType().getName());
        if (orderItem.getContractItem() != null) {
            if (orderItem.getContractItem().getParent() != null) {
                map.add("contractNumber", orderItem.getContractItem().getParent().getCode());
            } else {
                map.add("contractNumber", "");
            }
        } else {
            map.add("contractNumber", "");
        }
        if (orderItem.getUnitType() != null) {
            map.add("unit_type_name_en", orderItem.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", orderItem.getUnitType().getNameCyrl());
        }
        getAllResourceFile(orderItem, map);
        map.remove("hashESign");
        stateService.wrapStatus(map, orderItem.getState());
        return map;
    }

    private CoreMap wrapForSingle(_OrderItem orderItem1, CoreMap map) {
        if (orderItem1.getParent() != null) {
            map.add("order_id", orderItem1.getParent().getId());
            map.add("parentNumb", "" + orderItem1.getParent().getNumb());
        }
        if (orderItem1.getProduct() != null) {
            map.add("productId", orderItem1.getProduct().getId());
            map.add("productName", orderItem1.getProduct().getName());
            map.addDouble("hasProductCount", orderItem1.getProduct().getCount());
            map.addDouble("totalProductBron", orderItem1.getProduct().getBron());
        }
        if (orderItem1.getProductGroup() != null)
            map.add("productGroupName", orderItem1.getProductGroup().getName());
        if (orderItem1.getProductType() != null)
            map.add("productTypeName", orderItem1.getProductType().getName());


        if (orderItem1.getUnitType() != null) {
            map.add("unit_type_name_en", orderItem1.getUnitType().getNameEn());
            map.add("unit_type_name_ru", orderItem1.getUnitType().getNameRu());
            map.add("unit_type_name_uz", orderItem1.getUnitType().getNameUz());
            map.add("unit_type_name_cyrl", orderItem1.getUnitType().getNameCyrl());
        }
        map.remove("hashESign");
        stateService.wrapStatus(map, orderItem1.getState());
        return map;
    }

    private ItemResponse getDetails(_OrderItem orderItem) {
        ItemResponse response = new ItemResponse();
        CoreMap map = orderItem.getMap();
        stateService.wrapStatus(map, orderItem.getState());
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
            if (orderItem.getProduct().getMsds() != null)
                map.add("MSDSFileName", orderItem.getProduct().getMsds().getName());
            map.add("productId", orderItem.getProduct().getId());
            map.add("productName", orderItem.getProduct().getName());
            map.add("uniqueKey", orderItem.getProduct().getUniqueKey());
            if (orderItem.getProduct().getLimitCount() != null)
                map.addString("productRemainingPercentToLimit", "" + orderItem.getProduct().getPercentRemainingToLimit());
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
                map.add("contractId", orderItem.getContractItem().getParent().getId());
                map.add("contractNumber", orderItem.getContractItem().getParent().getCode());
                map.add("guessReceiveDate", "" + orderItem.getContractItem().getParent().getGuessReceiveDate());
                if (orderItem.getContractItem().getActualReceiveDate() != null)
                    map.add("actualReceiveDate", "" + orderItem.getContractItem().getActualReceiveDate());
                map.add("conclusionDate", "" + orderItem.getContractItem().getParent().getConclusionDate());
                if (orderItem.getContractItem().getParent().getSupplier() != null)
                    map.add("supplierName", orderItem.getContractItem().getParent().getSupplier().getName());
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
                map.add("offer_unit_type_name_cyrl", orderItem.getOfferUnitType().getNameCyrl());
            }
            if (orderItem.getOfferCount() != null)
                map.add("offerCount", "" + orderItem.getOfferCount());
            if (orderItem.getOfferAttachment() != null) {
                map.add("offerFileName", "" + orderItem.getOfferAttachment().getName());
                map.add("offerFileOriginalName", "" + orderItem.getOfferAttachment().getOriginalName());
            }
            if (orderItem.getOfferComment() != null)
                map.add("offerComment", "" + orderItem.getOfferComment());
        }
        response.setMaps(map.getInstance());
        return response;
    }
}