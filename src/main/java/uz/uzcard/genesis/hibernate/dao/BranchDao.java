package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Branch;

import java.util.stream.Stream;

public interface BranchDao extends Dao<_Branch> {
    Stream<_Branch> findParents();

    _Branch getByMfo(String mfo);

    Stream<_Branch> findChild(String mfo);
}
