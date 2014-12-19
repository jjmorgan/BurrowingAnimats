package env;

import java.awt.geom.Point2D;
import java.util.Comparator;

import nn.NeuralNetwork;
import nn.NeuralNetworkBase;

/**
 * Superclass for a simulated animat. The brain controller of the animat is a
 * neural network that is updated every epoch.
 * 
 * @author Justin Morgan
 *
 */
public class Animat {
	protected final double ENERGY_MAX = 600.0;
	
	protected NeuralNetwork controller = null;
	protected Environment env;
	
	protected Point2D loc = new Point2D.Double(0, 0);
	protected double rot = 0.0;
	
	protected double energy = ENERGY_MAX;
	protected double energy_consumption = 0.0;
	protected double fitness = 0.0;
	protected double avg_energy = ENERGY_MAX;
	protected int energy_samples = 1;
	
	protected boolean selected = false;
	
	/**
	 * Constructs an animat with random neural network connection weights.
	 * 
	 * @param nnb neural network base
	 * @param env environment
	 */
	public Animat(NeuralNetworkBase nnb, Environment env) {
		if (nnb != null) {
			this.controller = new NeuralNetwork(nnb);
			this.controller.setRandomConnections();
		}
		this.env = env;
	}
	
	/**
	 * Constructs an animat with inherited neural network connection weights.
	 * 
	 * @param nnb neural network base
	 * @param parent parent animat
	 * @param child apply mutations to weights
	 * @param env environment
	 */
	public Animat(NeuralNetworkBase nnb, Animat parent, Boolean child, Environment env) {
		if (nnb != null) {
			this.controller = new NeuralNetwork(nnb);
			if (!child)
				this.controller.copyConnections(parent.controller);
			else
				this.controller.inheritConnections(parent.controller);
		}
		this.env = env;
	}
	
	/**
	 * Sets the location.
	 * 
	 * @param loc 2D point
	 */
	public void setLocation(Point2D loc) {
		this.loc = loc;
	}
	
	/**
	 * Returns the location.
	 * 
	 * @return 2d point
	 */
	public Point2D getLocation() {
		return this.loc;
	}
	
	/**
	 * 
	 * @param name neuron name
	 * @param value value
	 */
	public void setNeuronValue(String name, Double value) {
		this.controller.setNeuronValue(name, value);
	}
	
	/**
	 * Returns the value of a neuron in the neural network.
	 * 
	 * @param name neuron name
	 * @return value
	 */
	public Double getNeuronValue(String name) {
		Double value = this.controller.getNeuronValue(name);
		return value != null ? value : 0.0;
	}
	
	/**
	 * Returns true if the animat is currently selected by the Watcher.
	 * 
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Returns true if the animat is alive. 
	 * 
	 * @return
	 */
	public Boolean isAlive() {
		return this.energy > 0.0;
	}
	
	/**
	 * Restores energy to the animat.
	 * 
	 * @param energy amount
	 */
	public void giveEnergy(Double energy) {
		this.energy += energy;
		if (this.energy > ENERGY_MAX)
			this.energy = ENERGY_MAX;
	}
	
	/**
	 * Updates the fitness value of the animat.
	 * 
	 * @param epoch_progress elapsed epochs
	 */
	public void updateFitness(double epoch_progress) {
		if (this.energy > 0.0) {
			this.avg_energy = (this.energy + energy_samples * this.avg_energy) / (this.energy_samples + 1);
			this.energy_samples++;
			this.fitness = this.avg_energy * epoch_progress;
		}
	}
	
	/**
	 * Returns the fitness of the animat.
	 * 
	 * @return fitness
	 */
	public Double getFitness() {
		return this.fitness;
	}
	
	/**
	 * Returns the energy of the animat.
	 * 
	 * @return energy
	 */
	public Double getEnergy() {
		return this.energy;
	}
	
	/**
	 * Returns how much energy the animat uses in the current epoch.
	 * 
	 * @return consumption
	 */
	public Double getEnergyConsumption() {
		return this.energy_consumption;
	}
	
	/**
	 * Returns the average energy of the animat in its lifetime.
	 * 
	 * @return average energy
	 */
	public Double getAverageEnergy() {
		return this.avg_energy;
	}
	
	/**
	 * Prints the status of the animat. Used by the Watcher.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("Energy: %.4f\n", this.energy));
		result.append(String.format("Consumption: %.4f\n", this.energy_consumption));
		result.append(String.format("Fitness: %.4f\n", this.fitness));
		result.append(this.controller.toString());
		return result.toString();
	}
}

/**
 * Comparator class for sorting animats by fitness level.
 * 
 * @author Justin Morgan
 *
 */
class AnimatComparator implements Comparator<Animat> {
	@Override
	public int compare(Animat a, Animat b) {
		return b.getFitness().compareTo(a.getFitness());
	}
}