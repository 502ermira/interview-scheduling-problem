import java.util.List;
import java.util.ArrayList;

/*
 * Job containing ID and set of events
 */
public class Job {
  private static int nextId = 0;

	private String name;
	private int id;
	private int nJunior;
	private int nSenior;
	private int nTrainee;

	/*
	 * Job class constructor
	 * @param id Job ID
	 */
	public Job(String name, int nJunior, int nSenior, int nTrainee) {
		this.name = name;
		this.nJunior = nJunior;
		this.nSenior =nSenior;
		this.nTrainee = nTrainee;
		id = nextId++;		
	}

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

	public int getnJunior() {
		return nJunior;
	}

	public int getnSenior() {
		return nSenior;
	}

	public int getnTrainee() {
		return nTrainee;
	}
	
	public void resetId() {
	  nextId = 0;
	}
}