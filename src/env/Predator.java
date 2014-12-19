package env;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import env.Environment.DebugState;
import nn.NeuralNetworkBase;

/**
 * Class for a predator animat. The predator is a vehicle with a fixed brain controller, by default.
 * 
 * @author Justin Morgan
 *
 */
public class Predator extends Animat {
	protected final double ENERGY_MAX = 400.0;
	
	private final double PI_2 = 2.0 * Math.PI;
	
	private final double SPEED_BASE = 1.8;
	private final double ROTATE_BASE = 77.5;
	
	private final int WIDTH = 20;
	private final int LENGTH = 32;
	private final Color COLOR = new Color(255, 130, 70);
	private final Color COLOR_SELECTED = new Color(255, 80, 20);
	
	/**
	 * Constructs a new predator.
	 * 
	 * @param nnb neural network base
	 * @param env environment
	 */
	public Predator(NeuralNetworkBase nnb, Environment env) {
		super(nnb, env);
		this.energy = ENERGY_MAX;
	}
	
	/**
	 * Sets the rotation of the predator.
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
	 * Returns the rotation of the predator.
	 * 
	 * @return degrees
	 */
	public double getRot() {
		return Math.toDegrees(this.rot);
	}
	
	/**
	 * Returns the position of the predator's left sensor.
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
	 * Returns the position of the predator's right sensor.
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
		// Natural energy decrease
		this.energy_consumption = 0.15;
		this.controller.setNeuronValue("energy", this.energy);
		
		// Invoke neural network to update values
		this.controller.update();
		
		// Movement
		double l_vel = this.controller.getNeuronValue("turnleft");
		double r_vel = this.controller.getNeuronValue("turnright");
		double speed = Math.max(l_vel, r_vel) * SPEED_BASE;
		setRot(Math.toDegrees(this.rot) + (r_vel - l_vel) * ROTATE_BASE);
		
		this.loc.setLocation(this.loc.getX() + Math.cos(this.rot) * speed,
							 this.loc.getY() + Math.sin(this.rot) * speed);
		
		this.energy_consumption += Math.max(l_vel, r_vel) / 5.0; // speed cost
		this.energy_consumption += Math.abs(r_vel - l_vel); // rotation cost (e.g. spinning in place)
		
		this.energy -= this.energy_consumption;
		if (this.energy < 0.0)
			this.energy = 0.0;
	}
	
	/**
	 * Draws the predator object.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		// Debug: display the gradient detectable by prey
		DebugState debug = env.getDebugState();
		if (debug == DebugState.All || debug == DebugState.Gradients) {
			double r = env.PREDATOR_G_RADIUS;
			
			AffineTransform old_d = g2d.getTransform();
			
			g2d.translate(this.loc.getX() - r, this.loc.getY() - r);
			
			g2d.setColor(Color.BLACK);
			g2d.drawOval(0, 0, (int)r * 2, (int)r * 2);
			
			g2d.setTransform(old_d);
		}
		
		AffineTransform old = g2d.getTransform();

		g2d.translate(this.loc.getX(), this.loc.getY());
		g2d.rotate(this.rot);
		g2d.translate(-(LENGTH / 2), -(WIDTH / 2));
		
		if (!this.selected)
			g2d.setColor(COLOR);
		else
			g2d.setColor(COLOR_SELECTED);
		g2d.fillRect(0, 0, LENGTH, WIDTH);
		g2d.setColor(new Color(255, 255, 255));
		g2d.fillRect(LENGTH, WIDTH/2 - 3, 6, 6);

		g2d.setTransform(old);
	}
}
