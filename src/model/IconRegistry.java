package model;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * IconRegistry - Centralized management of application icons. Ensures icons are loaded only once
 * and provides a default icon if a resource is missing.
 */
public class IconRegistry {

  private static final Map<String, ImageIcon> iconCache = new HashMap<>();
  private static final String ICON_PATH_PREFIX = "/icons/";

  public static ImageIcon getIcon(String name) {
    if (iconCache.containsKey(name)) {
      return iconCache.get(name);
    }

    String fullPath = ICON_PATH_PREFIX + name;
    URL imgUrl = IconRegistry.class.getResource(fullPath);

    ImageIcon icon;
    if (imgUrl != null) {
      icon = new ImageIcon(imgUrl);
    } else {
      System.err.println("Warning: Icon not found at " + fullPath);
      URL defaultUrl = IconRegistry.class.getResource(ICON_PATH_PREFIX + "file.png");
      if (defaultUrl != null) {
        icon = new ImageIcon(defaultUrl);
      } else {
        icon = new ImageIcon();
      }
    }

    if (icon.getImage() != null) {
      Image scaled = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
      icon = new ImageIcon(scaled);
    }

    iconCache.put(name, icon);
    return icon;
  }
}
