package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.PermissionRequest;
import uz.uzcard.genesis.dto.api.req.setting.RolePermissionRequest;
import uz.uzcard.genesis.hibernate.entity._Permission;

import java.util.stream.Stream;

public interface PermissionService {

    Stream<_Permission> getParents();

    Stream<_Permission> getChilds(_Permission permission);

    _Permission save(PermissionRequest request);

    void delete(Long id);

    void bindToRole(RolePermissionRequest request);
}
