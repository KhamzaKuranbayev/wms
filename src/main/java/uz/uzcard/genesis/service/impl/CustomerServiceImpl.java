package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.contract.CustomerFilterRequest;
import uz.uzcard.genesis.dto.api.req.contract.CustomerRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.hibernate.dao.CustomerDao;
import uz.uzcard.genesis.hibernate.entity._Customer;
import uz.uzcard.genesis.service.CustomerService;

import java.util.List;
import java.util.stream.Collectors;

/*
    bu service ishlatilmayotgan bo'lishi mumkin.
 */

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Override
    public _Customer save(CustomerRequest request) {
        if (request.getName() == null || request.getName() == "")
            throw new RpcException("MUST_ENTER_NAME");
        _Customer customer;
        if (request.getId() == null) {
            customer = customerDao.checkCustomerByName(request.getName());
            if (customer != null)
                throw new RpcException("CUSTUMER_WITH_SAME_NAME_IS_ALREADY_REGISTERED");
            customer = new _Customer();
            customer.setName(request.getName());
        } else {
            customer = customerDao.checkCustomerWithOutName(request.getName(), request.getId());
            if (customer != null)
                throw new RpcException("CUSTUMER_WITH_SAME_NAME_IS_ALREADY_REGISTERED");
            customer = customerDao.getById(request.getId());
            if (customer == null)
                throw new RpcException("CUSTOMER_NOT_FOUND");
            customer.setName(request.getName());
        }
        return customerDao.save(customer);
    }

    @Override
    public ListResponse list(CustomerFilterRequest request) {
        FilterParameters filterParameters = new FilterParameters();
        if (request.getId() != null)
            filterParameters.add("id", request.getId().toString());
        if (request.getName() != null && request.getName() != "")
            filterParameters.add("name", request.getName());

        filterParameters.setSize(request.getLimit());
        filterParameters.setStart(request.getPage() * request.getLimit());
        List<_Customer> customers = customerDao.list(filterParameters).collect(Collectors.toList());
        Integer total = customerDao.total(filterParameters);
        return ListResponse.of(customers, total);
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new RpcException("ID_REQUIRED");
        _Customer customer = customerDao.getById(id);
        if (customer == null)
            throw new RpcException("CUSTOMER_NOT_FOUND");
        customerDao.delete(id);
    }
}
