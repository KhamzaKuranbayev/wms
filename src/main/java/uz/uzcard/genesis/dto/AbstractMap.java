package uz.uzcard.genesis.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Virus on 03-Sep-16.
 */
public abstract class AbstractMap<T extends Serializable> {

    public static final String DELETED = "deleted";

    @JsonIgnore
    public abstract HashMap<String, String> getInstance();

    public abstract HashMap<String, List<String>> getInstance2();

    public void addString(String typeID, String entityID) {
        getInstance().put(typeID, entityID);
    }

    public void addStrings(String typeID, List<String> entityID) {
        getInstance2().put(typeID, entityID);
    }

    public String getString(String type) {
        String str = getInstance().get(type);
        return str == null || "null".equals(str) ? null : str;
    }

    public List<String> getStrings(String type) {
        List<String> str = getInstance2().get(type);
        return str != null && str.size() > 0 && "null".equals(str.get(0)) ? null : str;
    }

    public List<Long> getLongs(String type) {
        List<String> str = getInstance2().get(type);
        if (str == null) return null;
        if (str.isEmpty()) return new ArrayList<>();
        if ("null".equals(str.get(0))) return null;
        return str.stream().filter(s -> s != null && s.trim().length() > 0 && !"null".equals(s.trim())).map(x -> Long.parseLong(x)).collect(Collectors.toList());
    }

    protected void addInteger(String typeID, Integer entity) {
        if (entity == null) {
            addString(typeID, null);
        } else {
            addString(typeID, String.valueOf(entity));
        }
    }

    public Integer getInteger(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        return Integer.valueOf(getString(type));
    }

    protected void addInt(String typeID, int entity) {
        addInteger(typeID, entity);
    }

    public int getInt(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return 0;
        }
        return getInteger(type) == null ? 0 : getInteger(type);
    }

    protected void addLong(String typeID, Long entity) {
        if (entity == null) {
            addString(typeID, null);
        } else {
            addString(typeID, String.valueOf(entity));
        }
    }

    protected void addLongs(String typeID, List<Long> entities) {
        if (entities == null) {
            addStrings(typeID, null);
        } else {
            List<String> values = entities.stream().map(aLong -> String.valueOf(aLong)).collect(Collectors.toList());
            addStrings(typeID, values);
        }
    }

    public Long getLong(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        return Long.valueOf(getString(type));
    }

    public Long getMwiLong(String type, int digits) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        return ((Double) (getDouble(type) * Math.pow(10, digits))).longValue();
    }

    public Integer getMwiInteger(String type, int digits) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        return ((Double) (getDouble(type) * Math.pow(10, digits))).intValue();
    }

    public void addDouble(String typeID, Double entity) {
        if (entity == null) {
            addString(typeID, null);
        } else {
            addString(typeID, String.valueOf(entity));
        }
    }

    public Double getDouble(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        try {
            return Double.valueOf(getString(type).replace(",", "."));
        } catch (Exception e) {
            return Double.valueOf(getString(type).replace(".", ","));
        }
    }

    public Double getMwiDouble(String type, int digits) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        try {
            return Double.valueOf(getString(type).replace(",", ".")) / Math.pow(10, digits);
        } catch (Exception e) {
            return Double.valueOf(getString(type).replace(".", ",")) / Math.pow(10, digits);
        }
    }

    public double get_double(String typeID) {
        return getDouble(typeID) == null ? 0 : getDouble(typeID);
    }

    protected void addBigDecimal(String typeID, BigDecimal entity) {
        if (entity == null) {
            addString(typeID, null);
        } else {
            addString(typeID, String.valueOf(entity));
        }
    }

    protected BigDecimal getBigDecimal(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }

        return new BigDecimal(getString(type));
    }

    protected void add_long(String typeID, long entity) {
        addLong(typeID, entity);
    }

    public long get_long(String typeID) {
        return getLong(typeID) == null ? 0 : getLong(typeID);
    }

    public void addDate(String typeID, Date entity, boolean withOffset) {
        if (withOffset) {
            addDate(typeID, entity);
        } else {
            addString(typeID, JsonDateUtils.setNonConvertableDate(entity));
        }
    }

    public void addDate(String typeID, Date entity) {
        if (entity == null) {
            addString(typeID, null);
        } else
            addString(typeID, JsonDateUtils.setDate(entity));
    }

    public Date getDate(String type) {
        return JsonDateUtils.getDate(getString(type));
    }

    public Date getNonConvertibleDate(String type) {
        return JsonDateUtils.getNonConvertableDate(getString(type));
    }

    protected void addBoolean(String typeID, Boolean entity) {
        if (entity == null) {
            addString(typeID, null);
        } else {
            addString(typeID, String.valueOf(entity));
        }
    }

    public Boolean getBoolean(String type) {
        if (getString(type) == null || getString(type).trim().length() < 1) {
            return null;
        }
        return Boolean.valueOf(getString(type));
    }

    public boolean getBool(String typeID) {
        return Boolean.TRUE.equals(getBoolean(typeID));
    }

    public void addBool(String typeID, boolean entity) {
        addBoolean(typeID, entity);
    }

    public AbstractMap put(String name, String value) {
        getInstance().put(name, value);
        return this;
    }

    public AbstractMap put(String name, List<String> value) {
        getInstance2().put(name, value);
        return this;
    }

    public boolean isDeleted() {
        return getBool(DELETED);
    }

    public void setDeleted(boolean id) {
        addBoolean(DELETED, id);
    }

    public boolean has(String key) {
        return containts(key) && (getString(key) != null && getString(key).trim().length() > 0 || getStrings(key) != null
                && ((Stream) getStrings(key).stream()).filter(s -> s != null && s.toString().trim().length() > 0 && !"null".equals(s.toString().trim())).count() > 0);
    }

    public boolean containts(String key) {
        return getInstance().containsKey(key) || (getInstance2().containsKey(key) && getInstance2().get(key) != null && !getInstance2().get(key).isEmpty());
    }

    public String getSearchQuery(String key) {
        if (containts(key))
            return (getString(key).trim() + " ").replaceAll("'", "''")
                    .replaceAll(" ", "%")
                    .toLowerCase();
        return "%";
    }

    public void remove(String typeID) {
        getInstance().remove(typeID);
        getInstance2().remove(typeID);
    }
}