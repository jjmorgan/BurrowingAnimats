import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.ini4j.Wini;

import nn.NeuralNetworkBase;
import env.Environment;

/**
 * Main class for the simulation. Creates an environment with prey and
 * predator neural network bases constructed from external files.
 * 
 * @author Justin Morgan
 *
 */
public class Main {
	public static void main(String[] args) {
		NeuralNetworkBase prey_nn_base = null, predator_nn_base = null;
		String prey_nn_file = null, predator_nn_file = null;
		try {
			
			// Read config.ini
			Wini config = new Wini(new File("config.ini"));
			prey_nn_file = config.get("NN", "prey_nn");
			predator_nn_file = config.get("NN", "predator_nn");
			
		} catch (IOException e) {
			System.err.println("Error parsing config.ini: " + e.getMessage());
			System.exit(1);
		}
			
		try {
			
			// Prey NN Base
			BufferedReader prey_file = new BufferedReader(new FileReader(prey_nn_file));
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
			BufferedReader predator_file = new BufferedReader(new FileReader(predator_nn_file));
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
