package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.InventarizationFilter;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Inventarization;

public interface InventarizationService {
    PageStream<_Inventarization> list(InventarizationFilter request);

    _Inventarization getByWarehouse(Long warehouseId);

    _Inventarization start(Long warehouseId);

    _Inventarization end(Long warehouseId);
}