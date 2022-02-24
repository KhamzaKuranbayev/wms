package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.product.RentChangeStatusRequest;
import uz.uzcard.genesis.dto.api.req.product.RentFilterRequest;
import uz.uzcard.genesis.dto.api.req.product.RentRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Rent;

import java.util.Collection;

public interface RentService {

    _Rent save(RentRequest request);

    PageStream<_Rent> search(RentFilterRequest request);

    _Rent update(RentChangeStatusRequest request);
}
