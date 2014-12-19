package nn;

/**
 * Class for an axoaxonal connection instance. An axoaxonal connection connects a neuron to
 * another connection and has a multiplicative effect on the activation sent by the connection
 * to its destination.
 * 
 * @author Justin Morgan
 *
 */
public class AxoConnection {
	private String name;
	private double weight = 0.0;
	
	public final double weight_min;
	public final double weight_max;
	
	private Neuron from;
	private Connection to;
	
	/**
	 * Constructs a new axoaxonal connection object.
	 * 
	 * @param name name
	 * @param from neuron source
	 * @param to connection destination
	 * @param weight_min minimum weight value
	 * @param weight_max maximum weight value
	 */
	public AxoConnection(String name, Neuron from, Connection to, Double weight_min, Double weight_max) {
		this.name = name;
		this.from = from;
		this.to = to;
		
		this.weight_min = weight_min;
		this.weight_max = weight_max;
	}
	
	/**
	 * Returns the activation contributed by this axoaxonal connection.
	 * 
	 * @return activation value
	 */
	public double update() {
		return this.from.update() * this.weight;
	}
	
	/**
	 * Returns the weight of the axoaxonal connection.
	 * 
	 * @return weight
	 */
	public double getWeight() {
		return this.weight;
	}
	
	/**
	 * Sets the weight of the axoaxonal connection.
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
	 * Returns the source neuron.
	 * 
	 * @return source neuron
	 */
	public Neuron getFromNeuron() {
		return this.from;
	}
	
	/**
	 * Returns the destination connection
	 * 
	 * @return destination connection
	 */
	public Connection getToConnection() {
		return this.to;
	}
}
