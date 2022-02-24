package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.InventarizationDao;
import uz.uzcard.genesis.hibernate.entity.*;

/**
 * Created by norboboyev_h  on 08.09.2020  10:17
 */
@Component(value = "inventarizationDao")
public class InventarizationDaoImpl extends DaoImpl<_Inventarization> implements InventarizationDao {

    public InventarizationDaoImpl() {
        super(_Inventarization.class);
    }

    @Override
    public PageStream<_Inventarization> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();

        if (filter.has("auditorId")) {
            searchFilter.and("auditInfo.createdByUser.id", filter.getString("auditorId"));
        }
        if (filter.has("startFromDate") && filter.has("startToDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("startToDate")).plusDays(1).toDate();
            searchFilter.and(String.format("startedAt:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("startFromDate"), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        } else if (filter.has("startFromDate")) {
            searchFilter.and(String.format("startedAt:[%s TO %s]",
                    DateTools.dateToString(filter.getDate("startFromDate"), DateTools.Resolution.DAY),
                    DateTools.dateToString(new java.util.Date(), DateTools.Resolution.DAY)));
        } else if (filter.has("startToDate")) {
            java.util.Date toDate = new DateTime(filter.getDate("startToDate")).plusDays(1).toDate();
            searchFilter.and(String.format("startedAt:[%s TO %s]",
                    DateTools.dateToString(new java.util.Date(0, 0, 1), DateTools.Resolution.MILLISECOND),
                    DateTools.dateToString(toDate, DateTools.Resolution.MILLISECOND)));
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Inventarization.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setCacheable(true);
        fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Inventarization>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Inventarization getByWarehouse(_Warehouse warehouse) {
        return (_Inventarization) findSingle("select t from _Inventarization t " +
                        " where t.state != :deleted and t.warehouse = :warehouse " +
                        " order by t.id desc ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("warehouse", warehouse)));
    }

    @Override
    public _InventarizationLog getByInventarizationAndProductItem(_Inventarization inventarization, _ProductItem productItem) {
        return (_InventarizationLog) findSingle("select t from _InventarizationLog t left join t.inventarization i left join t.productItem p " +
                        " where i = :inventarization and p = :productItem and t.state != :deleted",
                preparing(new Entry("inventarization", inventarization), new Entry("productItem", productItem), new Entry("deleted", _State.DELETED)));
    }
}
