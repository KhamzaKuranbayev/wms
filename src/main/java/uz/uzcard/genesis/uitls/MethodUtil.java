package uz.uzcard.genesis.uitls;

import uz.uzcard.genesis.hibernate.base._Entity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class MethodUtil {
    public static Collection<Method> getDeclaredFields(Class t) {
        if (_Entity.methodsMap.get(t) == null) {
            _Entity.methodsMap.put(t, new HashMap<>());
            Arrays.stream(t.getDeclaredMethods()).forEach(method ->
                    _Entity.methodsMap.get(t).put(method.getName(), method));
        }
        return _Entity.methodsMap.get(t).values();
    }
}
