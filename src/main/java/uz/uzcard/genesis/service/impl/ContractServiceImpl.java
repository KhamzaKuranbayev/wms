package uz.uzcard.genesis.service.impl;

import org.hibernate.search.query.facet.Facet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.event.contract.ContractChangeStatusEvent;
import uz.uzcard.genesis.dto.event.contract.ContractCreateEvent;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;
import uz.uzcard.genesis.uitls.StateConstants;

import java.util.*;
import java.util.stream.Collectors;

import static uz.uzcard.genesis.uitls.StateConstants.PENDING_PURCHASE;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractItemService contractItemService;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private UserAgreementService userAgreementService;
    @Autowired
    private SupplierDao supplierDao;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UserDao userDao;
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private LotDao lotDao;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private UserAgreementDao userAgreementDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private CarriageDao carriageDao;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private RejectProductDao rejectProductDao;

    @Override
    public PageStream<_Contract> list(ContractFilterRequest request) {
        return contractDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());

            addBoolean("forQrCodePrint", request.isForQrCodePrint());
//            if (request.getCode() != null)
//                add("contractId", request.getCode());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (!ServerUtils.isEmpty(request.getCode()))
                add("contractCode", request.getCode());
            if (!ServerUtils.isEmpty(request.getGroupId()))
                addLong("groupId", request.getGroupId());
            if (!ServerUtils.isEmpty(request.getTypeId()))
                addLong("typeId", request.getTypeId());
            if (!ServerUtils.isEmpty(request.getProductName()))
                add("productName", request.getProductName());
            if (!ServerUtils.isEmpty(request.getInisator()))
                add("inisator", request.getInisator());
            if (!ServerUtils.isEmpty(request.getSupplierId()))
                addLong("supplierId", request.getSupplierId());
            if (!ServerUtils.isEmpty(request.getSupplierType()))
                add("supplierType", request.getSupplierType().name());
            if (!ServerUtils.isEmpty(request.getStatus()))
                add("contractStatus", request.getStatus());
            if (request.getFromDate() != null)
                addDate("fromDate", request.getFromDate());
            if (request.getToDate() != null)
                addDate("toDate", request.getToDate());
            if (request.getCodeSearch() != null)
                addString("codeSearch", request.getCodeSearch());

            if (!StringUtils.isEmpty(request.getRequestNumber())) {
                addString("orderNumber", request.getRequestNumber());
            }
            if (request.isMakeContract()) {
                addStrings("isMakeContract", List.of(_State.CONTRACT_ACCEPTED, _State.CONTRACT_REJECT, _State.DELETED));
            }
            if (request.isForMobile()) {
                addBool("isForMobile", true);
            }
        }});
    }

    @Override
    public _Contract save(ContractRequest request, MultipartFile file) {
        String eventType = _State.CONTRACT_CREATE_EVENT;
        if (request.getId() != null) {
            eventType = _State.CONTRACT_UPDATE_EVENT;
        }
        _Contract contract = contractSave(request, file);

        if (request.getOrderItems().isEmpty()) {
            Long contractItemId = contractItemService.save(request.wrapContractItemRequest(contract.getId())).getId();
            if (!ServerUtils.isEmpty(request.getUserIds()))
                userAgreementService.createForOzl(InitiatorOZLRequest.builder().userIds(request.getUserIds()).contractItemId(contractItemId).build());
        } else {
            contractItemService.saveMultipleOrderItem(contract, request.getOrderItems(), request.getSupplierId(), request.getSupplyType(), request.getUserIds());
        }
        contract = contractDao.get(contract.getId());
        contractDao.save(contract);

        // send socket message
        sendSocketMessage(contract, eventType);
        return contract;
    }

    @Override
    public _Contract update(ContractUpdateRequest request, MultipartFile file) {
        if (request.getId() == null)
            throw new ValidatorException("CONTRACT_REQUIRED");
        _Contract contract = contractDao.get(request.getId());
        if (contract == null)
            throw new RpcException(GlobalizationExtentions.localication("CONTRACT_NOT_FOUND"));

        if (request.getCode() != null) {
            if (contractDao.checkByIdWithCode(request.getId(), request.getCode()))
                throw new ValidatorException(String.format(GlobalizationExtentions.localication("CONTRACT_NUMBER_BUSY"), request.getCode()));
            contract.setCode(request.getCode());
        }

        if (request.getSupplierId() != null) {
            _Supplier supplier = supplierDao.get(request.getSupplierId());
            if (supplier == null) {
                throw new RpcException(GlobalizationExtentions.localication("SUPPLIER_NOT_FOUND"));
            }
            contract.setSupplier(supplier);
        }

        if (request.getConclusionDate() != null) {
            contract.setConclusionDate(request.getConclusionDate());
        }
        if (!ServerUtils.isEmpty(file)) {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            contract.setProductResource(attachment);
        }
        contractDao.save(contract);
        return contract;
    }

    @Override
    public _Contract saveOzl(ContractRequest request, MultipartFile file) {
        String eventType = _State.CONTRACT_CREATE_EVENT;
        if (request.getId() != null) {
            eventType = _State.CONTRACT_UPDATE_EVENT;
        }

        _Contract contract = contractSave(request, file);

        Long contractItemId = contractItemService.saveOzl(request.wrapContractItemRequest(contract.getId())).getId();
        UserAgreementRequest agreementRequest = new UserAgreementRequest();
        agreementRequest.setContractItemId(contractItemId);
        agreementRequest.setUserId(SessionUtils.getInstance().getUserId());
        userAgreementService.createByContractItem(agreementRequest);
        contractDao.save(contract);
        contract = contractDao.get(contract.getId());

        // send socket message
        sendSocketMessage(contract, eventType);
        return contract;
    }

    private _Contract contractSave(ContractRequest request, MultipartFile file) {
        _Contract contract = contractDao.get(request.getId());
        if (contract == null) {
            contract = new _Contract();
            if (contractDao.checkByNum(request.getCode()))
                throw new ValidatorException(String.format(GlobalizationExtentions.localication("CONTRACT_NUMBER_BUSY"), request.getCode()));
            contract.setCode(request.getCode());

            if (request.getSupplierId() == null) {
                throw new RpcException(GlobalizationExtentions.localication("SUPPLIER_REQUIRED"));
            }
            _Supplier supplier = supplierDao.get(request.getSupplierId());
            if (supplier == null) {
                throw new RpcException(GlobalizationExtentions.localication("SUPPLIER_NOT_FOUND"));
            }
            contract.setSupplier(supplier);

            if (request.getSupplyType() != null) {
                contract.setSupplyType(request.getSupplyType());
            } else {
                throw new RpcException(GlobalizationExtentions.localication("SUPPLIER_TYPE_REQUIRED"));
            }
        } else {
            if (request.getCode() != null) {
                contract.setCode(request.getCode());
                if (contractDao.checkByIdWithCode(request.getId(), request.getCode()))
                    throw new ValidatorException(String.format(GlobalizationExtentions.localication("CONTRACT_NUMBER_BUSY"), request.getCode()));
            }
        }

        if (!ServerUtils.isEmpty(file)) {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            contract.setProductResource(attachment);
        }
        if (request.getGuessReceiveDate() != null) {
            contract.setGuessReceiveDate(request.getGuessReceiveDate());
        }
        if (request.getConclusionDate() != null) {
            contract.setConclusionDate(request.getConclusionDate());
        }
        return contractDao.save(contract);
    }

    @Override
    public _Contract reject(ContractRejectRequest request, MultipartFile file) {
        _Contract contract = contractDao.get(request.getContractId());
        if (ServerUtils.isEmpty(contract)) {
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_NOT_FOUND"));
        }
        if (_State.CONTRACT_REJECT.equals(contract.getState()))
            throw new ValidatorException(GlobalizationExtentions.localication("ACTION_ALREADY_DONE"));
        if (ServerUtils.isEmpty(request.getReason())) {
            throw new ValidatorException(GlobalizationExtentions.localication("REJECT_REASON_REQUIRED"));
        }
        if (!ServerUtils.isEmpty(file)) {
            _AttachmentView attachment = attachmentService.uploadPdf(file);
            contract.setRejectResource(attachment);
        }
        contract.setState(_State.CONTRACT_REJECT);
        contract.setRejectReason(request.getReason());
        contractItemDao.findByParent(contract).forEach(contractItem -> {
            contractItemService.changeStatus(contractItem, _State.CONTRACT_ITEM_REJECT);
            orderItemDao.findByContractItem(contractItem).forEach(orderItem -> {
                orderItem.setContractItem(null);
                orderItemsService.changeStatus(orderItem, PENDING_PURCHASE);
            });
        });


        if (SessionUtils.getInstance().getUser() != null && SessionUtils.getInstance().getUser().getDepartment() != null) {
            Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
            if (departmentId != null) {
                List<String> usernames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(user -> user.getUsername()).collect(Collectors.toList());
                usernames.remove(SessionUtils.getInstance().getUser().getUsername());
                final ContractChangeStatusEvent event = new ContractChangeStatusEvent(contract, _State.CONTRACT_REJECTED_EVENT, usernames);
                eventPublisher.publishEvent(event);
            }
        }

        return contractDao.save(contract);
    }

    @Override
    public _Contract accept(Long id) {
        if (ServerUtils.isEmpty(id)) {
            throw new ValidatorException(GlobalizationExtentions.localication("ID_REQUIRED"));
        }
        _Contract contract = contractDao.get(id);
        if (ServerUtils.isEmpty(contract)) {
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_NOT_FOUND"));
        }
        contract.setState(_State.CONTRACT_ACCEPTED);
        contract.setCompletedDate(new Date());
        contract = contractDao.save(contract);
        contractItemDao.getByContract(contract).forEach(contractItem -> {
            contractItemService.changeStatus(contractItem, _State.CONTRACT_ITEM_ACCEPTED);
        });
        contractDao.reindex(List.of(contract.getId()));

        if (SessionUtils.getInstance().getUser() != null) {
            Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
            if (departmentId != null) {
                List<String> usernames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(user -> user.getUsername()).collect(Collectors.toList());
                usernames.remove(SessionUtils.getInstance().getUser().getUsername());
                final ContractChangeStatusEvent event = new ContractChangeStatusEvent(contract, _State.CONTRACT_ACCEPTED_EVENT, usernames);
                eventPublisher.publishEvent(event);
            }
        }
        return contract;
    }

    @Override
    public List<SelectItem> getItems(ContractFilterItemsRequest request) {
        return contractDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            add("code", request.getCode());
            add("order.item.status", StateConstants.YES_PRODUCT);
        }}).stream().map(contract ->
                new SelectItem(contract.getId(), contract.getCode(), "" + contract.getId())
        ).collect(Collectors.toList());
    }

    @Override
    public _Contract get(Long id) {
        return contractDao.get(id);
    }

    @Override
    public _AttachmentView agreementFile(Long id, MultipartFile file) {
        if (id == null)
            throw new ValidatorException("CONTRACT_REQUIRED");
        _Contract contract = contractDao.get(id);
        if (contract == null)
            throw new RpcException("CONTRACT_NOT_FOUND");
        if (file == null)
            throw new ValidatorException("FILE_REQUIRED");

        _AttachmentView uploadFile = attachmentService.uploadPdf(file);
        contract.getAgreementResources().add(uploadFile);
        contractDao.save(contract);
        return uploadFile;
    }

    @Override
    public void agreementFileDelete(AgreementFileDeleteRequest request) {
        if (request.getName() == null || "".equals(request.getName()))
            throw new ValidatorException("FILE_REQUIRED");

        _Attachment attachment = attachmentService.getByName(request.getName());
        if (attachment == null)
            throw new RpcException("FILE_NOT_FOUND");

        if (request.getContractId() == null)
            throw new ValidatorException("CONTRACT_REQUIRED");
        _Contract contract = contractDao.get(request.getContractId());
        if (contract == null)
            throw new RpcException("CONTRACT_NOT_FOUND");

        contract.getAgreementResources().remove(attachment);
        attachmentDao.delete(attachment);
        contractDao.save(contract);
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new ValidatorException("ID_REQUIRED");
        _Contract contract = contractDao.get(id);
        if (contract == null)
            throw new RpcException(GlobalizationExtentions.localication("CONTRACT_NOT_FOUND"));

        for (_ContractItem contractItem : contract.getItems()) {
            contractItemService.deleteItem(DeleteRequest.builder().objectId(contractItem.getId()).build());
        }
        contractDao.delete(contract);
    }

    @Override
    public ListResponse supplierBlackListByContract(DashboardFilter filterRequest) {
        List<Facet> bySupplier = contractDao.getBySupplier(filterRequest);
        int total = bySupplier.size();

        List<SelectItem> list = bySupplier.stream().skip(filterRequest.getPage() * filterRequest.getLimit()).limit(filterRequest.getLimit())
                .map(facet -> new SelectItem(facet.getValue(), "" + facet.getCount())).collect(Collectors.toList());
        return ListResponse.of(list, total);
    }

    @Override
    public SingleResponse getContractStatus(DashboardFilter filterRequest) {
        List<Facet> byContractConclution = new ArrayList<>();
        List<Facet> byContractAccepted = new ArrayList<>();
        switch (filterRequest.getResolutionType()) {
            case DAY:
                byContractConclution = contractDao.getByContractStatus(filterRequest, null, "creationDateFacetDay", "conclusionDate");
                byContractAccepted = contractDao.getByContractStatus(filterRequest, _State.CONTRACT_ACCEPTED, "completedDateFacetDay", "completedDate");
                break;
            case MONTH:
                byContractConclution = contractDao.getByContractStatus(filterRequest, null, "creationDateFacetMonth", "conclusionDate");
                byContractAccepted = contractDao.getByContractStatus(filterRequest, _State.CONTRACT_ACCEPTED, "completedDateFacetMonth", "completedDate");
                break;
        }
        Map<String, Integer> contractConclutions = new HashMap<>();
        Map<String, Integer> contractAccepted = new HashMap<>();

        Set<String> dataKeys = byContractConclution.stream().map(facet -> {
            contractConclutions.put(facet.getValue(), facet.getCount());
            return facet.getValue();
        }).collect(Collectors.toSet());
        byContractAccepted.forEach(facet -> {
            contractAccepted.put(facet.getValue(), facet.getCount());
            dataKeys.add(facet.getValue());
        });

        List<Map<String, String>> data = new ArrayList<>();
        dataKeys.forEach(key -> {
            Map<String, String> map = new HashMap<>();
            map.put("date", key);

            if (contractConclutions.containsKey(key)) {
                map.put("conclutionCount", "" + contractConclutions.get(key));
            } else {
                map.put("conclutionCount", "0");
            }

            if (contractAccepted.containsKey(key)) {
                map.put("acceptedCount", "" + contractAccepted.get(key));
            } else {
                map.put("acceptedCount", "0");
            }
            data.add(map);
        });
        return SingleResponse.of(data);
    }

    // socket send message;
    private void sendSocketMessage(_Contract contract, String eventType) {
        if (SessionUtils.getInstance().getUser() != null && SessionUtils.getInstance().getUser().getDepartment() != null) {
            Long departmentId = SessionUtils.getInstance().getUser().getDepartment().getId();
            if (departmentId != null) {
                List<String> usernames = userDao.list(new FilterParameters().add("departmentId", "" + departmentId)).map(user -> user.getUsername()).collect(Collectors.toList());
                usernames.remove(SessionUtils.getInstance().getUser().getUsername());
                final ContractCreateEvent event = new ContractCreateEvent(contract, usernames, eventType);
                eventPublisher.publishEvent(event);
            }
        }
    }

    //    @Override
//    public SingleResponse checkEDS(Long contractId) {
//        _Contract contract = contractDao.get(contractId);
//        if (contract == null)
//            throw new ValidatorException("Контракт яратилмаган");
//        HashMap<String, Object> map = new HashMap<>();
//        if (!ServerUtils.isEmpty(contract.getHashESign())) {
//            map.put("hasSignedBefore", false);
//        } else {
//            map.put("hasSignedBefore", true);
//            map.put("hashESign", contract.getHashESign());
//        }
//        return SingleResponse.of(map);
//    }
//
//    @Override
//    public SingleResponse setHashESign(HashESignRequest request) {
//        _Contract contract  = contractDao.get(request.getId());
//        if (contract == null)
//            throw new ValidatorException("Контракт яратилмаган");
//        contract.setHashESign(request.getHashESign());
//        contractDao.save(contract);
//        return SingleResponse.of(true);
//    }
}