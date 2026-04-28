package model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

public class TextFile extends FileItem {

  public TextFile(Path path, long size, LocalDateTime lastModified) {
    super(path, size, lastModified);
  }

  @Override
  public void open() {
    try {
      java.awt.Desktop.getDesktop().open(getPath().toFile());
    } catch (java.io.IOException e) {
      System.err.println("Could not open text file: " + e.getMessage());
    }
  }

  @Override
  public ImageIcon getIcon() {
    return IconRegistry.getIcon("text.png");
  }

  @Override
  public String getTypeLabel() {
    return "Text";
  }
}
