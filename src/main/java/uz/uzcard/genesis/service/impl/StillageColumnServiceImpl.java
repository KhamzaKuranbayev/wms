package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.warehouse.StillageColumnFilterRequest;
import uz.uzcard.genesis.hibernate.dao.StillageColumnDao;
import uz.uzcard.genesis.hibernate.entity._Stillage;
import uz.uzcard.genesis.hibernate.entity._StillageColumn;
import uz.uzcard.genesis.service.StillageColumnService;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StillageColumnServiceImpl implements StillageColumnService {
    @Autowired
    private StillageColumnDao stillageColumnDao;

    @Override
    public List<HashMap<String, String>> list(StillageColumnFilterRequest request) {
        return stillageColumnDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());

            if (!ServerUtils.isEmpty(request.getStillageId()))
                add("stillageId", request.getStillageId().toString());
        }}).stream().map(warehouse -> {
            CoreMap map = warehouse.getMap();
            return map.getInstance();
        }).collect(Collectors.toList());
    }

    @Override
    public _StillageColumn create(_Stillage stillage, String key, boolean preview) {

        _StillageColumn column = stillageColumnDao.getByCode(stillage, key);
        if (column == null) {
            column = new _StillageColumn();
        }
        column.setCode(key);
        column.setSortOrder(stillageColumnDao.getMaxOrderByStillage(stillage) + 1);
        column.setStillage(stillage);
        if (!preview)
            stillageColumnDao.save(column);
        return column;
    }
}