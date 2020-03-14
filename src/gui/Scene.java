package gui;

import gui.scenes.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class Scene {
	// List of Scenes used in the app
	public static SceneMain sceneMain;
	public static SceneModeWordsSelection sceneModeWordsSelection;

	public static SceneLearningCardIntroduction sceneLearningCardIntroduction;
	public static SceneLearningWordMeaning sceneLearningWordMeaning;
	public static SceneLearningWordReading sceneLearningWordReading;
	public static SceneLearningWordWriting sceneLearningWordWriting;

	private static boolean displayElementsBounds = false; // for development only
	private final Window holder;
	private final ArrayList<Element> elements = new ArrayList<>();

	public Scene(Window holder) {
		this.holder = holder;
	}

	protected final void forEachElements(Consumer<Element> consumer) {
		elements.forEach(consumer);
	}

	// paints elements transformed in case second argument isn't null
	public void paint(Graphics2D g, AffineTransform transform) {
		/* elements are drawn in a separate image to let them use
		 * AlphaCompositing without affecting background and without
		 * necessity for them to use separate BufferedImages for it
		 */
		Dimension size = getSize();
		BufferedImage elementsImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D elementsGraphics = elementsImage.createGraphics();

		// assign RenderingHints of g to elementsGraphics
		elementsGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
		elementsGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				g.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS));

		if (transform != null)
			elementsGraphics.transform(transform);
		else
			elementsGraphics.setClip(g.getClip());

		if (displayElementsBounds) {
			forEachElements(element -> {
				element.paint(elementsGraphics);
				elementsGraphics.setColor(Color.magenta);
				elementsGraphics.drawRect(element.x(), element.y(), element.w() - 1, element.h() - 1);
			});
		} else {
			forEachElements(element -> element.paint(elementsGraphics));
		}
		g.drawImage(elementsImage, 0, 0, null);
	}

	public final Dimension getSize() {
		return holder.getSize();
	}

	protected final void onContainerSizeChange(Dimension newSize) {
		forEachElements(element -> element.onContainerSizeChange(newSize));
	}

	protected final void addElement(Element element) {
		elements.add(element);
	}

	protected final Point getMousePosition() {
		return holder.getMousePosition();
	}

	public final void changeScene(Scene s) {
		holder.changeScene(s);
	}

	protected final void repaint(Rectangle area) {
		holder.repaint(area);
	}

	protected void onDisplay() {
		onContainerSizeChange(getSize());
		forEachElements(Element::onDisplay);
	}

	protected void onShut() {
		forEachElements(Element::onShut);
	}

	// 9 Swing listeners
	protected void mousePressed(MouseEvent e) {
		elements.forEach(element -> element.mousePressed(e));
	}

	protected void mouseReleased(MouseEvent e) {
		elements.forEach(element -> element.mouseReleased(e));
	}

	protected void mouseClicked(MouseEvent e) {
		elements.forEach(element -> element.mouseClicked(e));
	}

	protected void mouseMoved(MouseEvent e) {
		elements.forEach(element -> element.mouseMoved(e));
	}

	protected void mouseDragged(MouseEvent e) {
		elements.forEach(element -> element.mouseDragged(e));
	}

	protected void mouseWheelMoved(MouseEvent e) {
		elements.forEach(element -> element.mouseWheelMoved(e));
	}

	protected void keyPressed(KeyEvent e) {
		elements.forEach(element -> element.keyPressed(e));
	}

	protected void keyReleased(KeyEvent e) {
		elements.forEach(element -> element.keyReleased(e));
	}

	protected void keyTyped(KeyEvent e) {
		elements.forEach(element -> element.keyTyped(e));
	}
}
