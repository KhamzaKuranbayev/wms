package uz.uzcard.genesis.config.properties;

public interface ProjectConfig {

    boolean orderAttachmentRequired();

    boolean departmentWarehouseRequired();

    void setValue(String key, String value);
}