package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.WarehouseSearchEngineFilterRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.setting.WarehouseRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseSetupRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseYRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.hibernate.entity._WarehouseY;

import java.util.LinkedHashSet;
import java.util.List;

public interface WarehouseService {
    PageStream<_Warehouse> list(WarehouseFilterRequest request);

    PageStream<_Warehouse> ownList();

    _Warehouse save(WarehouseRequest request);

    List<SelectItem> getItems(String name);

    void delete(Long id);

    void permanentlyDelete(Long id);

    _Warehouse setup(WarehouseSetupRequest request);

    _Warehouse get(WarehouseFilterRequest request);

    PageStream<_Warehouse> searchEngine(WarehouseSearchEngineFilterRequest request);

    void reindex(Long id);

    LinkedHashSet<Long> searchByProduct(WarehouseFilterRequest request);

    void deleteCell(Long cellId);

    _WarehouseY updateCell(WarehouseYRequest request);

    void setPercentageAll();

    PageStream<_Warehouse> fullNess(DashboardFilter request);

    _Warehouse recalculate(Long warehouseId);

    _WarehouseY getCellInfo(Long cellId);
}