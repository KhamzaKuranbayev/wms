package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.contract.*;
import uz.uzcard.genesis.dto.api.req.dashboard.DashboardFilter;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._AttachmentView;
import uz.uzcard.genesis.hibernate.entity._Contract;

import java.util.List;

public interface ContractService {
    PageStream<_Contract> list(ContractFilterRequest request);

    _Contract save(ContractRequest request, MultipartFile file);

    _Contract update(ContractUpdateRequest request, MultipartFile file);

    _Contract saveOzl(ContractRequest request, MultipartFile file);

    _Contract reject(ContractRejectRequest request, MultipartFile file);

    _Contract accept(Long id);

    List<SelectItem> getItems(ContractFilterItemsRequest request);

    _Contract get(Long id);

    _AttachmentView agreementFile(Long id, MultipartFile file);

    void agreementFileDelete(AgreementFileDeleteRequest request);

    void delete(Long id);

    ListResponse supplierBlackListByContract(DashboardFilter filterRequest);

    SingleResponse getContractStatus(DashboardFilter filterRequest);

    //    SingleResponse checkEDS(Long contractId);
//
//    SingleResponse setHashESign(HashESignRequest request);
}