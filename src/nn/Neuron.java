package nn;

import java.util.Vector;

/**
 * Class for a neuron instance. A neuron classified as a sensor receives input from the
 * environment. An effector neuron outputs activation from other neurons in order to
 * perform a physical action. Other neurons transmit activation along connections
 * in the neural network.
 * 
 * @author Justin Morgan
 *
 */
public class Neuron {
	private String name;
	private NeuronType type;
	private ActivationType activation;
	
	private double value = 0.0;
	private boolean updated = false;
	
	private Vector<Connection> input_conn;
	private Vector<Connection> output_conn;
	private Vector<AxoConnection> output_axo_conn;
	
	/**
	 * Constructs a new neuron.
	 * 
	 * @param name name
	 * @param type neuron type
	 * @param activation activation function
	 */
	public Neuron(String name, NeuronType type, ActivationType activation) {
		this.name = name;
		this.type = type;
		this.activation = activation;
		
		this.input_conn = new Vector<Connection>();
		this.output_conn = new Vector<Connection>();
		this.output_axo_conn = new Vector<AxoConnection>();
	}
	
	/**
	 * Returns the activation of the neuron.
	 * 
	 * @return value
	 */
	public double update() {
		if (this.input_conn.size() > 0) {
			double input = 0.0;
			if (!this.updated) {
				for (Connection c : this.input_conn)
					input += c.update();
				
				if (this.activation == ActivationType.SIGMOID)
					this.value = getSigmoidActivation(input);
				else
					this.value = getStepActivation(input);

				this.updated = true;
			}
		}
		
		return this.value;
	}
	
	/**
	 * Returns the value of activation through the sigmoid function: 1 / (1 + e^(-input))
	 * 
	 * @param input
	 * @return value
	 */
	public double getSigmoidActivation(double input) {
		return 1.0 / (1.0 + Math.exp(-input));
	}
	
	/**
	 * Returns the value of activation through a step function that outputs either 0.0 or 1.0.
	 * 
	 * @param input
	 * @return value
	 */
	public double getStepActivation(double input) {
		return input >= 0.0 ? 1.0 : 0.0;
	}
	
	/**
	 * Adds a connection as input to the neuron.
	 * 
	 * @param c connection
	 */
	public void addInputConnection(Connection c) {
		this.input_conn.add(c);
	}
	
	/**
	 * Adds a connection as output from the neuron.
	 * 
	 * @param c connection
	 */
	public void addOutputConnection(Connection c) {
		this.output_conn.add(c);
	}
	
	/**
	 * Adds an axoaxonal connection as output from the neuron.
	 * 
	 * @param x axoaxonal connection
	 */
	public void addOutputAxoConnection(AxoConnection x) {
		this.output_axo_conn.add(x);
	}
	
	/**
	 * Returns the activation of the neuron.
	 * 
	 * @return value
	 */
	public double getValue() {
		return this.value;
	}
	
	/**
	 * Returns the type of the neuron.
	 * 
	 * @return type
	 */
	public NeuronType getType() {
		return this.type;
	}
	
	/**
	 * Resets the neuron in preparation for an update.
	 */
	public void resetUpdated() {
		this.updated = false;
	}
	
	/**
	 * Sets the neuron as updated.
	 */
	public void setUpdated() {
		this.updated = true;
	}
	
	/**
	 * Sets the value of the neuron.
	 * 
	 * @param value
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
	/**
	 * Returns the status of the neuron in the format: "Name: Value"
	 */
	@Override
	public String toString() {
		return this.name + " : " + Double.toString(this.value);
	}
}
