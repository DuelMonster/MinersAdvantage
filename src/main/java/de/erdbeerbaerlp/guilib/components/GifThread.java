package de.erdbeerbaerlp.guilib.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.icafe4j.image.gif.GIFFrame;
import com.icafe4j.image.reader.GIFReader;

import de.erdbeerbaerlp.guilib.util.ImageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GifThread extends Thread {
	private final ArrayList<Map.Entry<byte[], Integer>> gifData = new ArrayList<>();
	private final DynamicTexture                        outputTexture;
	private final boolean                               doGifLoop;
	
	public GifThread(final ByteArrayInputStream is, final DynamicTexture outputTexture, boolean resizingImage) {
		this(is, outputTexture, true, true, resizingImage);
	}
	
	public GifThread(final ByteArrayInputStream is, final DynamicTexture outputTexture, boolean keepAspectRatio, boolean doGifLoop, boolean resizingImage) {
		this.outputTexture = outputTexture;
		this.doGifLoop     = doGifLoop;
		setDaemon(true);
		setName("Gif Renderer " + UUID.randomUUID().toString());
		List<GIFFrame>  gifFrames;
		final GIFReader r = new GIFReader();
		try {
			is.reset();
			r.read(is);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		gifFrames = r.getGIFFrames();
		try {
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		for (final GIFFrame frame : gifFrames) {
			try {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				if (frame.getFrame().getWidth() <= outputTexture.getTextureData().getWidth() && frame.getFrame().getHeight() <= outputTexture.getTextureData().getHeight() && !resizingImage)
					ImageIO.write(frame.getFrame(), "png", os);
				else {
					if (keepAspectRatio)
						ImageIO.write(ImageUtil.scaleImageKeepAspectRatio(frame.getFrame(), outputTexture.getTextureData().getWidth(), outputTexture.getTextureData().getHeight()), "png", os);
					else
						ImageIO.write(ImageUtil.scaleImage(frame.getFrame(), outputTexture.getTextureData().getWidth(), outputTexture.getTextureData().getHeight()), "png", os);
				}
				gifData.add(new AbstractMap.SimpleEntry<>(os.toByteArray(), frame.getDelay()));
				os.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean paused = false;
	
	public void pause() {
		this.paused = true;
	}
	
	public void play() {
		this.paused = false;
	}
	
	@Override
	public void run() {
		// noinspection LoopConditionNotUpdatedInsideLoop
		do {
			final ArrayList<Map.Entry<NativeImage, Integer>> gifBuffer = new ArrayList<>();
			// Load all frames into NativeImages
			for (final Map.Entry<byte[], Integer> frame : gifData) {
				try {
					final ByteArrayInputStream is2 = new ByteArrayInputStream(frame.getKey());
					NativeImage                img;
					img = NativeImage.read(is2);
					gifBuffer.add(new AbstractMap.SimpleEntry<>(img, frame.getValue()));
					is2.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Render all frames
			for (final Map.Entry<NativeImage, Integer> frame : gifBuffer) {
				try {
					Minecraft.getInstance().execute(() -> {
						try {
							outputTexture.setTextureData(frame.getKey());
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						outputTexture.updateDynamicTexture();
					});
					sleep(frame.getValue() * 10);
					while (paused)
						sleep(1);
				}
				catch (InterruptedException ignored) {
					return;
				}
				catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
		while (doGifLoop);
	}
	
	public boolean isPaused() {
		return paused;
	}
	
}
