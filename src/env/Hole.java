package env;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import env.Environment.DebugState;

/**
 * Class for a hole object created by prey animats.
 * 
 * @author Justin Morgan
 *
 */
public class Hole {
	private final int RADIUS = 15;
	private final double DEPTH_DETECT_MAX = 0.4;
	private final int OCCUPY_MAX = 3;
	
	private Point2D loc;
	private double depth = 0.0;
	private Color color;
	
	private Prey owner = null;
	private boolean public_hole = true;
	private int occupy_count = 0;
	
	private Environment env;
	
	/**
	 * Constructs a new hole object at the given location.
	 * 
	 * @param loc 2D point
	 * @param owner prey object that created the hole
	 * @param env environment
	 */
	public Hole(Point2D loc, Prey owner, Environment env) {
		this.loc = loc;
		this.env = env;
		this.owner = owner;
		this.color = new Color(200, 100, 0);
	}
	
	/**
	 * Deepens the hole by the given depth.
	 * 
	 * @param depth amount
	 */
	public void dig(double depth) {
		double depth_rel = depth / 35.0;
		if (this.depth < 1.0) {
			if (this.depth + depth_rel > 1.0) {
				this.depth = 1.0;
				this.color = new Color(0, 0, 0);
			}
			else {
				this.depth += depth_rel;
				this.color = new Color((int)(200 - this.depth * 200.0), (int)(100 - this.depth * 100.0), 0);
			}
		}
	}
	
	/**
	 * Attempts to add a prey animat to the hole.
	 * 
	 * @return true if successful, false if hole is full
	 */
	public boolean occupy() {
		if (this.occupy_count < OCCUPY_MAX) {
			this.occupy_count++;
			return true;
		}
		return false;
	}
	
	/**
	 * Decreases number of prey occupying the hole.
	 */
	public void leave() {
		this.occupy_count--;
	}
	
	/**
	 * Returns the location of the hole.
	 * 
	 * @return 2D point
	 */
	public Point2D getLocation() {
		return this.loc;
	}
	
	/**
	 * Returns the number of prey occupying the hole.
	 * 
	 * @return count
	 */
	public Integer getOccupyCount() {
		return this.occupy_count;
	}
	
	/**
	 * Returns the depth of the hole.
	 * 
	 * @return depth
	 */
	public Double getDepth() {
		return this.depth;
	}
	
	/**
	 * Returns the prey object that created the hole.
	 * 
	 * @return prey object
	 */
	public Prey getOwner() {
		return this.owner;
	}
	
	/**
	 * Returns true if the hole can be occupied by anyone.
	 * 
	 * @return prey object
	 */
	public Boolean isPublic() {
		return this.public_hole;
	}
	
	/**
	 * Returns the factor multiplied by the activation of a prey's sensors for prey occupying this hole.
	 * If the hole is 0.4 or deeper, the predator cannot detect any prey in this hole, and the
	 * detectable depth factor is 0.
	 * 
	 * @return factor
	 */
	public Double getDetectableDepth() {
		if (this.depth >= DEPTH_DETECT_MAX)
			return 0.0;
		else
			return 1.0 - this.depth / DEPTH_DETECT_MAX;
	}
	
	/**
	 * Draws the hole object.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		// Debug: display the gradient detectable by prey
		DebugState debug = env.getDebugState();
		if (debug == DebugState.All || debug == DebugState.Gradients) {
			double r = env.HOLE_G_RADIUS;
			
			AffineTransform old_d = g2d.getTransform();
			
			g2d.translate(this.loc.getX() - r, this.loc.getY() - r);
			
			g2d.setColor(Color.BLACK);
			g2d.drawOval(0, 0, (int)r * 2, (int)r * 2);
			
			g2d.setTransform(old_d);
		}
		
		AffineTransform old = g2d.getTransform();
		
		g2d.translate(this.loc.getX() - RADIUS, this.loc.getY() - RADIUS);
		
		g2d.setColor(this.color);
		g2d.fillOval(0, 0, RADIUS * 2, RADIUS * 2);
		
		g2d.setTransform(old);
	}
}
