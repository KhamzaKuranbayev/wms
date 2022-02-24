package uz.uzcard.genesis.hibernate.dao.impl;

import org.springframework.stereotype.Component;
import uz.uzcard.genesis.hibernate.base.DaoImpl;
import uz.uzcard.genesis.hibernate.dao.WarehouseYDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._WarehouseY;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WarehouseYDaoImpl extends DaoImpl<_WarehouseY> implements WarehouseYDao {
    public WarehouseYDaoImpl() {
        super(_WarehouseY.class);
    }

    @Override
    public Stream<_WarehouseY> findByIds(List<Long> ids) {
        return find("select t from _WarehouseY t " +
                        " where t.id in (:ids)",
                preparing(new Entry("ids", ids)));
    }

    @Override
    public List<Long> findCellsByCarriage(List<Long> carriages) {
        return (List<Long>) find("select c.id from _Carriage t left join t.stillageColumn sc left join sc.stillage s left join s.cells c " +
                        " where t.state != :deleted and sc.state != :deleted and s.state != :deleted and t.id in (:carriages)",
                preparing(new Entry("deleted", _State.DELETED), new Entry("carriages", carriages))).collect(Collectors.toList());
    }
}