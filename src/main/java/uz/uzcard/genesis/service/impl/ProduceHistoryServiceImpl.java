package uz.uzcard.genesis.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.NotificationDataReq;
import uz.uzcard.genesis.dto.api.req.setting.ProduceHistoryFilter;
import uz.uzcard.genesis.dto.api.req.setting.ProduceHistoryRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.NotificationDao;
import uz.uzcard.genesis.hibernate.dao.OrderItemDao;
import uz.uzcard.genesis.hibernate.dao.ProduceHistoryDao;
import uz.uzcard.genesis.hibernate.entity._Notification;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._ProduceHistory;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.ProduceHistoryService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.Arrays;

import static uz.uzcard.genesis.uitls.StateConstants.REQUEST_DONE;

/**
 * Created by norboboyev_h  on 25.12.2020  15:31
 */
@Service
public class ProduceHistoryServiceImpl implements ProduceHistoryService {

    private final ProduceHistoryDao produceHistoryDao;
    private final OrderItemDao orderItemDao;
    private final NotificationDao notificationDao;
    private final AttachmentService attachmentService;

    public ProduceHistoryServiceImpl(ProduceHistoryDao produceHistoryDao, OrderItemDao orderItemDao, NotificationDao notificationDao, AttachmentService attachmentService) {
        this.produceHistoryDao = produceHistoryDao;
        this.orderItemDao = orderItemDao;
        this.notificationDao = notificationDao;
        this.attachmentService = attachmentService;
    }

    @Override
    public PageStream<_ProduceHistory> list(ProduceHistoryFilter filter) {
        return produceHistoryDao.search(new FilterParameters() {{
            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
            if (!ServerUtils.isEmpty(filter.getSortBy())) {
                setSortColumn(filter.getSortBy());
                setSortType(filter.getSortDirection());
            }
            addLong("orderItemId", filter.getOrderItemId());
        }});
    }

    @Override
    public _ProduceHistory add(ProduceHistoryRequest request, MultipartFile file) {
        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (orderItem != null) {
            if ((orderItem.getCount() - orderItem.getGiven()) < request.getCount())
                throw new ValidatorException("Zakaz bo'yicha berilishi kerak bo'lgan mahsulot  miqdordan ko'p so'rayapsiz");
            double countByOrder = produceHistoryDao.getCountByOrderItem(orderItem);
            double requestDoneCount = produceHistoryDao.getCountByOrderItemAndReqDone(orderItem);
            if ((orderItem.getCount()) < (request.getCount() + countByOrder + orderItem.getGiven() - requestDoneCount))
                throw new ValidatorException("Oldinroq so'ralgan mahsulot miqdorlari bilan birga zakaz bo'yicha berilishi kerak bo'lgan mahsulot  miqdoridano oshib ketdi");
            if (Arrays.asList(_State.READY_TO_PRODUCE, _State.YES_PRODUCT).contains(orderItem.getState())) {
                _ProduceHistory produceHistory = new _ProduceHistory();
                produceHistory.setOrderItem(orderItem);
                produceHistory.setCount(request.getCount());
                produceHistory.setRemain(request.getCount());
                produceHistory.setGuessedTakenAwayDate(request.getGuessedTakenAwayDate());
                produceHistory.setAttachment(attachmentService.uploadPdf(file));
                if (orderItem.isSeenInitiatorNotification()) {
                    orderItem.setSeenInitiatorNotification(false);
                    orderItemDao.save(orderItem);
                }
                return produceHistoryDao.save(produceHistory);
            } else throw new ValidatorException("Zakaz so'ralgan mahsulotlar beriladigan statusda emas");

        } else throw new ValidatorException("Zakaz topilmadi");
    }

    @Override
    public void makeStateDone(_ProduceHistory produceHistory, double count, Long notificationId) {
        _OrderItem orderItem = produceHistory.getOrderItem();
        if (!Arrays.asList(_State.READY_TO_PRODUCE, _State.YES_PRODUCT).contains(orderItem.getState()))
            throw new ValidatorException("Zakaz so'ralgan mahsulotlar beriladigan statusda emas");
        produceHistory.setRemain(produceHistory.getRemain() - count);
        boolean isDone = false;
        if (produceHistory.getRemain() == 0.0) {
            produceHistory.setState(REQUEST_DONE);
            isDone = true;
        }
        produceHistoryDao.save(produceHistory);
        _Notification notification = notificationDao.get(notificationId);
        NotificationDataReq dataReq;
        if (notification != null) {
            dataReq = notification.getData();
            if (dataReq != null) {
                if (isDone)
                    dataReq.setIsProduceHistoryStateDone(true);
                else
                    dataReq.setCount(produceHistory.getRemain());
                notificationDao.save(notification);
            }
        }
    }

    @Override
    public void markAsSeen(Long orderItemId) {
        _OrderItem orderItem = orderItemDao.get(orderItemId);
        if (orderItem != null) {
            if (!orderItem.isSeenInitiatorNotification()) {
                orderItem.setSeenInitiatorNotification(true);
                orderItemDao.save(orderItem);
            }
        } else throw new ValidatorException(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"));
    }
}
