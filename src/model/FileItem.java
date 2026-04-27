package model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

public abstract class FileItem {

  private final String name;
  private final Path path;
  private final long size;
  private final LocalDateTime lastModified;
  private boolean isEncrypted;

  public FileItem(Path path, long size, LocalDateTime lastModified) {
    this.name = path.getFileName().toString();
    this.path = path;
    this.size = size;
    this.lastModified = lastModified;
    this.isEncrypted = false;
  }

  public abstract void open();

  public abstract ImageIcon getIcon();

  public abstract String getTypeLabel();

  public String getName() {
    return name;
  }

  public Path getPath() {
    return path;
  }

  public long getSize() {
    return size;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public boolean isEncrypted() {
    return isEncrypted;
  }

  public void setEncrypted(boolean encrypted) {
    isEncrypted = encrypted;
  }

  @Override
  public String toString() {
    return (isEncrypted ? "[LOCKED] " : "") + getTypeLabel() + ": " + name;
  }
}
