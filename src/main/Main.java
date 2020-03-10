package main;

import cards.Cards;
import gui.Window;

public class Main {
	public static void main(String[] args) {
		Cards.init();
		new Window();
	}
}
