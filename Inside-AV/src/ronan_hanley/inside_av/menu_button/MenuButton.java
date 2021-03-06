package ronan_hanley.inside_av.menu_button;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import ronan_hanley.inside_av.InsideAV;

/**
 * Represents a rectangular button on the screen, that
 * can be clicked.
 * @author Ronan
 */
public final class MenuButton {
	private int x;
	private int y;
	private int width;
	private int height;
	private Color color;
	private Color hoverColor;
	private String text;
	private boolean hovering;
	
	public MenuButton(int x, int y, int width, int height, Color color, Color hoverColor, String text) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.hoverColor = hoverColor;
		this.text = text;
	}
	
	public void setHovering(boolean hovering) {
		this.hovering = hovering;
	}
	
	public boolean isHovering() {
		return hovering;
	}
	
	public void render(Graphics g) {
		Color currentColor = (hovering ? hoverColor : color);
		g.setColor(currentColor);
		g.fillRect(x, y, width, height);
		
		// calculate spacing to center the text
		// adjustments are made to account for the additional spacing of the characters
		// (1 pixel to the right and 2 pixels below)
		int fontScale = height / InsideAV.font.getCharHeight();
		int fontHeight = (InsideAV.font.getCharHeight() - 2) * fontScale;
		int marginY = height - fontHeight;
		
		int fontWidth = InsideAV.font.getCharWidth() * text.length() * fontScale;
		int marginX = width - fontWidth + fontScale;
		
		InsideAV.font.drawString(text, x + (marginX /2), y + (marginY /2), Color.white, fontScale, false, g);
	}
	
}
