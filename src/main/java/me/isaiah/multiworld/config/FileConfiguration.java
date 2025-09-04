/**
 * Configuration file format using SnakeYAML
 * Replaces the custom YAML parser with industry-standard SnakeYAML
 * 
 * Unlicense
 */
package me.isaiah.multiworld.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class FileConfiguration extends Configuration {

    private File file;
    private final Yaml yaml;

    public FileConfiguration(LinkedHashMap<String, Object> contentMap) {
        super(contentMap);
        this.yaml = new Yaml();
    }
    
    public FileConfiguration() {
        this.yaml = new Yaml();
        this.contentMap = new LinkedHashMap<>();
    }

    /**
     * Creates a new {@link FileConfiguration} from a {@link File}
     */
    public FileConfiguration(File f) throws IOException {
        this.yaml = new Yaml();
        this.file = f;
        this.loadFile(f);
    }
    
    public void loadFile(File f) throws IOException {
        this.file = f;
        this.contentMap = new LinkedHashMap<>();

        if (!(f.isFile() && f.exists())) {
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(f)) {
            // Load YAML content using SnakeYAML
            Object yamlData = yaml.load(fis);
            
            if (yamlData instanceof Map) {
                // Convert the loaded YAML to our flat key format
                @SuppressWarnings("unchecked")
                Map<String, Object> yamlMap = (Map<String, Object>) yamlData;
                flattenMap(yamlMap, "", this.contentMap);
            }
        } catch (Exception e) {
            // If YAML parsing fails, initialize with empty map
            this.contentMap = new LinkedHashMap<>();
        }
    }
    
    /**
     * Recursively flatten nested YAML structure into dot-notation keys
     * to maintain compatibility with existing code
     */
    private void flattenMap(Map<String, Object> map, String prefix, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMap(nestedMap, key, result);
            } else {
                result.put(key, value);
            }
        }
    }

    @Override
    public void save() throws IOException {
        save(file);
    }
    
    public static String getTextAfterLastDot(String str) {
        int lastIndex = str.lastIndexOf(".");
        if (lastIndex != -1) {
            return str.substring(lastIndex + 1);
        } else {
            return "";
        }
    }

    @Override
    public void save(File to) throws IOException {
        // Convert flat keys back to nested structure for YAML output
        Map<String, Object> nestedMap = unflattenMap(this.contentMap);
        
        try (FileWriter writer = new FileWriter(to)) {
            yaml.dump(nestedMap, writer);
        }
    }
    
    /**
     * Convert flat dot-notation keys back to nested structure for YAML output
     */
    private Map<String, Object> unflattenMap(Map<String, Object> flatMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip comment keys from the old format
            if (key.startsWith("#")) {
                continue;
            }
            
            String[] keyParts = key.split("\\.");
            Map<String, Object> current = result;
            
            // Navigate/create nested structure
            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                if (!current.containsKey(part)) {
                    current.put(part, new LinkedHashMap<String, Object>());
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) current.get(part);
                current = nested;
            }
            
            // Set the final value
            current.put(keyParts[keyParts.length - 1], value);
        }
        
        return result;
    }

}