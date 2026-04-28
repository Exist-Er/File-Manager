package model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;

public class EncryptedFile extends FileItem {

  public EncryptedFile(Path path, long size, LocalDateTime lastModified) {
    super(path, size, lastModified);
    setEncrypted(true);
  }

  @Override
  public void open() {

    System.out.println("File is encrypted. Decryption required for: " + getName());
  }

  @Override
  public ImageIcon getIcon() {
    return IconRegistry.getIcon("archive.png");
  }

  @Override
  public String getTypeLabel() {
    return "Encrypted File";
  }
}
