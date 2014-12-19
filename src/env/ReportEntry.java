package env;

/**
 * Entry for a report file written by the ReportWriter.
 * 
 * @author Justin Morgan
 *
 */
public class ReportEntry {
	private int generation;
	
	public double avg_fitness; // Average prey fitness
	
	public int hole_total; // Total number of holes
	
	public double avg_holes_per_prey; // Average number of holes per prey
	public double min_holes_per_prey;
	public double q1_holes_per_prey;
	public double median_holes_per_prey;
	public double q3_holes_per_prey;
	public double max_holes_per_prey;
	
	public double avg_hole_depth; // Average hole depth
	
	/**
	 * Constructs a new report entry.
	 * 
	 * @param generation generation number
	 */
	public ReportEntry(int generation) {
		this.generation = generation;
	}
	
	/**
	 * Returns the generation of the report.
	 * 
	 * @return generation
	 */
	public int getGeneration() {
		return this.generation;
	}
	
	/**
	 * Returns the report entry as a line in CSV format.
	 */
	@Override
	public String toString() {
		return String.format("%d,%.4f,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f", this.generation,
										this.avg_fitness,
										this.hole_total,
										this.avg_holes_per_prey,
										this.min_holes_per_prey,
										this.q1_holes_per_prey,
										this.median_holes_per_prey,
										this.q3_holes_per_prey,
										this.max_holes_per_prey,
										this.avg_hole_depth);
	}
}
