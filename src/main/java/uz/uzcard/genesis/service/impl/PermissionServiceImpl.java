package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.api.req.setting.PermissionRequest;
import uz.uzcard.genesis.dto.api.req.setting.RolePermissionRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.PermissionDao;
import uz.uzcard.genesis.hibernate.dao.RoleDao;
import uz.uzcard.genesis.hibernate.entity._Role;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.service.PermissionService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.util.List;
import java.util.stream.Stream;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private RoleDao roleDao;

    @Override
    public Stream<_Permission> getParents() {
        return permissionDao.list().filter(permission -> permission.getParent() == null);
    }

    @Override
    public Stream<_Permission> getChilds(_Permission permission) {
        return permissionDao.list().filter(child -> child.getParent() == permission);
    }

    @Override
    public _Permission save(PermissionRequest request) {
        _Permission permission = permissionDao.getByCode(request.getCode());
        if (permission == null) {
            permission = new _Permission();
            permission.setCode(request.getCode());
        }
        permission.setParent(permissionDao.getByCode(request.getParentCode()));
        permission.setName(request.getName());
        if (_State.DELETED.equals(permission.getState()))
            permission.setState(_State.NEW);
        return permissionDao.save(permission);
    }

    @Override
    public void delete(Long id) {
        permissionDao.delete(id);
    }

    @Override
    public void bindToRole(RolePermissionRequest request) {
        _Role role = roleDao.getByCode(request.getRole());
        if (role == null)
            throw new ValidatorException(GlobalizationExtentions.localication("ROLE_NOT_FOUND"));
        _Permission permission = permissionDao.getByCode(request.getPermission());
        if (permission == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PERMISSION_NOT_FOUND"));
        if (request.isAdd()) {
            if (!role.getPermissions().contains(permission)) {
                role.getPermissions().add(permission);
                permission.getRoles().add(role);
            }
        } else {
            role.getPermissions().remove(permission);
            permission.getRoles().remove(role);
        }
        permissionDao.save(permission);
        roleDao.save(role);
        permissionDao.reindex(List.of(permission.getId()));
        roleDao.reindex(List.of(role.getId()));
    }
}