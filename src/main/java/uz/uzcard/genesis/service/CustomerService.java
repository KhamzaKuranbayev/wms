package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.contract.CustomerFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.CustomerRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.hibernate.entity._Customer;

public interface CustomerService {
    _Customer save(CustomerRequest request);

    ListResponse list(CustomerFilterRequest request);

    void delete(Long id);
}
