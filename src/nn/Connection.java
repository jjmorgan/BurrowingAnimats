package nn;

import java.util.Vector;

/**
 * Class for a connection instance. A connection sends activation calculated as (activation of source * weight)
 * to a destination neuron.
 * 
 * @author Justin Morgan
 *
 */
public class Connection {
	private final double LEARNING_RATE = 0.005; //0.001;
	
	private String name;
	private Double weight = 0.0;
	private boolean learnable;
	private boolean has_default_weight = false;
	
	public final Double weight_min;
	public final Double weight_max;
	
	private Neuron from;
	private Neuron to;
	
	private Vector<AxoConnection> axo_connections;
	
	/**
	 * Creates a new connection object.
	 * 
	 * @param name name
	 * @param from source neuron
	 * @param to destination neuron
	 * @param learnable true if connection can change due to to Hebb's rule
	 * @param weight_min minimum weight value
	 * @param weight_max maximum weight value
	 */
	public Connection(String name, Neuron from, Neuron to, Boolean learnable, Double weight_min, Double weight_max) {
		this.name = name;
		this.from = from;
		this.to = to;
		this.learnable = learnable;
		
		this.weight_min = weight_min;
		this.weight_max = weight_max;
		
		this.axo_connections = new Vector<AxoConnection>();
	}
	
	/**
	 * Adds an input axoaxonal connection.
	 * 
	 * @param x axoaxonal connection
	 */
	public void addAxoConnection(AxoConnection x) {
		this.axo_connections.add(x);
	}
	
	/**
	 * Returns the weight of the connection.
	 * 
	 * @return weight
	 */
	public double getWeight() {
		return this.weight;
	}
	
	/**
	 * Sets the weight of the connection.
	 * 
	 * @param weight new weight
	 */
	public void setWeight(Double weight) {
		if (weight < this.weight_min)
			weight = this.weight_min;
		if (weight > this.weight_max)
			weight = this.weight_max;
		this.weight = weight;
	}
	
	/**
	 * Sets the default weight of the connection.
	 * 
	 * @param weight default weight
	 */
	public void setDefaultWeight(Double weight) {
		this.weight = weight;
		this.has_default_weight = true;
	}
	
	/**
	 * Returns true if the connection has a default weight.
	 * 
	 * @return
	 */
	public Boolean hasDefaultWeight() {
		return this.has_default_weight;
	}
	
	/**
	 * Returns the source neuron.
	 * 
	 * @return source neuron
	 */
	public Neuron getFromNeuron() {
		return this.from;
	}
	
	/**
	 * Returns the destination neuron.
	 * 
	 * @return destination neuron
	 */
	public Neuron getToNeuron() {
		return this.to;
	}
	
	/**
	 * Returns the activation contributed by this connection.
	 * 
	 * @return activation value
	 */
	public double update() {
		double value = this.from.update() * this.weight;
		for (AxoConnection a : this.axo_connections)
			value *= a.update();
		
		return value;
	}
	
	/**
	 * Updates the connection weight according to Hebb's rule.
	 * The update function is given by:
	 * 
	 * delta w = (learning rate) * (source activation) * (destination value)
	 */
	public void applyHebbianRule() {
		if (!this.learnable)
			return;
		
		double in_value = this.from.getValue();
		double out_value = this.to.getValue();
		
		double new_weight = in_value * out_value * LEARNING_RATE;
		this.weight += new_weight;
		if (this.weight > 5.0)
			this.weight = 5.0;
		if (this.weight  < -5.0)
			this.weight = -5.0;
	}
}
