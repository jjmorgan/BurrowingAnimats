package nn;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * Class for a neural network instance. The neural network serves as the brain controller
 * and behavior module for an animat, and consists of a collection of neurons, connections,
 * and axoaxonal connections. The sensors of the neural network serve as input and are set
 * before updating the network. The result of an update is the activation of effector
 * neurons, which collectively serve as the output of the network.
 * 
 * @author Justin Morgan
 *
 */
public class NeuralNetwork {
	private final double MUTATE_MAX = 1.5;
	
	private Vector<Neuron> neurons;
	private Vector<Connection> connections;
	private Vector<AxoConnection> axoconnections;
	
	private Vector<Integer> connection_links; //*
	private Vector<Integer> axoconnection_links; //*

	private HashMap<String,Neuron> neurons_by_name;
	private HashMap<Neuron,String> names_by_neuron;
	
	Random random;
	
	/**
	 * Constructs a new instance of a neural network from a base.
	 * 
	 * @param base neural network base
	 */
	public NeuralNetwork(NeuralNetworkBase base) {
		this.neurons = new Vector<Neuron>();
		this.connections = new Vector<Connection>();
		this.axoconnections = new Vector<AxoConnection>();
		
		this.connection_links = new Vector<Integer>(); //*
		this.axoconnection_links = new Vector<Integer>(); //*
		
		this.neurons_by_name = new HashMap<String,Neuron>();
		this.names_by_neuron = new HashMap<Neuron,String>();
		
		this.random = new Random();
		
		for (NeuralNetworkBase.NeuronBase nb : base.neurons) {
			Neuron n = new Neuron(nb.name, nb.type, nb.activation);
			if (nb.default_value != null)
				n.setValue(nb.default_value);
			this.neurons.add(n);
			this.neurons_by_name.put(nb.name, n);
			this.names_by_neuron.put(n, nb.name);
		}
		
		for (NeuralNetworkBase.ConnectionBase cb : base.connections){
			Neuron from = this.neurons.elementAt(cb.from);
			Neuron to = this.neurons.elementAt(cb.to);
			
			Connection c = new Connection(cb.name, from, to, cb.learnable, cb.min_weight, cb.max_weight);
			if (cb.default_weight != null)
				c.setDefaultWeight(cb.default_weight);
			this.connections.add(c);
			from.addOutputConnection(c);
			to.addInputConnection(c);

			this.connection_links.add(cb.link);
		}
		
		for (NeuralNetworkBase.AxoConnectionBase ab : base.axoconnections) {
			Neuron from = this.neurons.elementAt(ab.from);
			Connection to = this.connections.elementAt(ab.to);
			
			AxoConnection a = new AxoConnection(ab.name, from, to, ab.min_weight, ab.max_weight);
			this.axoconnections.add(a);
			from.addOutputAxoConnection(a);
			to.addAxoConnection(a);

			this.axoconnection_links.add(ab.link);
		}
	}
	
	/**
	 * Sets the weight of a connection.
	 * 
	 * @param i index in connection list
	 * @param weight weight
	 */
	private void setConnectionWeight(Integer i, Double weight) { //*
		this.connections.elementAt(i).setWeight(weight);
		Integer link = this.connection_links.elementAt(i);
		if (link != null)
			this.connections.elementAt(link).setWeight(weight);
	}
	
	/**
	 * Sets the weight of an axoaxonal connection.
	 * 
	 * @param i index in axoaxonal connection list
	 * @param weight weight
	 */
	private void setAxoConnectionWeight(Integer i, Double weight) { //*
		this.axoconnections.elementAt(i).setWeight(weight);
		Integer link = this.axoconnection_links.elementAt(i);
		if (link != null)
			this.axoconnections.elementAt(link).setWeight(weight);
	}
	
	/**
	 * Initializes the neural network with random weights.
	 */
	public void setRandomConnections() {
		for (int i = 0; i < this.connections.size(); i++) {
			Connection c = this.connections.elementAt(i);
			if (!c.hasDefaultWeight())
				this.setConnectionWeight(i, c.weight_min + this.random.nextDouble() * (c.weight_max - c.weight_min));
		}
		for (int i = 0; i < this.axoconnections.size(); i++) {
			AxoConnection a = this.axoconnections.elementAt(i);
			this.setAxoConnectionWeight(i, a.weight_min + this.random.nextDouble() * (a.weight_max - a.weight_min));
		}
	}
	
	/**
	 * Copies and mutates the connecation and axoaxonal connection weights of a parent animat.
	 * 
	 * @param parent neural network instance of parent
	 */
	public void inheritConnections(NeuralNetwork parent) {
		for (int i = 0; i < this.connections.size(); i++) {
			Connection c = this.connections.elementAt(i);
			double weight_parent = parent.connections.elementAt(i).getWeight();
			double min = -1 * Math.min(weight_parent - c.weight_min, MUTATE_MAX);
			double max = Math.min(c.weight_max - weight_parent, MUTATE_MAX);
			double weight_change = min + this.random.nextDouble() * (max - min);
			if ((weight_parent <= c.weight_min && weight_change < 0)
					|| (weight_parent >= c.weight_max && weight_change > 0))
				weight_change *= -1.0;
			this.setConnectionWeight(i, weight_parent + weight_change);
		}
		
		for (int i = 0; i < this.axoconnections.size(); i++) {
			AxoConnection a = this.axoconnections.elementAt(i);
			double weight_parent = parent.axoconnections.elementAt(i).getWeight();
			double weight_change = (-MUTATE_MAX + this.random.nextDouble() * (2 * MUTATE_MAX));
			if ((weight_parent <= a.weight_min && weight_change < 0)
					|| (weight_parent >= a.weight_max && weight_change > 0))
				weight_change *= -1.0;
			this.setAxoConnectionWeight(i, weight_parent + weight_change);
		}
	}
	
	/**
	 * Copies and mutates the connecation and axoaxonal connection weights of a parent animat.
	 * 
	 * @param parent neural network instance of parent
	 */
	public void copyConnections(NeuralNetwork parent) {
		for (int i = 0; i < this.connections.size(); i++)
			this.setConnectionWeight(i, parent.connections.elementAt(i).getWeight());
		
		for (int i = 0; i < this.axoconnections.size(); i++)
			this.setAxoConnectionWeight(i, parent.axoconnections.elementAt(i).getWeight());
	}
	
	/**
	 * Updates the activation of all neurons in the neural network.
	 */
	public void update() {
		for (Neuron n : this.neurons)
			n.resetUpdated();
		
		for (Neuron n : this.neurons) {
			if (n.getType() == NeuronType.EFFECTOR)
				n.update();
		}
		
		for (Connection c : this.connections)
			c.applyHebbianRule();
	}
	
	/**
	 * Sets the activation of a neuron.
	 * 
	 * @param name neuron name
	 * @param value value
	 */
	public void setNeuronValue(String name, Double value) {
		Neuron n = this.neurons_by_name.get(name);
		if (n != null)
			n.setValue(value);
	}
	
	/**
	 * Returns the activation of a neuron.
	 * 
	 * @param name neuron name
	 * @return value
	 */
	public Double getNeuronValue(String name) {
		Neuron n = this.neurons_by_name.get(name);
		if (n != null)
			return n.getValue();
		return 0.0; //TODO: return null?
	}
	
	/**
	 * Returns the status of all neurons, connections, and axoaxonal connections as a string block.
	 * Used primarily by the Watcher.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Neuron n : this.neurons) {
			result.append(String.format(this.names_by_neuron.get(n) + " = %.4f\n", n.getValue()));
		}
		for (Connection c : this.connections) {
			String from = this.names_by_neuron.get(c.getFromNeuron());
			String to = this.names_by_neuron.get(c.getToNeuron());
			result.append(String.format(from + "->" + to + " = %.4f\n", c.getWeight()));
		}
		for (AxoConnection a : this.axoconnections){
			String from = this.names_by_neuron.get(a.getFromNeuron());
			String c_from = this.names_by_neuron.get(a.getToConnection().getFromNeuron());
			String c_to = this.names_by_neuron.get(a.getToConnection().getToNeuron());
			result.append(String.format(from + "->(" + c_from + "->" + c_to + ") = %.4f\n", a.getWeight()));
		}
		return result.toString();
	}
}
