package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.dto.api.req.setting.PackageRequest;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._Package;

public interface PackageDao extends Dao<_Package> {
    _Package getByRequestFilter(PackageRequest request);
}
