package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class Scene {
	private final Window holder;
	private final ArrayList<Element> elements = new ArrayList<>();

	public Scene(Window holder) {
		this.holder = holder;
	}

	protected final void forEachElements(Consumer<Element> consumer) {
		elements.forEach(consumer);
	}

	public void paint(Graphics2D g) {
		Theme.paintBackground(g, getSize());

		/* elements are drawn in a separate image to let them use
		 * AlphaCompositing without affecting background and without
		 * necessity for them to use separate BufferedImages for it
		 */
		Dimension size = getSize();
		BufferedImage elementsImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D elementsGraphics = elementsImage.createGraphics();

		elementsGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
		elementsGraphics.setClip(g.getClip());
		forEachElements(element -> element.paint(elementsGraphics));

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

	protected final void repaint(Rectangle area) {
		holder.repaint(area);
	}

	protected void onDisplay() {
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
