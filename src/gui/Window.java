package gui;

import gui.scenes.SceneExample;
import gui.scenes.SceneMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public final class Window {
	// amount of ms to wait to hide mouse after last move
	private static final int SCENE_TRANSITION_PERIOD = 350;
	private static final int HIDE_MOUSE_AFT_INACTIVE = 3000;
	private static final int ROUND_CORNERS = 20;
	public static final Dimension INITIAL_PANEL_SIZE = new Dimension(1280, 720);
	public static FontRenderContext fontRenderContext;

	private static final Cursor HIDDEN_CURSOR;

	static {
		BufferedImage emptyTransparent = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		HIDDEN_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
				emptyTransparent, new Point(0, 0), "blank cursor");
	}

	boolean fullscreen;
	private JFrame frame;
	private JPanel content;
	private Scene scene;
	// window buttons & top bar, resize area
	private ArrayList<Element> windowElements;
	private Point lastMousePosition = new Point();
	private long lastMouseMoveTime;
	private boolean mouseHid;

	private Scene oldScene; // being null indicates no transition animation currently
	private long sceneChangeTime;
	private AnimatingElement sceneTransitionAnimate;

	public Window() {
		fontRenderContext = new FontRenderContext(null, true, true);

		initFrame();
		initScenes();
		frame.setVisible(true);
	}

	private void initScenes() {
		Scene.sceneMain = new SceneMain(this);
		Scene.sceneExample = new SceneExample(this);

		scene = Scene.sceneMain;
		scene.onDisplay();
	}

	private void initFrame() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.setSize(INITIAL_PANEL_SIZE);
		frame.setMinimumSize(new Dimension(400, 400));
		frame.setLocationRelativeTo(null);

		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));

		windowElements = new ArrayList<>();
		windowElements.add(new WindowTopBar(this, frame));
		windowElements.add(new WindowResizeArea(this, frame));

		content = new JPanel() {
			{ // add listeners on object creation
				// Swing mouse listeners
				MouseAdapter mouseAdapter = new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						scene.mouseClicked(e);
						windowElements.forEach(we -> we.mouseClicked(e));
					}

					@Override
					public void mousePressed(MouseEvent e) {
						scene.mousePressed(e);
						windowElements.forEach(we -> we.mousePressed(e));
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						scene.mouseReleased(e);
						windowElements.forEach(we -> we.mouseReleased(e));
					}

					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						scene.mouseWheelMoved(e);
						windowElements.forEach(we -> we.mouseWheelMoved(e));
					}

					@Override
					public void mouseDragged(MouseEvent e) {
						onMousePositionChange(e);
						scene.mouseDragged(e);
						windowElements.forEach(we -> we.mouseDragged(e));
					}

					@Override
					public void mouseMoved(MouseEvent e) {
						onMousePositionChange(e);
						scene.mouseMoved(e);
						windowElements.forEach(we -> we.mouseMoved(e));
					}
				};
				addMouseListener(mouseAdapter);
				addMouseMotionListener(mouseAdapter);
				addMouseWheelListener(mouseAdapter);
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

				// fill background because it's originally transparent
				g2d.setColor(Theme.getFG(255));
				g2d.fillRect(0, 0, getWidth(), getHeight());
				Theme.paintBackground(g2d, getSize());

				paintScene(g2d);
				windowElements.forEach(element -> element.paint(g2d));
			}
		};

		sceneTransitionAnimate = new AnimatingElement(content::repaint);
		sceneTransitionAnimate.setDisplayed(true);

		frame.setContentPane(content);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				scene.keyTyped(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				scene.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				scene.keyReleased(e);
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension size = getSize();

				scene.onContainerSizeChange(size); // relocate elements if needed
				windowElements.forEach(element -> element.onContainerSizeChange(size));
				if (oldScene != null)
					oldScene.onContainerSizeChange(size);

				Point mouse = MouseInfo.getPointerInfo().getLocation();
				Point window = frame.getLocation();
				Point mouseInWindow = new Point(mouse.x - window.x, mouse.y - window.y);

				lastMouseMoveTime = System.currentTimeMillis();
				lastMousePosition = mouseInWindow;
				if (mouseHid)
					showMouse();

				// as position is changed, mouse might be no longer atop the window without elements noticing
				MouseEvent mouseMove = new MouseEvent(content, 0, lastMouseMoveTime, 0,
						lastMousePosition.x, lastMousePosition.y, 0, false, 0);
				scene.mouseMoved(mouseMove);
				windowElements.forEach(element -> element.mouseMoved(mouseMove));

				// case for external change in window size, i.e. pressing Window+UP
				if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
					fullscreen = true;
				} else if (frame.getExtendedState() == Frame.NORMAL) {
					fullscreen = false;
				}

				double roundCorners = fullscreen ? 0 : ROUND_CORNERS;
				frame.setShape(new RoundRectangle2D.Double(0, 0,
						frame.getWidth(), frame.getHeight(), roundCorners, roundCorners));
			}
		});

		startMouseHider();
	}

	private void paintScene(Graphics2D g) {
		if (oldScene != null) { // in case currently animating or just finished
			long passed = System.currentTimeMillis() - sceneChangeTime;
			if (passed < SCENE_TRANSITION_PERIOD) { // currently animating
				double phase = (double) passed / SCENE_TRANSITION_PERIOD;

				Composite previous = g.getComposite();
				paintTransitingScenes(g, phase);
				g.setComposite(previous);
			} else if (passed < SCENE_TRANSITION_PERIOD + HoverCalc.AFT_STABILIZED_SPARE_DELAY) {
				scene.paint(g, null);
			} else { // halt the animation
				oldScene = null;
				sceneTransitionAnimate.setActive(false);
				scene.paint(g, null);
			}
		} else { // no animation case
			scene.paint(g, null);
		}
	}

	private void paintTransitingScenes(Graphics2D g, double phase) {
		double disappearPhase = phase / .5d; // at that phase, scene disappears
		if (disappearPhase < 1) { // if not disappeared yet
			double zDisappearing = -.8 * HoverCalc.easeCubicIn(disappearPhase); // gets closer when disappearing
			float disappearingAlpha = (float) HoverCalc.easeCubicInOut(1d - disappearPhase); // rate of fading out

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, disappearingAlpha));
			oldScene.paint(g, getTransformForZ(zDisappearing));
		}

		double zAppearing = appearRapidlyZFunction(phase); // how far the appearing scene is (gets closer)
		float appearingAlpha = (float) HoverCalc.easeCubicOut(phase); // opacity of the appearing scene

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, appearingAlpha));
		scene.paint(g, getTransformForZ(zAppearing));
	}

	// as x goes from 0 to 1 result goes from far (apx. goFrom) to close (0) slowing down at the end
	private static double appearRapidlyZFunction(double x) {
		double goFrom = 30d;
		double s = -9d;

		double f = goFrom / 2 * Math.exp(x * s) / (Math.exp(x * s) + 1);
		double at1 = goFrom / 2 * Math.exp(s) / (Math.exp(s) + 1); // at1 is used to make f(1) = 0
		return f - at1;
	}

	/* Finds scale value for given Z relative to 1
	 * and creates a transform instance with scale
	 * relative to screen center.
	 *
	 * Examples
	 * Z=-0.99	scale=100
	 * Z=-0.9	scale=10
	 * Z=-0.8	scale=5
	 * Z=0		scale=1 (no effect)
	 * Z=1		scale=0.5
	 * Z=9		scale=0.1
	 */
	private AffineTransform getTransformForZ(double z) {
		double scale = 1d / (1d + z);
		double halfX = getSize().getWidth() / 2d;
		double halfY = getSize().getHeight() / 2d;

		AffineTransform result = AffineTransform.getTranslateInstance(halfX, halfY);
		result.scale(scale, scale);
		result.translate(-halfX, -halfY);

		return result;
	}

	private void onMousePositionChange(MouseEvent e) {
		lastMousePosition = e.getPoint();
		lastMouseMoveTime = e.getWhen();
		if (mouseHid)
			showMouse();
	}

	private void hideMouse() {
		mouseHid = true;
		content.setCursor(HIDDEN_CURSOR);
	}

	private void showMouse() {
		mouseHid = false;
		content.setCursor(Cursor.getDefaultCursor());
	}

	// the process which checks if the mouse has been inactive for too long and hides it
	private void startMouseHider() {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!mouseHid) {
					long t = System.currentTimeMillis();
					if (t > lastMouseMoveTime + HIDE_MOUSE_AFT_INACTIVE)
						hideMouse();
				}
			}
		}).start();
	}

	protected Point getMousePosition() {
		return lastMousePosition;
	}

	public Dimension getSize() {
		Dimension result = frame.getContentPane().getSize();
		if (result.width == 0 && result.height == 0)
			return INITIAL_PANEL_SIZE; // avoid (0, 0) coordinates bug
		return result;
	}

	public void changeScene(Scene s) {
		s.onContainerSizeChange(getSize()); // the scene wasn't receiving resize info until now
		(oldScene = scene).onShut();
		(scene = s).onDisplay();

		sceneChangeTime = System.currentTimeMillis();
		sceneTransitionAnimate.setActive(true);
	}

	protected void repaint(Rectangle area) {
		content.repaint(area);
	}
}
