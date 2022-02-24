package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.history.ContractHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.OrderHistoryRequest;
import uz.uzcard.genesis.dto.api.req.history.ProductItemRequest;
import uz.uzcard.genesis.dto.backend.PreviousChangesRequest;

import java.util.HashMap;
import java.util.stream.Stream;

public interface HistoryService {
    Stream<HashMap<String, Object>> orders(OrderHistoryRequest request);

    Stream<HashMap<String, Object>> orderItems(OrderHistoryRequest request);

    Stream<HashMap<String, Object>> contracts(ContractHistoryRequest request);

    Stream<HashMap<String, Object>> contractItems(ContractHistoryRequest request);

    Stream<HashMap<String, Object>> getProductItemHistory(ProductItemRequest request);

    void previousChangesContract(PreviousChangesRequest request);

    void previousChangesContractItem(PreviousChangesRequest request);

    void previousChangesOrder(PreviousChangesRequest request);

    void previousChangesOrderItem(PreviousChangesRequest request);
}
