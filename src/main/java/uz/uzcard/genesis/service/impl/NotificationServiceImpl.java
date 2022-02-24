package uz.uzcard.genesis.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.ContractInitiatorNotificationRequest;
import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.dto.event.main.NotificationEveryOneEvent;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.NotificationCategoryType;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;
import uz.uzcard.genesis.service.NotificationService;
import uz.uzcard.genesis.service.ProduceHistoryService;
import uz.uzcard.genesis.telegram.TelegramService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 06.07.2020  17:46
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Log logger = LogFactory.getLog(getClass());
    private final FirebaseTokenDao firebaseTokenDao;
    private final ContractDao contractDao;
    private final ContractItemDao contractItemDao;
    private final UserDao userDao;
    private final UserAgreementDao userAgreementDao;
    private final NotificationDao notificationDao;
    private final OrderItemDao orderItemDao;
    private final ProduceHistoryService produceHistoryService;
    private final TelegramGroupDao telegramGroupDao;
    private final TelegramService telegramService;
    private final DepartmentDao departmentDao;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceImpl(FirebaseTokenDao firebaseTokenDao, ContractDao contractDao, ContractItemDao contractItemDao, UserDao userDao, UserAgreementDao userAgreementDao, NotificationDao notificationDao, OrderItemDao orderItemDao, ProduceHistoryService produceHistoryService, TelegramGroupDao telegramGroupDao, TelegramService telegramService, DepartmentDao departmentDao, ApplicationEventPublisher eventPublisher) {
        this.firebaseTokenDao = firebaseTokenDao;
        this.contractDao = contractDao;
        this.contractItemDao = contractItemDao;
        this.userDao = userDao;
        this.userAgreementDao = userAgreementDao;
        this.notificationDao = notificationDao;
        this.orderItemDao = orderItemDao;
        this.produceHistoryService = produceHistoryService;
        this.telegramGroupDao = telegramGroupDao;
        this.telegramService = telegramService;
        this.departmentDao = departmentDao;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @Override
    public void sendPnsToDevice(NotificationRequest request, boolean isWhenInitiatorAdded) {
        Map<String, String> map = new HashMap<>();
        map.put("title", request.getTitle());
        map.put("body", request.getBody());
        Message message = Message.builder()
                .setToken(request.getToken())
//                .setNotification(new Notification(request.getTitle(), request.getBody()))
                .putAllData(map)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            logger.error("Fail to send firebase notification", e);
        }
    }

    @Override
    public PageStream<_Notification> list(NotificationFilterRequest request) {
        return notificationDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            addLong("sentTo", notificationDao.getUser().getId());
        }});
    }

    @Override
    public Boolean saveFirebaseToken(FirebaseRequest request) {

        if (!ServerUtils.isEmpty(request.getDeviceId())) {
            _FirebaseToken firebaseToken = firebaseTokenDao.findByDeviceAndUser(request.getDeviceId());
            if (ServerUtils.isEmpty(firebaseToken)) {
                firebaseToken = new _FirebaseToken();
                firebaseToken.setDeviceId(request.getDeviceId());
                firebaseToken.setUserId(SessionUtils.getInstance().getUserId());
                firebaseToken.setToken(request.getToken());
            } else {
                firebaseToken.setToken(request.getToken());
            }
            firebaseTokenDao.save(firebaseToken);
            return true;
        }
        return false;
    }

    @Override
    public Boolean logout(String deviceId) {

        if (!ServerUtils.isEmpty(deviceId)) {
            FirebaseRequest request = new FirebaseRequest();
            request.setDeviceId(deviceId);
            _FirebaseToken firebaseToken = firebaseTokenDao.findByDeviceAndUser(request.getDeviceId());
            if (!ServerUtils.isEmpty(firebaseToken)) {
                firebaseToken.setState(_State.DELETED);
                firebaseTokenDao.save(firebaseToken);
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean sendFromOzlToOmtk(NotificationFromOZLToOMTKReq request) {
        _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
        NotificationRequest notification = new NotificationRequest();
        if (contractItem != null) {
            notification.setTitle("Tovarlar keldi!");
            notification.setBody(request.getBody());
            notification.setData(NotificationDataReq.builder().contractItemId(request.getContractItemId())
                    .contractCode(contractItem.getParent().getCode())
                    .contractId(contractItem.getParent().getId()).build());
            notification.setType(NotificationCategoryType.PRODUCT_ARRIVED_SENT_TO_OMTK);
        } else throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        userDao.findByDepartmentType(OrderClassification.OMTK).forEach(
                user -> {
                    firebaseTokenDao.findByUser(user.getId()).forEach(
                            firebaseToken -> {
                                notification.setToken(firebaseToken.getToken());
                                sendPnsToDevice(notification, false);
                            }
                    );
                    add(notification, user);
                }
        );
        _Department department = departmentDao.getOneByType(OrderClassification.OMTK);
        if (department != null)
            sendThroughTelegram(department, notification.getBody());
        return true;
    }

    @Override
    public Boolean sendFromInitiatorToOmtk(NotificationFromDepToOMTKReq request, MultipartFile file) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        _ProduceHistory produceHistory = produceHistoryService.add(ProduceHistoryRequest.builder().guessedTakenAwayDate(request.getGuessedTakenAwayDate())
                .orderItemId(request.getOrderItemId()).count(request.getCount()).build(), file);
        NotificationRequest notification;
        if (orderItem != null) {
            if (request.getCount() > orderItem.getCount())
                throw new ValidatorException("Zakazdagi so'ralgan mahsulot miqdoridan ko'p miqdor kiritilgan");
            notification = NotificationRequest.builder().body(request.getBody())
                    .data(NotificationDataReq.builder().orderItemId(request.getOrderItemId())
                            .produceHistoryId(produceHistory.getId())
                            .count(request.getCount())
                            .isProduceHistoryStateDone(false).build())
                    .type(NotificationCategoryType.REQUEST_FOR_PRODUCING_SENT_TO_OMTK).title("Inisiatordan").build();
            if (produceHistory.getAttachment() != null)
                notification.getData().setFilePath(produceHistory.getAttachment().getName());
        } else throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
        userDao.findByDepartmentType(OrderClassification.OMTK).forEach(
                user -> {
                    firebaseTokenDao.findByUser(user.getId()).forEach(
                            firebaseToken -> {
                                notification.setToken(firebaseToken.getToken());
                                sendPnsToDevice(notification, false);
                            }
                    );
                    add(notification, user);
                }
        );
        _Department department = departmentDao.getOneByType(OrderClassification.OMTK);
        if (department != null)
            sendThroughTelegram(department, notification.getBody());

        return true;
    }

    @Override
    public Boolean pushByContract(@NotNull ContractInitiatorNotificationRequest request) {
        if (!ServerUtils.isEmpty(request.getContractItemId())) {
            _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
            if (ServerUtils.isEmpty(contractItem)) {
                throw new ValidatorException("CONTRACT_ITEM_IS_NULL");
            }
            String body = contractItem.getParent().getCode() + " raqamli kontraktga initsiator bo'lib qo'shildingiz! \n" + " Kontrakt bo'yicha TMS \n" +
                    contractItem.getProduct().getName();
            NotificationRequest notification = NotificationRequest.builder().body(body)
                    .data(NotificationDataReq.builder().phoneNumberOMTK(contractItem.getAuditInfo().getCreatedByUser().getPhone())
                            .contractId(contractItem.getParent().getId())
                            .contractItemId(contractItem.getId())
                            .contractCode(contractItem.getParent().getCode())
                            .omtkInfo(notificationDao.getUser().getShortName()).build())
                    .type(NotificationCategoryType.INITIATOR_ADDED_SENT_TO_INITIATORS).title("Siz inisiator bo'lib qo'shildiz").build();
            if (!ServerUtils.isEmpty(request.getUserIds())) {
                Stream<_User> toSentUsers = userDao.findByIds(request.getUserIds());
                toSentUsers.forEach(user -> {
                    _UserAgreement userAgreement = userAgreementDao.getByContractItem(contractItem, user);
                    if (!ServerUtils.isEmpty(userAgreement)) {
                        userAgreement.setNotificationSent(true);
                        userAgreement.setNotificationSentTime(new Date());
                        userAgreementDao.save(userAgreement);
                        userAgreementDao.reindex(List.of(userAgreement.getId()));
                    }
                    if (!ServerUtils.isEmpty(user)) {
                        firebaseTokenDao.findByUser(user.getId()).forEach(firebaseToken -> {
                            notification.setToken(firebaseToken.getToken());
                            sendPnsToDevice(notification, true);
                        });
                    }
                    add(notification, user);
                    //todo department a'zolariga ham jo'natish
                    sendDepartmentMembers(contractItem, user);
                });
                contractItemDao.save(contractItem);
                contractDao.save(contractItem.getParent());
            }
        }
        return true;
    }

    @Override
    public Boolean pushAboutProduced(_OrderItem orderItem, Double count) {
        NotificationRequest notification = new NotificationRequest();
        notification.setTitle("Zakaz bo'yicha mahsulot berildi!");
        String body = orderItem.getParent().getNumb() + " raqamli buyurtma bo'yicha " + count + (orderItem.getUnitType() == null ? "" : orderItem.getUnitType().getNameUz()) +
                " " + orderItem.getProduct().getName() + " berildi";
        notification.setBody(body);
        notification.setType(NotificationCategoryType.PRODUCT_PRODUCED_SENT_TO_DEP_USER);
        firebaseTokenDao.findByUser(orderItem.getAuditInfo().getCreatedByUserId()).forEach(firebaseToken -> {
            notification.setToken(firebaseToken.getToken());
            sendPnsToDevice(notification, false);
        });
        add(notification, orderItem.getAuditInfo().getCreatedByUser());

        return true;
    }

    @Override
    public void add(NotificationRequest request, _User sentTo) {
        _Notification notification = new _Notification();
        notification.setSentTo(sentTo);
        notification.setBody(request.getBody());
        notification.setTitle(request.getTitle());
        notification.setType(request.getType());
        if (request.getData() != null)
            notification.setData(request.getData());
        notificationDao.save(notification);
    }

    private void sendDepartmentMembers(_ContractItem contractItem, _User initiator) {
        NotificationRequest notificationDep = new NotificationRequest();
        String bodyDep = initiator.getShortName() + contractItem.getParent().getCode() + " raqamli kontraktga initsiator bo'lib qo'shildi! \n" + " Kontrakt bo'yicha TMS \n" +
                contractItem.getProduct().getName();
        notificationDep.setBody(bodyDep);
        notificationDep.setTitle(initiator.getShortName() + " inisiator bo'lib qo'shildi!");
        if (initiator.getDepartment() != null)
            sendThroughTelegram(initiator.getDepartment(), bodyDep);
        notificationDep.setType(NotificationCategoryType.INITIATOR_ADDED_SENT_BY_DEP_USERS);
        userDao.findByDepartment(initiator.getDepartment()).forEach(userDep -> {
            if (!initiator.equals(userDep)) {
                firebaseTokenDao.findByUser(userDep.getId()).forEach(firebaseToken -> {
                    notificationDep.setToken(firebaseToken.getToken());
                    sendPnsToDevice(notificationDep, false);
                });
                add(notificationDep, userDep);
            }
        });
    }

    @Override
    public void makeAsRead(Long id) {

        _Notification notification = notificationDao.get(id);
        if (!ServerUtils.isEmpty(notification)) {
            notification.setRead(true);
            notificationDao.save(notification);
        } else throw new ValidatorException("Notification not found");
    }

    @Override
    public Boolean sendMessageEveryOne(NotificationSendEveryOneRequest request) {
        if (request.getComment() == null) {
            return true;
        }
        Long userId = SessionUtils.getInstance().getUserId();
        Stream<_User> sendMessageUsers = userDao.findAllWithoutId(userId);
        if (sendMessageUsers == null)
            return true;

        List<String> userNames = new ArrayList<>();
        List<_User> users = sendMessageUsers.map(user -> {
            userNames.add(user.getUsername());
            return user;
        }).collect(Collectors.toList());
        /**
         * send message with socket only online users
         */
        eventPublisher.publishEvent(new NotificationEveryOneEvent(request.getTitle(), request.getComment(), userNames));

        /**
         * send message with firebase
         */
        users.forEach(user -> {
            firebaseTokenDao.findByUser(user.getId()).forEach(firebaseToken -> {
                NotificationRequest notificationDep = new NotificationRequest();
                notificationDep.setToken(firebaseToken.getToken());
                notificationDep.setTitle(request.getTitle());
                notificationDep.setBody(request.getComment());
                sendPnsToDevice(notificationDep, false);
            });
        });

        /**
         * send message with telegram
         */
        List<_Department> departments = departmentDao.findAll().collect(Collectors.toList());
        for (_Department department : departments) {
            sendThroughTelegram(department, request.getComment());
        }

        return true;
    }

    private void sendThroughTelegram(_Department department, String body) {
        _TelegramGroups telegramGroups = telegramGroupDao.getByDepartment(department.getId());
        if (telegramGroups != null) {
            SendMessage message = new SendMessage();
            message.setChatId(telegramGroups.getChatId());
            message.setText(body);
            telegramService.sendSync(message);
        }
    }
}
