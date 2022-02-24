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
import uz.uzcard.genesis.hibernate.dao.UserAgreementDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.math.BigInteger;
import java.util.stream.Stream;

@Component(value = "userAgreementDao")
public class UserAgreementDaoImpl extends DaoImpl<_UserAgreement> implements UserAgreementDao {

    public UserAgreementDaoImpl() {
        super(_UserAgreement.class);
    }

    @Override
    public PageStream<_UserAgreement> search(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        if (filter.has("contractItemId"))
            searchFilter.and("contractItem.id", filter.getString("contractItemId"));
        if (filter.has("contractId"))
            searchFilter.and("contractItem.parent.id", filter.getString("contractId"));
        if (filter.has("userId"))
            searchFilter.and("user.id", filter.getString("userId"));
        if (filter.has("productNameSearch"))
            searchFilter.and("contractItem.product.name", filter.getString("productNameSearch").trim() + "*");
        if (filter.has("status"))
            searchFilter.and("statusType", filter.getString("status"));
        if (filter.getBool("notificationSent"))
            searchFilter.and("notificationSent:true");
        if (filter.getBool("forNotification")) {
            searchFilter.and("user.id", SessionUtils.getInstance().getUser().getId().toString());
            searchFilter.and("arrived:false");
            searchFilter.and("notificationSent:true");
        }
        if (searchFilter.toString().isEmpty())
            searchFilter.and("*:*");
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _UserAgreement.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        if (filter.getBool("forNotification"))
            fullTextQuery.setSort(new Sort(new SortField("notificationSentTime", SortField.Type.STRING, true)));
        else
            fullTextQuery.setSort(new Sort(new SortField("auditInfo.creationDate", SortField.Type.STRING, true)));
        fullTextQuery.setFirstResult(filter.getStart());
        fullTextQuery.setMaxResults(filter.getSize());
        return new PageStream<_UserAgreement>(fullTextQuery.stream(), fullTextQuery.getResultSize());
    }

    @Override
    public _UserAgreement getByContractItem(_ContractItem contractItem, _User user) {
        return (_UserAgreement) findSingle("select t from _UserAgreement t where t.state <> :deleted and " +
                        " t.contractItem = :item and t.user = :user order by t.id desc",
                preparing(new Entry("deleted", _State.DELETED), new Entry("item", contractItem), new Entry("user", user)));
    }

    @Override
    public Stream<_User> getByContractItem(Long contractId) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("contractItem.parent.id", "" + contractId);
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _UserAgreement.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
        return ((Stream<_UserAgreement>) fullTextQuery.stream()).map(userAgreement -> userAgreement.getUser()).distinct();
//        return find("select distinct t.user from _UserAgreement t left join _Contract c on t.contractItem.parent.id = c.id where t.state <> :deleted and " +
//                        " c.id = :contract",
//                preparing(new Entry("deleted", _State.DELETED), new Entry("contract", contractId)));
    }

    @Override
    public PageStream<_Contract> getContractListByCurrentUser(FilterParameters filter) {
        SearchFilter searchFilter = new SearchFilter();
        searchFilter.and("user.id", "" + SessionUtils.getInstance().getUser().getId());
        searchFilter.and("contractItem.state", "" + _State.NEW);
        if (filter.has("codeSearch")) {
            searchFilter.and("contractItem.parent.code", filter.getString("codeSearch") + "*");
        }
        searchFilter.and("-state", _State.DELETED);
        org.apache.lucene.search.Query luceneQuery = queryParser("search", searchFilter.toString());
        FullTextQuery fullTextQuery = fullTextSession().createFullTextQuery(luceneQuery, _UserAgreement.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SECOND_LEVEL_CACHE, DatabaseRetrievalMethod.QUERY);
//        fullTextQuery.setFirstResult(filter.getStart());
//        fullTextQuery.setMaxResults(filter.getSize());
        Stream<_Contract> contractStream = ((Stream<_UserAgreement>) fullTextQuery.stream()).map(userAgreement -> userAgreement.getContractItem().getParent())
                .distinct().skip(filter.getStart()).limit(filter.getSize());
        Long totalCount = ((Stream<_UserAgreement>) fullTextQuery.stream()).map(userAgreement -> userAgreement.getContractItem().getParent())
                .distinct().count();
        return new PageStream<_Contract>(contractStream, totalCount.intValue());
    }

    @Override
    public _UserAgreement save(_UserAgreement entity) {
        super.save(entity);
        return updateRelated(entity);
    }

    private _UserAgreement updateRelated(_UserAgreement entity) {
        _ContractItem contractItem = entity.getContractItem();
        contractItem.setInitiatorCount(getCountInitsiator(contractItem));
        contractItem.setInitiatorAcceptedCount(getAcceptedCountInitsiator(contractItem));
        return entity;
    }

    @Override
    public int getCountAcceptedAndTotalByContract(_Contract contract, boolean forTotal) {
        if (forTotal) {
            return ((Long) findSingle("select count(t) from _UserAgreement t " +
                            "left join _Contract c on c.id = t.contractItem.parent.id where c = :contract and t.user = :user and t.state <> :deleted and t.notificationSent = true"
                    , preparing(new Entry("deleted", _State.DELETED),
                            new Entry("contract", contract), new Entry("user", getUser())))).intValue();
        } else {
            return ((Long) findSingle("select count(t) from _UserAgreement t " +
                    "left join _Contract c on c.id = t.contractItem.parent.id where c = :contract and t.user = :user and t.state <> :deleted and " +
                    " t.statusType <> :statusType and t.notificationSent = true", preparing(new Entry("deleted", _State.DELETED),
                    new Entry("contract", contract), new Entry("user", getUser()), new Entry("statusType", UserAgreementStatusType.WAITING)))).intValue();
        }
    }

    private int getCountInitsiator(_ContractItem contractItem) {
        return ((Long) findSingle("select count(distinct t.user) from _UserAgreement t where t.state <> :deleted and " +
                " t.contractItem = :contractItem ", preparing(new Entry("deleted", _State.DELETED), new Entry("contractItem", contractItem)))).intValue();
    }

    private int getAcceptedCountInitsiator(_ContractItem contractItem) {
        BigInteger count = (BigInteger) findSingleNative("select sum(case t.status_type when 'WAITING' then 0 else 1 end) from user_agreement t " +
                        "   join (select max(id) as id ,contract_item_id, user_id from user_agreement" +
                        " where state <> :deleted group by contract_item_id,user_id) x " +
                        "   on t.id = x.id where t.contract_item_id = :contractItemId",
                preparing(new Entry("deleted", _State.DELETED), new Entry("contractItemId", contractItem.getId())));
        return count == null ? 0 : count.intValue();
    }
}