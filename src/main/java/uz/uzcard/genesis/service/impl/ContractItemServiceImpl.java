package uz.uzcard.genesis.service.impl;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.order.OrderItemStateChangeRequest;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.hibernate.enums.SupplyType;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;
import uz.uzcard.genesis.service.*;
import uz.uzcard.genesis.uitls.*;

import javax.servlet.ServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uz.uzcard.genesis.hibernate.enums.Permissions.CONTRACT_ITEM_DELETE;
import static uz.uzcard.genesis.uitls.StateConstants.*;

@Service
public class ContractItemServiceImpl implements ContractItemService {

    private final List<List<String>> statusMap = new ArrayList<>();
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private UserAgreementDao userAgreementDao;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private UserAgreementService userAgreementService;
    @Autowired
    private Gson gson;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private UnitTypeDao unitTypeDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemsService orderItemsService;
    @Autowired
    private RejectProductDao rejectProductDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private LotDao lotDao;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private CarriageService carriageService;
    @Autowired
    private CarriageDao carriageDao;

    {
        //contract
        statusMap.add(Arrays.asList(_State.CONTRACT_CONCLUTION, _State.CONTRACT_REJECT));
        statusMap.add(Arrays.asList(_State.CONTRACT_CONCLUTION, _State.CONTRACT_ACCEPTED));
        //contract item
        statusMap.add(Arrays.asList(_State.CONTRACT_CONCLUTION, CONTRACT_ITEM_DELETE));
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_ACCEPTED));
//        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_PART_ACCEPTED));
        statusMap.add(Arrays.asList(_State.NEW, _State.CONTRACT_ITEM_REJECT));
        statusMap.add(Arrays.asList(_State.NEW, CONTRACT_ITEM_DELETE));
    }

    @Override
    public _ContractItem save(ContractItemRequest request) {

        if (ServerUtils.isEmpty(request))
            throw new ValidatorException("CONTRACT_ITEM_REQUEST_IS_NULL");

        if (ServerUtils.isEmpty(request.getParentId()))
            throw new ValidatorException("PARENT_ID_FOR_CONTRACT_ITEM_NOT_FOUND");

        _Contract contract = contractDao.get(request.getParentId());

        if (ServerUtils.isEmpty(contract))
            throw new ValidatorException("CONTRACT_IS_NOT_FOUND");

        _Product product = productDao.get(request.getProductId());

        _OrderItem orderItem = orderItemDao.get(request.getOrderItemId());
        if (orderItem == null)
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("ORDER_ITEM_NOT_FOUND"), request.getOrderItemId()));

        _ContractItem contractItem = contractItemDao.get(request.getId());
        if (isProductReceived(contractItem))
            contractItem = null;
        if (contractItem == null) {
            contractItem = new _ContractItem();
        }
        if (request.getId() != null && request.getId().equals(contractItem.getId())) {
            contractItem.setCount(request.getCount());
        } else if (request.getCount() != null)
            contractItem.setCount(contractItem.getCount() + request.getCount());
        contractItem.setUnitType(orderItem.getUnitType());

        contractItem.setParent(contract);
//            if (!ServerUtils.isEmpty(contract.getItems())) {
//                contractItem.setNumb(contract.getItems().size() + 1);
//            } else {
//                contractItem.setNumb(1);
//            }
        if (contractItem.getNumb() < 1)
            contractItem.setNumb(contractItemDao.getMaxNumb(contract) + 1);

        if (!ServerUtils.isEmpty(product)) {
            contractItem.setProduct(product);
            if (!ServerUtils.isEmpty(product.getGroup())) {
                contractItem.setProductGroup(product.getGroup());
            }
            if (!ServerUtils.isEmpty(product.getType())) {
                contractItem.setProductType(product.getType());
            }
        }

        if (contract.getGuessReceiveDate() != null) {
            contractItem.setItemGuessReceiveDate(contract.getGuessReceiveDate());
        }

        contractItemDao.save(contractItem);

        orderItem.setContractItem(contractItem);
        orderItem.setContractDate(new Date());
        orderItem.setItemConclusionDate(new Date());
        orderItemsService.changeStatus(orderItem, _State.DELIVERY_EXPECTED);

        contractItemDateChange(orderItem);
        userAgreementService.createByOrderItem(orderItem);
        contractDao.save(contract);
        return contractItem;
    }

    @Override
    public _ContractItem saveOzl(ContractItemRequest request) {

        if (ServerUtils.isEmpty(request))
            throw new ValidatorException("CONTRACT_ITEM_REQUEST_IS_NULL");

        if (ServerUtils.isEmpty(request.getParentId()))
            throw new ValidatorException("PARENT_ID_FOR_CONTRACT_ITEM_NOT_FOUND");

        _Contract contract = contractDao.get(request.getParentId());
        if (ServerUtils.isEmpty(contract))
            throw new ValidatorException("CONTRACT_NOT_FOUND");

        _Product product = productDao.get(request.getProductId());

        boolean isNew = false;
        _ContractItem contractItem = contractItemDao.get(request.getId());
        if (contractItem == null)
            contractItem = contractItemDao.getByContractAndProduct(contract, productDao.get(request.getProductId()), unitTypeDao.get(request.getUnitTypeId()));
        if (isProductReceived(contractItem))
            contractItem = null;
        if (contractItem != null && contractItem.getParent().getDefaultYearly() != null)
            contractItem = null;
        if (contractItem == null) {
            contractItem = new _ContractItem();
            isNew = true;
        }
        if (contractItem.getNumb() < 1)
            contractItem.setNumb(contractItemDao.getMaxNumb(contract) + 1);
        if (!ServerUtils.isEmpty(contract))
            contractItem.setParent(contract);

        if (request.getId() != null && request.getId().equals(contractItem.getId())) {
            contractItem.setCount(request.getCount());
        } else if (request.getCount() != null)
            contractItem.setCount(contractItem.getCount() + request.getCount());
        if (request.getUnitTypeId() != null)
            contractItem.setUnitType(unitTypeDao.get(request.getUnitTypeId()));

        if (!ServerUtils.isEmpty(product)) {
            contractItem.setProduct(product);
            if (!ServerUtils.isEmpty(product.getGroup())) {
                contractItem.setProductGroup(product.getGroup());
            }
            if (!ServerUtils.isEmpty(product.getType())) {
                contractItem.setProductType(product.getType());
            }
        }
        if (request.getGuessReceiveDate() != null) {
            contractItem.setItemGuessReceiveDate(request.getGuessReceiveDate());
        }
        if (!ServerUtils.isEmpty(request.getUnitTypeId())) {
            _UnitType unitType = unitTypeDao.get(request.getUnitTypeId());
            if (ServerUtils.isEmpty(unitType))
                throw new ValidatorException("UNIT_TYPE_NOT_FOUND");
            contractItem.setUnitType(unitType);
        }

        contractItemDao.save(contractItem);
        if (isNew) {
            contract.getItems().add(contractItem);
            contractDao.save(contract);
            contractDao.reindex(List.of(contract.getId()));
        }
        contractItemDao.reindex(List.of(contractItem.getId()));
        return contractItem;
    }

    private boolean isProductReceived(_ContractItem contractItem) {
        return contractItem != null && contractItem.getUserAgreements().stream()
                .anyMatch(userAgreement -> !UserAgreementStatusType.WAITING.equals(userAgreement.getStatusType()));
    }

    @Override
    @Transactional
    public void saveMultipleOrderItem(_Contract contract, List<Long> orderItemsIds, Long supplierId, SupplyType supplyType, List<Long> userIds) {
        orderItemDao.findByIds(orderItemsIds).forEach(orderItem -> {
            if (orderItem.getContractItem() != null) return;
            _ContractItem contractItem = contractItemDao.getByContractAndProduct(contract, orderItem.getProduct(), orderItem.getUnitType());
            if (isProductReceived(contractItem))
                contractItem = null;
            if (contractItem == null) {
                contractItem = new _ContractItem();
                contractItem.setProduct(orderItem.getProduct());
                contractItem.setParent(contract);
                contractItem.setCount(orderItem.getCount());
            } else {
                contractItem.setCount(contractItem.getCount() + orderItem.getCount());
            }
            boolean isNew = contractItem.isNew();
            contractItem.setItemGuessReceiveDate(contract.getGuessReceiveDate());
            contractItem.setUnitType(orderItem.getUnitType());
            contractItem.setProductType(orderItem.getProductType());
            contractItem.setProductGroup(orderItem.getProductGroup());

            if (contractItem.getNumb() < 1) {
                Integer maxNumb = contractItemDao.getMaxNumb(contract);
                contractItem.setNumb((maxNumb == null ? 0 : maxNumb.intValue()) + 1);
            }

            if (!contractItem.getOrderItems().contains(orderItem))
                contractItem.getOrderItems().add(orderItem);
            contractItemDao.save(contractItem);

            orderItem.setContractItem(contractItem);
            orderItem.setContractDate(new Date());
            orderItem.setItemConclusionDate(new Date());
            orderItemsService.changeStatus(orderItem, _State.DELIVERY_EXPECTED);
            contractItemDateChange(orderItem);

            userAgreementService.createByOrderItem(orderItem);
            if (isNew) {
                contract.getItems().add(contractItem);
                contractDao.save(contract);
            }
            contractItemDao.save(contractItem);

            userAgreementService.createForOzl(InitiatorOZLRequest.builder().contractItemId(contractItem.getId()).userIds(userIds).build());
        });

        /*Iterator<_ContractItem> iterator = contract.getItems().iterator();
        while (iterator.hasNext()) {
            _ContractItem item = iterator.next();
            if (!actual.contains(item.getId())) {
                contractItemDao.delete(item);
                iterator.remove();
            }
        }*/
        contractDao.save(contract);
    }

    @Override
    public PageStream<_ContractItem> list(ContractItemFilterRequst request) {
        return contractItemDao.search(new FilterParameters() {{
            if (request.getContractId() != null)
                add("contractId", request.getContractId().toString());
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (!ServerUtils.isEmpty(request.getSortBy())) {
                setSortColumn(request.getSortBy());
                setSortType(request.getSortDirection());
            }
            if (!ServerUtils.isEmpty(request.getContractCode()))
                add("contractCode", request.getContractCode());
            if (!ServerUtils.isEmpty(request.getGroupId()))
                addLong("groupId", request.getGroupId());
            if (!ServerUtils.isEmpty(request.getTypeId()))
                addLong("typeId", request.getTypeId());
            if (!ServerUtils.isEmpty(request.getProductName()))
                add("productName", request.getProductName());
            if (!ServerUtils.isEmpty(request.getInisator()))
                add("inisator", request.getInisator());
            if (!ServerUtils.isEmpty(request.getContractStatus()))
                add("contractStatus", request.getContractStatus());
            if (request.getFromDate() != null)
                addDate("fromDate", request.getFromDate());
            if (request.getToDate() != null)
                addDate("toDate", request.getToDate());
            if (request.getState() != null)
                addString("state", request.getState());
            if (request.isForPrintQrCode()) {
                addBoolean("forPrintQrCode", request.isForPrintQrCode());
                addBoolean("forQrCode", request.getForQRStatus());
            }
            addLong("productId", request.getProductId());
            add("orderNumber", request.getOrderNumb());
        }});
    }

    @Override
    public void deleteItem(DeleteRequest request) {
        _ContractItem contractItem = contractItemDao.get(request.getObjectId());
        List<_Partition> partitions = contractItem.getPartitions();
        if (partitions != null) {
            HashSet<Long> carriageIdHashSet = new HashSet<>();
            for (_Partition partition : partitions) {
                productItemDao.findAllByPartition(partition).forEach(productItem -> {
                    carriageIdHashSet.addAll(productItem.getCarriages_id());
                    productItemDao.delete(productItem);
                });
                if (partition.getLots() != null) {
                    partition.getLots().forEach(lot -> {
                        lotDao.delete(lot);
                    });
                }

                if (partition.getGivens() != null) {
                    partition.getGivens().forEach(givenProducts -> {
                        givenProductsDao.delete(givenProducts);
                    });
                }
                partitionDao.delete(partition);
                List<Long> carriageIds = new ArrayList<>(carriageIdHashSet);
                carriageService.checkToHasProduct(carriageDao.findByIds(carriageIds).collect(Collectors.toList()));
            }
        }

        List<_UserAgreement> userAgreements = contractItem.getUserAgreements();
        if (userAgreements != null) {
            userAgreements.forEach(userAgreement -> {
                userAgreementDao.delete(userAgreement);
            });
        }
        rejectProductDao.findByContractItem(contractItem)
                .forEach(rejectProduct -> {
                    rejectProductDao.delete(rejectProduct);
                });

        List<_OrderItem> orderItems = orderItemDao.findByContractItem(contractItem);
        for (_OrderItem orderItem : orderItems) {
            orderItem.setContractItem(null);
            orderItem.setState(PAPER_EXPECTED_SPECIFICATION);
            orderItemDao.save(orderItem);
        }

        contractItemDao.delete(contractItem);
        List<_ContractItem> collect = contractItemDao.findByParent(contractItem.getParent()).collect(Collectors.toList());
        if (collect.isEmpty() || collect == null) {
            _Contract parent = contractItem.getParent();
            parent.setState(DELETED);
            contractDao.save(parent);
        }
    }

    @Override
    public _ContractItem accept(Long id) {
        if (ServerUtils.isEmpty(id))
            throw new ValidatorException(GlobalizationExtentions.localication("ID_REQUIRED"));

        _ContractItem contractItem = contractItemDao.get(id);
        if (ServerUtils.isEmpty(contractItem))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));

        if (Arrays.asList(_State.CONTRACT_ITEM_ACCEPTED, _State.CONTRACT_ITEM_REJECT).contains(contractItem.getState()))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_DECISION_HAS_ALREADY_BEEN_MADE"));

        changeStatus(contractItem, _State.CONTRACT_ITEM_ACCEPTED);
        contractItem.setAcceptedUser(SessionUtils.getInstance().getUser());
        contractItem.setAcceptedDate(new Date());
        contractItem.setAcceptedCount(contractItem.getCount());

        contractItemDao.save(contractItem);
        checkFinished(contractItem);

        // When omtk accepted contract item then order item status change to ISSUED_ORDER_ITEM
        orderItemDao.findByContractItem(contractItem).forEach(orderItem -> {
            orderItem.setState(ISSUED_ORDER_ITEM);
            orderItemDao.save(orderItem);
        });

        contractDao.save(contractItem.getParent());
        return contractItem;
    }

    private void checkFinished(_ContractItem contractItem) {
        if (contractItemDao.getTotalCount(contractItem.getParent()) == contractItemDao.getAcceptCount(contractItem.getParent())) {
            _Contract contract = contractItem.getParent();
            contract.setState(_State.CONTRACT_ACCEPTED);
            contract.setCompletedDate(new Date());
            contractDao.save(contract);
        }
    }

    @Override
    public _ContractItem changeStatus(ContractItemChangeStatusRequest request, List<MultipartFile> files, String statusName) {
        _ContractItem contractItem = contractItemDao.get(request.getContractItemId());
        if (ServerUtils.isEmpty(contractItem))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        if (request.getReject() > 0 && ServerUtils.isEmpty(request.getDescription()))
            throw new ValidatorException(GlobalizationExtentions.localication("DESCRIPTION_REQUIRED"));
        if (request.getReject() > 0 && ServerUtils.isEmpty(files))
            throw new ValidatorException(GlobalizationExtentions.localication("FILE_REQUIRED"));
        if (Arrays.asList(_State.CONTRACT_ITEM_ACCEPTED, _State.CONTRACT_ITEM_REJECT).contains(contractItem.getState()))
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_DECISION_HAS_ALREADY_BEEN_MADE"));

        changeStatus(contractItem, statusName);
        contractItem.setAcceptedUser(SessionUtils.getInstance().getUser());
        contractItem.setAcceptedDate(new Date());
        if (contractItem.getCount() < request.getAccept())
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_COUNT_LARGE"));

        if (!ServerUtils.isEmpty(files)) {
            List<_AttachmentView> attachmentViews = new ArrayList<>();
            _AttachmentView attachment = attachmentService.uploadPdf(files);
            attachmentViews.add(attachment);
            contractItem.setResources(attachmentViews);
        }
        if (CONTRACT_ITEM_PART_ACCEPTED.equals(statusName)) {
            double rejectCount = contractItem.getCount() - request.getAccept();
            contractItem.setAcceptedCount(request.getAccept());
            contractItem.setRejectedCount(rejectCount);
            rejectProductDao.save(new _RejectProduct(contractItem, contractItem.getParent().getSupplier(), rejectCount, request.getDescription()));
        } else if (CONTRACT_ITEM_PARTITION_ACCEPTED.equals(statusName)) {
            contractItem.setAcceptedCount(contractItem.getAcceptedCount() + request.getAccept());
            contractItem.setRejectedCount(contractItem.getRejectedCount() + request.getReject());
            rejectProductDao.save(new _RejectProduct(contractItem, contractItem.getParent().getSupplier(), request.getReject(), request.getDescription()));
            if (contractItem.getRemains() > 0) {
                userAgreementService.getInitsiators(new UserAgreementFilterRequest() {{
                    setContractItemId(contractItem.getId());
                }}).forEach(map -> {
                    if (map.has("initiatorId"))
                        userAgreementService.createByContractItem(UserAgreementRequest.builder().contractItemId(contractItem.getId()).userId(map.getLong("initiatorId")).build());
                });
            } else if (contractItem.getRemains() < 0) {
                throw new ValidatorException("Миқдордан ортиқ юк қабул қилолмайсиз!");
            } else {
                contractItem.setState(CONTRACT_ITEM_ACCEPTED);
            }
            //todo Mashxur aytdi
            contractItem.setInitiatorAcceptedCount(0);
            contractItem.setAcceptedDate(null);
        } else if (CONTRACT_ITEM_REJECT.equals(statusName)) {
            contractItem.setRejectedCount(contractItem.getRemains());
            rejectProductDao.save(new _RejectProduct(contractItem, contractItem.getParent().getSupplier(), contractItem.getRemains(), request.getDescription()));
        }

        contractItemDao.save(contractItem);

        checkFinished(contractItem);

        contractDao.save(contractItem.getParent());
        return contractItem;
    }

    @Override
    public CoreMap attachAktFile(Long orderItemId, MultipartFile file) {
        if (orderItemId == null)
            throw new ValidatorException("ORDER_ITEM_REQUIRED");

        _OrderItem orderItem = orderItemDao.get(orderItemId);
        if (orderItem == null)
            throw new RpcException("ORDER_ITEM_NOT_FOUND");

        _ContractItem contractItem = orderItem.getContractItem();
        if (contractItem == null)
            throw new RpcException("CONTRACT_ITEM_NOT_FOUND");
        _AttachmentView attachment = attachmentService.uploadPdf(file);
        contractItem.getAktFiles().add(attachment);
        contractItemDao.save(contractItem);
        CoreMap coreMap = new CoreMap();
        coreMap.add("aktFileLink", AttachmentUtils.getLink(attachment.getName()));
        coreMap.put("aktFileName", attachment.getOriginalName());
        return coreMap;
    }

    @Override
    public SingleResponse generateQrCode(@NotNull Long contractItemId) {
        _ContractItem contractItem = contractItemDao.get(contractItemId);
        return SingleResponse.of(contractItem, (contractItem1, map) -> {
            if (ServerUtils.isEmpty(contractItem.getProduct().getType()))
                throw new ValidatorException("PRODUCT_TYPE_NOT_FOUND");

            if (ServerUtils.isEmpty(contractItem.getProductGroup()))
                throw new ValidatorException("PRODUCT_GROUP_NOT_FOUND");

            map = new CoreMap();
            map.add("id", contractItem.getId());
            map.addDouble("count", contractItem.getCount());
            map.add("numb", "" + contractItem.getNumb());
            map.add("productGroupName", contractItem.getProductGroup().getName());
            map.add("productTypeName", contractItem.getProduct().getType().getName());
//            generateQrCode(map, response);
            return map;
        });
    }

    @Override
    public void generateQrCode(Long contractItemId, ServletResponse response) {
        OutputStream outputStream;

        if (ServerUtils.isEmpty(response))
            throw new ValidatorException("SERVLET_RESPONSE_FOR_GENERATE_QR_CODE_IS_NULL");

        _ContractItem contractItem = contractItemDao.get(contractItemId);

        if (ServerUtils.isEmpty(contractItem))
            throw new ValidatorException("CONTRACT_ITEM_NOT_FOUND");

        Map<String, String> map = new HashMap<>();
        map.put("id", contractItem.getId().toString());
//        map.put("count", contractItem.getCount().toString());
//        map.put("numb", "" + contractItem.getNumb());
//        map.put("productGroupName", contractItem.getProductGroup().getName());
//        map.put("productTypeName", contractItem.getProduct().getType().getName());
//        map.put("productTypeName", contractItem.getProduct().getType().getName());

        try {
            response.setContentType("image/png");
            outputStream = response.getOutputStream();
            outputStream.write(ServerUtils.getQRCodeImage(gson.toJson(map), 200, 200));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void contractItemDateChange(_OrderItem orderItem) {
        _ContractItem contractItem = orderItem.getContractItem();
        if (contractItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        orderItem.setItemGuessReceiveDate(contractItem.getItemGuessReceiveDate());
        orderItem.setItemReceiveDate(contractItem.getItemGuessReceiveDate());
        orderItemDao.save(orderItem);
        orderDao.save(orderItem.getParent());
    }

    @Override
    public SingleResponse checkEDS(Long contractItemId) {
        _ContractItem contractItem = contractItemDao.get(contractItemId);
        if (contractItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        HashMap<String, Object> map = new HashMap<>();
        if (!ServerUtils.isEmpty(contractItem.getHashESign())) {
            map.put("hasSignedBefore", false);
        } else {
            map.put("hasSignedBefore", true);
            map.put("hashESign", contractItem.getHashESign());
        }
        return SingleResponse.of(map);
    }

    @Override
    public _ContractItem updateItemCount(ItemCountRequest request) {
        if (ServerUtils.isEmpty(request.getItemId())) {
            throw new RpcException(GlobalizationExtentions.localication("ID_REQUIRED"));
        }
        _ContractItem contractItem = contractItemDao.get(request.getItemId());
        if (ServerUtils.isEmpty(contractItem))
            throw new ValidatorException("CONTRACT_ITEM_NOT_FOUND");
        if (!ServerUtils.isEmpty(request.getCount())) {
            contractItem.setCount(request.getCount());
        }
        contractItemDao.save(contractItem);
        contractDao.save(contractItem.getParent());
        return contractItem;
    }

    @Override
    public SingleResponse setHashESign(HashESignRequest request) {
        _ContractItem contractItem = contractItemDao.get(request.getId());
        if (contractItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
//        ServerUtils.checkESign(request.getHashESign());
        contractItem.setHashESign(request.getHashESign());
        contractItemDao.save(contractItem);
        return SingleResponse.of(true);
    }

    @Override
    public void changeStatus(_ContractItem contractItem, String state) {
        if (contractItem == null)
            throw new ValidatorException(GlobalizationExtentions.localication("CONTRACT_ITEM_NOT_FOUND"));
        if (!StateMap.match(contractItem.getState(), state)) {
            throw new ValidatorException(String.format(GlobalizationExtentions.localication("CONTRACT_ITEM_CHANGE_STATUS"),
                    GlobalizationExtentions.localication(contractItem.getState()),
                    GlobalizationExtentions.localication(state)));
        }
        contractItem.setState(state);
        contractItemDao.save(contractItem);
        contractDao.save(contractItem.getParent());
    }

    @Override
    public _ContractItem getSingle(ContractItemFilterRequst filter) {
        _Contract contract = contractDao.get(filter.getContractId());
        _Product product = productDao.get(filter.getProductId());
        _UnitType unitType = unitTypeDao.get(filter.getUnitTypeId());
        _ContractItem contractItem = contractItemDao.getByContractAndProduct(contract, product, unitType);
        return contractItem;
    }

    @Override
    public SingleResponse getAktFiles(Long orderItemId) {
        if (orderItemId == null)
            throw new ValidatorException("ORDER_ITEM_REQUIRED");

        _OrderItem orderItem = orderItemDao.get(orderItemId);
        if (orderItem == null)
            throw new RpcException("ORDER_ITEM_NOT_FOUND");

        if (orderItem.getContractItem() != null) {
            if (orderItem.getContractItem().getAktFiles() != null) {
                List<Map<String, String>> data = orderItem.getContractItem().getAktFiles().stream().map(attachmentView -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("aktFileLink", AttachmentUtils.getLink(attachmentView.getName()));
                    map.put("aktFileName", attachmentView.getOriginalName());
                    return map;
                }).collect(Collectors.toList());
                return SingleResponse.of(data);
            }
        }
        return SingleResponse.of(null);
    }

    @Override
    public _ContractItem changeState(OrderItemStateChangeRequest request) {
        _ContractItem contractItem = contractItemDao.get(request.getId());
        if (contractItem == null)
            throw new ValidatorException("CONTRACT_ITEM_NOT_FOUND");
        contractItem.setState(request.getState());
        contractItemDao.save(contractItem);
        return contractItem;
    }

    @Override
    public Stream<_ContractItem> findAll(_Contract contract) {
        return contractItemDao.findAllByParent(contract);
    }
}