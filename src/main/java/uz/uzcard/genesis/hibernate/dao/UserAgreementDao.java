package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Contract;
import uz.uzcard.genesis.hibernate.entity._ContractItem;
import uz.uzcard.genesis.hibernate.entity._User;
import uz.uzcard.genesis.hibernate.entity._UserAgreement;

import java.util.stream.Stream;

public interface UserAgreementDao extends Dao<_UserAgreement> {

    _UserAgreement getByContractItem(_ContractItem contractItem, _User user);

    Stream<_User> getByContractItem(Long contractId);

    PageStream<_Contract> getContractListByCurrentUser(FilterParameters filter);

    int getCountAcceptedAndTotalByContract(_Contract contract, boolean forTotal);
}
