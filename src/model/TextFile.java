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
    // UI layer (PreviewPanel) will call this via polymorphism.
    // For now, stub — your teammate wires the actual panel.
    System.out.println("Opening text viewer for: " + getName());
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
