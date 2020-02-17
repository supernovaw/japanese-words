package gui;

import java.awt.*;
import java.util.ArrayList;

public class AnimatingElement {
	private static ArrayList<AnimatingElement> animatingElements = new ArrayList<>();
	private static int animationsFramerate;

	static {
		start();
	}

	private final Runnable repaint;
	private boolean active;

	public AnimatingElement(Runnable repaint) {
		this.repaint = repaint;
		animatingElements.add(this);
	}

	public AnimatingElement(Element e) {
		this(e::repaint);
	}

	private static void start() {
		animationsFramerate = getAvailableFramerate();
		Thread loopThread = new Thread(() -> {
			int delay = 1000 / animationsFramerate;
			while (true) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (AnimatingElement element : animatingElements) {
					if (element.active) element.repaint.run();
				}
			}
		});
		loopThread.start();
	}

	private static int getAvailableFramerate() { // gets maximum available refresh rate of monitor(s)
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		int maxFPS = -1;
		for (GraphicsDevice gd : gds) {
			int FPS = gd.getDisplayMode().getRefreshRate();
			if (FPS == DisplayMode.REFRESH_RATE_UNKNOWN)
				continue;
			if (maxFPS < FPS)
				maxFPS = FPS;
		}
		if (maxFPS == -1)
			maxFPS = 60; // in case unknown, set to 60
		return maxFPS;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
