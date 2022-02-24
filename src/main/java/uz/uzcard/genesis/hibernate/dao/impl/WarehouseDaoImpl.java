package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.WarehouseDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Warehouse;

import java.util.List;
import java.util.stream.Stream;

@Component(value = "warehouseDao")
public class WarehouseDaoImpl extends DaoImpl<_Warehouse> implements WarehouseDao {
    public WarehouseDaoImpl() {
        super(_Warehouse.class);
    }

    @Override
    public PageStream<_Warehouse> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("ids")) {
            searchFilter.and(String.format("id : ( %s )", String.join(", ", stringCollection(filter.getStrings("ids")))));
        }
        if (filter.has("departmentIds")) {
            searchFilter.and(String.format("department.id : ( %s )", String.join(", ", stringCollection(filter.getStrings("departmentIds")))));
        }
        if (filter.has("departmentId")) {
            searchFilter.and("department.id", filter.getString("departmentId"));
        }
        if (filter.has("name"))
            searchFilter.and("name", QueryParser.escape(filter.getString("name")) + "*");
        if (filter.getBool("hasProduct")) {
            searchFilter.and("occupancyPercent:{0.0 TO *]");
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString(), true);
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Warehouse.class);
        if (!StringUtils.isEmpty(filter.getSortColumn())) {
            if ("percentSort".equals(filter.getSortColumn()))
                fullTextQuery.setSort(new Sort(new SortField("percentSort", SortField.Type.INT, true)));
            else
                fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.STRING, !Boolean.TRUE.equals(filter.getSortType()))));
        } else {
            fullTextQuery.setSort(new Sort(new SortField("idSort", SortField.Type.LONG, true)));
        }
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Warehouse>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Stream<_Warehouse> findAll() {
        return (Stream<_Warehouse>) find("select t from _Warehouse t where t.state != :deleted",
                preparing(new Entry("deleted", _State.DELETED)));
    }

    @Override
    public Stream<_Warehouse> getByIds(List<Long> ids) {
        return (Stream<_Warehouse>) find("select t from _Warehouse t where t.state != :deleted and t.id in (:ids)",
                preparing(new Entry("deleted", _State.DELETED), new Entry("ids", ids)));
    }

    @Override
    public Stream<_Warehouse> findAllWarehouseByDepartment(Long id) {
        return (Stream<_Warehouse>) find("select t from _Warehouse t where t.state != :deleted and t.department.id = id ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("id", id)));
    }
}