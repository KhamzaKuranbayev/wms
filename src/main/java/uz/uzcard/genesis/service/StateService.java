package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.setting.StateRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._State;

public interface StateService {
    PageStream<_State> listByEntityName(String entityName);

    _State save(StateRequest request);

    String getMessage(String code);

    String getColour(String code);

    String getDescription(String code);

    void wrapStatus(CoreMap map, String state);
}