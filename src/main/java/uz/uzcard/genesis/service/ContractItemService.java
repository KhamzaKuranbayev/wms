package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemChangeStatusRequest;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemFilterRequst;
import uz.uzcard.genesis.dto.api.req.contract.ContractItemRequest;
import uz.uzcard.genesis.dto.api.req.order.OrderItemStateChangeRequest;
import uz.uzcard.genesis.dto.api.req.setting.DeleteRequest;
import uz.uzcard.genesis.dto.api.req.setting.HashESignRequest;
import uz.uzcard.genesis.dto.api.req.setting.ItemCountRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.enums.SupplyType;

import javax.servlet.ServletResponse;
import java.util.List;
import java.util.stream.Stream;

public interface ContractItemService {
    _ContractItem save(ContractItemRequest request);

    _ContractItem saveOzl(ContractItemRequest request);

    void saveMultipleOrderItem(_Contract contract, List<Long> orderItemsIds, Long supplierId, SupplyType supplyType, List<Long> userIds);

    PageStream<_ContractItem> list(ContractItemFilterRequst request);

    void deleteItem(DeleteRequest request);

    _ContractItem accept(Long id);

    _ContractItem changeStatus(ContractItemChangeStatusRequest request, List<MultipartFile> files, String statusName);

    CoreMap attachAktFile(Long orderItemId, MultipartFile file);

    SingleResponse generateQrCode(Long contractItemId);

    void generateQrCode(Long contractItemId, ServletResponse response);

    SingleResponse checkEDS(Long contractItemId);

    _ContractItem updateItemCount(ItemCountRequest request);

    SingleResponse setHashESign(HashESignRequest request);

    void changeStatus(_ContractItem contractItem, String state);

    _ContractItem getSingle(ContractItemFilterRequst filter);

    SingleResponse getAktFiles(Long orderItemId);

    _ContractItem changeState(OrderItemStateChangeRequest request);

    Stream<_ContractItem> findAll(_Contract contract);
}
