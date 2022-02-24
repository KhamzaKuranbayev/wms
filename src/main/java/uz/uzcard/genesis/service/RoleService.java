package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.FilterBase;
import uz.uzcard.genesis.dto.api.req.setting.RoleFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.RoleRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._Role;

public interface RoleService {
    PageStream<_Role> list(RoleFilterRequest request);

    _Role save(RoleRequest request);

    void delete(Long id);
}
