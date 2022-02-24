package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Team;
import uz.uzcard.genesis.hibernate.entity._User;

import java.util.List;

public interface TeamDao extends Dao<_Team> {
    List<Long> findIdsByUser(_User user);

    List<Long> getMyChildTeamIds();
}