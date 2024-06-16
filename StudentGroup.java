import java.util.List;
import java.util.ArrayList;

public class StudentGroup {
	private static int nextId = 0;
	private int id;
	private String name;
	private int size;
	private List<Job> jobs;

	public StudentGroup(String name, int size) {
		this.name = name;
		this.size = size;
		id = nextId++;
		jobs = new ArrayList<Job>();
	}

	public void addJob(Job job) {
		jobs.add(job);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	public List<Job> getJobs() {
		return jobs;
	}
	
	public static void resetId() {
	  nextId = 0;
	}
}