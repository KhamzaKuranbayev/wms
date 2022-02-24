package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.product.InventarizationFilter;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.InventarizationDao;
import uz.uzcard.genesis.hibernate.dao.WarehouseDao;
import uz.uzcard.genesis.hibernate.entity._Inventarization;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.service.InventarizationService;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.Date;

/**
 * Created by norboboyev_h  on 08.09.2020  12:11
 */
@Service
public class InventarizationServiceImpl implements InventarizationService {
    @Autowired
    private InventarizationDao inventarizationDao;
    @Autowired
    private WarehouseDao warehouseDao;

    @Override
    public PageStream<_Inventarization> list(InventarizationFilter request) {
        return inventarizationDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (!SessionUtils.getInstance().getRoles().contains("ADMIN")) {
                addString("auditorId", "" + SessionUtils.getInstance().getUserId());
            }
            if (!ServerUtils.isEmpty(request.getStartToDate()))
                addDate("startToDate", request.getStartToDate());
            if (!ServerUtils.isEmpty(request.getStartFromDate()))
                addDate("startFromDate", request.getStartFromDate());
            if (!ServerUtils.isEmpty(request.getAuditorId()))
                addString("auditorId", "" + request.getAuditorId());
        }});
    }


    @Override
    public _Inventarization getByWarehouse(Long warehouseId) {
        _Warehouse warehouse = warehouseDao.get(warehouseId);
        if (warehouse == null)
            throw new ValidatorException("Омборхона торилмади");
        _Inventarization inventarization = inventarizationDao.getByWarehouse(warehouse);
        return inventarization;
    }

    @Override
    public _Inventarization start(Long warehouseId) {
        _Inventarization inventarization = getByWarehouse(warehouseId);
        if (inventarization == null || inventarization.getEndedAt() != null) {
            inventarization = new _Inventarization();
            inventarization.setWarehouse(warehouseDao.get(warehouseId));
            inventarization.setStartedAt(new Date());
            inventarizationDao.save(inventarization);
            return inventarization;
        }
        throw new ValidatorException("Ушбу омборда инвентаризация бошланган. Давом эттиришингиз мумкин");
    }

    @Override
    public _Inventarization end(Long warehouseId) {
        _Inventarization inventarization = getByWarehouse(warehouseId);
        if (inventarization == null || inventarization.getEndedAt() != null)
            throw new ValidatorException("Ушбу инвентаризация тугатилган. Янгидан бошлашингиз лозим");

        inventarization.setEndedAt(new Date());
        inventarizationDao.save(inventarization);
        return inventarization;
    }
}