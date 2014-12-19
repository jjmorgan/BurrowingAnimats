package env;

import java.awt.Graphics2D;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

/**
 * Component that writes a complete report of several parameters for each elasped generation
 * in the simulation. Reports are saved to the same directory as the executable with
 * the filename format:
 * 
 * report-MM.dd.yy_hh.mm.ss.csv
 * 
 * @author Justin Morgan
 *
 */
public class ReportWriter {
	private Vector<ReportEntry> entries = new Vector<ReportEntry>();
	
	private final int offs_x, offs_y;
	private String statusmsg = null;
	private int showstatus = 0;
	
	/**
	 * Constructs a new ReportWriter.
	 * 
	 * @param offs_x x position of status message
	 * @param offs_y y position of status message
	 */
	public ReportWriter(int offs_x, int offs_y) {
		this.offs_x = offs_x;
		this.offs_y = offs_y;
	}
	
	/**
	 * Creates a new report entry for the current generation.
	 * 
	 * @param generation_num generation number
	 * @param prey set of prey animats
	 * @param holes set of hole objects
	 */
	public void addGeneration(int generation_num, Vector<Prey> prey, Vector<Hole> holes) {
		ReportEntry entry = new ReportEntry(generation_num);
		
		// Average prey fitness
		double fitness_total = 0.0;
		for (Prey p : prey) {
			fitness_total += p.getFitness();
		}
		entry.avg_fitness = fitness_total / prey.size();
		
		// Total number of holes
		entry.hole_total = holes.size();
		
		// Average number of holes per prey
		int prey_count = prey.size();
		int[] prey_hole_count = new int[prey_count];
		for (int i = 0; i < prey_count; i++)
			prey_hole_count[i] = 0;
		for (int i = 0; i < holes.size(); i++) {
			Prey h_owner = holes.elementAt(i).getOwner();
			int ph_index = prey.indexOf(h_owner);
			prey_hole_count[ph_index]++;
		}
		double avg_holes_per_prey = 0.0;
		for (int i = 0; i < prey_count; i++)
			avg_holes_per_prey = (prey_hole_count[i] + i * avg_holes_per_prey) / (i + 1);
		entry.avg_holes_per_prey = avg_holes_per_prey;
		
		// Min, Q1, Median, Q2, Max of holes per prey
		Arrays.sort(prey_hole_count);
		entry.min_holes_per_prey = prey_hole_count[0];
		double q1_index = prey_count / 4.0;
		if ((q1_index % 0) == 0)
			entry.q1_holes_per_prey = prey_hole_count[(int)q1_index];
		else
			entry.q1_holes_per_prey = (prey_hole_count[(int)Math.floor(q1_index)] + prey_hole_count[(int)Math.ceil(q1_index)]) / 2.0;
		double median_index = prey_count / 2.0;
		if ((median_index % 0) == 0)
			entry.median_holes_per_prey = prey_hole_count[(int)median_index];
		else
			entry.median_holes_per_prey = (prey_hole_count[(int)Math.floor(median_index)] + prey_hole_count[(int)Math.ceil(median_index)]) / 2.0;
		double q3_index = prey_count * 3.0 / 4.0;
		if ((q3_index % 0) == 0)
			entry.q3_holes_per_prey = prey_hole_count[(int)q3_index];
		else
			entry.q3_holes_per_prey = (prey_hole_count[(int)Math.floor(q3_index)] + prey_hole_count[(int)Math.ceil(q3_index)]) / 2.0;
		entry.max_holes_per_prey = prey_hole_count[prey_count-1];
		
		// Average hole depth
		double avg_hole_depth = 0.0;
		for (int i = 0; i < holes.size(); i++)
			avg_hole_depth = (holes.elementAt(i).getDepth() + i * avg_hole_depth) / (i + 1);
		entry.avg_hole_depth = avg_hole_depth;
		
		this.entries.add(entry);
	}
	
	/**
	 * Returns the current timestamp as a string object.
	 * 
	 * @return timestamp
	 */
	private String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yy_hh.mm.ss");
		return sdf.format(new Date());
	}
	
	/**
	 * Writes the full report to a new file. If successful, sets a status message that
	 * displays for a short amount of time in the simulation window.
	 */
	public void write() {
		try {
			String timestamp = getTimestamp();
			String filename = "report-" + timestamp + ".csv";
			FileWriter file_out = new FileWriter(filename);
			
			file_out.write("Generation,Avg Fitness,Hole Total,Avg Holes Per Prey,"
					+ "Min Holes Per Prey,Q1 Holes Per Prey,Median Holes Per Prey,"
					+ "Q3 Holes Per Prey,Max Holes Per Prey,Avg Hole Depth\n");
			
			for (ReportEntry entry : this.entries)
				file_out.write(entry.toString() + "\n");
			
			file_out.close();
			
			this.statusmsg = "Wrote report successfully: " + filename;
			this.showstatus = 180;
		} catch (IOException e) {
			System.err.println("Error writing report: " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Draws the status message.
	 * 
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		if (showstatus > 0) {
			g2d.drawString(statusmsg, this.offs_x, this.offs_y);
			showstatus--;
		}
	}
}
