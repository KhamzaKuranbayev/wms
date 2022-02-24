package uz.uzcard.genesis.dto;

import java.io.Serializable;
import java.util.*;

public class CoreMap extends AbstractMap implements Serializable {
    private final HashMap<String, String> map = new LinkedHashMap<>();
    private final HashMap<String, List<String>> map2 = new LinkedHashMap<String, List<String>>();
    private Long id;
    private String state;

    public CoreMap(Long id) {
        this();
        this.id = id;
    }

    public CoreMap() {
    }

    public Long getId() {
        return id;
    }

    public CoreMap setId(Long id) {
        this.id = id;
        return this;
    }

    public CoreMap add(String key, String value) {
        addString(key, value);
        return this;
    }

    public CoreMap add(String key, Long value) {
        addLong(key, value);
        return this;
    }

    @Override
    public HashMap<String, String> getInstance() {
        return map;
    }

    @Override
    public HashMap<String, List<String>> getInstance2() {
        return map2;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void addAll(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null)
                addString(entry.getKey(), null);
            else
                switch (entry.getValue().getClass().getSimpleName()) {
                    case "String": {
                        addString(entry.getKey(), (String) entry.getValue());
                    }
                    break;
                    case "Double":
                    case "double": {
                        addDouble(entry.getKey(), (Double) entry.getValue());
                    }
                    break;
                    case "Boolean": {
                        addBoolean(entry.getKey(), (Boolean) entry.getValue());
                    }
                    break;
                    case "boolean": {
                        addBool(entry.getKey(), (boolean) entry.getValue());
                    }
                    break;
                    case "Integer": {
                        addInteger(entry.getKey(), (Integer) entry.getValue());
                    }
                    break;
                    case "int": {
                        addInt(entry.getKey(), (int) entry.getValue());
                    }
                    break;
                    case "Long": {
                        addLong(entry.getKey(), (Long) entry.getValue());
                    }
                    break;
                    case "long": {
                        add_long(entry.getKey(), (long) entry.getValue());
                    }
                    break;
                    case "Date": {
                        addDate(entry.getKey(), (Date) entry.getValue());
                    }
                    break;
//                case "Instant": {
//                    addDate(entry.getKey(), ((Instant) entry.getValue()));
//                    Date date = map.getDate(field.getName());
//                    if (date == null)
//                        field.set(this, null);
//                    else
//                        field.set(this, date.toInstant());
//                }
//                break;
//                case "LocalTime": {
//                    if (!StringUtils.isEmpty(map.getString(field.getName()))) {
//                        LocalTime time = LocalTime.parse(map.getString(field.getName()));
//                        field.set(this, time);
//                    } else
//                        field.set(this, null);
//                }
//                break;
//                case "LocalDate": {
//                    if (!StringUtils.isEmpty(map.getString(field.getName()))) {
//                        LocalDate time = LocalDate.parse(map.getString(field.getName()), ServerUtils.dateFormat);
//                        field.set(this, time);
//                    } else
//                        field.set(this, null);
//                }
//                break;
                    default: {
                        addString(entry.getKey(), "" + entry.getValue());
                    }
                }
        }
    }
}