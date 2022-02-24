package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.contract.RealizationFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.RealizationRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Realization;

public interface RealizationService {
    PageStream<_Realization> list(RealizationFilterRequest request);

    _Realization save(RealizationRequest request);
}
