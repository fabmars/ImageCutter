package org.mars.cutter.util;

import java.io.File;
import java.nio.file.Path;
import lombok.NonNull;

public class FileUtils {

  public static String getExt(@NonNull String fileName) {
    String ext = null;
    int dot = fileName.lastIndexOf('.');
    if (dot >= 0) {
      ext = fileName.substring(dot + 1);
    }
    return ext;
  }

  public static String getExt(Path file) {
    return getExt(file.getFileName().toString());
  }

  public static String[] getFileExt(String fileNameExt) {
    String[] result = null;

    int dotpos = fileNameExt.lastIndexOf('.');
    if (dotpos >= 0) {
      result = new String[] { fileNameExt.substring(0, dotpos), fileNameExt.substring(dotpos + 1) };
    }
    else {
      result = new String[] { fileNameExt, null };
    }

    return result;
  }

  public static String[] getFileExt(File file) {
    String[] result = null;
    if (file != null) {
      String fileName = file.getName();
      result = getFileExt(fileName);
    }
    return result;
  }

  public static String[] getFileExt(Path file) {
    String[] result = null;
    if (file != null) {
      String fileName = file.getFileName().toString();
      result = getFileExt(fileName);
    }
    return result;
  }

  public static File renameSameExt(String oldName, String newParent, String newBaseName) {
    return new File(newParent, renameSameExt(oldName, newBaseName));
  }

  public static File renameSameExt(File file, File newParent, String newBaseName) {
    return new File(newParent, renameSameExt(file.getName(), newBaseName));
  }

  public static Path renameSameExt(Path file, Path newParent, String newBaseName) {
    Path newFileName = renameSameExt(file.getFileName(), newBaseName);
    if(newParent == null) {
      return newFileName;
    }
    else {
      return newParent.resolve(newFileName);
    }
  }

  public static File renameSameExt(File file, String newBaseName) {
    return renameSameExt(file, file.getParentFile(), newBaseName);
  }

  public static Path renameSameExt(Path file, String newBaseName) {
    String newFileName = renameSameExt(file.getFileName().toString(), newBaseName);
    return file.resolveSibling(newFileName);
  }

  public static String renameSameExt(String oldName, String newBaseName) {
    if(oldName == null) {
      throw new NullPointerException("oldName is null");
    }

    String ext = getExt(oldName);
    if(ext != null) {
      newBaseName += '.' + ext;
    }

    return newBaseName;
  }

  public static File renameChangeExt(String oldName, String newParent, String newExt) {
    return new File(newParent, renameChangeExt(oldName, newExt));
  }

  public static File renameChangeExt(File file, File newParent, String newExt) {
    return new File(newParent, renameChangeExt(file.getName(), newExt));
  }

  public static Path renameChangeExt(Path file, Path newParent, String newExt) {
    Path newFile = renameChangeExt(file.getFileName(), newExt); // getFileName(), so there is no more parent here.
    if(newParent == null) {
      return newFile;
    }
    else {
      return newParent.resolve(newFile);
    }
  }

  public static File renameChangeExt(File file, String newExt) {
    return renameChangeExt(file, file.getParentFile(), newExt);
  }

  public static Path renameChangeExt(Path file, String newExt) {
    return file.resolveSibling(renameChangeExt(file.getFileName().toString(), newExt));
  }

  public static String renameChangeExt(String oldName, String newExt) {
    if(oldName == null) {
      throw new NullPointerException("oldName is null");
    }

    String[] fileExt = getFileExt(oldName);

    String newName = fileExt[0];
    if(newExt != null) {
      newName += dotExtension(newExt);
    }
    return newName;
  }

  public static String dotExtension(String ext) {
    if(ext != null && !ext.startsWith(".")) {
      ext = '.' + ext;
    }
    return ext;
  }
}
