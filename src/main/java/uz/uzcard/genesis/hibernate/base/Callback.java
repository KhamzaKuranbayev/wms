package uz.uzcard.genesis.hibernate.base;


import uz.uzcard.genesis.dto.CoreMap;

public interface Callback<T> {
    CoreMap execute(T t, CoreMap map);
}
