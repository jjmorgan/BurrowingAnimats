package env;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Component that displays information for the selected object in the simulation window.
 * 
 * @author Justin Morgan
 *
 */
public class Watcher {
	private enum WatchEntity {
		PREY,
		PREDATOR,
		FOOD,
		HOLE,
		NONE
	}
	
	private final int LINE_SPACING = 12;
	private final int COLUMN_SPACING = 200;
	private final int MAX_LINES = 16;
	
	private Prey prey_current = null;
	private Predator predator_current = null;
	private Food food_current = null;
	private Hole hole_current = null;
	
	private WatchEntity state = WatchEntity.NONE;
	
	private int text_x;
	private int text_y;
	private int text_x_start;
	private int text_y_start;
	private int lines = 0;
	
	/**
	 * Constructs a new Watcher at the given location in the window.
	 * 
	 * @param x x position
	 * @param y y position
	 */
	public Watcher(int x, int y) {
		this.text_x = this.text_x_start = x;
		this.text_y = this.text_y_start = y;
	}
	
	/**
	 * Sets the target to the given prey animat.
	 * 
	 * @param p prey
	 */
	public void setTarget(Prey p) {
		this.resetTarget();
		this.prey_current = p;
		this.state = WatchEntity.PREY;
	}
	
	/**
	 * Sets the target to the given predator animat.
	 * 
	 * @param p predator
	 */
	public void setTarget(Predator p) {
		this.resetTarget();
		this.predator_current = p;
		this.state = WatchEntity.PREDATOR;
	}
	
	/**
	 * Sets the target to the given food object.
	 * 
	 * @param f food
	 */
	public void setTarget(Food f) {
		this.resetTarget();
		this.food_current = f;
		this.state = WatchEntity.FOOD;
	}
	
	/**
	 * Sets the target to the given hole object.
	 * 
	 * @param h hole
	 */
	public void setTarget(Hole h) {
		this.resetTarget();
		this.hole_current = h;
		this.state = WatchEntity.HOLE;
	}

	/**
	 * Resets the current target.
	 */
	public void resetTarget() {
		if (this.state == WatchEntity.PREY)
			this.prey_current.setSelected(false);
		else if (this.state == WatchEntity.PREDATOR)
			this.predator_current.setSelected(false);
		this.state = WatchEntity.NONE;
	}
	
	/**
	 * Writes a block of text containing multiple lines on the simulation window.
	 * 
	 * @param block lines separated by \n
	 * @param g2d
	 */
	public void writeBlock(String block, Graphics2D g2d) {
		int start_index = 0, stop_index;
		while ((stop_index = block.indexOf("\n", start_index)) >= 0) {
			writeLine(block.substring(start_index, stop_index), g2d);
			start_index = stop_index + 1;
		}
		if (start_index < block.length())
			writeLine(block.substring(start_index), g2d);
	}
	
	/**
	 * Writes a line of text on the simulation window.
	 * 
	 * @param line line
	 * @param g2d
	 */
	public void writeLine(String line, Graphics2D g2d) {
		g2d.drawString(line, this.text_x, this.text_y);
		this.text_y += LINE_SPACING;
		this.lines++;
		if (this.lines >= MAX_LINES) {
			this.text_x += COLUMN_SPACING;
			this.text_y = this.text_y_start;
			this.lines = 0;
		}
	}
	
	/**
	 * Draws the current status of the selected object.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.white);
		this.text_x = this.text_x_start;
		this.text_y = this.text_y_start;
		this.lines = 0;
		
		switch (this.state) {
		case PREY:
			if (this.prey_current == null) {
				this.resetTarget();
				return;
			}
			Prey p = this.prey_current;
			
			this.writeLine("Prey", g2d);
			this.writeLine(String.format("Average Energy: %.4f", p.getAverageEnergy()), g2d);
			this.writeBlock(p.toString(), g2d);
			this.writeLine(String.format("Detectable Depth: %.4f", p.getDetectableDepth()), g2d);
				
			break;
		
		case PREDATOR:
			if (this.predator_current == null) {
				this.resetTarget();
				return;
			}
			Predator r = this.predator_current;
			this.writeLine("Predator", g2d);
			this.writeBlock(r.toString(), g2d);
			break;
			
		case HOLE:
			if (this.hole_current == null) {
				this.resetTarget();
				return;
			}
			Hole h = this.hole_current;
			
			this.writeLine("Hole", g2d);
			this.writeLine("Occupants: " + h.getOccupyCount(), g2d);
			this.writeLine(String.format("Depth: %.4f", h.getDepth()), g2d);
			break;
			
		case FOOD:
			if (this.food_current == null) {
				this.resetTarget();
				return;
			}
			Food f = this.food_current;
			
			this.writeLine("Food", g2d);
			this.writeLine(String.format("Remaining: %.4f", f.getRemaining()), g2d);
			break;
			
		case NONE:
			break;
		}
	}
}
