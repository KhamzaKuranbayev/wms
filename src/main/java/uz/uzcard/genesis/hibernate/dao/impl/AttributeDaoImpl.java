package uz.uzcard.genesis.hibernate.dao.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.AttributeDao;
import uz.uzcard.genesis.hibernate.entity._Attribute;
import uz.uzcard.genesis.hibernate.entity._State;

@Component(value = "attributeDao")
public class AttributeDaoImpl extends DaoImpl<_Attribute> implements AttributeDao {
    public AttributeDaoImpl() {
        super(_Attribute.class);
    }

    @Override
    public PageStream<_Attribute> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("name")) {
            searchFilter.and("(name: " + filter.getName().trim() + "*" + " OR items:" + filter.getString("name") + ")");
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Attribute.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        if (filter.getSortColumn() != null)
            fullTextQuery.setSort(new Sort(new SortField(filter.getSortColumn(), SortField.Type.STRING, filter.getSortType())));
        else
            fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Attribute>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public boolean checkByName(String name) {
        return ((Long) findSingle("select count(t) from _Attribute t where lower(trim(t.name)) = lower(trim(:name)) and t.state <> :deleted",
                preparing(new Entry("name", name), new Entry("deleted", _State.DELETED)))) > 0;
    }

    @Override
    public boolean checkByNameAndOwn(Long id, String name) {
        return ((Long) findSingle("select count(t) from _Attribute t where lower(trim(t.name)) = lower(trim(:name)) and t.id != :id and t.state <> :deleted",
                preparing(new Entry("id", id), new Entry("name", name), new Entry("deleted", _State.DELETED)))) > 0;
    }

    @Override
    public _Attribute getById(Long id) {
        return (_Attribute) findSingle("select t from _Attribute t where t.state <> :deleted and t.id = :id ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("id", id)));
    }
}
