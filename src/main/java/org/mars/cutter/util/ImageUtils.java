package org.mars.cutter.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {

  public final static ColorModel COLOR_MODEL_RGBA = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
  public final static ColorModel COLOR_MODEL_RGB = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 0 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
  public final static ColorModel COLOR_MODEL_GRAY = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 8 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
  public final static ColorModel COLOR_MODEL_ALPHA = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 0, 0, 0, 8 }, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

  private static ImageReaderSpi findImageReader(Object input) {
    try {
      ImageInputStream stream = ImageIO.createImageInputStream(input);

      Iterator<ImageReaderSpi> iter;
      try {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        iter = iioRegistry.getServiceProviders(ImageReaderSpi.class, true);
        while (iter.hasNext()) {
          ImageReaderSpi readerSpi = iter.next();
          if (readerSpi.canDecodeInput(stream)) {
            return readerSpi;
          }
        }
      }
      catch (IllegalArgumentException e) {
        // no reader then
      }

      stream.close();
    }
    catch (IOException e) {
      // no reader then
    }

    return null;
  }

  // ==============================================================================
  // ==============================================================================

  public static ColorModel findLegacyColorModel(boolean inColor, boolean hasAlpha) {
    ColorModel colorModel;

    if (inColor) {
      if (hasAlpha) {
        colorModel = COLOR_MODEL_RGBA;
      }
      else {
        colorModel = COLOR_MODEL_RGB;
      }
    }
    else {
      if (hasAlpha) {
        colorModel = COLOR_MODEL_ALPHA;
      }
      else {
        colorModel = COLOR_MODEL_GRAY;
      }
    }

    return colorModel;
  }

  public static ColorModel findLegacyColorModel(BufferedImage image) {
    ColorModel sourceColorModel = image.getColorModel();
    boolean isInColor = (sourceColorModel.getColorSpace().getType() != ColorSpace.TYPE_GRAY);
    boolean hasAlpha = sourceColorModel.hasAlpha();
    return findLegacyColorModel(isInColor, hasAlpha);
  }

  public static BufferedImage toBufferedImage(Image source, Component obs) {
    BufferedImage dest = null;

    if (source != null) {
      if (obs != null) {
        MediaTracker mediaTracker = new MediaTracker(obs);
        mediaTracker.addImage(source, 0);
        try {
          mediaTracker.waitForID(0);
        }
        catch (InterruptedException ie) {
          log.error("Cannot track image load.", ie);
        }
      }
      int imgWidth = source.getWidth(obs);
      int imgHeight = source.getHeight(obs);
      dest = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics2D = dest.createGraphics();
      graphics2D.drawImage(source, 0, 0, imgWidth, imgHeight, null);
    }

    return dest;
  }

  public static BufferedImage createBufferedImage(ColorModel colorModel, Dimension dim) {
    return createBufferedImage(colorModel, dim.width, dim.height);
  }

  public static BufferedImage createBufferedImage(ColorModel colorModel, int width, int height) {
    // Won't work between Indexed and RGB for example
    // int numComponents = colorModel.getNumComponents();
    // WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, numComponents, null);

    WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height);
    BufferedImage bufferedImage = new BufferedImage(colorModel, raster, false, new Hashtable<>());
    return bufferedImage;
  }

  public static ByteBuffer getByteBuffer(BufferedImage bufferedImage) {
    Raster raster = bufferedImage.getRaster();
    DataBufferByte dataBufferByte = (DataBufferByte) raster.getDataBuffer();
    byte[] data = dataBufferByte.getData();

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length);
    byteBuffer.order(ByteOrder.nativeOrder());
    byteBuffer.put(data, 0, data.length);
    byteBuffer.rewind();

    return byteBuffer;
  }

  // ==============================================================================
  // ==============================================================================

  /**
   * Scales the image, keeping aspect ratio, to the specified maximum height/width IF the size exceeds these dimensions
   */
  public static BufferedImage scaleBigger(Image source, int xMax, int yMax, ImageObserver obs) {
    return resize(source, xMax, yMax, false, true, obs);
  }

  /**
   * Scales the image, keeping aspect ratio, up to the specified maximum height/width bounds
   */
  public static BufferedImage scale(Image source, int xMax, int yMax, ImageObserver obs) {
    return resize(source, xMax, yMax, true, true, obs);
  }

  /**
   * Scales an image according to ratios
   */
  public static BufferedImage scale(Image source, float scaleX, float scaleY, ImageObserver obs) {
    return resize(source, (int) (source.getWidth(obs) * scaleX), (int) (source.getHeight(obs) * scaleY), true, false, obs);
  }

  /**
   * Scales an image according to a ratio (the result is thus aspect-ratio compliant)
   */
  public static BufferedImage scale(Image source, float ratio, ImageObserver obs) {
    return scale(source, ratio, ratio, obs);
  }

  /**
   * Resizes the image to the specified height/width
   */
  public static BufferedImage resize(Image source, int w, int h, ImageObserver obs) {
    return resize(source, w, h, true, false, obs);
  }

  public static BufferedImage resize(Image source, int maxW, int maxH, boolean maximizeSize, boolean aspectRatio, ImageObserver obs) {
    return resize(source, maxW, maxH, maximizeSize, aspectRatio, RenderingHints.VALUE_INTERPOLATION_BILINEAR, RenderingHints.VALUE_ANTIALIAS_ON, null, obs);
  }

  public static BufferedImage resize(Image source, int maxW, int maxH, boolean maximizeSize, boolean aspectRatio, Object interpolationType, Object antialiasType) {
    return resize(source, maxW, maxH, maximizeSize, aspectRatio, interpolationType, null);
  }

  public static BufferedImage resize(Image source, int maxW, int maxH, boolean maximizeSize, boolean aspectRatio, Object interpolationType, Object antialiasType, ColorModel colorModel, ImageObserver obs) {
    int width = source.getWidth(obs);
    int height = source.getHeight(obs);

    if (maximizeSize || width > maxW || height > maxH) {
      Dimension sourceDim = getMaximizedSize(width, height, maxW, maxH, aspectRatio);
      width = sourceDim.width;
      height = sourceDim.height;
    }

    BufferedImage bufDest;
    if (colorModel == null) {
      if (source instanceof BufferedImage) // trying to use the source CM
      {
        BufferedImage bufSource = (BufferedImage) source;
        colorModel = bufSource.getColorModel();
        bufDest = createBufferedImage(colorModel, width, height);
      }
      else { // default
        bufDest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      }
    }
    else {
      bufDest = createBufferedImage(colorModel, width, height);
    }
    Graphics2D graphics2D = bufDest.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationType);
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasType);
    if(graphics2D.drawImage(source, 0, 0, width, height, obs) && obs != null) { // the source as buffered image
      obs.imageUpdate(bufDest, ImageObserver.ALLBITS, 0, 0, width, height);
    }

    return bufDest;
  }

  public static Dimension getMaximizedSize(int sourceW, int sourceH, int destW, int destH, boolean aspectRatio) {
    float radiusX = (float) sourceW / (float) destW;
    float radiusY = (float) sourceH / (float) destH;

    if (aspectRatio) {
      radiusX = radiusY = Math.max(radiusX, radiusY);
    }

    sourceW = Math.round(sourceW / radiusX);
    sourceH = Math.round(sourceH / radiusY);

    return new Dimension(sourceW, sourceH);
  }


  // ==============================================================================
  // ==============================================================================

  public static void drawImage(Graphics g, int gWidth, int gHeight, Image image, Color backColor, boolean aspectRatio, ImageObserver obs) {
    drawImage(g, gWidth, gHeight, image, backColor, 0, null, aspectRatio, obs);
  }

  public static void drawImage(Graphics g, int gWidth, int gHeight, Image image, Color backColor, int borderWidth, Color borderColor, boolean aspectRatio, ImageObserver obs) {
    if (g != null) {
      g.setColor(backColor);
      g.fillRect(0, 0, gWidth, gHeight);

      g.setColor(borderColor);
      for (int i = 0; i < borderWidth; i++) {
        g.drawRect(i, i, gWidth - 1 - 2 * i, gHeight - 1 - 2 * i); // no need to check < 0
      }

      if (image != null) {
        int iWidth = image.getWidth(obs);
        int iHeight = image.getHeight(obs);
        Dimension destDim = getMaximizedSize(iWidth, iHeight, gWidth, gHeight, aspectRatio);
        g.drawImage(image, borderWidth, borderWidth, Math.max(destDim.width - borderWidth, 0), Math.max(destDim.height - borderWidth, 0), 0, 0, iWidth, iHeight, obs);
      }
    }
  }
}
