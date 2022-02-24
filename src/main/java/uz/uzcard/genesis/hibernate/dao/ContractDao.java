package uz.uzcard.genesis.hibernate.dao;

import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._Department;

import java.util.List;

public interface ContractDao extends Dao<_Contract> {
    _Contract getDefaultByDepartment(_Department department);

    boolean checkByNum(String code);

    boolean checkByIdWithCode(Long id, String code);

    _Contract getByCode(String contractCode);

    List<Facet> getBySupplier(DashboardFilter filterRequest);

    List<Facet> getByContractStatus(DashboardFilter filterRequest, String status, String groupBy, String filterBy);
}
