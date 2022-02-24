package uz.uzcard.genesis.hibernate.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.StringUtils;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.uitls.FieldUtil;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.MethodUtil;
import uz.uzcard.genesis.uitls.ServerUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
public abstract class _Entity implements Serializable, Cloneable {
    private static final long serialVersionUID = 10L;
    @Transient
    private static final Logger log = LogManager.getLogger(_Entity.class);
    @Transient
    public static HashMap<Class, List<Field>> fieldsMap = new HashMap<>();
    @Transient
    public static HashMap<Class, HashMap<String, Method>> methodsMap = new HashMap<>();

    public static String getEntityName(_Entity o) {
        if (o instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) o;
            return proxy.getHibernateLazyInitializer().getEntityName();
        }
        Entity entity = o.getClass().getAnnotation(Entity.class);
        if (entity != null) {
            return !"".equals(entity.name()) ? entity.name() : o.getClass().getName();
        }
        return "";
    }

    public static List<Field> getFields(Class<?> clazz) {
        if (!fieldsMap.containsKey(clazz)) {
            Class<?> cls = clazz;
            List<Field> fields = new ArrayList<>();
            HashMap<String, Method> methods = new HashMap<>();
            while (!_Entity.class.equals(cls)) {
                fields.addAll(FieldUtil.getDeclaredFields(cls));
//                Arrays.stream(MethodUtil.getPublicMethods(cls)).forEach(method -> methods.put(method.getName(), method));
                MethodUtil.getDeclaredFields(cls).forEach(method -> methods.put(method.getName(), method));
                cls = cls.getSuperclass();
            }
            fields = fields.stream().filter(field -> !field.getName().startsWith("$$")).collect(Collectors.toList());
            fieldsMap.put(clazz, fields);
            methodsMap.put(clazz, methods);
        }
        return fieldsMap.get(clazz);
    }

    public static <T extends _Entity> T getLazyColumn(T column) {
        if (!Hibernate.isInitialized(column)) {
            try {
                Hibernate.initialize(column);
            } catch (Exception one) {
                column = null;
            }
        }
        return column;
    }

    public abstract Long getId();

    public abstract void setId(Long id);

    @JsonIgnore
    public abstract String getState();

    public abstract void setState(String state);

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof _Entity)) {
            return false;
        }
        if (getId() == null) {
            return false;
        }

        _Entity unObject = (_Entity) o;

        String table1 = getEntityName(this);
        String table2 = getEntityName(unObject);
        return !(table1 == null || !table1.equals(table2)) && getId().equals(unObject.getId());
    }

    @JsonIgnore
    public Boolean isNew() {
        return getId() == null;
    }

    public String toString() {
        return "[id=" + getId() + "]";
    }

    public int hashCode() {
        return (getId() != null ? getId().hashCode() : 0);
    }

    @JsonIgnore
    public CoreMap getMap() throws ValidatorException {
        return getMap(false);
    }

    public void setMap(CoreMap map) throws ValidatorException {
        try {
            for (Field field : getFields(this.getClass())) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
                field.setAccessible(true);
                if (!map.containts(field.getName()))
                    continue;
                switch (field.getType().getSimpleName()) {
                    case "String": {
                        field.set(this, map.getString(field.getName()));
                    }
                    break;
                    case "Double": {
                        field.set(this, map.getDouble(field.getName()));
                    }
                    break;
                    case "double": {
                        field.set(this, map.get_double(field.getName()));
                    }
                    break;
                    case "Boolean": {
                        field.set(this, map.getBoolean(field.getName()));
                    }
                    break;
                    case "boolean": {
                        field.set(this, map.getBool(field.getName()));
                    }
                    break;
                    case "Integer": {
                        if (field.isAnnotationPresent(MwiDouble.class)) {
                            MwiDouble annotation = field.getAnnotation(MwiDouble.class);
                            field.set(this, map.getMwiInteger(field.getName(), annotation.digits()));
                        } else
                            field.set(this, map.getInteger(field.getName()));
                    }
                    break;
                    case "int": {
                        if (field.isAnnotationPresent(MwiDouble.class)) {
                            MwiDouble annotation = field.getAnnotation(MwiDouble.class);
                            Integer n = map.getMwiInteger(field.getName(), annotation.digits());
                            field.set(this, n == null ? 0 : n);
                        } else
                            field.set(this, map.getInteger(field.getName()));
                    }
                    break;
                    case "Long": {
                        if (field.isAnnotationPresent(MwiDouble.class)) {
                            MwiDouble annotation = field.getAnnotation(MwiDouble.class);
                            field.set(this, map.getMwiLong(field.getName(), annotation.digits()));
                        } else
                            field.set(this, map.getLong(field.getName()));
                    }
                    break;
                    case "long": {
                        if (field.isAnnotationPresent(MwiDouble.class)) {
                            MwiDouble annotation = field.getAnnotation(MwiDouble.class);
                            Long l = map.getMwiLong(field.getName(), annotation.digits());
                            field.set(this, l == null ? 0 : l);
                        } else
                            field.set(this, map.get_long(field.getName()));
                    }
                    break;
                    case "Date": {
                        field.set(this, map.getDate(field.getName()));
                    }
                    break;
                    case "Instant": {
                        Date date = map.getDate(field.getName());
                        if (date == null)
                            field.set(this, null);
                        else
                            field.set(this, date.toInstant());
                    }
                    break;
                    case "LocalTime": {
                        if (!StringUtils.isEmpty(map.getString(field.getName()))) {
                            LocalTime time = LocalTime.parse(map.getString(field.getName()));
                            field.set(this, time);
                        } else
                            field.set(this, null);
                    }
                    break;
                    case "LocalDate": {
                        if (!StringUtils.isEmpty(map.getString(field.getName()))) {
                            LocalDate time = LocalDate.parse(map.getString(field.getName()), ServerUtils.dateFormat);
                            field.set(this, time);
                        } else
                            field.set(this, null);
                    }
                    break;
                    default: {
                        if (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType())) {
                            continue;
                        } else {
//                            log.error(field.getType());
                        }
                    }
                }
            }
            if (this instanceof _Item) {
                if (map.containts("name")) {
                    GlobalizationExtentions.setName((_Item) this, map.getString("name"));
                }
            }
        } catch (Exception e) {
            System.out.println("!!!!!!!!!!!!!! PARSE ERROR !!!!!!!!!!!!!!!");
            for (Map.Entry<String, String> entry : map.getInstance().entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
            e.printStackTrace();
            throw new RpcException(e.getMessage());
        }
    }

    public CoreMap getMap(boolean deeply) throws ValidatorException {
        CoreMap map = new CoreMap();
        try {
            for (Field field : getFields(this.getClass())) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
                if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).insertable())
                    continue;
                field.setAccessible(true);

                Object value = field.get(this);
                if (value == null) {
                    map.addString(field.getName(), null);
                    continue;
                }

                switch (field.getType().getSimpleName()) {
                    case "String":
                    case "Boolean":
                    case "boolean": {
                        map.addString(field.getName(), String.valueOf(value));
                    }
                    break;
                    case "Double":
                    case "double":
                    case "Integer":
                    case "int":
                    case "Long":
                    case "long": {
                        map.addString(field.getName(), String.valueOf(value));
                        if (field.isAnnotationPresent(MwiDouble.class)) {
                            MwiDouble annotation = field.getAnnotation(MwiDouble.class);
                            value = map.getMwiDouble(field.getName(), annotation.digits());
                            map.addString(field.getName(), String.valueOf(value));
                        }
                    }
                    break;
                    case "Date": {
                        map.addDate(field.getName(), (Date) value);
                    }
                    break;
                    case "Instant": {
                        map.addDate(field.getName(), Date.from(((Instant) value)));
                    }
                    break;
                    case "LocalTime": {
                        map.addString(field.getName(), ((LocalTime) value).format(ServerUtils.getTimeFormat())/* value.toString()*/);
                    }
                    break;
                    case "LocalDate": {
                        map.addString(field.getName(), ((LocalDate) value).format(ServerUtils.dateFormat));
                    }
                    break;
                    default: {
                        if (deeply && _Entity.class.isAssignableFrom(field.getType())) {
                            map.addString(field.getName() + "_id", ((_Entity) value).getId().toString());
                        } else if (deeply && Collection.class.isAssignableFrom(field.getType())) {
//                            map.addStrings(field.getName(), ((Collection<_Entity>) value).stream().map(entity -> "" + entity.getId()).collect(Collectors.toList()));
                            String methodName = String.format("get%s", ServerUtils.upperCamelCase.translate(field.getName()));
                            if (methodsMap.get(this.getClass()).containsKey(methodName)) {
                                Method method = methodsMap.get(this.getClass()).get(methodName);
                                Object data = method.invoke(this);
                                map.addStrings(field.getName(), ((Collection<_Entity>) data).stream().map(entity -> "" + entity.getId()).collect(Collectors.toList()));
                            }
                        } else if (field.getType().isArray() || Map.class.isAssignableFrom(field.getType())) {
                            continue;
                        } else {
//                            log.error(field.getType());
                        }

                    }
                }
            }
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            ServerUtils.error(log, e);
            throw new RpcException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            ServerUtils.error(log, e);
            throw new RpcException(e.getMessage());
        } finally {
            map.setId(this.getId());
            map.setState(this.getState());
            map.addString("id", "" + getId());
            return map;
        }
    }
}