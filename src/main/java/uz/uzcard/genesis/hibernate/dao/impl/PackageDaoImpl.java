package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.api.req.setting.PackageRequest;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.PackageDao;
import uz.uzcard.genesis.hibernate.entity._Package;

@Component(value = "packageDao")
public class PackageDaoImpl extends DaoImpl<_Package> implements PackageDao {
    public PackageDaoImpl() {
        super(_Package.class);
    }

    @Override
    public _Package getByRequestFilter(PackageRequest request) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("product.id", "" + request.getProduct_id());
        searchFilter.and("type.id", "" + request.getPackageType_id());
        searchFilter.and("width", "" + request.getWidth());
        searchFilter.and("height", "" + request.getHeight());
        searchFilter.and("depth", "" + request.getDepth());

        org.apache.lucene.search.Query luceneQuery = queryParser("parents", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Package.class);
        fullTextQuery.initializeObjectsWith(
                ObjectLookupMethod.SECOND_LEVEL_CACHE,
                DatabaseRetrievalMethod.QUERY
        );
        fullTextQuery.setCacheable(true);
        return (_Package) fullTextQuery.uniqueResult();
    }
}