package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.RoleFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.RoleRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.entity._Role;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public PageStream<_Role> list(RoleFilterRequest request) {
        return roleDao.search(new FilterParameters() {{
            setStart(request.getPage() * request.getLimit());
            setSize(request.getLimit());
            if (request.getCode() != null)
                add("code", request.getCode());
        }});
    }

    @Override
    public _Role save(RoleRequest request) {
        _Role role = roleDao.getByCode(request.getCode());
        if (role == null) {
            role = new _Role();
            role.setCode(request.getCode());
        }
        role.setString(request.getName());
        if (_State.DELETED.equals(role.getState()))
            role.setName(_State.NEW);
        return roleDao.save(role);
    }

    @Override
    public void delete(Long id) {
        roleDao.delete(id);
    }
}
