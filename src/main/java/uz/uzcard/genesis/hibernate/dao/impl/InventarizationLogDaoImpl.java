package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.InventarizationDao;
import uz.uzcard.genesis.hibernate.dao.InventarizationLogDao;
import uz.uzcard.genesis.hibernate.entity._Inventarization;
import uz.uzcard.genesis.hibernate.entity._InventarizationLog;
import uz.uzcard.genesis.hibernate.entity._State;

/**
 * Created by norboboyev_h  on 08.09.2020  10:38
 */
@Component(value = "inventarizationLogDao")
public class InventarizationLogDaoImpl extends DaoImpl<_InventarizationLog> implements InventarizationLogDao {

    @Autowired
    private InventarizationDao inventarizationDao;

    public InventarizationLogDaoImpl() {
        super(_InventarizationLog.class);
    }

    @Override
    public PageStream<_InventarizationLog> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("userId"))
            searchFilter.and("inventarization.auditInfo.createdByUser.id", filter.getString("userId"));
        if (filter.has("inventarizationId"))
            searchFilter.and("inventarization.id", filter.getString("inventarizationId"));
        if (filter.has("productId"))
            searchFilter.and("productItem.product.id", filter.getString("productId"));
        if (filter.has("productGroupId"))
            searchFilter.and("productItem.product.group.id", filter.getString("productGroupId"));
        if (filter.has("productTypeId"))
            searchFilter.and("productItem.product.type.id", filter.getString("productTypeId"));
        if (filter.has("isValid"))
            searchFilter.and("valid", "" + filter.getBool("isValid"));
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");

        searchFilter.and("-state", _State.DELETED);

        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _InventarizationLog.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_InventarizationLog>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _InventarizationLog save(_InventarizationLog entity) {
        super.save(entity);
        if (entity.getInventarization() != null) {
            _Inventarization inventarization = entity.getInventarization();
            inventarization.setInvalidsCount(getValidsAndInvalidsCount(inventarization, false));
            inventarization.setValidsCount(getValidsAndInvalidsCount(inventarization, true));
            inventarizationDao.save(inventarization);
        }
        return entity;
    }

    public Integer getValidsAndInvalidsCount(_Inventarization inventarization, boolean isForValids) {
        return ((Long) findSingle("select count(t.id) from _InventarizationLog t where t.inventarization = :inventarization and t.state <> :deleted and t.valid = :isValid",
                preparing(new Entry("inventarization", inventarization), new Entry("deleted", _State.DELETED), new Entry("isValid", isForValids)))).intValue();
    }
}
