package uz.uzcard.genesis.hibernate.dao;

import org.hibernate.search.query.facet.Facet;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.List;
import java.util.stream.Stream;

public interface OrderItemDao extends Dao<_OrderItem> {
    List<String> getCategoriesByProduct(Long product_id);

    Stream<_OrderItem> findByIds(List<Long> ids);

    Stream<_OrderItem> findByOrder(_Order order);

    Stream<Long> getStatusChangedUsers(String name);

    List<_OrderItem> findByContractItem(_ContractItem contractItem);

    List findByContractItemAndByNotStates(_ContractItem contractItem, String... state);

    Boolean findOrderItemsByOrder(_Order order, String... state);

    Integer findOrderItemMaxNumb(Long orderId);

    _OrderItem getByProductInOrder(Long orderId, Long productId);

    Double getAllBronByProduct(_Product product);

    _OrderItem getLastByOrder(_Order order);

    List<Facet> getOrderItemsByProductCategory(FilterParameters filter);

    List<Facet> getDefineOrderStatusCount(FilterParameters filter);

    List<Facet> departmentOrderItems(FilterParameters filter);

    Stream<_OrderItem> findAll(_Order order);

    Stream<_OrderItem> findAllByContractItem(_ContractItem contractItem);

    int getTotalCount(_Order order);

    int getAcceptCount(_Order order);
}