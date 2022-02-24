package uz.uzcard.genesis.config.properties;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

@Component
public class ProjectConfigImpl implements ProjectConfig {

    private final Properties configProp = new Properties();

    private ProjectConfigImpl() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            configProp.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LazyHolder {
        private static final ProjectConfigImpl INSTANCE = new ProjectConfigImpl();
    }

    public static ProjectConfigImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getProperty(String key) {
        return configProp.getProperty(key);
    }

    public boolean containsKey(String key) {
        return configProp.containsKey(key);
    }

    public void setProperty(String key, String value) {
        configProp.setProperty(key, value);
    }

    public void flush() throws IOException {
        File file = new File("src/main/resources/config.properties");
        if (file.exists()) {
            System.out.println("Hi");
        }

        try (final OutputStream outputStream = new FileOutputStream(file);) {
            configProp.store(outputStream, "File Updated!");
        }
    }

    @Override
    public boolean orderAttachmentRequired() {
        return Boolean.parseBoolean(configProp.getProperty("order.attachment.required"));
    }

    @Override
    public boolean departmentWarehouseRequired() {
        return Boolean.parseBoolean(configProp.getProperty("department.warehouse.required"));
    }

    @Override
    public void setValue(String key, String value) {
        boolean containsKey = getInstance().containsKey(key);
        if (containsKey) {
            getInstance().setProperty(key, value);
        }
        try {
            getInstance().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}