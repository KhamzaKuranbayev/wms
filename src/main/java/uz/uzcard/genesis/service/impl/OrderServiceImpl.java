package uz.uzcard.genesis.service.impl;

import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.order.MultipleOrderCreateRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderFilterRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.event.order.OrderCreateEvent;
import uz.uzcard.genesis.dto.event.order.OrderSendDepartmentEvent;
import uz.uzcard.genesis.dto.event.order.OrderSendOmtkEvent;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.hibernate.enums.Permissions;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static uz.uzcard.genesis.uitls.StateConstants.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private StateService stateService;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private TeamService teamService;

    @Override
    public PageStream<_Order> list(OrderFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            } else {
                setSortType(false);
            }

//            setSize(request.getLimit());
//            setStart(request.getPage() * request.getLimit());
            add("orderNumber", request.getOrderNumber());
            addLong("groupId", request.getGroupId());
            addLong("typeId", request.getTypeId());
            add("name", request.getName());
            addDate("fromDate", request.getFromDate());
            addDate("toDate", request.getToDate());
            add("ordertype", request.getOrdertype().name());
            add("contractNumber", request.getContractNumber());
            add("positionState", request.getPositionState());
            add("requestState", request.getRequestState());
            addLong("initiator", request.getInitiator());
            addLong("statusChangedUser", request.getStatusChangedUser());
            addLong("departmentId", request.getDepartmentId());
            if (request.isForProduce()) {
                addStrings("itemStatuses",
                        Arrays.asList(TO_REVIEW_WAREHOUSE, READY_TO_PRODUCE));
            }
//            setSortColumn(request.getSortBy());
//            setSortType(request.getSortDirection());
            if (request.getOrdertype() == null)
                throw new ValidatorException("Сўров турини белгиланг");
            if (!SessionUtils.getInstance().getPermissions().contains(Permissions.SEE_ALL_ORDER) && !SessionUtils.getInstance().getTeams().isEmpty()) {
                addLongs("teams", SessionUtils.getInstance().getTeams());
            }
            switch (request.getOrdertype()) {
                case DEPARTMENT: {
                    addBool("sendDateIsNotNull", false);
                    if (!SessionUtils.getInstance().getRoles().contains("ADMIN") && orderDao.getUser().getDepartment() != null) {
//                    if (!SessionUtils.getInstance().getRoles().stream().anyMatch(role -> Arrays.asList("ADMIN", "OMTK").contains(role))) {
                        addLong("departmentId", orderDao.getUser().getDepartment().getId());
                    }
                }
                break;
                case OMTK: {
                    addBool("sendDateIsNotNull", true);
                    List<Long> teams = teamService.getMyChildTeamIds();
                    addLongs("teams", teams);
                    if (teams.isEmpty()) {
                        break;
                    }
                    if (getSortColumn() == null) {
                        setSortColumn("sendDate");
                        setSortType(false);
                    }
                    addStrings("itemStatuses",
                            Arrays.asList(_State.READY_TO_PRODUCE, _State.TO_REVIEW_WAREHOUSE, _State.PAPER_EXPECTED_SPECIFICATION, _State.YES_PRODUCT,
                                    _State.REJECT_ORDER_ITEM, _State.PENDING_PURCHASE, _State.APPLICATION_REJECTED_OZL, _State.DELIVERY_EXPECTED, _State.ISSUED_ORDER_ITEM, _State.RECEIVED));
                }
                break;
                case OZL: {
                    addBool("sendDateIsNotNull", true);
                    addStrings("itemStatuses",
                            Arrays.asList(_State.PAPER_EXPECTED_SPECIFICATION, _State.PENDING_PURCHASE, _State.APPLICATION_REJECTED_OZL, _State.READY_TO_PRODUCE, _State.DELIVERY_EXPECTED,
                                    _State.ISSUED_ORDER_ITEM, _State.RECEIVED));
                    addBool("fromOzl", true);
                }
                break;
            }
        }};
        if (request.isHasContract())
            filter.add("isHasContract", "" + request.isHasContract());
        if (request.getSupplierId() != null) {
            filter.add("supplierId", "" + request.getSupplierId());
        }

        return orderDao.search(filter);
    }

    @Override
    public _Order addMultiple(MultipleOrderCreateRequest request, List<MultipartFile> files) {
        List<_AttachmentView> attachments = new ArrayList<>();
        files.forEach(file -> {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            attachments.add(attachment);
        });
        _Order order = orderDao.get(request.getId());
        if (order == null) {
            order = new _Order();
            if (ServerUtils.isEmpty(files)) {
                throw new RpcException("FILE_REQUIRED");
            }
        }
        if (request.getTimeToBeEntered() != null)
            order.setTimeToBeEntered(request.getTimeToBeEntered());
        else if (order.getTimeToBeEntered() == null)
            order.setTimeToBeEntered(new Date());
        if (order.getDepartment() == null) {
            _User user = SessionUtils.getInstance().getUser();
            if (user == null)
                throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
            if (user.getDepartment() == null)
                throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
            order.setDepartment(user.getDepartment());
        }
        if (!attachments.isEmpty())
            order.getAttachments().addAll(attachments);
        _Order finalOrder = order;
        request.getDeletedFiles().forEach(name -> {
            _AttachmentView attachment = attachmentService.delete(name);
            finalOrder.getAttachments().remove(attachment);
        });
        order.setState(NEW);
        orderDao.save(order);

        for (MultipleOrderCreateRequest.MultipleOrderItemRequest item : request.getItems()) {
            if (item.getId() == null || item.isChanged()) {
                _OrderItem orderItem = orderItemsService.save(item.wrap(order.getId()));
            }
        }

        Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
        String username = SessionUtils.getInstance().getUser().getUsername();
        List<String> userNames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(_User::getUsername).collect(Collectors.toList());
        userNames.remove(username);
        final OrderCreateEvent event = new OrderCreateEvent(order, userNames);
        eventPublisher.publishEvent(event);

        return order;
    }

    @Override
    public _Order addFiles(Long orderId, List<MultipartFile> files) {
        _Order order = orderDao.get(orderId);
        if (order == null)
            throw new ValidatorException("ORDER_NOT_FOUND");
        List<_AttachmentView> attachments = new ArrayList<>();
        files.forEach(file -> {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            attachments.add(attachment);
        });
        if (!attachments.isEmpty())
            order.getAttachments().addAll(attachments);
        return orderDao.save(order);
    }

    @Override
    public ListResponse getOrderBySendDateFacet(DashboardFilter filterRequest) {
        List<Facet> allSendOrder = orderDao.getAllSendOrder(filterRequest);
        int total = allSendOrder.size();

        List<SelectItem> list = allSendOrder.stream().skip(filterRequest.getPage() * filterRequest.getLimit()).limit(filterRequest.getLimit())
                .map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(list, total);
    }

    @Override
    public _Order get(Long id) {
        return orderDao.get(id);
    }

    @Override
    public _Order add(OrderRequest request, List<MultipartFile> files) {
        if (ServerUtils.isEmpty(files)) {
            throw new RpcException(GlobalizationExtentions.localication("FILE_REQUIRED"));
        }
        _AttachmentView attachment = null;
        attachment = attachmentService.uploadPdf(files);
        _Order order = null;
        if (request.getId() != null) {
            order = orderDao.get(request.getId());
            if (order == null)
                throw new ValidatorException("Заявка топилмади");
            if (attachment != null)
                order.setAttachments(Arrays.asList(attachment));
        } else if (request.isDefaultYearly()) {
            order = orderDao.getDefaultByDepartment(userDao.getUser().getDepartment());
        }
        if (order == null) {
            order = new _Order();
            if (attachment != null)
                order.setAttachments(Arrays.asList(attachment));
            if (request.getTimeToBeEntered() != null)
                order.setTimeToBeEntered(request.getTimeToBeEntered());
            else
                order.setTimeToBeEntered(new Date());

            if (order.getDepartment() == null) {
                _User user = SessionUtils.getInstance().getUser();
                if (user == null)
                    throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
                if (user.getDepartment() == null)
                    throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
                order.setDepartment(user.getDepartment());
            }

            orderDao.save(order);
            _OrderItem orderItem = orderItemsService.save(request.wrapOrderItemRequest(order.getId()));
            order.setState(NEW);
        }
        if (request.isDefaultYearly()) {
            order.setDefaultYearly(Calendar.getInstance().get(Calendar.YEAR));
            order.setSendDate(new Date());
            order.setState(NOT_ACCEPTED_ORDER);
            order.setAttachments(Arrays.asList(attachment));

            if (order.getDepartment() == null) {
                _User user = SessionUtils.getInstance().getUser();
                if (user == null)
                    throw new RpcException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
                if (user.getDepartment() == null)
                    throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
                order.setDepartment(user.getDepartment());
            }

            _OrderItem orderItem = orderItemsService.save(request.wrapOrderItemRequest(order.getId()));
            orderDao.save(order);
        }

        Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
        String username = SessionUtils.getInstance().getUser().getUsername();
        List<String> userNames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(_User::getUsername).collect(Collectors.toList());
        userNames.remove(username);
        final OrderCreateEvent event = new OrderCreateEvent(order, userNames);
        eventPublisher.publishEvent(event);

        return order;
    }

    @Override
    public void delete(Long id) {
        if (ServerUtils.isEmpty(id)) {
            throw new RpcException("ID_REQUIRED");
        }
        _Order order = orderDao.get(id);
        if (ServerUtils.isEmpty(order)) {
            throw new RpcException("ORDER_NOT_FOUND");
        }
        orderDao.delete(order);
        orderItemDao.findByOrder(order).forEach(orderItem -> {
            orderItemDao.delete(orderItem);
        });
    }

    @Override
    public SingleResponse send(Long orderId) {
        _Order order = orderDao.get(orderId);
        if (order == null)
            throw new ValidatorException("Маълумот топилмади");

        if (order.getTimeToBeEntered() != null)
            order.setSendDate(order.getTimeToBeEntered());
        else
            order.setSendDate(new Date());

        order.setState(_State.NOT_ACCEPTED_ORDER);
        order.setStatusChangedUser(orderDao.getUser());

        List<HashMap<String, String>> maps = new ArrayList<>();
        orderItemDao.findByOrder(order).forEach(orderItem -> {
            orderItemsService.changeStatus(orderItem, _State.TO_REVIEW_WAREHOUSE);
            CoreMap coreMap = orderItem.getMap();
            stateService.wrapStatus(coreMap, orderItem.getState());
            maps.add(coreMap.getInstance());
        });

        _Order save = orderDao.save(order);
        Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
        String username = SessionUtils.getInstance().getUser().getUsername();
        List<String> userNameDepartment = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(user -> user.getUsername()).collect(Collectors.toList());
        userNameDepartment.remove(username);

        final OrderSendDepartmentEvent departmentEvent = new OrderSendDepartmentEvent(save, userNameDepartment);
        eventPublisher.publishEvent(departmentEvent);

        _Department omtk = departmentDao.getOneByType(OrderClassification.OMTK);
        if (omtk == null)
            throw new RpcException("ОМТК бўлими топилмади");
        List<String> userNameOmtk = userDao.list(new FilterParameters().add("departmentId", "" + omtk.getId())).map(user -> user.getUsername()).collect(Collectors.toList());
        userNameOmtk.remove(username);

        final OrderSendOmtkEvent omtkEvent = new OrderSendOmtkEvent(order, userNameOmtk);
        eventPublisher.publishEvent(omtkEvent);

        return SingleResponse.of(orderDao.save(order), (order1, map) -> {
            stateService.wrapStatus(map, order1.getState());
            map.addStrings("items", maps);
            return map;
        });
    }

    @Override
    public _OrderItem addDefaultOrder(OrderRequest request, List<MultipartFile> files) {
        request.setDefaultYearly(true);
        _Order order = add(request, files);
        _OrderItem orderItem = orderItemDao.getLastByOrder(order);
        orderItem.setState(PENDING_PURCHASE);
        orderItemDao.save(orderItem);
        order.getItems().add(orderItem);
        orderDao.save(order);
        return orderItem;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean teamLogicsFix(int start, int limit) {
        final boolean[] empty = {true};
        orderDao.list(new FilterParameters() {{
            setStart(start);
            setSize(limit);
        }}).forEach(order -> {
            empty[0] = false;
            orderDao.save(order);
        });
        return empty[0];
    }
}