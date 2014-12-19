import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import nn.NeuralNetworkBase;
import env.Environment;

/**
 * Main class for the simulation. Creates an environment with external
 * prey and predator neural network bases.
 * 
 * @author Justin Morgan
 *
 */
public class Main {
	public static void main(String[] args) {
		NeuralNetworkBase prey_nn_base = null, predator_nn_base = null;
		try {
			
			// Prey NN Base
			BufferedReader prey_file = new BufferedReader(new FileReader("preynn.txt"));
			prey_nn_base = new NeuralNetworkBase();
			prey_nn_base.parseNetworkFromFile(prey_file);
			prey_file.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			System.err.println("Error parsing preynn.txt: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
			System.exit(1);
		}
		
		try {
			// Predator NN Base
			BufferedReader predator_file = new BufferedReader(new FileReader("predatornn.txt"));
			predator_nn_base = new NeuralNetworkBase();
			predator_nn_base.parseNetworkFromFile(predator_file);
			predator_file.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			System.err.println("Error parsing predatornn.txt: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
			System.exit(1);
		}
		
		
		// Set up environment
		Environment env = new Environment(prey_nn_base, predator_nn_base);
		env.start();
		
		System.out.println("Environment set up successfully.");
	}
}
