package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._OutGoingContract;

public interface OutGoingContractDao extends Dao<_OutGoingContract> {

    Boolean checkByContractNumber(String contractNumber);

    _OutGoingContract getByContractNumber(String contractNumber);
}
