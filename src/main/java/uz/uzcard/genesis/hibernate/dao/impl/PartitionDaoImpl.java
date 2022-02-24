package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.PartitionDao;
import uz.uzcard.genesis.hibernate.dao.ProductDao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._State;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PartitionDaoImpl extends DaoImpl<_Partition> implements PartitionDao {
    @Autowired
    private ProductDao productDao;

    public PartitionDaoImpl() {
        super(_Partition.class);
    }

    @Override
    public PageStream<_Partition> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractItemId"))
            searchFilter.and("contractItem.id", filter.getString("contractItemId"));
        if (filter.has("status"))
            searchFilter.and("statusType", filter.getString("status"));
        if (filter.has("productId"))
            searchFilter.and("product.id", filter.getString("productId"));
        if (filter.has("warehouseNameSearch"))
            searchFilter.and("warehouse.name", filter.getString("warehouseNameSearch").trim() + "*");
        if (filter.has("warehouseId"))
            searchFilter.and("warehouse.id", filter.getString("warehouseId"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("remains:{0.0 TO *]");


        if (filter.has("FROM_DATE") && filter.has("TO_DATE")) {
            Date toDate = filter.getDate("TO_DATE");
            searchFilter.and(String.format("expiration:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("FROM_DATE"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("FROM_DATE")) {
            searchFilter.and(String.format("expiration:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("FROM_DATE"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("TO_DATE")) {
            Date toDate = filter.getDate("TO_DATE");
            searchFilter.and(String.format("expiration:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }

        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Partition.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Partition>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Double getRemainsByProduct(_Product product) {
        return (Double) findSingle("select sum(t.remains) from _Partition t where t.state <> :deleted and t.product = :product",
                preparing(new Entry("product", product), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public _Partition get(Long contractItemId, Long warehouseId, LocalDate date) {
        return (_Partition) findSingle("select t from _Partition t left join t.contractItem ci left join t.warehouse w " +
                        " where t.state != :deleted and ci.id = :contractItem and w.id = :warehouseId and t.date = :date",
                preparing(new Entry("deleted", _State.DELETED), new Entry("contractItem", contractItemId),
                        new Entry("warehouseId", warehouseId), new Entry("date", date)));
    }

    @Override
    public Stream<_Partition> findAll() {
        return (Stream<_Partition>) find("select t from _Partition t where t.state != :deleted",
                preparing(new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_Partition> findAllByContractItem(_ContractItem contractItem) {
        return find("select t from _Partition t where t.contractItem = :contractItem",
                preparing(new Entry("contractItem", contractItem)));
    }

    @Transactional
    @Override
    public List<String> getExpiringPartitions(Long id, Long depId) {
        return (List<String>) findNativeInterval(" select distinct concat('(', wy.row_number, ':', wx.column_name, ')') from product_item pi " +
                " left join partitions p on pi.product_id = p.product_id " +
                " left join product_item_warehouse_y piwy on pi.id = piwy._product_item_id " +
                " left join warehouse_y wy on wy.id = piwy.cells_id " +
                " left join warehouse_x wx on wx.id = wy.column_id " +
                " left join warehouses w on pi.warehouse_id = w.id " +
                " left join department d on w.id = d.warehouse_id " +
                " where d.id = " + depId + " and p.id = " + id, null, 0, 0).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<String> getAllPartitions(Long depId) {

        return (List<String>) findNativeInterval(" select distinct concat(pi.name, p.expiration) " +
                        " from partitions p " +
                        " left join product_item pi on p.id = pi.partition_id " +
                        " left join warehouses w on pi.warehouse_id = w.id " +
                        " left join department d on w.id = d.warehouse_id " +
                        " where d.id = :id " +
                        " and p.state <> 'DELETED' " +
                        " and p.expiration <= current_date + 5 and p.expiration > current_date",
                _Partition.class, preparing(new Entry("id", depId)), 0, 0).collect(Collectors.toList());
    }

    @Override
    public _Partition save(_Partition entity) {
        super.save(entity);
        _Product product = entity.getProduct();
        Double remains = getRemainsByProduct(product);
        if (remains == null)
            remains = 0.0;
        product.setRemains(remains);
        productDao.save(product);
        return entity;
    }
}