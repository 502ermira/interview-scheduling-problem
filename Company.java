public class Company {
	protected static int nextId = 0;
	protected String name;
	protected int id;
	protected int capacity;
	protected Event.Type position;

	public Company(String name, int capacity, Event.Type position) {
		this.name = name;
		this.capacity = capacity;
		this.position = position;
		id = nextId++;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public Event.Type getType() {
		return position;
	}
	
	public static void resetId() {
	  nextId = 0;
	}
}