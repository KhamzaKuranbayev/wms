package uz.uzcard.genesis.hibernate.dao.impl;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.OtpMessageDao;
import uz.uzcard.genesis.hibernate.entity._OTPMessage;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.enums.OtpType;

import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

@Repository(value = "otpMessageDao")
public class OtpMessageDaoImpl extends DaoImpl<_OTPMessage> implements OtpMessageDao {
    public OtpMessageDaoImpl() {
        super(_OTPMessage.class);
    }

    @Override
    public Stream<_OTPMessage> list(FilterParameters filter) {
        OtpMessageDaoImpl.CustomFilter customFilter = new OtpMessageDaoImpl.CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return findInterval("select t from _OTPMessage t " +
                " where t.state <> 2 " + filterQuery +
                " order by t.id desc ", params, filter.getStart(), filter.getSize());
    }

    @Override
    public Integer total(FilterParameters filter) {
        OtpMessageDaoImpl.CustomFilter customFilter = new OtpMessageDaoImpl.CustomFilter(filter).invoke();
        String filterQuery = customFilter.getFilterQuery();
        Map<String, Object> params = customFilter.getParams();
        return ((Long) findSingle("select count(t) from _OTPMessage t " +
                " where t.state <> 2 " + filterQuery, params)).intValue();
    }

    @Override
    public _OTPMessage findByUserName(String uniqueParam) {
        return (_OTPMessage) findSingle("select t from _OTPMessage t left join t.user u " +
                        " where (u.userName = :user or u.email=:user or u.phone=:user) and t.state <> 'DELETED' order by t.id desc",
                preparing(new Entry("user", uniqueParam)));
    }

    @Override
    public Long countByUser(_User user) {
        Date dayOfMonth = DateTime.now().dayOfMonth().getDateTime().toDate();
        return (Long) findSingle("select count(t) from _OTPMessage t where t.state <> 'DELETED' and t.user = :user and t.createdDate >= :date",
                preparing(new Entry("user", user), new Entry("date", dayOfMonth)));
    }

    @Override
    public Date lastSentCode(_User user) {
        return (Date) findSingle("select t.createdDate from _OTPMessage t where t.state <> 'DELETED' and t.user = :user",
                preparing(new Entry("user", user)));
    }

    @Override
    public Long getAllMessagesByUserName(Long userId, Date before, Date current) {

        return (Long) findSingle("select count(t) from _OTPMessage t where t.user_id = :userId and " +
                        "t.createdDate >= :before and t.createdDate <= :current",
                preparing(new Entry("userId", userId), new Entry("before", before), new Entry("current", current)));
    }

    private class CustomFilter {
        private final FilterParameters filter;
        private String filterQuery;
        private Map<String, Object> params;

        public CustomFilter(FilterParameters filter) {
            this.filter = filter;
        }

        public String getFilterQuery() {
            return filterQuery;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public CustomFilter invoke() {
            filterQuery = "";
            params = preparing();
            if (filter.has("typeName")) {
                filterQuery += " and t.type = :type ";
                params.put("type", OtpType.valueOf(filter.getString("typeName")));
            }

            return this;
        }
    }
}
