package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._Warehouse;
import uz.uzcard.genesis.hibernate.enums.OrderClassification;

import java.util.List;
import java.util.stream.Stream;

public interface DepartmentDao extends Dao<_Department> {

    _Department getOneByType(OrderClassification type);

    Stream<_Department> getDepartmentByIds(List<Long> ids);

    List<Long> findChildAndMe(_Department department);

    List<Long> getByAllTeamsByDepartments(List<Long> teamIds);

    Stream<_Department> findAll();

    Stream<_Department> findDepartmentByWarehouse(_Warehouse warehouse);
}