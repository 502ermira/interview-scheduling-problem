import java.util.List;
import java.util.ArrayList;

public class Interviewer {
  private static int nextId = 0;

  private String name;
  private int id;
	private List<Job> jobs;

  public Interviewer(String name) {
		this.name = name;
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

	public List<Job> getJobs() {
		return jobs;
	}

  public boolean canInterview(Job job) {
    for (Job c : jobs) {
      if (c.getName().equals(job.getName()))
        return true;
    }

    return false;
  }
  
  public static void resetId() {
    nextId = 0;
  }
}