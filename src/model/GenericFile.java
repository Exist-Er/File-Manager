package model;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

public class GenericFile extends FileItem {

  public GenericFile(Path path, long size, LocalDateTime lastModified) {
    super(path, size, lastModified);
  }

  @Override
  public void open() {
    try {
      java.awt.Desktop.getDesktop().open(getPath().toFile());
    } catch (java.io.IOException e) {
      System.err.println("Could not open file: " + e.getMessage());
    }
  }

  @Override
  public ImageIcon getIcon() {
    return IconRegistry.getIcon("file.png");
  }

  @Override
  public String getTypeLabel() {
    return "File";
  }
}
