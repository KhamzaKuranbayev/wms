package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.SelectItem;
import uz.uzcard.genesis.dto.api.req.patient.ProduceRequest;
import uz.uzcard.genesis.dto.api.req.product.*;
import uz.uzcard.genesis.dto.api.req.setting.TakenProductRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Partition;
import uz.uzcard.genesis.hibernate.entity._Product;
import uz.uzcard.genesis.hibernate.entity._ProductItem;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ProductItemService {

    List<SelectItem> getPackageTypesByProduct(Long product_id);

    List<Map<String, String>> getPackageByProjectAndPackageType(Long product_id, Long packageType_id);

    PageStream<_ProductItem> list(ProductFilterItemRequest request);

    void checkProductCount(_Partition partition, Double count);

    Boolean income(ProductItemIncomeReq request);

//    Boolean reset();

    Boolean save(ProductItemRequest request);

    SingleResponse generateQrCode(Long qrCode, HttpServletResponse response, boolean forOnlyGetImage);

    ListResponse getDetailsQrCodeList(ProductItemQrCodeList request);

    ListResponse getDetailsByIds(TakenProductRequest request);

    SingleResponse getDetailsById(Long productItemId);

    SingleResponse getDetailsByQrCode(Long qrCode);

    SingleResponse getQrCodeFullInfo(Long qrCode);

    void putPosition(_ProductItem productItem, CoreMap map);

    PageStream<_ProductItem> getByCarriage(ProductItemByCarriageFilterRequest request);

    void print(ProductFilterItemRequest request);

    _ProductItem getByAccountingCode(String accountingCode);

    _Product getProductByAccountingCode(String accountingCode);

    _ProductItem saveAccounting(SaveAccountingRequest request);

    PageStream<_ProductItem> givenPatient(ProductFilterItemRequest request);

    _ProductItem get(Long id);

    PageStream<_ProductItem> getByCell(ProductItemByCellFilterRequest request);

    Map<String, Long> excelImport(MultipartFile file);

    Boolean produce(ProduceRequest request);

    Boolean returnProduct();

    List<String> getProductItemAddress(Long partitionId);

    boolean deleteByWarehouse(Long warehouseId, int offset, int limit);

    boolean hasAnyByWarehouse(Long warehouseId);

    _ProductItem split(SplitProductItemRequest request);

    _ProductItem useItem(UseProductItemRequest request, MultipartFile file);
}