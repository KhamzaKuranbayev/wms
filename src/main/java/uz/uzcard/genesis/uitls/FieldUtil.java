package uz.uzcard.genesis.uitls;

import uz.uzcard.genesis.hibernate.base._Entity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldUtil {
    public static List<Field> getDeclaredFields(Class t) {
        if (_Entity.fieldsMap.get(t) == null)
            _Entity.fieldsMap.put(t, Arrays.asList(t.getDeclaredFields()));
        return _Entity.fieldsMap.get(t);
    }
}