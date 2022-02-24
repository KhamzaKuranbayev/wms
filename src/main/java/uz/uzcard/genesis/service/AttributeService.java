package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.AttributeFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.AttributeRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Attribute;

public interface AttributeService {
    PageStream<_Attribute> list(AttributeFilterRequest request);

    _Attribute save(AttributeRequest request);

    void delete(Long id);
}
