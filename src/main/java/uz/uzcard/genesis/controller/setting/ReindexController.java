package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.search.FullTextSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.entity.*;
import uz.uzcard.genesis.service.AccountService;
import uz.uzcard.genesis.service.PartitionService;
import uz.uzcard.genesis.service.WarehouseService;

@PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
@Api(value = "Reindex Controller")
@RestController
@RequestMapping("/api/reindex")
public class ReindexController {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PartitionService partitionService;
    @Autowired
    private WarehouseService warehouseService;

    @Transactional
    @ApiOperation(value = "State")
    @GetMapping(value = "/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public void state(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_State.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Package")
    @GetMapping(value = "/package", produces = MediaType.APPLICATION_JSON_VALUE)
    public void packages(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_PackageType.class, _Package.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Product")
    @GetMapping(value = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
    public void product(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Product.class, _ProductGroup.class, _ProductType.class, _GivenProducts.class, _ProduceHistory.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(1000)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
            fullTextSession.createIndexer(_ProductItem.class)
                    .threadsToLoadObjects(10000)
                    .batchSizeToLoadObjects(10000)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();

            fullTextSession.createIndexer(_ProductItemSummByDepartment.class)
                    .threadsToLoadObjects(10000)
                    .batchSizeToLoadObjects(10000)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Contract")
    @GetMapping(value = "/contract", produces = MediaType.APPLICATION_JSON_VALUE)
    public void contract(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Contract.class, _ContractItem.class, _Partition.class, _Lot.class, _UserAgreement.class, _RejectProduct.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Order")
    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public void order(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Order.class, _OrderItem.class, _OrderItemPickUpTime.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Outgoing Contract")
    @GetMapping(value = "/outgoing-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    public void outGoingContract(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_OutGoingContract.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Attributes")
    @GetMapping(value = "/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public void attribute(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Attribute.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Supplier")
    @GetMapping(value = "/supplier", produces = MediaType.APPLICATION_JSON_VALUE)
    public void supplier(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Supplier.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Column settings")
    @GetMapping(value = "/column-settings", produces = MediaType.APPLICATION_JSON_VALUE)
    public void columnSettings(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_ColumnSettings.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Role & Permission")
    @GetMapping(value = "/role-permission", produces = MediaType.APPLICATION_JSON_VALUE)
    public void role(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Role.class, _Permission.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Warehouse")
    @GetMapping(value = "/warehouse", produces = MediaType.APPLICATION_JSON_VALUE)
    public void warehouse(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Warehouse.class, _Stillage.class, _StillageColumn.class, _Carriage.class, _WarehouseX.class, _WarehouseY.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Notification")
    @GetMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public void notification(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Notification.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Inventarization")
    @GetMapping(value = "/inventarization", produces = MediaType.APPLICATION_JSON_VALUE)
    public void inventarization(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Inventarization.class, _InventarizationLog.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Realization")
    @GetMapping(value = "/realization", produces = MediaType.APPLICATION_JSON_VALUE)
    public void realization(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Realization.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Unit-Type")
    @GetMapping(value = "/unit-type", produces = MediaType.APPLICATION_JSON_VALUE)
    public void unitType(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_UnitType.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Rent ")
    @GetMapping(value = "/rent", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rent(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Rent.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @ApiOperation(value = "Patient and History ")
    @GetMapping(value = "/patient-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public void patientHistory(Boolean reindexAll) {
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(roleDao.getSession());
        try {
            fullTextSession.createIndexer(_Patient.class, _HistoryOfMedicineTaken.class)
                    .threadsToLoadObjects(1000)
                    .batchSizeToLoadObjects(200)
                    .typesToIndexInParallel(10)
                    .optimizeOnFinish(true)
                    .purgeAllOnStart(true)
                    .optimizeAfterPurge(true)
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}