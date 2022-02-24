package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.api.req.setting.PackageTypeRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.PackageTypeDao;
import uz.uzcard.genesis.hibernate.entity._PackageType;
import uz.uzcard.genesis.service.PackageTypeService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

@Service
public class PackageTypeServiceImpl implements PackageTypeService {
    @Autowired
    private PackageTypeDao packageTypeDao;

    @Override
    public Long save(PackageTypeRequest request) {
        _PackageType packageType = packageTypeDao.get(request.getId());
        if (packageType == null) {
            packageType = new _PackageType();
            if (packageTypeDao.checkByName(request.getName()))
                throw new ValidatorException(String.format(GlobalizationExtentions.localication("PACKAGE_TYPE_NAME_CREATE_ALREADY"), request.getName()));
        }
        packageType.setName(request.getName());
        packageTypeDao.save(packageType);
        return packageType.getId();
    }

    @Override
    public void delete(Long id) {
        _PackageType packageType = packageTypeDao.get(id);
        if (packageType == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PACKAGE_TYPE_NOT_FOUND"));
        packageTypeDao.delete(packageType);
    }
}
