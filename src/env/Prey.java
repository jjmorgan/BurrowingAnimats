package env;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import env.Environment.DebugState;
import nn.NeuralNetworkBase;

/**
 * Class for a prey animat. The prey is a vehicle with an evolvable and learnable brain controller,
 * by default. Predators can consume both live and dead prey.
 * 
 * @author Justin Morgan
 *
 */
public class Prey extends Animat {
	private final double PI_2 = 2.0 * Math.PI;
	
	private final double SPEED_BASE = 1.2;
	private final double ROTATE_BASE = 50.0;
	
	private final double AMOUNT_PER_BITE = 1.0; // eaten by predator
	private final int TIMEOUT = 60;
	
	private final int WIDTH = 10;
	private final int LENGTH = 16;
	private final Color COLOR = new Color(170, 230, 255);
	private final Color COLOR_HEAD = Color.white;
	private final Color COLOR_HEAD_SHOUT = Color.red;
	private final Color COLOR_SELECTED = new Color(70, 130, 255);
	private final Color COLOR_DEAD = new Color(60, 70, 80);
	private final Color COLOR_DEAD_HEAD = new Color(90, 110, 110);
	private final Color COLOR_DEAD_SELECTED = new Color(0, 10, 100);
	
	private int timeout = 0;
	private int shout_epochs = 0;
	private double remaining = 1.0;
	private boolean hidden = false;
	
	private double last_hear_shout = 0.0;
	
	private Hole hole_occupying = null;
	
	/**
	 * Constructs a new prey.
	 * 
	 * @param nnb neural network base
	 * @param env environment
	 */
	public Prey(NeuralNetworkBase nnb, Environment env) {
		super(nnb, env);
	}
	
	/**
	 * Constructs a prey with inherited connections from a parent.
	 * 
	 * @param nnb neural network base
	 * @param parent parent prey
	 * @param child mutate connections
	 * @param env environment
	 */
	public Prey(NeuralNetworkBase nnb, Prey parent, Boolean child, Environment env) {
		super(nnb, parent, child, env);
	}
	
	/**
	 * Sets the rotation of the prey.
	 * 
	 * @param rot degrees
	 */
	public void setRot(double rot) {
		this.rot = Math.toRadians(rot);
		while (this.rot < 0.0)
			this.rot += PI_2;
		while (this.rot >= PI_2)
			this.rot -= PI_2;
	}
	
	/**
	 * Returns the rotation of the prey.
	 * 
	 * @return degrees
	 */
	public double getRot() {
		return Math.toDegrees(this.rot);
	}
	
	/**
	 * Returns true if the prey was consumed by a predator.
	 * 
	 * @return
	 */
	public boolean isGone() {
		return this.remaining == 0.0;
	}
	
	/**
	 * Hides the prey from the environment.
	 * 
	 */
	public void hide() {
		this.hidden = true;
	}
	
	/**
	 * Consumes the prey if collided with a predator.
	 */
	public void consume() { // by predator
		if (this.energy > 0.0)
			this.energy = 0.0;

		if (this.timeout > 0)
			return;
		
		if (this.remaining > 0.0) {
			if (this.remaining - AMOUNT_PER_BITE < 0.0) {
				this.remaining = 0.0;
			}
			else {
				this.remaining -= AMOUNT_PER_BITE;
			}
			this.timeout = TIMEOUT;
		}
	}
	
	/**
	 * Places the prey in a given hole.
	 * 
	 * @param hn object
	 * @return true if successful, false if the hole is full
	 */
	public boolean occupy_hole(Hole h) {
		if (h == null || this.hole_occupying != null)
			return false;
		
		if (h.occupy()) {
			this.hole_occupying = h;
			Point2D h_loc = h.getLocation();
			this.loc = new Point2D.Double(h_loc.getX(), h_loc.getY());
			return true;
		}
		return false;
	}
	
	/**
	 * Removes the prey from its current hole.
	 * 
	 * @return true if successful, false if the prey is not in a hole
	 */
	public boolean leave_hole() {
		if (this.hole_occupying == null)
			return false;
		
		this.hole_occupying.leave();
		this.hole_occupying = null;
		return true;
	}
	
	/**
	 * Returns true if the prey is currently in a hole.
	 * 
	 * @return
	 */
	public boolean in_hole() {
		return this.hole_occupying != null;
	}
	
	/**
	 * Returns the factor multiplied by the activation of a prey's sensors for prey occupying this hole.
	 * If the hole is 0.4 or deeper, the predator cannot detect any prey in this hole, and the
	 * detectable depth factor is 0. If the prey is not in a hole, the factor is 1.
	 * 
	 * @return factor
	 */
	public double getDetectableDepth() {
		if (this.hole_occupying != null)
			return this.hole_occupying.getDetectableDepth();
		else
			return 1.0;
	}
	
	/**
	 * Returns the location of the prey's left sensor.
	 * 
	 * @return 2D point
	 */
	public Point2D getLeftSensorLoc() {
		Double sx = LENGTH / 2.0, sy =  - WIDTH / 2.0, srx, sry;
		srx = sx * Math.cos(this.rot) - sy * Math.sin(this.rot);
		sry = sx * Math.sin(this.rot) + sy * Math.cos(this.rot);
		return new Point2D.Double(srx + this.loc.getX(), sry + this.loc.getY());
	}
	
	/**
	 * Returns the location of the prey's right sensor.
	 * 
	 * @return 2D point
	 */
	public Point2D getRightSensorLoc() {
		Double sx = LENGTH / 2.0, sy = WIDTH / 2.0, srx, sry;
		srx = sx * Math.cos(this.rot) - sy * Math.sin(this.rot);
		sry = sx * Math.sin(this.rot) + sy * Math.cos(this.rot);
		return new Point2D.Double(srx + this.loc.getX(), sry + this.loc.getY());
	}
	
	/**
	 * Updates the neural network of the predator. Each action incurs additional
	 * energy consumption.
	 */
	public void update() {
		if (this.hidden)
			return;
		
		if (this.energy == 0.0) {
			if (this.timeout > 0)
				this.timeout--;
			return;
		}
		
		// Natural energy decrease
		this.energy_consumption = 0.1;
		this.controller.setNeuronValue("energy", this.energy / ENERGY_MAX);
		this.controller.setNeuronValue("hunger", 1.0 - this.energy / ENERGY_MAX);
		
		// Hear Shout (Decay slowly)
		double hear_shout = this.controller.getNeuronValue("hearshout");
		if (this.last_hear_shout - 0.005 > hear_shout) {
			this.last_hear_shout -= 0.005;
			this.controller.setNeuronValue("hearshout", this.last_hear_shout);
		}
		else
			this.last_hear_shout = hear_shout;

		// Invoke neural network to update values
		this.controller.update();
		
		// Movement
		if (this.hole_occupying == null) {
			double l_vel = this.controller.getNeuronValue("turnleft");
			double r_vel = this.controller.getNeuronValue("turnright");
			double speed = Math.max(l_vel, r_vel) * SPEED_BASE;
			setRot(Math.toDegrees(this.rot) + (r_vel - l_vel) * ROTATE_BASE);
			
			this.loc.setLocation(this.loc.getX() + Math.cos(this.rot) * speed,
								 this.loc.getY() + Math.sin(this.rot) * speed);
			
			this.energy_consumption += Math.max(l_vel, r_vel) / 8.0; // speed cost
			this.energy_consumption += Math.abs(r_vel - l_vel); // rotation cost (e.g. spinning in place)
		}
		else
			this.energy_consumption += 0.1;
		
		// Hole
		if (this.hole_occupying != null)
			this.controller.setNeuronValue("holedepth", this.hole_occupying.getDepth());
		
		// Digging
		double dig_depth = this.controller.getNeuronValue("dig") - 0.55;
		if (dig_depth > 0.0 && this.hole_occupying != null) {
			this.hole_occupying.dig(dig_depth);
			this.energy_consumption += dig_depth * 1.5; //1.5;
		}

		// Eating
		if (this.controller.getNeuronValue("eat") == 1.0)
			this.energy_consumption += 0.1;
		
		// Shouting
		double shout = this.controller.getNeuronValue("shout") - 0.5;
		if (shout > 0.0) {
			this.shout_epochs++;
			this.energy_consumption += shout * (shout_epochs / 30.0);
			//this.energy_consumption = shout * 5.0;
		}
		else
			this.shout_epochs = 0;
		
		
		this.energy -= this.energy_consumption;
		if (this.energy < 0.0)
			this.energy = 0.0;
	}
	
	/**
	 * Draws the prey object.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		if (this.hidden)
			return;
		
		// Debug: display the shout radius
		DebugState debug = env.getDebugState();
		double shout_value = this.controller.getNeuronValue("shout");
		if (isAlive() && (debug == DebugState.All || debug == DebugState.Shout || selected)) {
			if (shout_value > 0.5) {
				double r = env.PREY_SHOUT_MIN_RADIUS + (shout_value - 0.5) * (env.PREY_SHOUT_MAX_RADIUS - env.PREY_SHOUT_MIN_RADIUS);
				AffineTransform old_d = g2d.getTransform();
				
				g2d.translate(this.loc.getX() - r, this.loc.getY() - r);
				
				if (selected)
					g2d.setColor(Color.WHITE);
				else
					g2d.setColor(Color.RED);
				g2d.drawOval(0, 0, (int)r * 2, (int)r * 2);
				
				g2d.setTransform(old_d);
			}
		}
		
		AffineTransform old = g2d.getTransform();
		
		g2d.translate(this.loc.getX(), this.loc.getY());
		g2d.rotate(this.rot);
		g2d.translate(-(LENGTH / 2), -(WIDTH / 2));
		
		// Body
		if (this.isAlive())
			if (!this.selected)
				g2d.setColor(COLOR);
			else
				g2d.setColor(COLOR_SELECTED);
		else
			if (!this.selected)
				g2d.setColor(COLOR_DEAD);
			else
				g2d.setColor(COLOR_DEAD_SELECTED);
		g2d.fillRect(0, 0, LENGTH, WIDTH);
		
		// Head (colored red if shouting)
		if (this.isAlive())
			if (shout_value > 0.5)
				g2d.setColor(COLOR_HEAD_SHOUT);
			else
				g2d.setColor(COLOR_HEAD);
		else
			g2d.setColor(COLOR_DEAD_HEAD);
		g2d.fillRect(LENGTH, WIDTH/2 - 3, 6, 6);

		g2d.setTransform(old);
	}
}
