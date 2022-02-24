package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.warehouse.StillageColumnFilterRequest;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;

import java.util.HashMap;
import java.util.List;

public interface StillageColumnService {
    List<HashMap<String, String>> list(StillageColumnFilterRequest request);

    _StillageColumn create(_Stillage stillage, String key, boolean preview);
}
