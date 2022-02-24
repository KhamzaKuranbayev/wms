package uz.uzcard.genesis.service.impl;

import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.WarehouseSearchEngineFilterRequest;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.setting.WarehouseRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseFilterRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseSetupRequest;
import uz.uzcard.genesis.dto.api.req.warehouse.WarehouseYRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.Permissions;
import uz.uzcard.genesis.service.CarriageService;
import uz.uzcard.genesis.service.ProductItemService;
import uz.uzcard.genesis.service.WarehouseService;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;
    @Autowired
    private WarehouseXDao warehouseXDao;
    @Autowired
    private WarehouseYDao warehouseYDao;
    @Autowired
    private StillageDao stillageDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private ProductItemService productItemService;
    @Autowired
    private CarriageService carriageService;

    @Override
    public PageStream<_Warehouse> list(WarehouseFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            setSortType(request.getSortDirection());
            setSortColumn(request.getSortBy());
            setName(request.getName());
            if (request.getDepartmentId() != null)
                add("departmentId", "" + request.getDepartmentId());
        }};
        hasPermission(filter);
        return warehouseDao.search(filter);
    }

    @Override
    public PageStream<_Warehouse> ownList() {
        if (!SessionUtils.getInstance().getRoles().contains("ADMIN")) {
            Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
            FilterParameters filter = new FilterParameters() {{
                addLong("departmentId", departmentId);
            }};
            return warehouseDao.search(filter);
        }
        return null;
    }

    private void hasPermission(FilterParameters filter) {
        if (!SessionUtils.getInstance().getPermissions().contains(Permissions.SEE_ALL_WAREHOUSE)) {
            if (SessionUtils.getInstance().getPermissions().contains(Permissions.SEE_ALL_WAREHOUSE_BY_TEAM)) {
                List<Long> teams = SessionUtils.getInstance().getTeams();
                filter.addStrings("departmentIds", departmentDao.getByAllTeamsByDepartments(teams).stream().map(aLong -> "" + aLong).collect(Collectors.toList()));
            } else {
                filter.addStrings("departmentIds", departmentDao.findChildAndMe(warehouseDao.getUser().getDepartment()).stream().map(aLong -> "" + aLong).collect(Collectors.toList()));
            }
        }
    }

    @Override
    public _Warehouse save(WarehouseRequest request) {
        _Warehouse warehouse = warehouseDao.get(request.getId());
        if (warehouse == null) {
            warehouse = new _Warehouse();
        }
        warehouse.setAddress(request.getAddress());
        warehouse.setNameEn(request.getNameEn());
        warehouse.setNameRu(request.getNameRu());
        warehouse.setNameUz(request.getNameUz());
        warehouseDao.save(warehouse);
        return warehouse;
    }

    @Override
    public List<SelectItem> getItems(String name) {
        FilterParameters filter = new FilterParameters() {
            {
                addString("name", name);
            }
        };
        hasPermission(filter);
        return warehouseDao.search(filter).stream()
                .map(warehouse -> new SelectItem(warehouse.getId(), warehouse.getNameByLanguage(), "" + warehouse.getId())).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new RpcException("ID_REQUIRED");
        _Warehouse warehouse = warehouseDao.get(id);
        if (warehouse == null)
            throw new RpcException("WAREHOUSE_NOT_FOUND");
        if (productItemService.hasAnyByWarehouse(id))
            throw new ValidatorException("Омборда маҳсулот мавжуд. Ўчиролмайсиз");
        warehouseDao.delete(id);
    }

    @Override
    public void permanentlyDelete(Long id) {
        delete(id);
        boolean has = true;
        int offset = 0, limit = 50;
        do {
            has = productItemService.deleteByWarehouse(id, offset, limit);
            offset += 100;
        } while (has);
    }

    @Override
    public _Warehouse setup(WarehouseSetupRequest request) {
        _Warehouse warehouse = warehouseDao.get(request.getId());
        if (warehouse == null)
            throw new ValidatorException("Омборхонани танланг");
        for (int colIndex = warehouse.getColumns().size(); colIndex < request.getColumnCount(); colIndex++) {
            _WarehouseX warehouseX = new _WarehouseX(warehouse, ServerUtils.getAlphabetCode(colIndex), colIndex);
            if (!request.isPreview())
                warehouseXDao.save(warehouseX);
            warehouse.getColumns().add(warehouseX);
        }
        for (int colIndex = 0; colIndex < request.getColumnCount(); colIndex++) {
            if (request.getRows().size() <= colIndex)
                throw new ValidatorException("Қаторлар сони етарли эмас");

            _WarehouseX column = warehouse.getColumns().get(colIndex);
            List<WarehouseSetupRequest.Cell> rows = request.get(colIndex);

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                WarehouseSetupRequest.Cell cell = rows.get(rowIndex);
                _WarehouseY row = column.getRows().size() > rowIndex
                        ? column.getRows().get(rowIndex)
                        : new _WarehouseY(column, rowIndex, cell.getPlaceType());
                row.setPlaceType(cell.getPlaceType());
                if (!request.isPreview())
                    warehouseYDao.save(row);
                if (!column.getRows().contains(row))
                    column.getRows().add(row);
            }
        }
        if (!request.isPreview())
            warehouseDao.save(warehouse);
        return warehouse;
    }

    @Override
    public _Warehouse get(WarehouseFilterRequest request) {
        return warehouseDao.get(request.getId());
    }

    @Override
    public PageStream<_Warehouse> searchEngine(WarehouseSearchEngineFilterRequest request) {
        List<String> founds = new ArrayList<>();
        if (!StringUtils.isEmpty(request.getName())) {
            List<Facet> facets = productItemDao.searchWarehouseByProduct(request);
            founds.addAll(facets.stream().skip(request.getPage() * request.getLimit()).limit(request.getLimit())
                    .map(Facet::getValue).collect(Collectors.toList()));
        }
        if (!StringUtils.isEmpty(request.getName()) && founds.isEmpty())
            return new PageStream<>(Stream.empty(), 0);
        FilterParameters filter = new FilterParameters() {{
            setStart(0);
            setSize(Integer.MAX_VALUE);
            addStrings("ids", founds);
        }};
        hasPermission(filter);
        return warehouseDao.search(filter);
    }

    @Override
    public void reindex(Long id) {
        warehouseDao.reindex(List.of(id));
    }

    @Override
    public LinkedHashSet<Long> searchByProduct(WarehouseFilterRequest request) {
        return productItemDao.searchWarehouseYByProduct(request);
    }

    @Override
    public void deleteCell(Long cellId) {
        _WarehouseY warehouseY = warehouseYDao.get(cellId);
        _Stillage stillage = stillageDao.getByCell(cellId);
        if (stillage != null)
            throw new ValidatorException("Бу ерда стилаж бор");
        warehouseYDao.delete(warehouseY);
    }

    @Override
    public _WarehouseY updateCell(WarehouseYRequest request) {
        _WarehouseY warehouseY = warehouseYDao.get(request.getId());
        if (warehouseY == null)
            throw new ValidatorException("Ячейка топилмади");
        warehouseY.setPlaceType(request.getPlaceType());
        warehouseYDao.save(warehouseY);
        return warehouseY;
    }

    @Override
    public void setPercentageAll() {
        warehouseDao.findAll().forEach(warehouse -> {
            warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
            warehouseDao.save(warehouse);
        });
    }

    @Override
    public PageStream<_Warehouse> fullNess(DashboardFilter request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            addBoolean("hasProduct", true);
            setSortColumn("percentSort");
            setSortType(false);
        }};
        hasPermission(filter);
        return warehouseDao.search(filter);
    }

    @Override
    public _Warehouse recalculate(Long warehouseId) {
        _Warehouse warehouse = warehouseDao.get(warehouseId);
        if (warehouse == null)
            throw new ValidatorException("Омборхона топилмади");
        List<_Carriage> carriages = carriageDao.findByWarehouse(warehouseId).collect(Collectors.toList());
        carriageService.checkToHasProduct(carriages);
        warehouse.setOccupancyPercent(carriageDao.totalCarriageCountyWarehouse(warehouse));
        return warehouseDao.save(warehouse);
    }

    @Override
    public _WarehouseY getCellInfo(Long cellId) {
        return warehouseYDao.get(cellId);
    }
}