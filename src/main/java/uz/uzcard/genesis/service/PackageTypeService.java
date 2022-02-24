package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.PackageTypeRequest;

public interface PackageTypeService {

    Long save(PackageTypeRequest request);

    void delete(Long id);
}
