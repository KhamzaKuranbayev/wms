package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.UnitTypeFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.UnitTypeRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._UnitType;

public interface UnitTypeService {
    _UnitType save(UnitTypeRequest request);

    PageStream<_UnitType> search(UnitTypeFilterRequest request);

    void delete(Long id);
}
