package gui;

import main.Assets;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class Theme {
	private static Color background = Color.black, foreground = Color.white;
	private static double backgroundDim = 0.25;
	private static BufferedImage bgImage;

	private static Font fontEnglish;
	private static Font fontJapanese;
	private static float UIFontsize = 20f;
	private static Font UIFont;

	static {
		bgImage = Assets.loadImage("bg_airplane_wing.jpg");
		fontEnglish = Assets.loadFont("SFProText-Light.ttf");
		fontJapanese = Assets.loadFont("YuGothL.ttc");
		UIFont = fontEnglish.deriveFont(UIFontsize);
	}

	public static Color getFG() {
		return foreground;
	}

	public static Color getBG() {
		return background;
	}

	public static Color getFG(int withAlpha) {
		int rgb = foreground.getRGB() & 0xffffff;
		return new Color(rgb | (withAlpha << 24), true);
	}

	public static Color getBG(int withAlpha) {
		int rgb = background.getRGB() & 0xffffff;
		return new Color(rgb | (withAlpha << 24), true);
	}

	public static Color getFG(double withAlpha) {
		return getFG((int) (255 * withAlpha));
	}

	public static Color getBG(double withAlpha) {
		return getBG((int) (255 * withAlpha));
	}

	public static Font getUIFont() {
		return UIFont;
	}

	public static Font getFontJapanese() {
		return fontJapanese;
	}

	public static Font getFontEnglish() {
		return fontEnglish;
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
