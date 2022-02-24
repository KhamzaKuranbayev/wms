package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.PackageRequest;

import java.util.HashMap;
import java.util.List;

public interface PackageService {
    List<HashMap<String, String>> list();

    Long save(PackageRequest request);

    void delete(Long id);
}
