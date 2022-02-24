package uz.uzcard.genesis.hibernate.dao;

import uz.uzcard.genesis.dto.api.req.setting.FirebaseRequest;
import uz.uzcard.genesis.hibernate.base.Dao;
import uz.uzcard.genesis.hibernate.entity._FirebaseToken;

import java.util.stream.Stream;

/**
 * Created by norboboyev_h  on 07.07.2020  17:42
 */

public interface FirebaseTokenDao extends Dao<_FirebaseToken> {
    _FirebaseToken findByDeviceAndUser(String deviceId);

    Stream<_FirebaseToken> findByUser(Long userId);
}
