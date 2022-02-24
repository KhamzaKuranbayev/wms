package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.ContractDao;
import uz.uzcard.genesis.hibernate.dao.ContractItemDao;
import uz.uzcard.genesis.hibernate.dao.UserAgreementDao;
import uz.uzcard.genesis.hibernate.dao.UserDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;
import uz.uzcard.genesis.service.AttachmentService;
import uz.uzcard.genesis.service.UserAgreementService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserAgreementServiceImpl implements UserAgreementService {

    @Autowired
    private UserAgreementDao userAgreementDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private ContractDao contractDao;

    @Override
    public Collection<CoreMap> getInitsiators(UserAgreementFilterRequest request) {
        LinkedHashMap<_User, CoreMap> map = new LinkedHashMap<>();
        request.setLimit(Integer.MAX_VALUE);
        list(request).stream().forEach(userAgreement -> {
            _User user = userAgreement.getUser();
            if (!map.containsKey(user)) {
                map.put(user, new CoreMap(userAgreement.getId()).add("initiator", user.getShortName())
                        .add("description", userAgreement.getDescription())
                        .add("initiatorId", user.getId())
                        .add("contractItemId", userAgreement.getContractItem().getId())
                        .add("statusType", userAgreement.getStatusType().name())
                        .add("state", userAgreement.getState())
                        .add("id", userAgreement.getId()));
                map.get(user).addBool("read", userAgreement.isRead());
                map.get(user).addBool("notificationSent", userAgreement.isNotificationSent());
                map.get(user).addBool("ozl", userAgreement.isOzl());
            }
//            if (map.get(user).has("statusType") && userAgreement.getStatusType().ordinal() > UserAgreementStatusType.valueOf(map.get(user).getString("statusType")).ordinal())
//                map.get(user).add("statusType", userAgreement.getStatusType().name());
//            if (userAgreement.isRead())
//                map.get(user).addBool("read", userAgreement.isRead());
//            if (userAgreement.isNotificationSent())
//                map.get(user).addBool("notificationSent", userAgreement.isNotificationSent());
        });
        return map.values();
    }

    @Override
    public PageStream<_UserAgreement> list(UserAgreementFilterRequest request) {
        return userAgreementDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (request.isForInitiator()) {
                addLong("userId", SessionUtils.getInstance().getUserId());
            }
            if (!ServerUtils.isEmpty(request.getContractId())) {
                addString("contractId", "" + request.getContractId());
            }
            if (!ServerUtils.isEmpty(request.getContractItemId())) {
                addString("contractItemId", "" + request.getContractItemId());
            }
            if (!ServerUtils.isEmpty(request.getStatus())) {
                addString("status", "" + request.getStatus());
            }
            if (!ServerUtils.isEmpty(request.getProductNameSearch())) {
                addString("productNameSearch", "" + request.getProductNameSearch());
            }
            if (request.isForInitiator()) {
                addString("userId", SessionUtils.getInstance().getUser().getId().toString());
                addBool("notificationSent", true);
            }
            if (request.isForNotification())
                addBool("forNotification", true);
        }});
    }

    @Override
    public ListResponse getContracts(UserAgreementFilterRequest request) {
        List<Map<String, String>> list = userAgreementDao.getByContractItem(request.getContractId()).map(user -> {
            Map<String, String> map = new HashMap();
            map.put("id", user.getId().toString());
            map.put("name", user.getFirstName() + " " + user.getLastName());
            return map;
        }).collect(Collectors.toList());
        return ListResponse.of(list);
    }

    @Override
    public ListResponse getContracts(UserAgreementByInitiatorFilterRequest filter) {
        return ListResponse.of(contractDao.search(new FilterParameters() {{
            setStart(filter.getPage() * filter.getLimit());
            setSize(filter.getLimit());
            if (!ServerUtils.isEmpty(filter.getSortBy())) {
                setSortColumn(filter.getSortBy());
                setSortType(filter.getSortDirection());
            }
            addBool("forInitiator", true);
            if (!ServerUtils.isEmpty(filter.getCodeSearch()))
                addString("codeSearch", filter.getCodeSearch());
        }}), ((contract, map) -> {
            map.add("acceptCount", "" + userAgreementDao.getCountAcceptedAndTotalByContract(contract, false));
            map.add("totalCount", "" + userAgreementDao.getCountAcceptedAndTotalByContract(contract, true));
            map.remove("hashESign");
            return map;
        }));
    }

    @Override
    public void createByOrderItem(_OrderItem orderItem) {
        if (orderItem.getAuditInfo() == null || orderItem.getAuditInfo().getCreatedByUser() == null)
            throw new RpcException(String.format(GlobalizationExtentions.localication("ORDER_INITIATOR_NOT_FOUND"), orderItem.getItemNumb()));
//        _UserAgreement userAgreementByOzl = userAgreementDao.getByContractItem(orderItem.getContractItem(), contractDao.getUser());
//        if (userAgreementByOzl == null) {
//            userAgreementByOzl = new _UserAgreement();
//            userAgreementByOzl.setContractItem(orderItem.getContractItem());
//            userAgreementByOzl.setUser(contractDao.getUser());
//            userAgreementByOzl.setStatusType(UserAgreementStatusType.WAITING);
//            userAgreementByOzl.setOzl(true);
//            userAgreementDao.save(userAgreementByOzl);
//        }
        _UserAgreement userAgreement = userAgreementDao.getByContractItem(orderItem.getContractItem(), orderItem.getAuditInfo().getCreatedByUser());
        if (userAgreement == null) {
            userAgreement = new _UserAgreement();
            userAgreement.setContractItem(orderItem.getContractItem());
            userAgreement.setUser(orderItem.getAuditInfo().getCreatedByUser());
            userAgreement.setStatusType(UserAgreementStatusType.WAITING);
            userAgreementDao.save(userAgreement);
            if (!orderItem.getContractItem().getUserAgreements().contains(userAgreement))
                orderItem.getContractItem().getUserAgreements().add(userAgreement);
        }

    }

    @Override
    public _UserAgreement createByContractItem(UserAgreementRequest request) {
        _UserAgreement userAgreement = new _UserAgreement();
        if (!ServerUtils.isEmpty(request) && !ServerUtils.isEmpty(request.getContractItemId())) {
            _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
            if (!ServerUtils.isEmpty(contractItem)) {
                _User user = userDao.get(request.getUserId());
                if (!ServerUtils.isEmpty(user)) {
                    if (!_State.CONTRACT_ITEM_PARTITION_ACCEPTED.equals(contractItem.getState()) && !ServerUtils.isEmpty(userAgreementDao.getByContractItem(contractItem, user))) {
                        throw new ValidatorException("Такой инициатор уже существует");
                    }

                    userAgreement.setContractItem(contractItem);
                    userAgreement.setUser(user);
                    userAgreement.setStatusType(UserAgreementStatusType.WAITING);
                    userAgreement.setOzl(request.isOzl());
                    userAgreementDao.save(userAgreement);
                    contractItem.getUserAgreements().add(userAgreement);
                    contractItemDao.save(contractItem);
                    userAgreementDao.reindex(List.of(userAgreement.getId()));
                    contractItemDao.reindex(List.of(contractItem.getId()));
                } else {
                    throw new ValidatorException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
                }
            } else {
                throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
            }
        }
        return userAgreement;
    }

    @Override
    public void createForOzl(InitiatorOZLRequest request) {
        if (!ServerUtils.isEmpty(request) && !ServerUtils.isEmpty(request.getContractItemId())) {
            _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
            if (!ServerUtils.isEmpty(contractItem) && !ServerUtils.isEmpty(request.getUserIds())) {
                userDao.findByIds(request.getUserIds()).forEach(
                        user -> {
                            if (!_State.CONTRACT_ITEM_PARTITION_ACCEPTED.equals(contractItem.getState()) && !ServerUtils.isEmpty(userAgreementDao.getByContractItem(contractItem, user))) {
                                return;
                            }
                            _UserAgreement userAgreement = new _UserAgreement();
                            userAgreement.setContractItem(contractItem);
                            userAgreement.setUser(user);
                            userAgreement.setStatusType(UserAgreementStatusType.WAITING);
                            userAgreement.setOzl(true);
                            userAgreementDao.save(userAgreement);
                            contractItem.getUserAgreements().add(userAgreement);
                            contractItemDao.save(contractItem);
                            userAgreementDao.reindex(List.of(userAgreement.getId()));
                        });
                contractItemDao.reindex(List.of(contractItem.getId()));
            } else
                throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        }
    }

    @Override
    public Boolean checkInitiatorByContractItem(UserAgreementSaveByContractRequest request) {
        _ContractItem item = contractItemDao.get(request.getContractItemId());
        if (ServerUtils.isEmpty(item))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        _User user = userDao.get(request.getUserId());
        if (ServerUtils.isEmpty(user))
            throw new ValidatorException(GlobalizationExtentions.localication("INITIATOR_NOT_FOUND"));

        _UserAgreement userAgreement = userAgreementDao.getByContractItem(item, user);
        if (!ServerUtils.isEmpty(userAgreement)) {
            throw new ValidatorException("Такой инициатор уже существует");
        }

        return true;
    }

    @Override
    public _UserAgreement changeStatus(UserAgreementChangeStatusRequest request, UserAgreementStatusType
            statusType, List<MultipartFile> files) {
        _UserAgreement userAgreement = userAgreementDao.get(request.getUserAgreementId());
        if (userAgreement == null)
            throw new ValidatorException(GlobalizationExtentions.localication("HAS_NOT_ACCESS"));
        userAgreement.setStatusType(statusType);
        userAgreement.setDescription(request.getReason());
        if (!ServerUtils.isEmpty(files)) {
            _AttachmentView attachmentView = attachmentService.uploadPdf(files);
            if (attachmentView != null) {
                userAgreement.getResources().add(attachmentView);
            }
            /*List<_AttachmentView> attachmentViews = new ArrayList<>();
            files.forEach(multipartFile -> {
                _AttachmentView attachment = attachmentService.uploadPdf(multipartFile);
                attachmentViews.add(attachment);
            });
            userAgreement.setResources(attachmentViews);*/
        }
        userAgreementDao.save(userAgreement);

        // todo
        if (userAgreement.getContractItem() == null || userAgreement.getContractItem().getParent() == null)
            throw new ValidatorException("Контракт с этим номером не найден");
        contractItemDao.save(userAgreement.getContractItem());
        contractDao.save(userAgreement.getContractItem().getParent());
        return userAgreement;
    }


    @Override
    public _UserAgreement accept(Long id) {
        _UserAgreement userAgreement = userAgreementDao.get(id);
        if (userAgreement == null)
            throw new ValidatorException(GlobalizationExtentions.localication("HAS_NOT_ACCESS"));
        userAgreement.setStatusType(UserAgreementStatusType.ACCEPTED);
        userAgreementDao.save(userAgreement);

        if (userAgreement.getContractItem() == null || userAgreement.getContractItem().getParent() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        contractItemDao.save(userAgreement.getContractItem());
        contractDao.save(userAgreement.getContractItem().getParent());
        return userAgreement;
    }

    @Override
    public void readAndArrive(UserAgreementReadAcceptRequest request) {
        _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
        if (ServerUtils.isEmpty(contractItem) || ServerUtils.isEmpty(contractItem.getParent()))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));

        UserAgreementRequest createRequest = new UserAgreementRequest();
        _UserAgreement userAgreement = userAgreementDao.getByContractItem(contractItem, userAgreementDao.getUser());
        if (userAgreement == null) {
            createRequest.setUserId(SessionUtils.getInstance().getUserId());
            createRequest.setContractItemId(contractItem.getId());
            userAgreement = createByContractItem(createRequest);
        }
        if (request.isArrived())
            userAgreement.setArrived(true);
        if (request.isRead())
            userAgreement.setRead(true);
        userAgreementDao.save(userAgreement);
        contractItemDao.save(contractItem);
        contractDao.save(userAgreement.getContractItem().getParent());
    }

    @Override
    public _UserAgreement changeInitsiator(UserAgreementChangeInitiatorRequest request) {
        if (!ServerUtils.isEmpty(request.getUserAgreementId())) {
            _UserAgreement userAgreement = userAgreementDao.get(request.getUserAgreementId());
            _UserAgreement userAgreementNew = new _UserAgreement();
            if (userAgreement == null)
                throw new ValidatorException(GlobalizationExtentions.localication("HAS_NOT_ACCESS"));
            _User user = userDao.get(request.getUserId());
            if (ServerUtils.isEmpty(user)) {
                throw new ValidatorException(GlobalizationExtentions.localication("USER_NOT_FOUND"));
            } else {
                if (!ServerUtils.isEmpty(userAgreementDao.getByContractItem(userAgreement.getContractItem(), user)))
                    throw new ValidatorException("Такой инициатор уже существует");
                userAgreement.setState(_State.DELETED);
                userAgreementNew.setContractItem(userAgreement.getContractItem());
                userAgreementNew.setUser(user);
                userAgreementDao.save(userAgreement);
                if (!userAgreement.getContractItem().getUserAgreements().contains(userAgreementNew))
                    contractItemDao.save(userAgreement.getContractItem());
                return userAgreementDao.save(userAgreementNew);
            }
        } else throw new ValidatorException(GlobalizationExtentions.localication("USER_AGREEMENT_REQUIRED"));
    }

    @Override
    public void delete(DeleteRequest request) {
        if (request.getObjectId() == null) {
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        }
        _UserAgreement userAgreement = userAgreementDao.get(request.getObjectId());
        if (userAgreement == null) {
            throw new RpcException(GlobalizationExtentions.localication("USER_AGREEMENT_NOT_FOUND"));
        }
        userAgreementDao.delete(userAgreement);
        if (userAgreement.getContractItem() != null) {
            contractItemDao.save(userAgreement.getContractItem());
            if (userAgreement.getContractItem().getParent() != null) {
                contractDao.save(userAgreement.getContractItem().getParent());
            }
        }
    }

    @Override
    public SingleResponse getDetail(Long id) {
        if (id == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ID_REQUIRED"));
        _UserAgreement userAgreement = userAgreementDao.get(id);
        // todo
        if (userAgreement == null)
            throw new RpcException(GlobalizationExtentions.localication("USER_AGREEMENT_NOT_FOUND"));
        return SingleResponse.of(userAgreement, (userAgreement1, map) -> {
            map = new CoreMap();
            if (userAgreement.getResources() != null) {
                List<Map<String, String>> files = new ArrayList<>();
                userAgreement.getResources().forEach(attachmentView -> {
                    Map<String, String> file = new HashMap<>();
                    file.put("attachmentLink", AttachmentUtils.getLink(attachmentView.getName()));
                    file.put("attachmentResourceName", attachmentView.getOriginalName());
                    files.add(file);
                });
                map.addStrings("files", files);
            }
            map.add("statusType", userAgreement1.getStatusType().name());
            map.add("reason", userAgreement1.getDescription());
            return map;
        });
    }

    @Override
    public SingleResponse checkEDS(Long userAgreementId) {
        _UserAgreement userAgreement = userAgreementDao.get(userAgreementId);
        if (userAgreement == null)
            throw new ValidatorException(GlobalizationExtentions.localication("USER_AGREEMENT_NOT_FOUND"));
        HashMap<String, Object> map = new HashMap<>();
        if (!ServerUtils.isEmpty(userAgreement.getHashESign())) {
            map.put("hasSignedBefore", false);
        } else {
            map.put("hasSignedBefore", true);
            map.put("hashESign", userAgreement.getHashESign());
        }
        return SingleResponse.of(map);
    }

    @Override
    public SingleResponse setHashESign(HashESignRequest request) {
        _UserAgreement userAgreement = userAgreementDao.get(request.getId());
        if (userAgreement == null)
            throw new ValidatorException(GlobalizationExtentions.localication("USER_AGREEMENT_NOT_FOUND"));
        userAgreement.setHashESign(request.getHashESign());
        userAgreementDao.save(userAgreement);
        return SingleResponse.of(true);
    }
}