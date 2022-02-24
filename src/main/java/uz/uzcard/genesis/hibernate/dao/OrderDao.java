package uz.uzcard.genesis.hibernate.dao;

import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._Order;

import java.util.List;

public interface OrderDao extends Dao<_Order> {
    _Order getDefaultByDepartment(_Department department);

    _Order getByNumber(Integer numb);

    List<Facet> getAllSendOrder(DashboardFilter filterRequest);
}
