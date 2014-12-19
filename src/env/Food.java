package env;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import env.Environment.DebugState;

/**
 * Class for a food object consumable by prey animats.
 * 
 * @author Justin Morgan
 *
 */
public class Food {
	private final int SIZE = 10;
	private final double AMOUNT_PER_BITE = 0.25;
	private final int TIMEOUT = 60;
	
	private Point2D loc;
	private Color color;
	private double remaining = 1.0;
	private int timeout = 0;
	
	private Environment env;
	
	/**
	 * Constructs a food object at the given location.
	 * 
	 * @param loc 2D point
	 * @param env environment
	 */
	public Food(Point2D loc, Environment env) {
		this.loc = loc;
		this.env = env;
		this.color = new Color(255, 105, 90);
	}
	
	/**
	 * Reduces the amount of food remaining after being consumed by a prey animat.
	 */
	public void consume() {
		if (this.timeout > 0)
			return;
		
		if (this.remaining > 0.0) {
			if (this.remaining - AMOUNT_PER_BITE < 0.0) {
				this.remaining = 0.0;
				this.color = new Color(0, 0, 0);
			}
			else {
				this.remaining -= AMOUNT_PER_BITE;
				this.color = new Color((int)(this.remaining * 255.0), 0, 0);
			}
			this.timeout = TIMEOUT;
		}
	}
	
	/**
	 * Returns true if none of the food remains.
	 * @return
	 */
	public boolean isGone() {
		return this.remaining == 0.0;
	}
	
	/** 
	 * Updates the timeout of the food between successful bites.
	 */
	public void update() {
		if (this.timeout > 0)
			this.timeout--;
	}
	
	/**
	 * Returns the location of the food.
	 * 
	 * @return 2D point
	 */
	public Point2D getLocation() {
		return this.loc;
	}
	
	/**
	 * Returns the remaining amout of food.
	 * 
	 * @return amount
	 */
	public double getRemaining() {
		return this.remaining;
	}
	
	/**
	 * Draws the food object.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		// Debug: display the gradient detectable by prey
		DebugState debug = env.getDebugState();
		if (debug == DebugState.All || debug == DebugState.Gradients) {
			double r = env.FOOD_G_RADIUS;
			
			AffineTransform old_d = g2d.getTransform();
			
			g2d.translate(this.loc.getX() - r, this.loc.getY() - r);
			
			g2d.setColor(Color.BLACK);
			g2d.drawOval(0, 0, (int)r * 2, (int)r * 2);
			
			g2d.setTransform(old_d);
		}
		
		AffineTransform old = g2d.getTransform();
		
		g2d.translate(this.loc.getX() - (SIZE / 2), this.loc.getY() - (SIZE / 2));
		
		g2d.setColor(this.color);
		g2d.fillRect(0, 0, SIZE, SIZE);
		
		g2d.setTransform(old);
	}
}
