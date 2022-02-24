package uz.uzcard.genesis.service.impl;

import org.hibernate.search.query.facet.Facet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ProductItemDao;
import uz.uzcard.genesis.hibernate.dao.ProductItemSummByDepartmentDao;
import uz.uzcard.genesis.hibernate.entity._ProductItem;
import uz.uzcard.genesis.hibernate.entity._ProductItemSummByDepartment;
import uz.uzcard.genesis.service.ProductItemSummByDepartmentService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductItemSummByDepartmentServiceImpl implements ProductItemSummByDepartmentService {

    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private ProductItemSummByDepartmentDao productItemSummByDepartmentDao;

    @Override
    public ListResponse getByMonth(DashboardFilter filterRequest) {
        Date date = new Date();
        PageStream<_ProductItemSummByDepartment> search = productItemSummByDepartmentDao.search(new FilterParameters() {{
            add("forDashboard", "null");
        }});
        return ListResponse.of(search, (productItemSummByDepartment, map) -> map);
    }

    @Override
    public ListResponse getByAllHistories(DashboardFilter filterRequest) {
        PageStream<_ProductItemSummByDepartment> search = productItemSummByDepartmentDao.search(new FilterParameters() {{
            add("departmentId", "" + filterRequest.getDepartmentId());
            setSize(10);
        }});
        return ListResponse.of(search, (productItemSummByDepartment, map) -> map);
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void setSummByMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 00);
        Date fromDate = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        Date toDate = cal.getTime();

        List<Facet> facetByUserDepartment = productItemDao.facetByUserDepartment(new DashboardFilter() {{
            setDateType(DateType.Date);
            setFromDate(fromDate);
            setToDate(toDate);
        }});
        if (facetByUserDepartment == null)
            return;

        for (Facet facet : facetByUserDepartment) {
            Double summ = new Double(0);
            List<_ProductItem> takenAwayUserDepartments = productItemDao.search(new FilterParameters() {{
                add("takenAwayUserDepartmentId", facet.getValue());
            }}).stream().collect(Collectors.toList());

            if (takenAwayUserDepartments != null) {
                for (_ProductItem takenAwayUserDepartment : takenAwayUserDepartments) {
                    if (takenAwayUserDepartment.getPrice() != null)
                        summ += (takenAwayUserDepartment.getPrice() * takenAwayUserDepartment.getCount());
                }

                _ProductItemSummByDepartment productItemSummByDepartment = new _ProductItemSummByDepartment();
                productItemSummByDepartment.setDepartmentId(takenAwayUserDepartments.get(0).getTakenAwayUser().getDepartment().getId());
                productItemSummByDepartment.setDepartmentName(takenAwayUserDepartments.get(0).getTakenAwayUser().getDepartment().getNameUz());
                productItemSummByDepartment.setProductSumm(summ);
                productItemSummByDepartment.setCalculatedDate(new Date());

                productItemSummByDepartmentDao.save(productItemSummByDepartment);
            }
        }
    }
}
