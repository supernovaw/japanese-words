package gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public final class Theme {
	private static Color background = Color.black, foreground = Color.white;
	private static double backgroundDim = 0.25;
	private static BufferedImage bgImage;

	private static Font fontEnglish;
	private static float UIFontsize = 20f;
	private static Font UIFont;

	static {
		try {
			bgImage = ImageIO.read(new File("assets/bg.jpg"));
			fontEnglish = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/SFProText-Light.ttf"));
			UIFont = fontEnglish.deriveFont(UIFontsize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Color getBG() {
		return background;
	}

	public static Color getFG() {
		return foreground;
	}

	public static Color getFG(int withAlpha) {
		int rgb = foreground.getRGB() & 0xffffff;
		return new Color(rgb | (withAlpha << 24), true);
	}

	public static Color getBG(int withAlpha) {
		int rgb = background.getRGB() & 0xffffff;
		return new Color(rgb | (withAlpha << 24), true);
	}

	public static Font getUIFont() {
		return UIFont;
	}

	public static void paintBackground(Graphics2D g, Dimension screen) {
		double scaleX = screen.getWidth() / bgImage.getWidth();
		double scaleY = screen.getHeight() / bgImage.getHeight();
		double scale = Math.max(scaleX, scaleY); // fill entire screen

		// position image at the center
		AffineTransform at = new AffineTransform();
		at.translate(screen.getWidth() / 2, screen.getHeight() / 2);
		at.scale(scale, scale);
		at.translate(bgImage.getWidth() / -2d, bgImage.getHeight() / -2d);

		g.drawImage(bgImage, at, null);

		g.setColor(new Color(background.getRGB() & 0xffffff | ((int) (255 * backgroundDim)) << 24, true));
		g.fillRect(0, 0, screen.width, screen.height); // dim the background
	}
}
