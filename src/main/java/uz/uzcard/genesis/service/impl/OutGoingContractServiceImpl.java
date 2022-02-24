package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractRequest;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;
import uz.uzcard.genesis.hibernate.entity._OutGoingContract;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.OutGoingContractService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;

@Service
public class OutGoingContractServiceImpl implements OutGoingContractService {

    @Autowired
    private ProductDao productDao;
    @Autowired
    private SupplierDao supplierDao;
    @Autowired
    private OutGoingContractDao outGoingContractDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private UnitTypeDao unitTypeDao;

    @Override
    public _OutGoingContract save(OutGoingContractRequest request, MultipartFile file) {
        if (outGoingContractDao.checkByContractNumber(request.getContractNumber())) {
            throw new CriticException(GlobalizationExtentions.localication("OUTGOING_CONTRACT_NUMBER_BUSY"));
        }
        _OutGoingContract outGoingContract = new _OutGoingContract();
        outGoingContract.setContractNumber(request.getContractNumber());
        outGoingContract.setUnitType(unitTypeDao.get(request.getUnitTypeId()));

        // todo
        if (request.getRequestCount() == null) {
            throw new RpcException("REQUESTCOUNT_IS_REQUIRED");
        } else {
            outGoingContract.setRequestCount(request.getRequestCount());
            outGoingContract.setContractBalance(request.getRequestCount());
        }
        outGoingContract.setCloseContractDate(request.getCloseContractDate());
        outGoingContract.setCloseDate(request.getCloseDate());

        if (!ServerUtils.isEmpty(request.getProductId())) {
            outGoingContract.setProduct(productDao.get(request.getProductId()));
        }
        if (!ServerUtils.isEmpty(request.getCustomerId())) {
            outGoingContract.setCustomer(customerDao.getById(request.getCustomerId()));
        }
        if (file != null) {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            outGoingContract.setProductResource(attachment);
        }

        return outGoingContractDao.save(outGoingContract);
    }

    @Override
    public PageStream<_OutGoingContract> list(OutGoingContractFilterRequest request) {
        return outGoingContractDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (request.getContractNumber() != null)
                add("contractNumber", request.getContractNumber());
            if (request.getSupplierId() != null)
                addLong("supplierId", request.getSupplierId());
            if (request.getProductId() != null)
                addLong("productId", request.getProductId());
            if (request.getCustomerId() != null)
                addLong("customerId", request.getCustomerId());
            if (request.getRequestCount() != null)
                addDouble("requestCount", request.getRequestCount());
            if (request.getContractBalance() != null)
                addDouble("contractBalance", request.getContractBalance());
            // closeContractDate
            if (request.getFromCloseContractDate() != null)
                addDate("fromCloseContractDate", request.getFromCloseContractDate());
            if (request.getToCloseContractDate() != null)
                addDate("toCloseContractDate", request.getToCloseContractDate());

            // closeDate
            if (request.getFromCloseDate() != null)
                addDate("fromCloseDate", request.getFromCloseDate());
            if (request.getToCloseDate() != null)
                addDate("toCloseDate", request.getToCloseDate());
            if (request.isCompleted()) {
                add("isCompleted", "true");
            }
            if (request.isNotCompleted()) {
                add("isNotCompleted", "true");
            }
        }});
    }
}
