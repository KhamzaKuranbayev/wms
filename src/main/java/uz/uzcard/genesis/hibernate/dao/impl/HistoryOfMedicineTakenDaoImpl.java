package uz.uzcard.genesis.hibernate.dao.impl;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.springframework.stereotype.Component;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.HistoryOfMedicineTakenDao;
import uz.uzcard.genesis.hibernate.entity._HistoryOfMedicineTaken;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Patient;
import uz.uzcard.genesis.hibernate.entity._State;

import java.util.Arrays;

@Component("historyOfMedicineTakenDao")
public class HistoryOfMedicineTakenDaoImpl extends DaoImpl<_HistoryOfMedicineTaken> implements HistoryOfMedicineTakenDao {

    public HistoryOfMedicineTakenDaoImpl() {
        super(_HistoryOfMedicineTaken.class);
    }

    @Override
    public PageStream<_HistoryOfMedicineTaken> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("id"))
            searchFilter.and("id", filter.getString("id"));
        if (filter.has("medicineId"))
            searchFilter.and("medicine.id", filter.getString("medicineId"));
        if (filter.has("patientId"))
            searchFilter.and("patient.id", filter.getString("patientId"));

        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        if (filter.has("sessionId")) {
            searchFilter.and("doctor.id", filter.getString("sessionId"));
            searchFilter.and("state", _State.ON_HOLD);
        } else {
            searchFilter.and("state", _State.TAKEN_AWAY);
        }
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _HistoryOfMedicineTaken.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_HistoryOfMedicineTaken>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public Long getCount(Long patientId, Long sessionUserId) {
        return (Long) findSingle("select count(t) from _HistoryOfMedicineTaken t " +
                        " where t.state != :deleted and t.state = :status " +
                        " and t.doctor.id = :sessionId and t.patient.id = :patientId ",
                preparing(new Entry("deleted", _State.DELETED), new Entry("status", _State.ON_HOLD),
                        new Entry("patientId", patientId), new Entry("sessionId", sessionUserId)));
    }

    @Override
    public _HistoryOfMedicineTaken getByPatientAndMedicineAndState(Long patientId, Long medicineId, Long userId, String state) {
        return (_HistoryOfMedicineTaken) findSingle("select t from _HistoryOfMedicineTaken t " +
                        " where t.state = :status and t.patient.id = :patientId and " +
                        " t.medicine.id = :medicineId and t.doctor.id = :userId ",
                preparing(new Entry("status", state), new Entry("patientId", patientId),
                        new Entry("medicineId", medicineId), new Entry("userId", userId)));
    }
}
