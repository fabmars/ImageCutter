package org.mars.cutter.ui;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.mars.cutter.CutterService;

public class Application {


  public static final Path DEFAULT_ROOT = Paths.get("E:\\Photos Scans\\Kléber et Georgette\\A découper");
  private static boolean bypass = false;

  public static void main(String[] args) throws IOException {

    GraphicsDevice screenDevice = ScreenUtils.getDefaultScreenDevice();
    Rectangle screenBounds = ScreenUtils.getBounds(screenDevice);
    int winHeight = (int)(screenBounds.height * 0.8);
    int winWidth = winHeight * 25/30;


    Path rootDir;
    if((rootDir = chooseDir(DEFAULT_ROOT)) != null) {
      CutterService service = new CutterService(rootDir);
      CutterDialog cutterDialog = new CutterDialog(service);
      cutterDialog.setSize(winWidth, winHeight);
      cutterDialog.setLocation((screenBounds.width-winWidth)/2, 0);
      cutterDialog.setVisible(true);
      cutterDialog.skip();
      cutterDialog.revalidate();
    }
  }


  public static Path chooseDir(Path currentDir) {
    if(bypass) {
      return DEFAULT_ROOT;
    } else {
      JFileChooser fc = new JFileChooser(currentDir != null ? currentDir.toFile() : null);
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setMultiSelectionEnabled(false);
      JFrame chooserFrame = new JFrame("Choose root");
      chooserFrame.setUndecorated(true);
      chooserFrame.setVisible(true);

      int result = fc.showOpenDialog(chooserFrame);
      chooserFrame.dispose();
      if (result == JFileChooser.APPROVE_OPTION) {
        return fc.getSelectedFile().toPath();
      } else {
        return null;
      }
    }
  }
}
