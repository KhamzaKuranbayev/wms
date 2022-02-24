package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.warehouse.StillageFilterRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.StillageListFilterRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.StillageRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.StillageSetupRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Stillage;

public interface StillageService {
    PageStream<_Stillage> board(StillageFilterRequest request);

    PageStream<_Stillage> list(StillageListFilterRequest request);

    _Stillage save(StillageRequest request);

    void splitColumn(Long stillage_id, Integer columnCount);

    _Stillage setup(StillageSetupRequest request);

    _Stillage get(StillageFilterRequest request);

    _Stillage getByCell(Long cellId);
}