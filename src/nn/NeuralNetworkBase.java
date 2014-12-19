package nn;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

/**
 * Class for a neural network base. The base is used to construct neural network instances
 * for newly spawned animats. The architecture of the neural network base is parsed from
 * an input file.
 * 
 * @author Justin Morgan
 *
 */
public class NeuralNetworkBase {
	public Vector<NeuronBase> neurons;
	public Vector<ConnectionBase> connections;
	public Vector<AxoConnectionBase> axoconnections;
	
	/**
	 * Skeleton for a neuron instance.
	 * 
	 * @author Justin Morgan
	 *
	 */
	public class NeuronBase {
		String name;
		NeuronType type;
		ActivationType activation;
		Double default_value;
		
		public Vector<Integer> input_conn; // this.connections
		public Vector<Integer> output_conn; // this.connections
		public Vector<Integer> output_axo_conn; // this.axoconnections
		
		public NeuronBase(String name, NeuronType type, ActivationType activation, Double default_value) {
			this.name = name;
			this.type = type;
			this.activation = activation;
			this.default_value = default_value;
			
			this.input_conn = new Vector<Integer>();
			this.output_conn = new Vector<Integer>();
			this.output_axo_conn = new Vector<Integer>();
		}
	}
	
	/**
	 * Skeleton for a connection instance.
	 * 
	 * @author Justin Morgan
	 *
	 */
	public class ConnectionBase {
		public String name;
		public Integer from; // this.neurons
		public Integer to; // this.neurons
		public Integer link = null; // this.connections
		public Boolean learnable;
		
		public Double min_weight;
		public Double max_weight;
		public Double default_weight;
		
		public Vector<Integer> axo_connections;
		
		public ConnectionBase(String name, Integer from, Integer to, Boolean learnable, Double min_weight, Double max_weight, Double default_weight) {
			this.name = name;
			this.from = from;
			this.to = to;
			this.learnable = learnable;
			
			this.min_weight = min_weight;
			this.max_weight = max_weight;
			this.default_weight = default_weight;

			this.axo_connections = new Vector<Integer>();
		}
		
		public void setLink(Integer link) {
			this.link = link;
		}
	}
	
	/**
	 * Skeleton for an axoaxonal connection.
	 * 
	 * @author Justin Morgan
	 *
	 */
	public class AxoConnectionBase {
		String name;
		public Integer from; // this.neurons
		public Integer to; // this.connections
		public Integer link = null; // this.axoconnections
		
		public Double min_weight, max_weight;
		
		public AxoConnectionBase(String name, Integer from, Integer to, Double min_weight, Double max_weight) {
			this.name = name;
			this.from = from;
			this.to = to;
			
			this.min_weight = min_weight;
			this.max_weight = max_weight;
		}
		
		public void setLink(Integer link) {
			this.link = link;
		}
	}
	
	/**
	 * Constructs a new neural network base.
	 */
	public NeuralNetworkBase() {
		this.neurons = new Vector<NeuronBase>();
		this.connections = new Vector<ConnectionBase>();
		this.axoconnections = new Vector<AxoConnectionBase>();
	}
	
	/**
	 * Parses a neural network base from a text file.
	 * 
	 * @param nnFile input file
	 * @throws IOException
	 * @throws ParseException
	 */
	public void parseNetworkFromFile(BufferedReader nnFile) throws IOException, ParseException {
		String line;
		int line_count = 0;
		while ((line = nnFile.readLine()) != null) {
			line_count++;
			String[] parts = line.split(" ");
			if (parts[0].equals(""))
				continue;
			
			if (parts[0].startsWith("//")) // Comment
				continue;
			
			else if (parts[0].equals("n")) { // Neuron
				if (parts.length == 3 || parts.length == 4) {
					ActivationType activation = parts[2].equals("1") ? ActivationType.STEP : ActivationType.SIGMOID;
					NeuronBase n;
					if (parts.length == 3)
						n = new NeuronBase(parts[1], NeuronType.NORMAL, activation, null);
					else
						n = new NeuronBase(parts[1], NeuronType.NORMAL, activation, Double.parseDouble(parts[3]));
					this.neurons.add(n);
				}
				else
					throw new ParseException("Bad neuron definition", line_count);
			}
			
			else if (parts[0].equals("s")) { // Neuron (Sensor)
				if (parts.length == 3 || parts.length == 4) {
					ActivationType activation = parts[2].equals("1") ? ActivationType.STEP : ActivationType.SIGMOID;
					NeuronBase n;
					if (parts.length == 3)
						n = new NeuronBase(parts[1], NeuronType.SENSOR, activation, null);
					else
						n = new NeuronBase(parts[1], NeuronType.SENSOR, activation, Double.parseDouble(parts[3]));
					this.neurons.add(n);
				}
				else
					throw new ParseException("Bad sensor definition", line_count);
			}
			
			else if (parts[0].equals("e")) { // Neuron (Effector)
				if (parts.length == 3 || parts.length == 4) {
					ActivationType activation = parts[2].equals("1") ? ActivationType.STEP : ActivationType.SIGMOID;
					NeuronBase n;
					if (parts.length == 3)
						n = new NeuronBase(parts[1], NeuronType.EFFECTOR, activation, null);
					else
						n = new NeuronBase(parts[1], NeuronType.EFFECTOR, activation, Double.parseDouble(parts[3]));
					this.neurons.add(n);
				}
				else
					throw new ParseException("Bad effector definition", line_count);
			}
			
			else if (parts[0].equals("c")) { // Connection
				if (parts.length == 7 || parts.length == 8) {
					Integer from_index = null, to_index = null;
					for (int i = 0; i < this.neurons.size(); i++) {
						NeuronBase n = this.neurons.elementAt(i);
						if (n.name.equals(parts[2]))
							from_index = i;
						else if (n.name.equals(parts[3]))
							to_index = i;
					}
					if (from_index == null || to_index == null)
						throw new ParseException("Neuron name not found", line_count);
					
					boolean learnable = parts[4].equals("1") ? true : false;
					ConnectionBase c;
					if (parts.length == 8)
						c = new ConnectionBase(parts[1], from_index, to_index, learnable, Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), Double.parseDouble(parts[7]));
					else
						c = new ConnectionBase(parts[1], from_index, to_index, learnable, Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), null);
					int i = this.connections.size();
					this.connections.add(c);
					this.neurons.elementAt(from_index).input_conn.add(i);
					this.neurons.elementAt(to_index).output_conn.add(i);
				}
				else
					throw new ParseException("Bad connection definition", line_count);
			}
			
			else if (parts[0].equals("x")) { // Axoaxonal Connection
				if (parts.length != 6)
					throw new ParseException("Bad axoaxonal connection definition", line_count);
				
				Integer n_index = null;
				for (int i = 0; i < this.neurons.size(); i++) {
					NeuronBase n = this.neurons.elementAt(i);
					if (n.name.equals(parts[2]))
						n_index = i;
				}
				if (n_index == null)
					throw new ParseException("Neuron name not found", line_count);
				
				Integer c_index = null;
				for (int i = 0; i < this.connections.size(); i++) {
					ConnectionBase c = this.connections.elementAt(i);
					if (c.name.equals(parts[3]))
						c_index = i;
				}
				if (c_index == null)
					throw new ParseException("Connection name not found", line_count);
				
				AxoConnectionBase x = new AxoConnectionBase(parts[1], n_index, c_index, Double.parseDouble(parts[4]), Double.parseDouble(parts[5]));
				int i = this.axoconnections.size();
				this.axoconnections.add(x);
				this.neurons.elementAt(n_index).output_axo_conn.add(i);
				this.connections.elementAt(c_index).axo_connections.add(i);
			}
			
			else if (parts[0].equals("l")) { // Connection Link
				if (parts.length != 3)
					throw new ParseException("Bad connection link definition", line_count);
				
				Integer from = null, to = null;
				for (int i = 0; i < this.connections.size(); i++) {
					ConnectionBase c = this.connections.elementAt(i);
					if (c.name.equals(parts[1]))
						from = i;
					else if (c.name.equals(parts[2]))
						to = i;
				}
				if (from == null || to == null)
					throw new ParseException("Connection name not found", line_count);
				
				this.connections.elementAt(from).setLink(to);
				this.connections.elementAt(to).setLink(from);
			}
			
			else if (parts[0].equals("k")) { // Axoaxonal Connection Link
				if (parts.length != 3)
					throw new ParseException("Bad axoaxonal connection link definition", line_count);
				
				Integer from = null, to = null;
				for (int i = 0; i < this.axoconnections.size(); i++) {
					AxoConnectionBase x = this.axoconnections.elementAt(i);
					if (x.name.equals(parts[1]))
						from = i;
					else if (x.name.equals(parts[2]))
						to = i;
				}
				if (from == null || to == null)
					throw new ParseException("Axoaxonal connection name not found", line_count);
				
				this.axoconnections.elementAt(from).setLink(to);
				this.axoconnections.elementAt(to).setLink(from);
			}
			
			else
				throw new ParseException("Unexpected component type", line_count);
		}
	}
}
