package uz.uzcard.genesis.service.impl;

import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.hibernate.dao.RejectProductDao;
import uz.uzcard.genesis.service.RejectProductService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RejectProductServiceImpl implements RejectProductService {
    @Autowired
    private RejectProductDao rejectProductDao;

    @Override
    public ListResponse productRejectedList(DashboardFilter request) {
        List<Facet> facets = rejectProductDao.productRejectedList(new FilterParameters() {{
            Date[] period = request.getPeriod();
            if (period != null) {
                setFromDate(period[0]);
                setToDate(period[1]);
            }
            setStart(request.getPage() * request.getLimit());
        }});
        int total = facets.size();
        List<SelectItem> list = facets.stream().skip(request.getPage() * request.getLimit()).limit(request.getLimit())
                .map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(list, total);
    }
}