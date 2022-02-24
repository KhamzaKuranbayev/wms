package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.DepartmentFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.DepartmentRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;

public interface DepartmentService {
    ListResponse items(DepartmentFilterRequest request);

    ListResponse list(DepartmentFilterRequest request);

    SingleResponse save(DepartmentRequest request);

    void delete(Long id);
}