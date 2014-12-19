package env;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nn.NeuralNetworkBase;

/**
 * The Environment class creates and draws the smiluation window, and handles the creation
 * and interaction of all objects in the simulation. 
 * 
 * @author Justin Morgan
 * 
 */

public class Environment extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	/* Rendering and Component Constants */
	
	private final int VIEW_WIDTH = 1400;
	private final int VIEW_HEIGHT = 1000;
	
	private final int REFRESH_RATE = 17; // ms
	
	private final int WATCHER_OFFS_X = 10;
	private final int WATCHER_OFFS_Y = VIEW_HEIGHT - 230;
	
	private final int REPORT_STATUS_OFFS_X = 20;
	private final int REPORT_STATUS_OFFS_Y = VIEW_HEIGHT - 50;
	
	private final int PREY_SELECT_RADIUS = 10;
	private final int PREDATOR_SELECT_RADIUS = 25;
	private final int HOLE_SELECT_RADIUS = 20;
	private final int FOOD_SELECT_RADIUS = 10;
	
	/* Simulation Constants */
	
	private final int GEN_EPOCH_LEN = 3600; // 1 minute
	private final int MEASURE_FITNESS_INTERVAL = 60; // every second
	
	/* Prey Constants */
	
	private final int PREY_COUNT = 30;
	private final int PREY_PARENT_MAX = 5;
	private final int PREY_CHILDREN_PER = 6;
	public final double PREY_G_RADIUS = 120.0;
	private final double PREY_C_RADIUS = 20.0;
	public final double PREY_SHOUT_MIN_RADIUS = 40.0;
	public final double PREY_SHOUT_MAX_RADIUS = 800.0;
	private final double PREY_ENERGY = 40.0;
	
	/* Predator Constants */
	
	private final int PREDATOR_MAX = 6;
	private final int PREDATOR_NEXT_EPOCH_MAX = 350;
	private final double PREDATOR_START_ANGLE = 35.0;
	public final double PREDATOR_G_RADIUS = 160.0;

	/* Food Constants */
	
	private final int FOOD_COUNT = 35;
	public final double FOOD_G_RADIUS = 75.0;
	private final double FOOD_C_RADIUS = 15.0;
	private final double FOOD_NEAREST_DISTANCE = 100.0;
	private final double FOOD_ENERGY = 60.0;
	private final boolean FOOD_RESPAWN = true;
	
	/* Hole Constants */
	
	public final double HOLE_G_RADIUS = 150.0;
	private final double HOLE_ENTER_RADIUS = 15.0;
	private final double HOLE_SEPARATION = 30.0;
	
	/* Zone Constants */
	
	private final int HABITAT_ZONE_LEFT = 350;
	private final int HABITAT_ZONE_RIGHT = 1050;
	private final int HABITAT_ZONE_TOP = 250;
	private final int HABITAT_ZONE_BOTTOM = 750;
	private final double HABITAT_ZONE_G_RADIUS = 300.0;
	private final Color FORAGING_ZONE_COLOR = new Color(60, 100, 60);
	private final Color HABITAT_ZONE_COLOR = new Color(70, 170, 70);
	
	/** 
	 * DebugState contains options for displaying gradient ranges in the window.
	 * States are toggled with the 'd' key.
	*/
	public enum DebugState {
		None,
		All,
		Gradients,
		Shout {
			@Override
			public DebugState next() {
				return values()[0];
			};
		};
		
		public DebugState next() {
			return values()[ordinal() + 1];
		}
	}
	
	/* Locals */
	
	// Controllers
	private ViewPanel viewPanel;
	private Timer timer;
	private Watcher watcher;
	private ReportWriter reportwriter;
	private boolean started = false;
	private DebugState debug = DebugState.None;
	private Random random;
	
	// Simulation
	private int epoch;
	private int generation_num;
	private int next_predator_epoch;
	private boolean pause = false;
	private boolean retry = false;
	
	// Neural Networks
	private NeuralNetworkBase prey_nn_base;
	private NeuralNetworkBase predator_nn_base;
	
	// Entities
	private Vector<Prey> prey;
	private Vector<Predator> predators;
	private Vector<Food> food;
	private Vector<Hole> holes;
	
	
	/**
	 * Constructs an Environment object and initializes the simulation window.
	 * 
	 * @param prey_nn_base neural network base for prey animats
	 * @param predator_nn_base neural network base for predator animats
	 */
	public Environment(NeuralNetworkBase prey_nn_base, NeuralNetworkBase predator_nn_base) {
		this.prey_nn_base = prey_nn_base;
		this.predator_nn_base = predator_nn_base;
		initWindow();
	}

	/**
	 * Creates the simulation window and the Watcher and ReportWriter components.
	 */
	public void initWindow() {
		setTitle("CS263C - Burrowing Animats");
		
		setSize(VIEW_WIDTH, VIEW_HEIGHT);
		this.setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		this.watcher = new Watcher(WATCHER_OFFS_X, WATCHER_OFFS_Y);
		this.reportwriter = new ReportWriter(REPORT_STATUS_OFFS_X, REPORT_STATUS_OFFS_Y);
		this.random = new Random();
		
		this.viewPanel = new ViewPanel();
		add(this.viewPanel);
		
		setVisible(true);
	}
	
	/**
	 * Starts the main loop of the simulation.
	 */
	public void start() {
		// Setup initial populations
		spawnGeneration();
		
		this.started = true;
		
		// Start main loop
		timer = new Timer();
		timer.scheduleAtFixedRate(new MainLoop(this), 0, REFRESH_RATE);
	}
	
	/**
	 * Begins a new generation. Clears any existing prey, predators, holes, and food from the environment,
	 * and spawns new prey in the habitat zone and food in the foraging zone. If called after the first
	 * generation, parents with the highest fitness level from the previous generation are chosen
	 * to reproduce.
	 */
	public void spawnGeneration() {
		// Prey
		if (this.generation_num > 0) {
			Collections.sort(this.prey, new AnimatComparator());
			Vector<Prey> children = new Vector<Prey>();
			
			for (int i = 0; i < PREY_PARENT_MAX; i++) {
				for (int j = 0; j < PREY_CHILDREN_PER; j++) {
					Prey pc = new Prey(this.prey_nn_base, this.prey.elementAt(i), true, this);
					pc.setLocation(new Point2D.Double(
							HABITAT_ZONE_LEFT + this.random.nextDouble() * (HABITAT_ZONE_RIGHT - HABITAT_ZONE_LEFT),
							HABITAT_ZONE_TOP + this.random.nextDouble() * (HABITAT_ZONE_BOTTOM - HABITAT_ZONE_TOP)
									));
					pc.setRot(this.random.nextDouble() * 360.0);
					children.add(pc);
				}
			}

			this.prey = children;
		}
		else {
			this.prey = new Vector<Prey>();
			for (int i = 0; i < PREY_COUNT; i++) {
				Prey p = new Prey(this.prey_nn_base, this);
				p.setLocation(new Point2D.Double(
						HABITAT_ZONE_LEFT + this.random.nextDouble() * (HABITAT_ZONE_RIGHT - HABITAT_ZONE_LEFT),
						HABITAT_ZONE_TOP + this.random.nextDouble() * (HABITAT_ZONE_BOTTOM - HABITAT_ZONE_TOP)
								));
				p.setRot(this.random.nextDouble() * 360.0);
				this.prey.add(p);
			}
		}
		
		// Predators (clear)
		this.predators = new Vector<Predator>();
		this.next_predator_epoch = this.random.nextInt(PREDATOR_NEXT_EPOCH_MAX - 1) + 1;
		
		// Food
		this.food = new Vector<Food>();
		for (int i = 0; i < FOOD_COUNT; i++)
			spawnOneFood();
		
		// Holes (clear)
		this.holes = new Vector<Hole>();
		
		// Update parameters
		this.generation_num++;
		this.epoch = 0;
	}
	
	/**
	 * Creates one food object in the foraging zone.
	 */
	private void spawnOneFood() {
		boolean success = false;
		while (!success) {
			double x, y;
			int region = this.random.nextInt() % 4;
			if (region == 0) {
				x = this.random.nextDouble() * VIEW_WIDTH;
				y = this.random.nextDouble() * HABITAT_ZONE_TOP;
			}
			else if (region == 1) {
				x = this.random.nextDouble() * HABITAT_ZONE_LEFT;
				y = this.random.nextDouble() * (HABITAT_ZONE_BOTTOM - HABITAT_ZONE_TOP) + HABITAT_ZONE_TOP;
			}
			else if (region == 2) {
				x = this.random.nextDouble() * (VIEW_WIDTH - HABITAT_ZONE_RIGHT) + HABITAT_ZONE_RIGHT;
				y = this.random.nextDouble() * (HABITAT_ZONE_BOTTOM - HABITAT_ZONE_TOP) + HABITAT_ZONE_TOP;
			}
			else {
				x = this.random.nextDouble() * VIEW_WIDTH;
				y = this.random.nextDouble() * (VIEW_HEIGHT - HABITAT_ZONE_BOTTOM) + HABITAT_ZONE_BOTTOM;
			}
			
			// Food must be spaced apart to discourage clustering
			success = true;
			for (Food f : this.food) {
				if (f.getLocation().distance(x, y) <= FOOD_NEAREST_DISTANCE) {
					success = false;
					break;
				}
			}
			if (!success)
				continue;
			
			this.food.add(new Food(new Point2D.Double(x, y), this));
		}
	}
	
	
	/**
	 * The main loop class for the simulation. Responsible for keeping track of
	 * elapsed epochs in the current generation and updating all objects in the environment.
	 * 
	 * @author Justin Morgan
	 *
	 */
	private class MainLoop extends TimerTask {
		private Environment env;
		
		public MainLoop(Environment env) {
			this.env = env;
		}
		
		/**
		 * Runs each time the timer expires.
		 */
		@Override
		public void run() {
			
			/* Begin Main Loop */

			if (pause) {
				viewPanel.repaint();
				return;
			}
			
			epoch++;
			if (epoch == GEN_EPOCH_LEN || retry) {
				// Update report for last generation
				reportwriter.addGeneration(generation_num, prey, holes);
				
				// Begin new generation
				spawnGeneration();
				retry = false;
				watcher.resetTarget();
			}

			for (Food f : food)
				f.update();
			
			// Update prey
			double prey_alive = 0;
			for (Prey p : prey) {
				if (!p.isAlive())
					continue;
				
				updatePreySensors(p);
				checkPreyCollision(p);
				p.update();
				
				Point2D loc = p.getLocation();
				double x = loc.getX(), y = loc.getY();
				
				// Bounds check (bounce off environment wall)
				if (x < 0 || x > VIEW_WIDTH || y < 0 || y > VIEW_HEIGHT) {
					double rot = -45.0 + random.nextDouble() * 90.0;
					if (x < 0)
						x = 0;
					if (x > VIEW_WIDTH) {
						x = VIEW_WIDTH;
						rot += 180.0;
					}
					if (y < 0) {
						y = 0;
						rot += 90.0;
					}
					if (y > VIEW_HEIGHT) {
						y = VIEW_HEIGHT;
						rot += 270.0;
					}
					p.setRot(rot);
				}
				
				if (epoch % MEASURE_FITNESS_INTERVAL == 0)
					p.updateFitness((double)epoch / GEN_EPOCH_LEN);
				
				prey_alive++;
			}
			if (prey_alive == 0)
				retry = true;

			// Update predators
			synchronized(predators) {
				for (Iterator<Predator> i = predators.iterator(); i.hasNext(); ) {
					Predator r = i.next();
					
					if (!r.isAlive()) {
						i.remove(); // Predators do not leave a corpse
						continue;
					}
					
					updatePredatorSensors(r);
					checkPredatorCollision(r);
					r.update();
					
					Point2D loc = r.getLocation();
					double x = loc.getX(), y = loc.getY();
					
					// Bounds check (bounce off environment wall)
					if (x < -PREDATOR_G_RADIUS || x > VIEW_WIDTH + PREDATOR_G_RADIUS || y < -PREDATOR_G_RADIUS || y > VIEW_HEIGHT + PREDATOR_G_RADIUS) {
						double rot = -45.0 + random.nextDouble() * 90.0;
						if (x < -PREDATOR_G_RADIUS)
							x = -PREDATOR_G_RADIUS;
						if (x > VIEW_WIDTH + PREDATOR_G_RADIUS) {
							x = VIEW_WIDTH + PREDATOR_G_RADIUS;
							rot += 180.0;
						}
						if (y < -PREDATOR_G_RADIUS) {
							y = PREDATOR_G_RADIUS;
							rot += 90.0;
						}
						if (y > VIEW_HEIGHT + PREDATOR_G_RADIUS) {
							y = VIEW_HEIGHT + PREDATOR_G_RADIUS;
							rot += 270.0;
						}
						r.setRot(rot);
					}
				}
				next_predator_epoch--;
				if (next_predator_epoch == 0) {
					if (predators.size() < PREDATOR_MAX) {
						// Spawn a new predator on the edge of the environment
						Predator r = new Predator(predator_nn_base, this.env);
						double x, y;
						double rot = -PREDATOR_START_ANGLE + random.nextDouble() * PREDATOR_START_ANGLE * 2;
						int side = random.nextInt() % 4;
						if (side == 0) {
							//x = 1;
							x = -PREDATOR_G_RADIUS;
							y = random.nextDouble() * VIEW_HEIGHT;
						}
						else if (side == 1) {
							x = random.nextDouble() * VIEW_WIDTH;
							//y = 1;
							y = -PREDATOR_G_RADIUS;
							rot += 90.0;
						}
						else if (side == 2) {
							//x = VIEW_WIDTH - 1;
							x = VIEW_WIDTH + PREDATOR_G_RADIUS;
							y = random.nextDouble() * VIEW_HEIGHT;
							rot += 180.0;
						}
						else {
							x = random.nextDouble() * VIEW_WIDTH;
							//y = VIEW_HEIGHT - 1;
							y = VIEW_HEIGHT + PREDATOR_G_RADIUS;
							rot += 270.0;
						}
						r.setLocation(new Point2D.Double(x, y));
						r.setRot(rot);
						predators.add(r);
					}
					next_predator_epoch = random.nextInt(PREDATOR_NEXT_EPOCH_MAX - 1) + 1;
				}
			}
			
			// Redraw environment
			viewPanel.repaint();
			
			/* End Main Loop */
		}
	}
	
	/**
	 * Updates sensors of a prey animat.
	 * 
	 * @param p prey object
	 */
	private void updatePreySensors(Prey p) {
		Point2D lsensor_loc = p.getLeftSensorLoc();
		Point2D rsensor_loc = p.getRightSensorLoc();
		
		// Nearest food
		double l_food_max = 0.0, r_food_max = 0.0;
		for (Food f : this.food) { 
			Point2D f_loc = f.getLocation();
			double lpower = getSensorPower(lsensor_loc, f_loc, FOOD_G_RADIUS);
			if (l_food_max < lpower)
				l_food_max = lpower;
			double rpower = getSensorPower(rsensor_loc, f_loc, FOOD_G_RADIUS);
			if (r_food_max < rpower)
				r_food_max = rpower;
		}
		p.setNeuronValue("foodleft", l_food_max);
		p.setNeuronValue("foodright", r_food_max);
		
		// Nearest predator
		double l_predator_max = 0.0, r_predator_max = 0.0;
		for (Predator r : this.predators) {
			Point2D r_loc = r.getLocation();
			double lpower = getSensorPower(lsensor_loc, r_loc, PREDATOR_G_RADIUS);
			if (l_predator_max < lpower)
				l_predator_max = lpower;
			double rpower = getSensorPower(rsensor_loc, r_loc, PREDATOR_G_RADIUS);
			if (r_predator_max < rpower)
				r_predator_max = rpower;
		}
		p.setNeuronValue("predatorleft", l_predator_max);
		p.setNeuronValue("predatorright", r_predator_max);
		
		// Other prey shouting
		double hearshout_max = 0.0;
		for (Prey p_other : this.prey) {
			if (p.equals(p_other) || !p_other.isAlive())
				continue;

			double shout_value = p_other.getNeuronValue("shout");
			if (shout_value > 0.5) {
				double hearshout = getSensorPower(p.getLocation(), p_other.getLocation(),
						PREY_SHOUT_MIN_RADIUS + (shout_value - 0.5) * (PREY_SHOUT_MAX_RADIUS - PREY_SHOUT_MIN_RADIUS));
				if (hearshout_max < hearshout)
					hearshout_max = hearshout;
			}
		}
		p.setNeuronValue("hearshout", hearshout_max);
		
		// Nearest hole
		double l_hole_max = 0.0, r_hole_max = 0.0;
		for (Hole h : this.holes) {
			if (h.getOwner() != p && !h.isPublic())
				continue;
			
			Point2D h_loc = h.getLocation();
			double lpower = getSensorPower(lsensor_loc, h_loc, HOLE_G_RADIUS);
			if (l_hole_max < lpower)
				l_hole_max = lpower;
			double rpower = getSensorPower(rsensor_loc, h_loc, HOLE_G_RADIUS);
			if (r_hole_max < rpower)
				r_hole_max = rpower;
		}
		p.setNeuronValue("holeleft", l_hole_max);
		p.setNeuronValue("holeright", r_hole_max);
		
		// Habitat zone
		if (inHabitatZone(p.getLocation())) {
			p.setNeuronValue("habitatleft", 1.0);
			p.setNeuronValue("habitatright", 1.0);
		}
		else {
			double x = p.getLocation().getX(), y = p.getLocation().getY();
			double bx = x, by = y;
			if (x < HABITAT_ZONE_LEFT)
				bx = HABITAT_ZONE_LEFT;
			else if (x > HABITAT_ZONE_RIGHT)
				bx = HABITAT_ZONE_RIGHT;
			if (y < HABITAT_ZONE_TOP)
				by = HABITAT_ZONE_TOP;
			else if (y > HABITAT_ZONE_BOTTOM)
				by = HABITAT_ZONE_BOTTOM;
			p.setNeuronValue("habitatleft", getSensorPower(lsensor_loc, new Point2D.Double(bx, by), HABITAT_ZONE_G_RADIUS));
			p.setNeuronValue("habitatright", getSensorPower(rsensor_loc, new Point2D.Double(bx, by), HABITAT_ZONE_G_RADIUS));
		}
	}
	
	/**
	 * Updates sensors of a predator animat.
	 * 
	 * @param r predator object
	 */
	private void updatePredatorSensors(Predator r) {
		Point2D lsensor_loc = r.getLeftSensorLoc();
		Point2D rsensor_loc = r.getRightSensorLoc();
		
		double l_prey_max = 0.0, r_prey_max = 0.0;
		for (Prey p : this.prey) {
			if (p.isGone())
				continue;
			
			Point2D p_loc = p.getLocation();
			double detect_depth = p.getDetectableDepth();
			double factor = 1.0;
			if (inHabitatZone(p_loc))
				factor = 2.0;
			double lpower = getSensorPower(lsensor_loc, p_loc, PREY_G_RADIUS) / factor * detect_depth;
			if (l_prey_max < lpower)
				l_prey_max = lpower;
			double rpower = getSensorPower(rsensor_loc, p_loc, PREY_G_RADIUS) / factor * detect_depth;
			if (r_prey_max < rpower)
				r_prey_max = rpower;
		}
		r.setNeuronValue("preyleft", l_prey_max);
		r.setNeuronValue("preyright", r_prey_max);
	}
	
	/**
	 * Calculates the activation of a sensor from a given source and radius of the
	 * gradient the source produces.
	 * 
	 * @param sensorloc 2D positoin of the sensor
	 * @param sourceloc 2D position of the source
	 * @param sourceradius radius of the gradient from the source
	 * @return sensor value
	 */
	private double getSensorPower(Point2D sensorloc, Point2D sourceloc, double sourceradius) {
		double d = sensorloc.distance(sourceloc) / sourceradius;
		if (d <= 1.0)
			return 1.0 - d; //Math.pow(2, 1.0 - d) - 1;
		return 0.0;
	}
	
	/**
	 * Determines if a point lies in the habitat zone.
	 * 
	 * @param loc 2D point
	 * @return
	 */
	private boolean inHabitatZone(Point2D loc) {
		double x = loc.getX(), y = loc.getY();
		return x >= HABITAT_ZONE_LEFT && x <= HABITAT_ZONE_RIGHT && y >= HABITAT_ZONE_TOP && y <= HABITAT_ZONE_BOTTOM;
	}
	
	/**
	 * Handles prey collision with other objects in the environment
	 * 
	 * @param p prey object
	 */
	private void checkPreyCollision(Prey p) {
		Point2D p_loc = p.getLocation();
		
		// Food
		boolean add = false;
		if (!p.in_hole()) {
			synchronized(food) {
				for (Iterator<Food> i = this.food.iterator(); i.hasNext(); ) {
					Food f = i.next();
					Point2D f_loc = f.getLocation();
					
					if (p_loc.distance(f_loc) <=  FOOD_C_RADIUS && p.getNeuronValue("eat") == 1.0) {
						f.consume();
						p.giveEnergy(FOOD_ENERGY);
						if (f.isGone()) {
							try {
								i.remove();
							} catch (IllegalStateException e) {
								e.printStackTrace();
								System.err.println("Food: " + food.size());
								break;
							}
							add = true;
						}
					}
				}
				if (add && FOOD_RESPAWN)
					spawnOneFood();
			}
		}
		
		// Holes
		boolean dig = p.getNeuronValue("dig") > 0.55;
		boolean enter = p.getNeuronValue("enterhole") == 1.0;
		synchronized(holes) {
			if (dig || enter) {
				if (!p.in_hole()) {
					Hole nearest = null;
					double distance_min = Double.POSITIVE_INFINITY;
					for (Hole h : this.holes) {
						if (h.getOwner() != p && !h.isPublic())
							continue;
						
						Point2D h_loc = h.getLocation();
						double distance = p_loc.distance(h_loc);
						if (distance < distance_min) {
							distance_min = distance;
							nearest = h;
						}
					}
					if (nearest != null && distance_min <= HOLE_ENTER_RADIUS)
						p.occupy_hole(nearest);
					else if (dig && distance_min >= HOLE_SEPARATION) {
						Hole h_new = new Hole(new Point2D.Double(p_loc.getX(), p_loc.getY()), p, this);
						this.holes.add(h_new);
						p.occupy_hole(h_new);
					}
				}
			}
			else if (p.in_hole())
				p.leave_hole();
		}
	}
	
	/**
	 * Handles predator collision with prey animats.
	 * 
	 * @param r predator object
	 */
	private void checkPredatorCollision(Predator r) {
		Point2D r_loc = r.getLocation();
		
		for (Iterator<Prey> i = this.prey.iterator(); i.hasNext(); ) {
			Prey p = i.next();
			if (p.isGone())
				continue;
			Point2D p_loc = p.getLocation();
			
			if (r_loc.distance(p_loc) <= PREY_C_RADIUS && p.getDetectableDepth() > 0.0) {
				p.consume();
				r.giveEnergy(PREY_ENERGY);
				if (p.isGone())
					p.hide();
			}
		}
	}
	
	
	/**
	 * Handles keyboard actions that affect the simulation display.
	 * 
	 * @author Justin Morgan
	 *
	 */
	private class KeyboardInput implements KeyListener {
		private boolean[] keysPressed;
		
		public KeyboardInput() {
			this.keysPressed = new boolean[256];
		}

		/**
		 * Called when a key is pressed
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			int c = e.getKeyCode();
			if (!this.keysPressed[c]) {
				char key = e.getKeyChar();
				
				if (key == 'd') { // Toggle Debug
					debug = debug.next();
				}
				
				else if (key == 'n') { // Skip to Next Generation
					reportwriter.addGeneration(generation_num, prey, holes);
					spawnGeneration();
					retry = false;
					watcher.resetTarget();
				}
				
				else if (key == 'p') { // Pause
					pause = !pause;
				}
				
				else if (key == 'w') { // Write Report
					reportwriter.write();
				}
				
			}
			this.keysPressed[c] = true;
		}

		/**
		 * Called when a key is released
		 */
		@Override
		public void keyReleased(KeyEvent e) {
			this.keysPressed[e.getKeyCode()] = false;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			
		}
	}
	
	
	/**
	 * Handles mouse clicks in the simulation window. Used primarily for selecting
	 * objects to be reported on screen by the Watcher.
	 * 
	 * @author Justin
	 *
	 */
	private class MouseInput implements MouseListener {
		
		/**
		 * Called when the mouse is clicked in the window.
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			Point2D m_loc = new Point2D.Double(e.getX(), e.getY());

			for (Prey p : prey) {
				Point2D p_loc = p.getLocation();
				if (p_loc.distance(m_loc) < PREY_SELECT_RADIUS && !p.isGone()) {
					watcher.setTarget(p);
					p.setSelected(true);
					return;
				}
			}
			for (Predator r : predators) {
				Point2D r_loc = r.getLocation();
				if (r_loc.distance(m_loc) < PREDATOR_SELECT_RADIUS) {
					watcher.setTarget(r);
					r.setSelected(true);
					return;
				}
			}
			for (Hole h : holes) {
				Point2D h_loc = h.getLocation();
				if (h_loc.distance(m_loc) < HOLE_SELECT_RADIUS) {
					watcher.setTarget(h);
					return;
				}
			}
			for (Food f : food) {
				Point2D f_loc = f.getLocation();
				if (f_loc.distance(m_loc) < FOOD_SELECT_RADIUS) {
					watcher.setTarget(f);
					return;
				}
			}
			
			watcher.resetTarget();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}
	}
	
	
	/**
	 * Draws the simulation window, including all objects and components.
	 * 
	 * @author Justin Morgan
	 *
	 */
	private class ViewPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ViewPanel() {
			setBackground(FORAGING_ZONE_COLOR);
			setFocusable(true);
			addKeyListener(new KeyboardInput());
			addMouseListener(new MouseInput());
		}
		
		/**
		 * Called each time the window is redrawn.
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D)g;
		
			RenderingHints rh = new RenderingHints(
		             RenderingHints.KEY_ANTIALIASING,
		             RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.setRenderingHints(rh);
			
			// ** RENDER LOGIC ** //
			
			g2d.setColor(HABITAT_ZONE_COLOR);
			g2d.fillRect(HABITAT_ZONE_LEFT, HABITAT_ZONE_TOP,
					HABITAT_ZONE_RIGHT - HABITAT_ZONE_LEFT,
					HABITAT_ZONE_BOTTOM - HABITAT_ZONE_TOP);

			int prey_alive_count = 0;
			
			if (started) {
				synchronized(holes) {
					for (Hole h : holes)
						h.draw(g2d);
				}
				
				synchronized(food) {
					for (Food f : food)
						f.draw(g2d);
				}
				
				for (Prey p : prey) {
					p.draw(g2d);
					if (p.isAlive())
						prey_alive_count++;
				}
				
				synchronized(predators) {
					for (Predator r : predators)
						r.draw(g2d);
				}
			}
			
			g2d.setColor(Color.white);
			g2d.drawString("Generation " + generation_num, 10, 20);
			g2d.drawString("Time (epochs): " + epoch, 10, 40);
			g2d.drawString("Next Predator (epochs): " + next_predator_epoch, 10, 60);
			g2d.drawString("Prey Alive: " + prey_alive_count, 10, 80);
			synchronized(holes) {
				g2d.drawString("Hole Count: " + holes.size(), 10, 100);
			}
			synchronized(food) {
				g2d.drawString("Food Count: " + food.size(), 10, 140);
			}
			synchronized(predators) {
				g2d.drawString("Predator Count: " + predators.size(), 10, 120);
			}
			
			watcher.draw(g2d);
			reportwriter.draw(g2d);
			
			// ** END RENDER LOGIC ** //
			
		}
	}
	
	/**
	 * Returns the debug state.
	 * 
	 * @return debug state
	 */
	public DebugState getDebugState() {
		return this.debug;
	}
	
}