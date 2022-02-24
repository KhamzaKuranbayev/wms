package uz.uzcard.genesis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzcard.genesis.dto.api.req.product.ProductFilterItemRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.base._Entity;
import uz.uzcard.genesis.hibernate.dao.*;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._ProductItem;

import java.util.List;

@Api(value = "Test controller", description = "Test")
@RestController
@RequestMapping(value = "/api/test")
public class TestController {

    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private ContractItemDao contractItemDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private UserAgreementDao userAgreementDao;
    @Autowired
    private ProductItemDao productItemDao;
    @Autowired
    private GivenProductsDao givenProductsDao;
    @Autowired
    private PartitionDao partitionDao;
    @Autowired
    private LotDao lotDao;

    private <T extends _Entity> T revert(Dao dao, T entity) {
        T entity2 = (T) dao.get(entity.getId());
        entity2.setMap(entity.getMap());
        return entity2;
    }

    private <T extends _Entity> int getCurrentRevision(T entity) {
        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());

        Object[] singleResult = (Object[]) reader.createQuery().forRevisionsOfEntity(entity.getClass(), false, true)
                .add(AuditEntity.id().eq(entity.getId()))
                .add(AuditEntity.revisionNumber().maximize())
                .getSingleResult();
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) singleResult[1];
        return revisionEntity.getId();
    }

    public <T extends _Entity> T getPreviousVersion(T entity, int current_rev) {
        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());

        Number prior_revision = (Number) reader.createQuery()
                .forRevisionsOfEntity(entity.getClass(), false, true)
                .addProjection(AuditEntity.revisionNumber().max())
                .add(AuditEntity.id().eq(entity.getId()))
                .add(AuditEntity.revisionNumber().lt(current_rev))
                .getSingleResult();

        if (prior_revision != null)
            return (T) reader.find(entity.getClass(), entity.getId(), prior_revision);
        else
            return null;
    }

    @ApiOperation(value = "Get items")
    @Transactional
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse items(@RequestParam(value = "contractId") Long id) {
        AuditReader reader = AuditReaderFactory.get(orderItemDao.getSession());
        List<Object[]> list = reader.createQuery().forRevisionsOfEntity(_ContractItem.class, false, true)
                .add(AuditEntity.id().eq(532l))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
        _Contract contract = new _Contract();
        contract.setId(id);
        int current_rev = getCurrentRevision(contract);
        contract = getPreviousVersion(contract, current_rev);
        contractDao.save(revert(contractDao, contract));
        int finalCurrent_rev = current_rev;
        contract.getItems().forEach(contractItem -> {
            contractItemDao.save(revert(contractItemDao, contractItem));

            contractItem.getUserAgreements().forEach(userAgreement -> {
                userAgreementDao.save(revert(userAgreementDao, userAgreement));
            });

            contractItem.getPartitions().forEach(partition -> {
                partitionDao.save(revert(partitionDao, partition));
                partition.getLots().forEach(lot -> {
                    lotDao.save(revert(lotDao, lot));
                });
                partition.getGivens().forEach(givenProducts -> {
                    givenProductsDao.save(revert(givenProductsDao, givenProducts));
                });
                List<_ProductItem> productItems = reader.createQuery().forEntitiesAtRevision(_ProductItem.class, finalCurrent_rev)
                        .add(AuditEntity.property("partition").eq(partitionDao.get(partition.getId())))
                        .getResultList();
                productItems.forEach(productItem -> {
                    productItem = getPreviousVersion(productItem, finalCurrent_rev);
                    productItemDao.save(revert(productItemDao, productItem));
                });

            });
        });
        return ListResponse.of(list);
    }
}