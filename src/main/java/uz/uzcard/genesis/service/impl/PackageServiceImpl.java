package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.PackageRequest;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.PackageDao;
import uz.uzcard.genesis.hibernate.dao.PackageTypeDao;
import uz.uzcard.genesis.hibernate.dao.ProductDao;
import uz.uzcard.genesis.hibernate.entity._Package;
import uz.uzcard.genesis.service.PackageService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {
    @Autowired
    private PackageDao packageDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private PackageTypeDao packageTypeDao;

    @Override
    public List<HashMap<String, String>> list() {
        return packageDao.search(new FilterParameters()).stream()
                .map(aPackage -> {
                    CoreMap map = aPackage.getMap();
                    if (aPackage.getProduct() != null)
                        map.add("productName", aPackage.getProduct().getName());
                    if (aPackage.getType() != null)
                        map.add("typeName", aPackage.getType().getName());
                    return map.getInstance();
                }).collect(Collectors.toList());
    }

    @Override
    public Long save(PackageRequest request) {
        _Package packages = packageDao.getByRequestFilter(request);
        if (packages == null) {
            packages = new _Package();
            packages.setProduct(productDao.get(request.getProduct_id()));
            packages.setType(packageTypeDao.get(request.getPackageType_id()));
            packages.setWidth(request.getWidth());
            packages.setHeight(request.getHeight());
            packages.setDepth(request.getDepth());
            packages.setCode(String.format("%s x %s x %s", request.getWidth(), request.getHeight(), request.getDepth()));
            packageDao.save(packages);
        }
        return packages.getId();
    }

    @Override
    public void delete(Long id) {
        _Package packageType = packageDao.get(id);
        if (packageType == null)
            throw new ValidatorException(GlobalizationExtentions.localication("PACKAGE_NOT_FOUND"));
        packageDao.delete(packageType);
    }
}
