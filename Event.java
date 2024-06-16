
public class Event {

	public static enum Type { JUNIOR, SENIOR, TRAINEE };

	private static int nextId = 1;
	
  private final Type type;
	private final int id;
  private final int eventGroupId;
  private final int size;
  private final Interviewer interviewer;
	private final Job job;
  private final StudentGroup studentGroup;

	public Event(Type t, int size, Interviewer l, Job c, StudentGroup s,
                                                      int eventGroupId) {
    this.type = t;
    this.size = size;
    this.interviewer = l;
		this.job = c;
    this.studentGroup = s;
    id = nextId++;
    this.eventGroupId = eventGroupId;
	}

 	public int getId() {
		return id;
	}

  public int getEventGroupId() {
    return eventGroupId;
  }

  public int getSize() {
    return size;
  }

	public Job getJob() {
		return job;
	}

  public Interviewer getInterviewer() {
    return interviewer;
  }

	public Type getType() {
		return type;
	}
  
  public StudentGroup getStudentGroup() {
    return studentGroup;
  }
  
	public static void resetId() {
	  nextId = 1;
	}
	
	public static Type generateType(int i) {
		switch (i) {
			case 0:
				return Type.JUNIOR;
			case 1:
				return Type.SENIOR;
			case 2:
				return Type.TRAINEE;
			default:
				break;
		}
		return null;
	}
}