package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.setting.ProduceHistoryFilter;
import uz.uzcard.genesis.dto.api.req.setting.ProduceHistoryRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._ProduceHistory;

import javax.mail.Multipart;

public interface ProduceHistoryService {
    PageStream<_ProduceHistory> list(ProduceHistoryFilter filter);

    _ProduceHistory add(ProduceHistoryRequest request, MultipartFile file);

    void makeStateDone(_ProduceHistory produceHistory, double count, Long notificationId);

    void markAsSeen(Long orderItemId);
}
