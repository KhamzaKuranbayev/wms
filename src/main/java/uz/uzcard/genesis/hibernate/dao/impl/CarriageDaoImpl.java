package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.CarriageDao;
import uz.uzcard.genesis.hibernate.entity.*;

import java.util.List;
import java.util.stream.Stream;

@Component(value = "carriageDao")
public class CarriageDaoImpl extends DaoImpl<_Carriage> implements CarriageDao {
    public CarriageDaoImpl() {
        super(_Carriage.class);
    }

    @Override
    public PageStream<_Carriage> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("stillageColumnId"))
            searchFilter.and("stillageColumn.id", filter.getString("stillageColumnId"));
        if (filter.has("allSearch")) {
            searchFilter.and("stillageColumn.code", filter.getString("allSearch").trim() + "*");
//            searchFilter.or("stillageColumn.stillage.name", filter.getString("allSearch").trim() + "*");
//            searchFilter.or("stillageColumn.stillage.address", filter.getString("allSearch").trim() + "*");
//            searchFilter.or("stillageColumn.stillage.warehouse.address", filter.getString("allSearch").trim() + "*");
//            searchFilter.or("stillageColumn.stillage.warehouse.name", filter.getString("allSearch").trim() + "*");
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");

        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Carriage.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());

        return new PageStream<_Carriage>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public int getMaxOrderByColumn(_StillageColumn stillageColumn) {
        if (stillageColumn == null)
            return 0;
        if (stillageColumn.isNew())
            return stillageColumn.getCarriages().size();
        Integer numb = (Integer) findSingle("select max(t.sortOrder) from _Carriage t where t.stillageColumn = :stillageColumn and t.state <> :deleted",
                preparing(new Entry("stillageColumn", stillageColumn), new Entry("deleted", _State.DELETED)));
        if (numb == null) return 0;
        return numb.intValue();
    }

    @Override
    public Stream<_Carriage> findByIds(List<Long> ids) {
        return find("select t from _Carriage t where t.id in (:ids)",
                preparing(new Entry("ids", ids)));
    }

    @Override
    public Integer totalCarriageCountyWarehouse(_Warehouse warehouse) {
        Double percentage = ((Double) findSingle("select round(sum(case true when t.full then 100.0 when t.hasProduct then 50.0 else 0.0 end) / " +
                        "(case when count(t.id) = 0 or count(t.id) is null then 1 else count(t.id) end)) from _Carriage t " +
                        " left join t.stillageColumn sc " +
                        " left join sc.stillage s " +
                        " left join s.warehouse w  " +
                        " where t.state <> :deleted and sc.state <> :deleted and s.state <> :deleted and w = :warehouse",
                preparing(new Entry("deleted", _State.DELETED), new Entry("warehouse", warehouse))));
        return percentage == null ? 0 : percentage.intValue();
    }

    @Override
    public Stream<_Carriage> findByPartition(_Partition partition) {
        return (Stream<_Carriage>) findNativeInterval("select distinct c.* from product_item t " +
                        "left join product_item_carriage pic on t.id = pic._product_item_id " +
                        "left join carriage c on pic.carriages_id = c.id " +
                        "left join partitions p on t.partition_id = p.id " +
                        "where t.state <> :deleted and c.state <> :deleted and p.state <> :deleted and p.id = :partitionId", _Carriage.class,
                preparing(new Entry("deleted", _State.DELETED), new Entry("partitionId", partition.getId())), 0, Integer.MAX_VALUE);
    }

    @Override
    public Stream<_Carriage> findByWarehouse(Long warehouseId) {
        return find("select t from _Carriage t left join t.stillageColumn c left join c.stillage s left join s.warehouse w " +
                        " where t.state != :deleted and w.id = :warehouseId ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("warehouseId", warehouseId)));
    }
}
