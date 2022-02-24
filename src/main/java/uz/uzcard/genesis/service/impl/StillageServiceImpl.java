package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.warehouse.*;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.StillageColumnService;
import uz.uzcard.genesis.service.StillageService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StillageServiceImpl implements StillageService {

    @Autowired
    private StillageDao stillageDao;
    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private StillageColumnDao stillageColumnDao;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private StillageColumnService stillageColumnService;
    @Autowired
    private WarehouseYDao warehouseYDao;
    @Autowired
    private CarriageDao carriageDao;

    @Override
    public PageStream<_Stillage> board(StillageFilterRequest request) {
        return stillageDao.search(new FilterParameters() {{
            setId(request.getStillageId());
            addLong("cellId", request.getCellId());
            setSize(Integer.MAX_VALUE);
        }});
    }

    @Override
    public PageStream<_Stillage> list(StillageListFilterRequest request) {
        return stillageDao.search(new FilterParameters() {{
            addLong("warehouseId", request.getWarehouseId());
            add("name", request.getName());
        }});
    }

    @Override
    public _Stillage save(StillageRequest request) {
        _Stillage stillage = stillageDao.get(request.getId());
        if (stillage == null) {
            stillage = new _Stillage();
        }
        stillage.setName(request.getName());
        stillage.setAddress(request.getAddress());
        stillage.setWidth(request.getWidth());
        stillage.setHeight(request.getHeight());
        stillage.setDepth(request.getDepth());
        stillage.setWarehouse(warehouseDao.get(request.getWarehouse_id()));
        stillage.setColumnCount(request.getColumnCount());
        if (!request.isPreview())
            stillageDao.save(stillage);

        /*if (!ServerUtils.isEmpty(request.getCarriageRequest())) {
            createStillageColumnAndCarriage(request.getCarriageRequest(), stillage);
        } else*/
        if (!request.isPreview())
            splitColumn(stillage.getId(), request.getColumnCount());
        return stillage;
    }

    @Override
    public void splitColumn(Long stillage_id, Integer columnCount) {
        _Stillage stillage = stillageDao.get(stillage_id);
        if (ServerUtils.isEmpty(stillage)) {
            throw new ValidatorException("STILLAGE_IS_NULL");
        }
        stillage.setColumnCount(columnCount);

        for (int i = 0; i < stillage.getColumnCount(); i++) {
            String key = ServerUtils.getAlphabetCode(i);
            _StillageColumn column = stillageColumnService.create(stillage, key, false);
            stillage.getColumns().add(column);
        }
        stillageColumnDao.findByStillage(stillage).skip(columnCount)
                .forEach(stillageColumn -> {
                    stillage.getColumns().remove(stillageColumn);
                    stillageColumnDao.delete(stillageColumn);
                });
    }

    @Override
    public _Stillage setup(StillageSetupRequest request) {
        _Warehouse warehouse = warehouseDao.get(request.getWarehouseId());
        if (warehouse == null)
            throw new ValidatorException(GlobalizationExtentions.localication("WAREHOUSE_NOT_FOUND"));
        if (request.getCellIds() == null || request.getCellIds().isEmpty())
            throw new ValidatorException("Ячейкани танланг");
        _Stillage stillage = stillageDao.getByCell(request.getCellIds().get(0));
//        _Stillage stillage = stillageDao.get(request.getId());
        if (stillage == null) {
            stillage = save(new StillageRequest() {{
                setWarehouse_id(request.getWarehouseId());
                if (request.getWidth() != null) {
                    setWidth(request.getWidth());
                }
                if (request.getHeight() != null) {
                    setHeight(request.getHeight());
                }
                setDepth(1);
                setAddress(warehouse.getAddress());
                setName(warehouse.getNameByLanguage());
                setPreview(request.isPreview());
            }});

            warehouse.getStillages().add(stillage);
        }
        List<_WarehouseY> cells = warehouseYDao.findByIds(request.getCellIds()).collect(Collectors.toList());
        if (stillage.getCells().isEmpty())
            stillage.getCells().addAll(cells);
        else
            stillage.getCells().retainAll(cells);

        for (int colIndex = 0; colIndex < request.getColumnCount(); colIndex++) {
            String code = ServerUtils.getAlphabetCode(colIndex);
            _StillageColumn column = null;
            if (stillage.getColumns().size() > colIndex) {
                column = stillage.getColumns().get(colIndex);
                column.setSortOrder(colIndex);
                column.setCode(code);
                stillageColumnDao.save(column);
            } else {
                column = stillageColumnService.create(stillage, code, request.isPreview());
                stillage.getColumns().add(column);
            }
        }
        for (int colIndex = 0; colIndex < request.getColumnCount(); colIndex++) {
            _StillageColumn column = stillage.getColumns().get(colIndex);
            if (request.getRowCounts().size() <= colIndex)
                throw new ValidatorException(GlobalizationExtentions.localication("NUMBER_OF_ROWS_IS_NOT_ENOUGH"));
            for (int rowIndex = 0; rowIndex < request.getRowCounts().get(colIndex); rowIndex++) {
                _Carriage carriage = null;
                if (column.getCarriages().size() > rowIndex) {
                    carriage = column.getCarriages().get(rowIndex);
                    carriage.setSortOrder(rowIndex);
                    carriageDao.save(carriage);
                } else {
                    carriage = carriageService.create(column, new CarriageRequest(column.getId(), request.isPreview()));
                    column.getCarriages().add(carriage);
                }
            }
            if (!request.isPreview())
                stillageColumnDao.save(column);
        }
        if (!request.isPreview()) {
            stillage.setWidth(request.getWidth());
            stillage.setHeight(request.getHeight());
            stillageDao.save(stillage);
            warehouseDao.save(warehouse);
        }
        return stillage;
    }

    @Override
    public _Stillage get(StillageFilterRequest request) {
        if (request.getStillageId() != null)
            return stillageDao.get(request.getStillageId());
        PageStream<_Stillage> pageStream = board(request);

        Optional<_Stillage> first = pageStream.stream().findFirst();
        return first.orElse(null);
    }

    @Override
    public _Stillage getByCell(Long cellId) {
        return stillageDao.getByCell(cellId);
    }
}