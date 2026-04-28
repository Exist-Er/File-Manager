package model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javax.swing.ImageIcon;

public class FolderItem extends FileItem {

  private final Consumer<Path> onNavigate;

  public FolderItem(Path path, long size, LocalDateTime lastModified, Consumer<Path> onNavigate) {
    super(path, size, lastModified);
    this.onNavigate = onNavigate;
  }

  @Override
  public void open() {
    if (onNavigate != null) {
      onNavigate.accept(getPath());
    }
  }

  @Override
  public ImageIcon getIcon() {
    return IconRegistry.getIcon("folder.png");
  }

  @Override
  public String getTypeLabel() {
    return "Folder";
  }
}
