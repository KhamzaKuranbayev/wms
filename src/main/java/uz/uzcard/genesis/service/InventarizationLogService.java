package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.contract.InventarizationLogFilter;
import uz.uzcard.genesis.dto.api.req.product.InventarizationLogRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._InventarizationLog;

public interface InventarizationLogService {
    PageStream<_InventarizationLog> list(InventarizationLogFilter request);

    void save(InventarizationLogRequest request);
}
