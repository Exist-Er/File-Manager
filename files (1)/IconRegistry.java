package model;

import java.awt.Image;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.ImageIcon;

/**
 * IconRegistry - Centralized management of application icons.
 * Ensures icons are loaded only once and provides a default icon
 * if a resource is missing.
 */
public class IconRegistry {

  // FIX #15: Use ConcurrentHashMap instead of plain HashMap.
  // If any background thread ever calls getIcon() (e.g. during file listing),
  // a plain HashMap causes data races. ConcurrentHashMap is safe for concurrent reads
  // and for the rare simultaneous first-load of the same icon name.
  private static final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();
  private static final String ICON_PATH_PREFIX = "/icons/";

  public static ImageIcon getIcon(String name) {
    // computeIfAbsent is atomic: the icon is loaded exactly once even under contention
    return iconCache.computeIfAbsent(name, IconRegistry::loadIcon);
  }

  private static ImageIcon loadIcon(String name) {
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

    return icon;
  }
}
