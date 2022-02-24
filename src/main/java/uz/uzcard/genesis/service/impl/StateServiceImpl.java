package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.StateRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.StateDao;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.StateService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

@Service
public class StateServiceImpl implements StateService {
    @Autowired
    private StateDao stateDao;

    @Override
    public PageStream<_State> listByEntityName(String entityName) {
        return stateDao.search(new FilterParameters() {{
            addString("entityName", entityName);
            setSize(Integer.MAX_VALUE);
        }});
    }

    @Override
    public _State save(StateRequest request) {
        _State state = stateDao.getByCode(request.getCode());
        if (state == null) {
            state = new _State();
            state.setCode(request.getCode());
        }
        state.setEntityName(request.getEntityName());
        state.setSortOrder(request.getSortOrder());
        state.setName(request.getName());
        state.setDescription(request.getDescription());
        state.setColour(request.getColour());
        return stateDao.save(state);
    }

    @Override
    public String getMessage(String code) {
        String status = GlobalizationExtentions.localication(code);
        if (StringUtils.isEmpty(status) || code.equals(status)) {
            _State state = stateDao.getByCode(code);
            return status != null ? status : state.getName();
        }
        return status;
    }

    @Override
    public String getColour(String code) {
        _State state = stateDao.getByCode(code);
        if (state == null) return null;
        return state.getColour();
    }

    @Override
    public String getDescription(String code) {
        _State state = stateDao.getByCode(code);
        if (state == null) return null;
        return state.getDescription();
    }

    @Override
    public void wrapStatus(CoreMap map, String state) {
        map.add("statusCode", state);
        map.add("statusName", getMessage(state));
        map.add("statusDescription", getDescription(state));
        map.add("statusColour", getColour(state));
        map.remove("hashESign");
    }
}