package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public final class Assets {
	private static final String DIRECTORY = "assets/";

	public static InputStream getStream(String name) throws FileNotFoundException {
		return new FileInputStream(DIRECTORY + name);
	}

	public static byte[] loadStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read;
		byte[] buffer = new byte[16384];
		while ((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);
		in.close();
		return out.toByteArray();
	}

	public static byte[] loadBytes(String name) throws IOException {
		return loadStream(getStream(name));
	}

	public static BufferedImage loadImage(String name) {
		try {
			return ImageIO.read(new ByteArrayInputStream(loadBytes(name)));
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + name);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Font loadFont(String name) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, getStream("fonts/" + name));
		} catch (FontFormatException e) {
			System.err.println("File not found: " + name);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
