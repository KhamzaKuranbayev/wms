package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.PatientDao;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Patient;
import uz.uzcard.genesis.hibernate.entity._State;

import static uz.uzcard.genesis.uitls.StateConstants.DELETED;

@Component("patientDao")
public class PatientDaoImpl extends DaoImpl<_Patient> implements PatientDao {

    public PatientDaoImpl() {
        super(_Patient.class);
    }

    @Override
    public PageStream<_Patient> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("id"))
            searchFilter.and("id", filter.getString("id"));
        if (filter.has("fio"))
            searchFilter.and("fio", filter.getString("fio").trim() + "*");
        if (filter.has("passportNumber"))
            searchFilter.and("pasNumber", filter.getString("passportNumber").trim() + "*");

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _Patient.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_Patient>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _Patient getByPassportNumber(String passportNumber) {
        return (_Patient) findSingle("select t from _Patient t " +
                        " where t.state != :deleted and t.pasNumber = :passportNumber ",
                preparing(new Entry("deleted", DELETED), new Entry("passportNumber", passportNumber)));
    }
}
