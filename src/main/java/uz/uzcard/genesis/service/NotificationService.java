package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.contract.ContractInitiatorNotificationRequest;
import uz.uzcard.genesis.dto.api.req.setting.*;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Notification;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._User;

public interface NotificationService {

    void sendPnsToDevice(NotificationRequest request, boolean isWhenInitiatorAdded);

    PageStream<_Notification> list(NotificationFilterRequest request);

    Boolean saveFirebaseToken(FirebaseRequest request);

    Boolean logout(String deviceId);

    Boolean sendFromOzlToOmtk(NotificationFromOZLToOMTKReq request);

    Boolean sendFromInitiatorToOmtk(NotificationFromDepToOMTKReq request, MultipartFile file);

    Boolean pushByContract(ContractInitiatorNotificationRequest request);

    Boolean pushAboutProduced(_OrderItem orderItem, Double count);

    void add(NotificationRequest request, _User sentTo);

    void makeAsRead(Long id);

    Boolean sendMessageEveryOne(NotificationSendEveryOneRequest request);
}
