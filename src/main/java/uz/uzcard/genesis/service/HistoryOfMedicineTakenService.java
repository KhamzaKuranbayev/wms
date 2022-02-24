package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenFilterRequest;
import uz.uzcard.genesis.dto.api.req.patient.HistoryOfMedicineTakenRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignsRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._HistoryOfMedicineTaken;

import java.util.Collection;
import java.util.List;

public interface HistoryOfMedicineTakenService {

    PageStream<_HistoryOfMedicineTaken> list(HistoryOfMedicineTakenFilterRequest filter);

    PageStream<_HistoryOfMedicineTaken> holdOn(HistoryOfMedicineTakenFilterRequest filter);

    SingleResponse save(HistoryOfMedicineTakenRequest request);

    Long count(Long patientId);

    SingleResponse produce(List<Long> ids);

    SingleResponse removeHold(Long id);

    SingleResponse setHashESign(HashESignsRequest request);
}
