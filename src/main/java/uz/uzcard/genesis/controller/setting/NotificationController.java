package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.contract.ContractInitiatorNotificationRequest;
import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.FirebaseTokenDao;
import uz.uzcard.genesis.hibernate.dao.NotificationDao;
import uz.uzcard.genesis.service.NotificationService;
import uz.uzcard.genesis.uitls.ServerUtils;

import javax.validation.Valid;

/**
 * Created by norboboyev_h  on 06.07.2020  17:40
 */
@Api(value = "Notification")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FirebaseTokenDao firebaseTokenDao;

    @Autowired
    private NotificationDao notificationDao;

    @Transactional
    @ApiOperation(value = "Push notification")
    @PostMapping(value = "/push/by-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse pushNotification(@RequestBody ContractInitiatorNotificationRequest request) {
        return SingleResponse.of(notificationService.pushByContract(request));
    }


    @Transactional
    @ApiOperation(value = "Save Firebase Token")
    @PostMapping(value = "/firebase-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody FirebaseRequest request) {
        return SingleResponse.of(notificationService.saveFirebaseToken(request));
    }

    @Transactional
    @ApiOperation(value = "Save Firebase Token")
    @DeleteMapping(value = "/firebase-token/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse logout(@PathVariable String deviceId) {
        return SingleResponse.of(notificationService.logout(deviceId));
    }

    @Transactional
    @ApiOperation(value = "Send from OZL to OMTK Token")
    @PostMapping(value = "/contract/ozl-to-omtk", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse send(@RequestBody NotificationFromOZLToOMTKReq req) {
        return SingleResponse.of(notificationService.sendFromOzlToOmtk(req));
    }

    @Transactional
    @ApiOperation(value = "Send from OZL to OMTK Token")
    @PostMapping(value = "/contract/initiator-to-omtk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse send(@Valid NotificationFromDepToOMTKReq req, @RequestPart(value = "file", required = true) MultipartFile file) {
        return SingleResponse.of(notificationService.sendFromInitiatorToOmtk(req, file));
    }

    @Transactional
    @ApiOperation(value = "Mark As Read")
    @PostMapping(value = "/mark-as-read/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse markAsRead(@PathVariable Long id) {
        notificationService.makeAsRead(id);
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "Notification List")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse notificationList(NotificationFilterRequest filter) {
        return ListResponse.of(notificationService.list(filter), ((notification, map) -> {

            if (!ServerUtils.isEmpty(notification.getData()))
                map.add("data", ServerUtils.jsonParserIgnoreNull(notification.getData()));
            if (!ServerUtils.isEmpty(notification.getType()))
                map.add("categoryType", notification.getType().name());
            if (notification.getAuditInfo() != null)
                map.addDate("sentAt", notification.getAuditInfo().getCreationDate());

            return map;
        }));
    }

    @Transactional
    @ApiOperation(value = "Get Unread Notifications Count")
    @GetMapping(value = "/unread-messages-count", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getUnreadMessagesCount() {
        return SingleResponse.of(notificationDao.getUnreadMessageCount());
    }

    @Transactional
    @ApiOperation(value = "Send message every one to every communications")
    @PostMapping(value = "/send-every-one", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse sendEveryOne(@RequestBody NotificationSendEveryOneRequest request) {
        return SingleResponse.of(notificationService.sendMessageEveryOne(request));
    }
}
