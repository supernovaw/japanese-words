package gui.elements;

import cards.Kana;
import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TextField extends Element {
	private static Color selectionFillColor = new Color(0x3c0078d7, true);
	private static final int KANA_SWITCH_KEY = KeyEvent.VK_TAB;

	private boolean isKanaInput;
	private int kanaType = Kana.HIRAGANA; // takes a value of either HIRAGANA or KATAKANA, only used with isKanaInput

	private int offsetXFixed; // X offset from bounds start. Letters like 'j' stick out to the left and need this gap
	private int offsetX; // offset to paint text, may vary if text is larger than bounds
	private String text;
	private int caretPos, selectionFromPos;
	private Font font;
	private Font hintFont;
	private float fontSize;
	private int slideWhenFading; // distance text slides up when fading away
	private int caretHeight; // caret and selection height
	private boolean hold; // for selecting text by dragging

	private String hintText; // when no focus and no text
	private HoverCalc hintDisplayCalc;
	private HoverCalc focusCalc;
	private Runnable onEnter;

	// fadingText has object parameters String (text) and int (offsetX)
	private OneWayAnimating fadingText;

	public TextField(String hintText, Runnable onEnter, Scene container, Bounds bounds) {
		super(container, bounds);

		text = "";
		this.onEnter = onEnter;

		hintDisplayCalc = new HoverCalc(300, this);
		hintDisplayCalc.setInitially(text.isEmpty());
		this.hintText = hintText;

		fontSize = h() / 2f;
		font = Theme.getUIFont().deriveFont(fontSize);
		hintFont = Theme.getUIFont().deriveFont(fontSize);

		caretHeight = (int) (fontSize * 1.2d);
		offsetXFixed = offsetX = (int) (h() - fontSize) / 2;
		slideWhenFading = (int) (0.7d * fontSize);

		focusCalc = new HoverCalc(300, this);
		fadingText = new OneWayAnimating(400, () -> repaint(new Rectangle(
				x(), y() - slideWhenFading, w(), h() + slideWhenFading)));
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		caretPos = selectionFromPos = text.length(); // set caret to the end
		offsetX = offsetXFixed; // reset offset
		repaint();
	}

	public String flushText() {
		String oldText = text;
		text = "";
		if (!oldText.isBlank()) {
			fadingText.animate(oldText, offsetX);
		}
		caretPos = selectionFromPos = 0;
		offsetX = offsetXFixed;
		return oldText;
	}

	public void setKanaInput(boolean b) {
		if (b)
			font = Theme.getFontJapanese().deriveFont(fontSize);
		else
			font = Theme.getUIFont().deriveFont(fontSize);
		isKanaInput = b;
	}

	public void triggerFocus() {
		focusCalc.setHovered(true);
		updateHintState();
	}

	/* Hint display condition may depend on focus and the text, so
	 * whenever those are changed they should call updateHintState
	 * to make sure it's in the correct state. Presently there is
	 * only text.isEmpty() check in the condition, but that might
	 * be changed in the future.
	 */
	private void updateHintState() {
		hintDisplayCalc.setHovered(text.isEmpty());
	}

	@Override
	protected void paint(Graphics2D g) {
		Shape clipBefore = g.getClip();
		Composite compositeBefore = g.getComposite();
		g.setClip(x(), y() - slideWhenFading, w(), h() + slideWhenFading); // don't let the text go outside bounds

		paintHintText(g);

		g.setFont(font);
		paintFadingText(g);

		g.setColor(Theme.getFG());
		g.fill(getTextArea(text, x() + offsetX, centerStringY(g, y() + h() / 2), g)); // paint text

		double focusInOut = focusCalc.getCubicInOut();
		float focusSine = (float) focusCalc.getSine();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, focusSine));
		int caretX = x() + offsetX + stringWidth(text.substring(0, caretPos));
		int selectionX = x() + offsetX + stringWidth(text.substring(0, selectionFromPos));

		if (selectionFromPos != caretPos) { // if there are characters selected
			g.setColor(selectionFillColor);
			g.fillRect(Math.min(caretX, selectionX), y() + (h() - caretHeight) / 2,
					Math.abs(caretX - selectionX), caretHeight);
		}
		g.setColor(Theme.getFG()); // paint caret
		g.drawLine(caretX, y() + (h() - caretHeight) / 2, caretX, y() + (h() + caretHeight) / 2);

		if (focusInOut != 1) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - focusSine));
			int underlineWidth = (int) ((1d - focusInOut) * w());
			int underlineY = y() + h() - 1;
			g.drawLine(x() + (w() - underlineWidth) / 2, underlineY, x() + (w() + underlineWidth) / 2, underlineY);
		}

		g.setClip(clipBefore);
		g.setComposite(compositeBefore);
	}

	private void paintHintText(Graphics2D g) {
		double phase = hintDisplayCalc.getSine();
		if (phase == 0)
			return;
		g.setColor(Theme.getFG(phase * 0.45));
		g.setFont(hintFont);
		g.fill(getTextArea(hintText, x() + offsetXFixed, centerStringY(g, y() + h() / 2), g));
	}

	private void paintFadingText(Graphics2D g) {
		if (fadingText.isFinished())
			return;

		String fadingString = (String) fadingText.getParameter(0);
		int fadingTextOffset = (int) fadingText.getParameter(1);

		double phase = fadingText.getPhase();
		g.setColor(Theme.getFG(HoverCalc.easeSine(1d - phase)));
		int slide = (int) (slideWhenFading * HoverCalc.easeCubicOut(phase));
		g.fill(getTextArea(fadingString, x() + fadingTextOffset, centerStringY(g, y() + h() / 2) - slide, g));
	}

	private int stringWidth(String s) {
		return (int) stringWidth(s, font);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;

		boolean contains = contains(e.getPoint());
		focusCalc.setHovered(contains);
		updateHintState();
		if (!contains)
			return;

		setCaret(getPos(e.getX() - x() - offsetX), true); // set caret to where mouse clicked
		hold = true; // indicate that mouse drags starting inside bounds
		repaint();
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		hold = false; // stop dragging (holding)
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (hold) { // if started dragging inside bounds
			setCaret(getPos(e.getX() - x() - offsetX), false); // set selection accordingly to mouse position
			repaint();
		}
	}

	// gets caret position corresponding to X value (without counting x() and offset)
	private int getPos(int x) {
		if (x <= 0)
			return 0;

		if (x >= stringWidth(text))
			return text.length();

		for (int i = 1; true; i++) {
			int stringW = stringWidth(text.substring(0, i));
			if (stringW < x)
				continue;

			int charW = stringWidth(text.substring(i - 1, i));
			if (stringW - x > charW / 2d) // if mouse is closer to the left position, subtract 1
				return i - 1;
			else
				return i;
		}
	}

	// is setSelection=true, selection is cleared, otherwise not affected
	private void setCaret(int pos, boolean setSelection) {
		// assign new values
		caretPos = pos;
		if (setSelection)
			selectionFromPos = pos;

		// if caret is this close to bounds, move text X offset to increase gap
		int wrapFrom = w() / 4;

		// width of text from start to caret
		int textW = stringWidth(text.substring(0, pos));
		int caretGapLeft = textW + offsetX;
		int caretGapRight = w() - caretGapLeft;

		int newOffset = offsetX; // in case it will not be affected, use old value
		if (caretGapRight < wrapFrom) {
			newOffset = -textW + w() - wrapFrom;
		} else if (caretGapLeft < wrapFrom) {
			newOffset = -textW + wrapFrom;
		}
		newOffset = Math.min(newOffset, offsetXFixed); // prevent empty gap with no text on the left
		offsetX = newOffset;
	}

	@Override
	protected void keyPressed(KeyEvent e) {
		if (!focusCalc.isHovered())
			return; // if no focus
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			onEnter.run();
			return;
		}

		if (isKanaInput) { // use Japanese layout (translated with Kana class)
			handleKanaInput(e);
		} else { // default input type
			boolean isChar = e.getKeyCode() != KeyEvent.CHAR_UNDEFINED &&
					!e.isActionKey() && font.canDisplay(e.getKeyChar());
			if (isChar) {
				type(e.getKeyChar()); // case for letters, characters, punctuation, etc.
			} else {
				executeEditingShortcuts(e);
			}
		}
		updateHintState();
		repaint();
	}

	private void handleKanaInput(KeyEvent e) {
		int code = e.getKeyCode();

		if (code == KANA_SWITCH_KEY) {
			if (kanaType == Kana.HIRAGANA)
				kanaType = Kana.KATAKANA;
			else
				kanaType = Kana.HIRAGANA;
			return;
		}

		if (code == Kana.DAKUTEN_KEYCODE || code == Kana.HANDAKUTEN_KEYCODE) {
			/* Dakuten/handakuten can only be added if:
			 * shift isn't down;
			 * there is no selection;
			 * there is a character before the caret.
			 */
			if (!e.isShiftDown() && selectionFromPos == caretPos && caretPos != 0) {
				char c = text.charAt(caretPos - 1);
				c = code == Kana.DAKUTEN_KEYCODE ? Kana.addDakuten(c) : Kana.addHandakuten(c);
				text = text.substring(0, caretPos - 1) + c + text.substring(caretPos);
			}
			return;
		}

		char kana = Kana.getTypedChar(code, e.isShiftDown(), kanaType);
		// if the key is not kana or Ctrl is down, only execute as a shortcut
		if (kana == 0 || e.isControlDown()) {
			executeEditingShortcuts(e);
		} else { // kana letter
			type(kana);
		}
	}

	// actions like Left Arrow, Ctrl + C, Shift + Ins
	private void executeEditingShortcuts(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_BACK_SPACE: // (Ctrl)+Backspace
				keyBackspace(e);
				break;
			case KeyEvent.VK_DELETE: // (Ctrl)+(Shift)+Delete
				keyDelete(e);
				break;
			case KeyEvent.VK_LEFT: // (Ctrl)+(Shift)+Left
				keyLeft(e);
				break;
			case KeyEvent.VK_RIGHT: // (Ctrl)+(Shift)+Right
				keyRight(e);
				break;
			case KeyEvent.VK_V: // Ctrl+V
				if (e.isControlDown())
					paste();
				break;
			case KeyEvent.VK_INSERT: // (Ctrl)+(Shift)+Insert
				if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK)
					paste();
				if (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK)
					copy();
				break;
			case KeyEvent.VK_C: // Ctrl+C
				if (e.isControlDown())
					copy();
				break;
			case KeyEvent.VK_UP: // (Shift)+Up
			case KeyEvent.VK_HOME: // (Shift)+Home
				caretToBeginning(e);
				break;
			case KeyEvent.VK_DOWN: // (Shift)+Down
			case KeyEvent.VK_END: // (Shift)+End
				caretToEnd(e);
				break;
			case KeyEvent.VK_A: // Ctrl+A
				if (e.isControlDown())
					selectAll();
				break;
			case KeyEvent.VK_X: // Ctrl+X
				if (e.isControlDown())
					cut();
				break;
		}
	}

	private void type(char c) {
		int min = Math.min(selectionFromPos, caretPos);
		int max = Math.max(selectionFromPos, caretPos);

		// replace selected text (or an empty area) with typed character
		text = text.substring(0, min) + c + text.substring(max);
		setCaret(min + 1, true);
	}

	private void keyBackspace(KeyEvent e) {
		if (selectionFromPos != caretPos) { // if deleting selected area
			int min = Math.min(selectionFromPos, caretPos);
			int max = Math.max(selectionFromPos, caretPos);
			text = text.substring(0, min) + text.substring(max);
			setCaret(min, true);
		} else { // if deleting single character or word
			if (caretPos == 0) // if nothing to delete
				return;

			if (e.isControlDown()) { // delete a word
				int i = ctrlLeft();
				text = text.substring(0, i) + text.substring(caretPos);
				setCaret(i, true);
			} else { // delete a character
				text = text.substring(0, caretPos - 1) + text.substring(caretPos);
				setCaret(caretPos - 1, true);
			}
		}
	}

	private void keyDelete(KeyEvent e) {
		if (selectionFromPos != caretPos) { // if deleting selected area
			int min = Math.min(selectionFromPos, caretPos);
			int max = Math.max(selectionFromPos, caretPos);

			if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
				StringSelection sel = new StringSelection(text.substring(min, max));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
			}

			text = text.substring(0, min) + text.substring(max);
			setCaret(min, true);
		} else { // if deleting single character or word
			if (caretPos == text.length()) // if nothing to delete
				return;

			if (e.isControlDown()) { // delete a word
				int i = ctrlRight();
				text = text.substring(0, caretPos) + text.substring(i);
			} else { // delete a character
				text = text.substring(0, caretPos) + text.substring(caretPos + 1);
			}
		}
	}

	private void keyLeft(KeyEvent e) {
		if (caretPos == 0) { // if nowhere to move
			if (!e.isShiftDown()) // if shift isn't held, reset selection
				selectionFromPos = caretPos;
			return;
		}
		int newCaretPos = e.isControlDown() ? ctrlLeft() : caretPos - 1;
		setCaret(newCaretPos, !e.isShiftDown());
	}

	private void keyRight(KeyEvent e) {
		if (caretPos == text.length()) { // if nowhere to move
			if (!e.isShiftDown()) // if shift isn't held, reset selection
				selectionFromPos = caretPos;
			return;
		}
		int newCaretPos = e.isControlDown() ? ctrlRight() : caretPos + 1;
		setCaret(newCaretPos, !e.isShiftDown());
	}

	private void paste() {
		try {
			String clipboard = (String) Toolkit.getDefaultToolkit()
					.getSystemClipboard().getData(DataFlavor.stringFlavor);
			int min = Math.min(selectionFromPos, caretPos);
			int max = Math.max(selectionFromPos, caretPos);

			// replace selected text (or an empty area) with clipboard contents
			text = text.substring(0, min) + clipboard + text.substring(max);
			setCaret(min + clipboard.length(), true);
		} catch (Exception ignore) { // empty or non-text clipboard, ignore
		}
	}

	private void copy() {
		int min = Math.min(selectionFromPos, caretPos);
		int max = Math.max(selectionFromPos, caretPos);
		if (min == max) // if nothing selected
			return;
		StringSelection sel = new StringSelection(text.substring(min, max));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
	}

	private void cut() {
		int min = Math.min(selectionFromPos, caretPos);
		int max = Math.max(selectionFromPos, caretPos);
		if (min == max) // if nothing selected
			return;
		StringSelection sel = new StringSelection(text.substring(min, max));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		text = text.substring(0, min) + text.substring(max); // cut copied area
		setCaret(min, true);
	}

	private void caretToBeginning(KeyEvent e) {
		// if shift is held, make a selection by not affecting selectionFromPos
		setCaret(0, !e.isShiftDown());
	}

	private void caretToEnd(KeyEvent e) {
		// if shift is held, make a selection by not affecting selectionFromPos
		setCaret(text.length(), !e.isShiftDown());
	}

	private void selectAll() {
		selectionFromPos = 0;
		setCaret(text.length(), false);
	}

	// calculate new caret position for moving with Ctrl held
	private int ctrlLeft() {
		int index = caretPos - 1;
		while (index != 0) {
			if (text.charAt(index) == ' ')
				index--;
			else
				break;
		}
		while (index != 0) {
			if (text.charAt(index - 1) != ' ')
				index--;
			else
				break;
		}
		return index;
	}

	// calculate new caret position for moving with Ctrl held
	private int ctrlRight() {
		int index = caretPos;
		while (index != text.length()) {
			if (text.charAt(index) != ' ')
				index++;
			else
				break;
		}
		while (index != text.length()) {
			if (text.charAt(index) == ' ')
				index++;
			else
				break;
		}
		return index;
	}

	@Override
	protected void onDisplay() {
		hintDisplayCalc.setDisplayed(true);
		focusCalc.setDisplayed(true);
		fadingText.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		hintDisplayCalc.setDisplayed(false);
		focusCalc.shut();
		updateHintState();
		fadingText.setDisplayed(false);
	}
}
