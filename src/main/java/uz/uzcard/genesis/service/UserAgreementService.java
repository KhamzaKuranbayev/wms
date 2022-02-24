package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._OrderItem;
import uz.uzcard.genesis.hibernate.entity._UserAgreement;
import uz.uzcard.genesis.hibernate.enums.UserAgreementStatusType;

import java.util.Collection;
import java.util.List;

public interface UserAgreementService {
    Collection<CoreMap> getInitsiators(UserAgreementFilterRequest request);

    PageStream<_UserAgreement> list(UserAgreementFilterRequest request);

    ListResponse getContracts(UserAgreementFilterRequest request);

    ListResponse getContracts(UserAgreementByInitiatorFilterRequest filter);

    void createByOrderItem(_OrderItem orderItem);

    _UserAgreement createByContractItem(UserAgreementRequest request);

    void createForOzl(InitiatorOZLRequest request);

    Boolean checkInitiatorByContractItem(UserAgreementSaveByContractRequest request);

    _UserAgreement changeStatus(UserAgreementChangeStatusRequest request, UserAgreementStatusType statusType, List<MultipartFile> files);

    _UserAgreement accept(Long id);

    void readAndArrive(UserAgreementReadAcceptRequest request);

    _UserAgreement changeInitsiator(UserAgreementChangeInitiatorRequest request);

    void delete(DeleteRequest request);

    SingleResponse getDetail(Long id);

    SingleResponse checkEDS(Long userAgreementId);

    SingleResponse setHashESign(HashESignRequest request);
}
