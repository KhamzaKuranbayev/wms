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
import uz.uzcard.genesis.hibernate.dao.UnitTypeDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._UnitType;

import java.util.List;
import java.util.stream.Stream;

@Component(value = "unitTypeDao")
public class UnitTypeDaoImpl extends DaoImpl<_UnitType> implements UnitTypeDao {
    public UnitTypeDaoImpl() {
        super(_UnitType.class);
    }

    @Override
    public PageStream<_UnitType> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("name")) {
            SearchFilter temp = new SearchFilter();
            temp.and("nameEn", filter.getString("name").toLowerCase().trim());
            temp.or("nameUz", filter.getString("name").toLowerCase().trim());
            temp.or("nameRu", filter.getString("name").toLowerCase().trim());
            searchFilter.and(String.format("( %s )", temp.toString()));
        }
        if (searchFilter.toString().isEmpty()) {
            searchFilter.and("*:*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _UnitType.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setSort(new Sort(new SortField("id", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_UnitType>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Stream<_UnitType> findAllByIds(List<Long> ids) {
        return find("select t from _UnitType t where t.id in (:ids) and t.state <> :deleted ",
                preparing(new Entry("ids", ids), new Entry("deleted", _State.DELETED)));
    }

    @Override
    public _UnitType getById(Long id) {
        return (_UnitType) findSingle("select t from _UnitType t where t.id in (:id) and t.state <> :deleted ",
                preparing(new Entry("id", id), new Entry("deleted", _State.DELETED)));
    }
}
