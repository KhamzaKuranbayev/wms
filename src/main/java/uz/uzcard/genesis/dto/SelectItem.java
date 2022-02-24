package uz.uzcard.genesis.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Virus on 27-Aug-16.
 */
public class SelectItem extends AbstractMap implements Serializable {

    public static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String SELECTED = "selected";
    private static final String DESCRIPTION = "description";
    private static final String GROUP = "GROUP";
    @JsonIgnore
    private final HashMap<String, String> map = new HashMap<>();
    private HashMap<String, List<String>> map2 = new HashMap<>();
    private Long id;

    public SelectItem() {
    }

    public SelectItem(String name, String value) {
        this(name);
        setValue(value);
    }

    public SelectItem(String name, String value, boolean selected) {
        this(name, value);
        setSelected(selected);
    }

    public SelectItem(Long id, String name) {
        setId(id);
        setName(name);
    }

    public SelectItem(Long id, String name, String value) {
        this(id, name);
        setValue(value);
    }

    public SelectItem(Long id, String name, String description, String value) {
        this(id, name, value);
        setDescription(description);
    }

    public SelectItem(Long id, String name, String description, String value, String group) {
        this(id, name, description, value);
        setGroup(group);
    }

    public SelectItem(String name) {
        setName(name);
    }

    public SelectItem(String name, String value, String description) {
        this(name, value);
        setDescription(description);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        addString(NAME, name);
    }

    public String getValue() {
        return getString(VALUE);
    }

    public void setValue(String value) {
        addString(VALUE, value);
    }

    public boolean isSelected() {
        return getBool(SELECTED);
    }

    public void setSelected(boolean selected) {
        addBool(SELECTED, selected);
    }

    public String getDescription() {
        return getString(DESCRIPTION);
    }

    public void setDescription(String description) {
        addString(DESCRIPTION, description);
    }

    @JsonIgnore
    public HashMap<String, String> getInstance() {
        return map;
    }

    @JsonIgnore
    public HashMap<String, List<String>> getInstance2() {
        return map2;
    }

    public String getGroup() {
        return getString(GROUP);
    }

    public void setGroup(String group) {
        addString(GROUP, group);
    }

    public void setMap(HashMap<String, List<String>> map) {
        this.map2 = map;
    }

    @JsonIgnore
    @Override
    public boolean isDeleted() {
        return super.isDeleted();
    }
}