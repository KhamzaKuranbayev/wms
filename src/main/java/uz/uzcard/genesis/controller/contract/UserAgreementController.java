package uz.uzcard.genesis.controller.contract;

/**
 * Created by norboboyev_h  on 07.08.2020  9:25
 */


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;
import uz.uzcard.genesis.service.UserAgreementService;
import uz.uzcard.genesis.uitls.AttachmentUtils;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(value = "User Agreement controller", description = "User Agreements")
@RestController
@RequestMapping(value = "/api/user-agreement")
public class UserAgreementController {

    @Autowired
    private UserAgreementService userAgreementService;


    @ApiOperation(value = "Get Initsiators")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get-initsiators", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getInitsiators(UserAgreementFilterRequest request) {
        return ListResponse.of(userAgreementService.getInitsiators(request)
                .stream().map(coreMap -> coreMap.getInstance()).collect(Collectors.toList()));
    }

    @ApiOperation(value = "User Agreement list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(UserAgreementFilterRequest request) {
        return ListResponse.of(userAgreementService.list(request), (userAgreement, map) -> {
            if (request.isForInitiator()) {
                if (userAgreement.getContractItem() != null) {
                    map.add("contractItemCount", "" + userAgreement.getContractItem().getCount());
                    map.add("contractItemNumb", "" + userAgreement.getContractItem().getNumb());
                    if (userAgreement.getContractItem().getProduct() != null) {
                        map.add("productName", userAgreement.getContractItem().getProduct().getName());
                        map.addStrings("attrs", userAgreement.getContractItem().getProduct().getAttr());
                        if (userAgreement.getContractItem().getProduct().getGroup() != null)
                            map.add("productGroupName", userAgreement.getContractItem().getProduct().getGroup().getName());
                        if (userAgreement.getContractItem().getProduct().getType() != null)
                            map.add("productTypeName", userAgreement.getContractItem().getProduct().getType().getName());
                    }
                    if (userAgreement.getContractItem().getUnitType() != null) {
                        map.add("unit_type_name_en", userAgreement.getContractItem().getUnitType().getNameEn());
                        map.add("unit_type_name_ru", userAgreement.getContractItem().getUnitType().getNameRu());
                        map.add("unit_type_name_uz", userAgreement.getContractItem().getUnitType().getNameUz());
                        map.add("unit_type_name_cyrl", userAgreement.getContractItem().getUnitType().getNameCyrl());
                    }
                }
                map.add("statusType", userAgreement.getStatusType().name());
            } else {
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
                if (!ServerUtils.isEmpty(userAgreement.getStatusType()))
                    map.add("statusType", userAgreement.getStatusType().name());
                if (!ServerUtils.isEmpty(userAgreement.getContractItem())) {
                    map.add("contractItemId", userAgreement.getContractItem().getId());
                    map.add("contractItemNumb", "" + userAgreement.getContractItem().getNumb());
                    if (userAgreement.getContractItem().getProduct() != null) {
                        map.addStrings("attrs", userAgreement.getContractItem().getProduct().getAttr());
                    }
                }
                if (!ServerUtils.isEmpty(userAgreement.getUser())) {
                    map.add("initiator", userAgreement.getUser().getFirstName() + " " + userAgreement.getUser().getLastName());
                    map.add("initiatorId", userAgreement.getUser().getId());
                }
            }
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "User Agreement list")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/notification-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse notificationList() {
        UserAgreementFilterRequest request = new UserAgreementFilterRequest();
        request.setForNotification(true);
        return ListResponse.of(userAgreementService.list(request), (userAgreement, map) -> {
            StringBuilder body = new StringBuilder();
            map = new CoreMap();
            if (userAgreement.getContractItem() != null && userAgreement.getContractItem().getParent() != null) {
                body.append(userAgreement.getContractItem().getParent().getCode()).append(" raqamli kontraktga initsiator bo'lib qo'shildingiz! \n").append(" Kontrakt bo'yicha TMS \n ");
                map.addString("contractItemId", userAgreement.getContractItem().getId().toString());
            }
            if (userAgreement.getContractItem() != null && userAgreement.getContractItem().getProduct() != null) {
                body.append(userAgreement.getContractItem().getProduct().getName());
            }


            map.addString("body", body.toString());
            map.addString("phoneNumberOMTK", userAgreement.getAuditInfo().getCreatedByUser().getPhone());
            map.addString("omtkInfo", userAgreement.getAuditInfo().getCreatedByUser().getShortName());
            if (!ServerUtils.isEmpty(userAgreement.getNotificationSentTime()))
                map.addString("sentTime", "" + userAgreement.getNotificationSentTime());

            map.addString("title", "OMTK");
            if (userAgreement.getContractItem() != null) {
                if (userAgreement.getContractItem().getParent() != null) {
                    map.add("contractId", userAgreement.getContractItem().getParent().getId());
                    map.add("contractCode", userAgreement.getContractItem().getParent().getCode());
                }
                if (userAgreement.getContractItem().getProduct() != null)
                    map.add("productName", userAgreement.getContractItem().getProduct().getName());
            }
            return map;
        });
    }

    @ApiOperation(value = "User Agreement list By Contract")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-initiator/by-conctract", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getInitiatorByContract(UserAgreementFilterRequest request) {
        return userAgreementService.getContracts(request);
    }

    @ApiOperation(value = "Contract list By Initiator")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/list-contract/by-initiator", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse getInitiatorByContract(UserAgreementByInitiatorFilterRequest filter) {
        return userAgreementService.getContracts(filter);
    }


//    @ApiOperation(value = "User Agreement Create")
//    @Transactional()
//    @PostMapping(value = "/create/by-conctract", produces = MediaType.APPLICATION_JSON_VALUE)
//    public SingleResponse createByContract(UserAgreementSaveByContractRequest request) {
//        return SingleResponse.of(userAgreementService.createByContract(request));
//    }

    @ApiOperation(value = "User Agreement create")
    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody UserAgreementRequest request) {
        return SingleResponse.of(userAgreementService.createByContractItem(request), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "User Agreement change initiator")
    @Transactional
    @PostMapping(value = "/change-initiator", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse change(@RequestBody UserAgreementChangeInitiatorRequest request) {
        return SingleResponse.of(userAgreementService.changeInitsiator(request), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "User Agreement read notification")
    @Transactional
    @PostMapping(value = "/read-and-arrived", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse read(@RequestBody UserAgreementReadAcceptRequest request) {
        userAgreementService.readAndArrive(request);
        return SingleResponse.of(true);
    }

    @ApiOperation(value = "User Agreement status change to accept")
    @Transactional
    @PostMapping(value = "/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse accept(@RequestBody Long contractItemId) {
        return SingleResponse.of(userAgreementService.accept(contractItemId), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "User Agreement status change to reject")
    @Transactional
    @PostMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse reject(UserAgreementChangeStatusRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return SingleResponse.of(userAgreementService.changeStatus(request, UserAgreementStatusType.REJECTED, files), (contract, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @ApiOperation(value = "User Agreement status change to part accept")
    @Transactional
    @PostMapping(value = "/part-accept", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse partAcceptItem(UserAgreementChangeStatusRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return SingleResponse.of(userAgreementService.changeStatus(request, UserAgreementStatusType.PART_ACCEPTED, files), (contractItem, map) -> {
            map.remove("hashESign");
            return map;
        });
    }

    @Transactional
    @ApiOperation(value = "Delete contract item")
    @DeleteMapping(value = "/delete-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse deleteItem(@RequestBody DeleteRequest request) {
        userAgreementService.delete(request);
        return SingleResponse.of(true);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get detail")
    @GetMapping(value = "/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse getDetail(@PathVariable Long id) {
        return userAgreementService.getDetail(id);
    }

    @Transactional
    @ApiOperation(value = "Check Hash ESign")
    @PostMapping(value = "/check-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(Long id) {
        return userAgreementService.checkEDS(id);
    }

    @Transactional
    @ApiOperation(value = "Check Before Sign")
    @PostMapping(value = "/check-before-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkBeforeSigning(UserAgreementSaveByContractRequest request) {
        return SingleResponse.of(userAgreementService.checkInitiatorByContractItem(request));
    }

    @Transactional
    @ApiOperation(value = "Set Hash ESign")
    @PostMapping(value = "/set-hash-e-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse checkHashESign(@RequestBody HashESignRequest request) {
        return userAgreementService.setHashESign(request);
    }
}