package gui;

import gui.elements.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public final class Window {
	// amount of ms to wait to hide mouse after last move
	private static final int HIDE_MOUSE_AFT_INACTIVE = 3000;
	private static final int ROUND_CORNERS = 20;

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

	public Window() {
		scene = new Scene(this) {
			{
				addElement(new Button("Click me", () -> System.out.println("I'm working"),
						this, new Bounds(0, 0, 125, 30, 0, 0)));
			}
		};
		initFrame();
		frame.setVisible(true);
	}

	private void initFrame() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.setSize(1280, 720);
		frame.setMinimumSize(new Dimension(400, 400));
		frame.setLocationRelativeTo(null);

		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));

		windowElements = new ArrayList<>();
		windowElements.add(new WindowTopBar(this, frame));
		windowElements.add(new WindowResizeArea(this, frame));

		content = new JPanel() {
			{ // add listeners on object creation
				// 9 Swing listeners
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

				// fill black background because it's originally transparent
				g2d.setColor(Color.black);
				g2d.fillRect(0, 0, getWidth(), getHeight());

				scene.paint(g2d);
				windowElements.forEach(element -> element.paint(g2d));
			}
		};

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
				scene.onContainerSizeChange(getSize()); // relocate elements if needed
				windowElements.forEach(element -> element.onContainerSizeChange(getSize()));

				Point mouse = MouseInfo.getPointerInfo().getLocation();
				Point window = frame.getLocation();
				Point mouseInWindow = new Point(mouse.x - window.x, mouse.y - window.y);

				lastMouseMoveTime = System.currentTimeMillis();
				lastMousePosition = mouseInWindow;
				if (mouseHid)
					showMouse();

				// as position is changed, mouse might no longer be atop the window without elements noticing
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

		new AnimatingElement(() -> { // to hide mouse when inactive
			if (!mouseHid) {
				long t = System.currentTimeMillis();
				if (t > lastMouseMoveTime + HIDE_MOUSE_AFT_INACTIVE)
					hideMouse();
			}
		}).setActive(true);
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

	protected Point getMousePosition() {
		return lastMousePosition;
	}

	public Dimension getSize() {
		return frame.getContentPane().getSize();
	}

	protected void repaint(Rectangle area) {
		content.repaint(area);
	}
}
