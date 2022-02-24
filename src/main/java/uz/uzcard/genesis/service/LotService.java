package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.lot.LotFilterRequest;
import uz.uzcard.genesis.dto.api.req.partition.LotAddRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Lot;
import uz.uzcard.genesis.hibernate.enums.WarehouseReceivedType;

import java.util.HashMap;
import java.util.List;

public interface LotService {
    _Lot add(LotAddRequest request, WarehouseReceivedType warehouseReceivedType, boolean used);

    PageStream<_Lot> list(LotFilterRequest request);

    List<HashMap<String, String>> print(Long id);

    void reCalculate(_Lot lot);
}