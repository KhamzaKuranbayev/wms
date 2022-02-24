/*
package uz.uzcard.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzcard.genesis.controller.contract.ContractController;
import uz.uzcard.genesis.controller.contract.ContractItemController;
import uz.uzcard.genesis.dto.api.req.contract.ContractRequest;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.enums.SupplyType;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class ContractTest {
    @Autowired
    private ContractController contractController;
    @Autowired
    private ContractItemController contractItemController;
    @Autowired
    private SupplierTest supplierTest;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(Long orderItemId, String fileName) throws IOException {
        SingleResponse response = contractController.save(new ContractRequest() {{
            setCode(ServerUtils.generateUniqueCode());
            setOrderItemId(orderItemId);
            setConclusionDate(new Date());
            setGuessReceiveDate(new Date());
            setSupplyType(SupplyType.Import);
            setSupplierId(supplierTest.getFirst());
        }}, FileUtilsTest.getMultipartFile(fileName));
        assertTrue(response.isSuccess());
    }
}*/
