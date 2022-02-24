package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.config.properties.ProjectConfig;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.dao.WarehouseDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.service.DepartmentService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao departmentDao;

    private final UserDao userDao;
    private final WarehouseDao warehouseDao;
    private final ProjectConfig projectConfig;

    @Autowired
    public DepartmentServiceImpl(DepartmentDao departmentDao, UserDao userDao, WarehouseDao warehouseDao,
                                 ProjectConfig projectConfig) {
        this.departmentDao = departmentDao;
        this.userDao = userDao;
        this.warehouseDao = warehouseDao;
        this.projectConfig = projectConfig;
    }

    @Override
    public ListResponse items(DepartmentFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getLimit() * request.getPage());
            setSize(request.getLimit());
            setSortColumn(request.getSortBy());
            if (!ServerUtils.isEmpty(request.getName()))
                addString("name", request.getName());
            if (!ServerUtils.isEmpty(request.getParentId()))
                addLong("parentId", request.getParentId());
        }};
        filter.setSortType(request.getSortDirection());
        Stream<_Department> departments = departmentDao.list(filter);

        Integer total = departmentDao.total(filter);
        return ListResponse.of(departments.map(department -> {
            return new SelectItem(department.getId(), department.getNameByLanguage(), "" + department.getId());
        }).collect(Collectors.toList()), total);
    }

    @Override
    public ListResponse list(DepartmentFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getLimit() * request.getPage());
            setSize(request.getLimit());
            setSortColumn(request.getSortBy());
            if (!ServerUtils.isEmpty(request.getName()))
                addString("name", request.getName());
            if (!ServerUtils.isEmpty(request.getParentId()))
                addLong("parentId", request.getParentId());
            if (request.isByCurrentUserTeams())
                addLongs("teamIds", SessionUtils.getInstance().getTeams());
        }};
        filter.setSortType(request.getSortDirection());
        Stream<_Department> departments = departmentDao.list(filter);

        Integer total = departmentDao.total(filter);
        return ListResponse.of(departments, total, (department, map) -> {
            map.add("name", department.getNameByLanguage());
            if (!department.getWarehouses().isEmpty()) {
                List<Map<String, String>> warehouses = new ArrayList<>();
                department.getWarehouses().forEach(warehouse -> {
                    Map<String, String> warehouseMap = new HashMap<>();
                    warehouseMap.put("warehouseId", "" + warehouse.getId());
                    warehouseMap.put("warehouseName", warehouse.getNameByLanguage());
                    warehouses.add(warehouseMap);
                });
                map.addStrings("warehouses", warehouses);
            }
            if (department.getParent() != null) {
                map.add("parentId", department.getParent().getId());
                map.add("parentName", department.getParent().getNameByLanguage());
            }
            if (department.getDepType() != null)
                map.add("depType", department.getDepType().name());
            return map;
        });
    }

    @Override
    public SingleResponse save(DepartmentRequest request) {
        if (request.getDepType() == null)
            throw new ValidatorException("Бўлим типини киритинг");
        _Department department = departmentDao.get(request.getId());
        if (department == null)
            department = new _Department();
        if (!ServerUtils.isEmpty(request.getParentId())) {
            _Department parent = departmentDao.get(request.getParentId());
            if (parent == null) {
                throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));
            }
            department.setParent(departmentDao.get(request.getParentId()));
        }

        if (projectConfig.departmentWarehouseRequired() && ServerUtils.isEmpty(request.getWarehouseIds()))
            throw new ValidatorException(GlobalizationExtentions.localication("WAREHOUSE_IS_REQUIRED"));

        Set<_Warehouse> warehouses = warehouseDao.getByIds(request.getWarehouseIds()).collect(Collectors.toSet());
        if (ServerUtils.isEmpty(warehouses))
            throw new ValidatorException(GlobalizationExtentions.localication("WAREHOUSE_NOT_FOUND"));

        for (_Warehouse warehouse : warehouses) {
            if (warehouse.getDepartment() != null) {
                if (warehouse.getDepartment() != null) {
                    _Department department1 = warehouse.getDepartment();
                    department1.getWarehouses().remove(warehouse);
                    departmentDao.save(department1);
                }
            }
        }

        department.setNameEn(request.getNameEn());
        department.setNameRu(request.getNameRu());
        department.setNameUz(request.getNameUz());
        department.setWarehouses(null);
        department.setDepType(request.getDepType());
        departmentDao.save(department);

        for (_Warehouse warehouse : warehouses) {
            warehouse.setDepartment(department);
            warehouseDao.save(warehouse);
        }
        department.setWarehouses(warehouses);
        departmentDao.save(department);
        return SingleResponse.of(department, (department2, map) -> {
            map.add("name", department2.getNameByLanguage());
            return map;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        _Department department = departmentDao.get(id);
        if (department == null)
            throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_NOT_FOUND"));

        List<_User> users = userDao.findByDepartment(department).collect(Collectors.toList());
        if (users != null && !users.isEmpty())
            throw new RpcException(GlobalizationExtentions.localication("DEPARTMENT_HAS_USERS_NOT_DELETE"));
        departmentDao.delete(department);
    }

}
