/**
 * Isaiah's Configuration File Format
 * Now using SnakeYAML for robust YAML parsing
 * 
 * Unlicense
 */
package me.isaiah.multiworld.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

public class FileConfiguration extends Configuration {

    private File file;
    private List<String> originalLines; // Store original lines to preserve comments/formatting

    public FileConfiguration(LinkedHashMap<String, Object> contentMap) {
        super(contentMap);
    }
    
    public FileConfiguration() {
        
    }

    /**
     * Creates a new FileConfiguration from a File
     */
    public FileConfiguration(File f) throws IOException {
        this.file = f;
        this.loadFile(f);
    }
    
    public void loadFile(File f) throws IOException {
        this.file = f;
        this.contentMap = new LinkedHashMap<>();
        this.originalLines = new ArrayList<>();

        if (!(f.isFile() && f.exists())) {
            return;
        }

        // Store original lines for comment preservation
        this.originalLines = Files.readAllLines(f.toPath());
        
        // Use SnakeYAML for parsing
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions), new Representer(new DumperOptions()), new DumperOptions(), loaderOptions);
        
        try (FileInputStream fis = new FileInputStream(f)) {
            Map<String, Object> data = yaml.load(fis);
            if (data != null) {
                this.contentMap = flattenMap("", data, new LinkedHashMap<>());
            }
        }
    }

    /**
     * Flatten nested map structure to match the original dot-notation format
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> flattenMap(String prefix, Map<String, Object> map, LinkedHashMap<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, result);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Unflatten dot-notation map back to nested structure for SnakeYAML
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> unflattenMap(LinkedHashMap<String, Object> flatMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip comment keys (used by original implementation)
            if (key.startsWith("#")) {
                continue;
            }
            
            String[] parts = key.split("\\.");
            Map<String, Object> current = result;
            
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new LinkedHashMap<String, Object>());
            }
            
            current.put(parts[parts.length - 1], value);
        }
        
        return result;
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
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(4);
        
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()), new Representer(options), options, new LoaderOptions());
        
        Map<String, Object> nestedData = unflattenMap(this.contentMap);
        
        try (FileWriter writer = new FileWriter(to)) {
            yaml.dump(nestedData, writer);
        }
    }

    /**
     * Helper method for backwards compatibility
     */
    public String repeat(String str, int n) {
        return str.repeat(n);
    }

}