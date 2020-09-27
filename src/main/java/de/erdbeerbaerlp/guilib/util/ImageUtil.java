package de.erdbeerbaerlp.guilib.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.imgscalr.Scalr;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageUtil {
	/**
	 * This method resizes the image and returns the BufferedImage object that can be drawn
	 */
	public static BufferedImage scaleImage(final BufferedImage img, int width, int height) {
		int w = img.getWidth();
		int h = img.getHeight();
		if (w == width && h == height) return img;
		return Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
	}
	
	/**
	 * This method resizes the image and returns the BufferedImage object that can be drawn<br>
	 * This method keeps aspect ratio
	 */
	public static BufferedImage scaleImageKeepAspectRatio(final BufferedImage img, int width, int height) {
		int w = img.getWidth();
		int h = img.getHeight();
		if (w == width && h == height) return img;
		final Dimension scaled    = getScaledDimension(new Dimension(w, h), new Dimension(width, height));
		final int       newWidth  = (int) scaled.getWidth();
		final int       newHeight = (int) scaled.getHeight();
		return Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, newWidth, newHeight);
	}
	
	private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
		int original_width  = imgSize.width;
		int original_height = imgSize.height;
		int bound_width     = boundary.width;
		int bound_height    = boundary.height;
		int new_width       = original_width;
		int new_height      = original_height;
		if (original_width > bound_width) {
			new_width  = bound_width;
			new_height = (new_width * original_height) / original_width;
		}
		if (new_height > bound_height) {
			new_height = bound_height;
			new_width  = (new_height * original_width) / original_height;
		}
		
		return new Dimension(new_width, new_height);
	}
	
	private static byte[] toByteArray(final InputStream is) throws IOException {
		final ByteArrayOutputStream os     = new ByteArrayOutputStream();
		byte[]                      buffer = new byte[1024];
		int                         len;
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		final byte[] out = os.toByteArray();
		os.close();
		is.close();
		return out;
	}
	
	public static ByteArrayInputStream getInputStreamFromImageURL(String url) throws IOException {
		final HttpURLConnection httpcon = (HttpURLConnection) new URL(url).openConnection();
		httpcon.addRequestProperty("User-Agent", "Minecraft");
		final ByteArrayInputStream is = convertToByteArrayIS(httpcon.getInputStream());
		httpcon.disconnect();
		return is;
	}
	
	public static ByteArrayInputStream convertToByteArrayIS(InputStream is) throws IOException {
		return new ByteArrayInputStream(toByteArray(is));
	}
	
	public static NativeImage getImageFromIS(final ByteArrayInputStream is, boolean keepAspectRatio, int width, int height, boolean resizingImage) throws IOException {
		final BufferedImage img = ImageIO.read(is);
		is.reset();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (img.getWidth() <= width && img.getHeight() <= height && !resizingImage)
			ImageIO.write(img, "png", os);
		else {
			if (keepAspectRatio)
				ImageIO.write(ImageUtil.scaleImageKeepAspectRatio(img, width, height), "png", os);
			else
				ImageIO.write(ImageUtil.scaleImage(img, width, height), "png", os);
		}
		final ByteArrayInputStream is2  = new ByteArrayInputStream(os.toByteArray());
		final NativeImage          imgo = NativeImage.read(is2);
		is2.close();
		is.reset();
		return imgo;
	}
	
	public static boolean isISGif(final ByteArrayInputStream is) throws IOException {
		final ImageInputStream      iis          = ImageIO.createImageInputStream(is);
		final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
		while (imageReaders.hasNext()) {
			final ImageReader reader = imageReaders.next();
			System.out.println("formatName: " + reader.getFormatName());
			if (reader.getFormatName().endsWith("gif")) {
				iis.close();
				reader.dispose();
				return true;
			}
			reader.dispose();
		}
		iis.close();
		is.reset();
		return false;
	}
}
