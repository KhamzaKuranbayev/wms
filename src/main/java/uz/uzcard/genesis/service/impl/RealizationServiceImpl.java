package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.RealizationFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.RealizationRequest;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.OutGoingContractDao;
import uz.uzcard.genesis.hibernate.dao.RealizationDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity._OutGoingContract;
import uz.uzcard.genesis.hibernate.entity._Realization;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.RealizationService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

@Service
public class RealizationServiceImpl implements RealizationService {

    @Autowired
    private RealizationDao realizationDao;
    @Autowired
    private OutGoingContractDao outGoingContractDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public PageStream<_Realization> list(RealizationFilterRequest request) {
        return realizationDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (request.getId() != null) {
                addLong("id", request.getId());
            }
            if (request.getContractNumber() != null) {
                add("contractNumber", request.getContractNumber());
            }
            if (request.getRealizatorId() != null) {
                addLong("realizatorId", request.getRealizatorId());
            }
            if (request.getDepartmentId() != null) {
                addLong("departmentId", request.getDepartmentId());
            }
            if (request.getProductId() != null) {
                addLong("productId", request.getProductId());
            }

            if (request.getFromDate() != null) {
                addDate("fromDate", request.getFromDate());
            }
            if (request.getToDate() != null) {
                addDate("toDate", request.getToDate());
            }
        }});
    }

    @Override
    public _Realization save(RealizationRequest request) {
        if (request.getContractNumber() == null)
            throw new RpcException(GlobalizationExtentions.localication("CONTRACT_NUMBER_REQUIRED"));
        _OutGoingContract outGoingContract = outGoingContractDao.getByContractNumber(request.getContractNumber());
        if (outGoingContract == null)
            throw new RpcException(String.format(GlobalizationExtentions.localication("OUTGOING_CONTRACT_NOT_FOUND"), request.getContractNumber()));
        if (request.getCount() > outGoingContract.getContractBalance())
            throw new RpcException(String.format(GlobalizationExtentions.localication("OUTGOING_CONTRACT_COUNT_NOT_DOT_MATCH"), request.getCount()));

        Double balance = outGoingContract.getContractBalance() - request.getCount();
        _Realization realization = new _Realization();
        realization.setContractNumber(request.getContractNumber());
        realization.setProduct(outGoingContract.getProduct());
        realization.setCount(request.getCount());
        realization.setContractBalance(balance);
        realization.setRealizationDate(request.getRealizationDate());
        realization.setUnitType(outGoingContract.getUnitType());
        if (request.getDepartmentId() != null)
            realization.setDepartment(departmentDao.get(request.getDepartmentId()));

        outGoingContract.setContractBalance(balance);
        if (balance == 0) {
            outGoingContract.setState(_State.COMPLETED);
        }
        outGoingContractDao.save(outGoingContract);
        return realizationDao.save(realization);
    }
}
