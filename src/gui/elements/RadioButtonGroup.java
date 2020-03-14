package gui.elements;

import java.util.ArrayList;
import java.util.List;

public class RadioButtonGroup {
	private List<RadioButton> buttons;
	private int selected;

	public RadioButtonGroup(int selected) {
		buttons = new ArrayList<>();
		this.selected = selected;
	}

	public void add(RadioButton b) {
		b.setInitially(buttons.size() == selected);
		buttons.add(b);
	}

	void push(RadioButton b) {
		for (int i = 0; i < buttons.size(); i++) {
			boolean eq = buttons.get(i) == b;
			buttons.get(i).setEnabled(eq);
			if (eq)
				selected = i;
		}
	}

	public int getSelectedIndex() {
		return selected;
	}
}
