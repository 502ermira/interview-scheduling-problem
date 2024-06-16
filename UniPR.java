import java.util.*;

/*
* Represents all the persistent information from the input
*/
public class UniPR {
    private Map<Integer, Company> companies;
    private Map<Integer, Job> jobs;
    private Map<Integer, StudentGroup> studentGroups;
    private Map<Integer, Interviewer> interviewers;
    private Map<Integer, Event> events;
    private ArrayList<Integer> eventIds;

    public UniPR() {
        companies = new HashMap<Integer, Company>();
        jobs = new HashMap<Integer, Job>();
        studentGroups = new HashMap<Integer, StudentGroup>();
        interviewers = new HashMap<Integer, Interviewer>();
        events = new HashMap<Integer, Event>();
        eventIds = new ArrayList<Integer>();
    }

    public int addCompany(Company company) {
        companies.put(company.getId(), company);
        return company.getId();
    }

    public Map<Integer, Company> getCompanies() {
        return companies;
    }

    public int getNumCompanies() {
        return companies.size();
    }

    public int addJob(Job job) {
        jobs.put(job.getId(), job);
        return job.getId();
    }

    public Map<Integer, Job> getJobs() {
        return jobs;
    }

    public int addStudentGroup(StudentGroup studentGroup) {
        studentGroups.put(studentGroup.getId(), studentGroup);
        return studentGroup.getId();
    }

    public Map<Integer, StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public int addInterviewer(Interviewer interviewer) {
        interviewers.put(interviewer.getId(), interviewer);
        return interviewer.getId();
    }

    public Map<Integer, Interviewer> getInterviewers() {
        return interviewers;
    }

    public Event getEvent(int id) {
        return events.get(id);
    }

    public int getRandomEventId(Random rand) {
        return eventIds.get(rand.nextInt(eventIds.size()));
    }

    public Map<Integer, Event> getEvents() {
        return events;
    }

    public void createEvents() {
        // event group ids are unique
        int eventGroupID = 1;

        for (StudentGroup sg : studentGroups.values()) {
            for (Job job : sg.getJobs()) {

                // create lecture events
                for (int i = 0; i < job.getnTrainee(); i++) {
                    // find a lecturer for this course
                    // TODO: right now, only one lecturer per course, fixit!
                    List<Interviewer> possibleInterviewers = new ArrayList<Interviewer>();
                    for (Interviewer interviewer : interviewers.values()) {
                        if (interviewer.canInterview(job)) {
                            possibleInterviewers.add(interviewer);
                        }
                    }

                    // temp, just take the first possible interviewer
                    Event event = new Event(Event.Type.JUNIOR,
                        sg.getSize(),
                        possibleInterviewers.get(0),
                        job,
                        sg,
                        eventGroupID);

                    events.put(event.getId(), event);
                    eventIds.add(event.getId());

                    // update event group id
                    eventGroupID++;
                }

                // TODO: should maxsize of a subgroup be 40? to fit in the rooms
                int lessonSize = 40;

                // create lesson events
                for (int i = 0; i < job.getnSenior(); i++) {
                    int sgSize = sg.getSize();

                    // create several events with a part of this studentgroup's
                    // size until their combined size is the same as
                    // the studentgroup's size
                    while (sgSize > 0) {
                        int evSize = sgSize > lessonSize ? lessonSize : sgSize;
                        Event event = new Event(Event.Type.SENIOR,
                            evSize,
                            null, // should this be null or some default TA value?
                            job,
                            sg,
                            eventGroupID);

                        events.put(event.getId(), event);
                        eventIds.add(event.getId());
                        sgSize = sgSize - evSize;

                    }

                    // update event group id
                    eventGroupID++;
                }

                // TODO: is this size good?
                int labSize = 25;

                // create lab events
                for (int i = 0; i < job.getnTrainee(); i++) {
                    int sgSize = sg.getSize();

                    while (sgSize > 0) {
                        int evSize = sgSize > labSize ? labSize : sgSize;
                        Event event = new Event(Event.Type.TRAINEE, evSize,   null, job, sg,  eventGroupID);

                        events.put(event.getId(), event);
                        eventIds.add(event.getId());
                        sgSize = sgSize - evSize;
                    }

                    // update event group id
                    eventGroupID++;
                }
            }
        }
    }

    public void clear() {
        Company.resetId();
        Event.resetId();
        Interviewer.resetId();
        StudentGroup.resetId();
        companies.clear();
        jobs.clear();
        studentGroups.clear();
        interviewers.clear();
        events.clear();
        eventIds.clear();
    }
}