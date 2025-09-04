package me.isaiah.multiworld.test;

import me.isaiah.multiworld.config.FileConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Simple test to verify SnakeYAML integration works correctly
 */
public class ConfigTest {
    
    public static void runTest() {
        try {
            System.out.println("Starting FileConfiguration test...");
            
            // Create a test configuration
            FileConfiguration config = new FileConfiguration();
            
            // Test setting various types of values
            config.set("test.string", "hello world");
            config.set("test.number", 42);
            config.set("test.boolean", true);
            config.set("test.double", 3.14);
            config.set("portals.myportal.owner", "testuser");
            config.set("portals.myportal.destination", "myworld");
            config.set("portals.myportal.location", "0,64,0:10,74,10");
            
            // Test list support
            config.set("test.list", Arrays.asList("item1", "item2", "item3"));
            
            // Save to file
            File testFile = new File("/tmp/test_config.yml");
            config.save(testFile);
            
            System.out.println("Configuration saved successfully to: " + testFile.getPath());
            
            // Load the configuration back
            FileConfiguration loadedConfig = new FileConfiguration(testFile);
            
            // Test reading values
            System.out.println("String value: " + loadedConfig.getString("test.string"));
            System.out.println("Number value: " + loadedConfig.getInt("test.number"));
            System.out.println("Boolean value: " + loadedConfig.getBoolean("test.boolean"));
            System.out.println("Double value: " + loadedConfig.getDouble("test.double"));
            System.out.println("Portal owner: " + loadedConfig.getString("portals.myportal.owner"));
            
            // Test section functionality
            System.out.println("Has portals section: " + loadedConfig.hasSection("portals"));
            System.out.println("Portal section keys: " + loadedConfig.getSection("portals").keySet());
            
            System.out.println("\nFileConfiguration test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("FileConfiguration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}