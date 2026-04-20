package calculator;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigManager.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar config.properties, usando valores por defecto.");
        }
    }

    public static Color getColor(String key, String defaultHex) {
        String hex = props.getProperty(key, defaultHex);
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.decode(defaultHex);
        }
    }

    public static Font getFont(String key, int style, int size, String defaultFamily) {
        String family = props.getProperty(key, defaultFamily);
        return new Font(family, style, size);
    }
}