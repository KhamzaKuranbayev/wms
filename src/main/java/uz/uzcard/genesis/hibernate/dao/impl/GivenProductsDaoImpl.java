package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.commons.lang.text.StrBuilder;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.order.GivenProductProductItemFilter;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.GivenProductsDao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 26.09.2020  16:07
 */
@Component("GivenProductsDao")
public class GivenProductsDaoImpl extends DaoImpl<_GivenProducts> implements GivenProductsDao {

    public GivenProductsDaoImpl() {
        super(_GivenProducts.class);
    }

    @Override
    public PageStream<_GivenProducts> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractCode")) {
            searchFilter.and("contractItem.parent.code", filter.getString("contractCode"));
        }
        if (filter.has("orderNumb")) {
            searchFilter.and("orderItem.parent.numb", filter.getString("orderNumb"));
        }
        if (filter.has("orderItemId")) {
            searchFilter.and("orderItem.id", filter.getString("orderItemId"));
        }
        if (filter.has("warehouseId")) {
            searchFilter.and("warehouse.id", filter.getString("warehouseId"));
        }
        if (filter.has("productName")) {
            searchFilter.and("partition.product.name", filter.getString("productName"));
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        searchFilter.and("-state", _State.PRODUCT_ACCEPTED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _GivenProducts.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_GivenProducts>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Double getRemainsPartition(_Partition partition) {
        Double count = (Double) findSingle("select sum(t.remains) from _GivenProducts t where t.state = :state and t.partition = :partition ",
                preparing(new Entry("partition", partition), new Entry("state", _State.NEW)));

        return count == null ? 0 : count;
    }

    @Override
    public Double getRemainsOrderItemByState(_OrderItem orderItem, String state) {
        Double count = (Double) findSingle("select sum(t.count) from _GivenProducts t where t.state = :state and t.orderItem = :orderItem ",
                preparing(new Entry("orderItem", orderItem), new Entry("state", state)));
        return count == null ? 0 : count;
    }

    @Override
    public Double getRemainsOrderItem(_OrderItem orderItem) {
        Double count = (Double) findSingle("select sum(t.count) from _GivenProducts t where t.state != :deleted and t.orderItem = :orderItem ",
                preparing(new Entry("orderItem", orderItem), new Entry("deleted", _State.DELETED)));
        return count == null ? 0 : count;
    }

    @Override
    public _GivenProducts getByPartitionAndOrderItem(_Partition partition, _OrderItem orderItem) {
        return (_GivenProducts) findSingle("select t from _GivenProducts t where t.state <> :deleted and t.partition = :partition and t.orderItem = :orderItem",
                preparing(new Entry("partition", partition), new Entry("orderItem", orderItem), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public _GivenProducts getNewByPartitionAndOrderItem(_Partition partition, _OrderItem orderItem) {
        return (_GivenProducts) findSingle("select t from _GivenProducts t where t.state not in (:states) and t.partition = :partition and t.orderItem = :orderItem",
                preparing(new Entry("partition", partition), new Entry("orderItem", orderItem),
                        new Entry("states", Arrays.asList(_State.DELETED, _State.PRODUCT_ACCEPTED))));
    }

    @Override
    public Stream<_ProductItem> getByGivenProductParamsForProductItem(GivenProductProductItemFilter filter) {
        int limit = filter.getLimit();
        int offet = filter.getLimit() * filter.getPage();
        List<Entry> entries = new ArrayList<>() {{
            add(new Entry("deleted", _State.DELETED));
        }};
        StrBuilder query = new StrBuilder("select distinct pi from _GivenProducts pg " +
                " left join pg.partition p on pg.partition = p " +
                " left join _ProductItem pi on p = pi.partition " +
                " where pg.state <> :deleted and p.state <> :deleted and pi.state <> :deleted ");

        if (filter.getOrderItemId() != null) {
            query.append("and pg.orderItem.id = :orderItemId");
            entries.add(new Entry("orderItemId", filter.getOrderItemId()));
        }
        if (filter.getOrderId() != null) {
            query.append("and pg.orderItem.parent.id = :orderId");
            entries.add(new Entry("orderId", filter.getOrderId()));
        }

        if (filter.getContractItemId() != null) {
            query.append("and pg.contractItem.id = :contractItemId");
            entries.add(new Entry("contractItemId", filter.getContractItemId()));
        }
        if (filter.getContractId() != null) {
            query.append("and pg.contractItem.parent.id = :contractId");
            entries.add(new Entry("contractId", filter.getContractId()));
        }
        if (filter.getPartionId() != null) {
            query.append("and pg.partition.id = :partitionId");
            entries.add(new Entry("partitionId", filter.getPartionId()));
        }
        if (filter.getProductName() != null) {
            query.append("and lower(pg.partition.orderItem.product.name) = lower(:productName)");
            entries.add(new Entry("productName", filter.getProductName()));
        }

        Entry[] params = entries.toArray(Entry[]::new);
        return findInterval(query.toString(),
                preparing(params), offet, limit);
    }

    @Override
    public Long getByGivenProductParamsForProductItemCount(GivenProductProductItemFilter filter) {
        List<Entry> entries = new ArrayList<>() {{
            add(new Entry("deleted", _State.DELETED));
        }};
        StrBuilder query = new StrBuilder("select count(distinct pi) from _GivenProducts pg " +
                " left join pg.partition p on pg.partition = p " +
                " left join _ProductItem pi on p = pi.partition " +
                " where pg.state <> :deleted and p.state <> :deleted and pi.state <> :deleted ");

        if (filter.getOrderItemId() != null) {
            query.append("and pg.orderItem.id = :orderItemId");
            entries.add(new Entry("orderItemId", filter.getOrderItemId()));
        }
        if (filter.getOrderId() != null) {
            query.append("and pg.orderItem.parent.id = :orderId");
            entries.add(new Entry("orderId", filter.getOrderId()));
        }

        if (filter.getContractItemId() != null) {
            query.append("and pg.contractItem.id = :contractItemId");
            entries.add(new Entry("contractItemId", filter.getContractItemId()));
        }
        if (filter.getContractId() != null) {
            query.append("and pg.contractItem.parent.id = :contractId");
            entries.add(new Entry("contractId", filter.getContractId()));
        }
        if (filter.getPartionId() != null) {
            query.append("and pg.partition.id = :partitionId");
            entries.add(new Entry("partitionId", filter.getPartionId()));
        }
        if (filter.getProductName() != null) {
            query.append("and lower(pg.partition.orderItem.product.name) = lower(:productName)");
            entries.add(new Entry("productName", filter.getProductName()));
        }

        Entry[] params = entries.toArray(Entry[]::new);
        return (Long) findSingle(query.toString(), preparing(params));
    }

    @Override
    public List<String> getWarehouseList(_OrderItem orderItem) {
        if (orderItem == null)
            return Collections.emptyList();
        PageStream<_GivenProducts> pageStream = search(new FilterParameters() {{
            setSize(Integer.MAX_VALUE);
            addLong("orderItemId", orderItem.getId());
            add("status", _State.NEW);
        }});
        if (pageStream.getSize() < 1)
            return Collections.emptyList();
        return pageStream.stream().map(givenProduct -> {
            if (givenProduct.getWarehouse() == null) {
                return null;
            }
            return givenProduct.getWarehouse().getNameByLanguage();
        }).distinct().collect(Collectors.toList());
    }

    /*@Override
    public PageStream<_OrderItem> getOrderItems(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("numbSearch"))
            searchFilter.and("orderItem.parent.numb", filter.getString("numbSearch") + "*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _GivenProducts.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        Stream<_OrderItem> orderItemStream = ((Stream<_GivenProducts>) fullTextQuery.stream()).map(givenProducts -> givenProducts.getOrderItem())
                .distinct().skip(filter.getStart()).limit(filter.getSize());
        Long totalCount = ((Stream<_GivenProducts>) fullTextQuery.stream()).map(givenProducts -> givenProducts.getOrderItem()).distinct().count();
        return new PageStream<_OrderItem>(orderItemStream, totalCount.intValue());
    }*/

}
