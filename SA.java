import java.util.*;
import java.io.*;
import java.text.*;

/**
* Performs the Simulated Annealing(SA) on the KTH data set.
*/
public class SA implements Algorithm {

    private UniPR unipr;
    private double START_TEMPERATURE = 100;
    private double FINAL_TEMPERATURE = 0.7;
    private double HEATING = 0.9995;
    private int runLaps = 0;
    private int bestSol = Integer.MIN_VALUE;
    private int DESIRED_FITNESS = 0;
    private double temp;

    public SA() {
        unipr = new UniPR();
    }

    /**
     * Returns a schedule based on the given constraints
     */
    public TimeTable generateTimeTable() {
        // create the initial random population
        TimeTable currentSolution = createTrivialSolution();
        TimeTable bestSolution = new TimeTable(currentSolution);
        bestSolution.getFitness(unipr);
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        
        temp = START_TEMPERATURE;
        runLaps = 0;
        long startTime = System.currentTimeMillis();
        while (temp > FINAL_TEMPERATURE && bestSol < DESIRED_FITNESS) {
            runLaps++;
            TimeTable newSolution = changeSomething(currentSolution);

           
            int currentVal = currentSolution.getFitness(unipr);
            int newVal = newSolution.getFitness(unipr);
            double accept = accept(currentVal, newVal, temp);
            //System.out.println("Temp: " + numberFormat.format(temp) + ",\tNuvarande: " + currentVal + ",\tNy: " + newVal + ",\tAccept: " + accept);
           
            if (accept > Math.random()) {
                
                currentSolution = new TimeTable(newSolution);
            } else {
             
            }

            
            if (currentSolution.getFitness(true) > bestSolution.getFitness(true)) {
                bestSolution = new TimeTable(currentSolution);
                bestSol = currentSolution.getFitness(true);
            }
            //System.out.println("#TIME: " + numberFormat.format((System.currentTimeMillis()-startTime)/1000.0) + ", ITERATION: " + runLaps + " FITNESS: " + bestSolution.getFitness(true) + " TEMP: " + numberFormat.format(temp));
            System.out.println(numberFormat.format((System.currentTimeMillis()-startTime)/1000.0) + ";" + runLaps + ";" + bestSolution.getFitness(true) + ";" + numberFormat.format(temp));

            // Make it cold and fuzzy
            temp *= HEATING;
        }

        return bestSolution;
    }

   
    public static double accept(int value, int newValue, double temperature) {
       
        if (newValue >= value) {
            return 1.0;
        }
        double delta = newValue - value;
        
        double val = Math.exp(50 * delta / temperature);
        return val;
    }

    public TimeTable changeSomething(TimeTable currentSolution) {
       
        TimeTable newSolution = new TimeTable(currentSolution);
        int temp1 = 0; 
        int temp2 = 0;
        int rtt1timeslot = 0, rtt2timeslot = 0, rtt1day = 0, rtt2day = 0, rand1 = 0, rand2 = 0;
        CompanyTimeTable rtt1 = null, rtt2 = null;
        CompanyTimeTable[] rtts = newSolution.getCompanyTimeTables();
        int eventsPerCompany = CompanyTimeTable.NUM_TIMESLOTS * CompanyTimeTable.NUM_DAYS;
        int interval = unipr.getNumCompanies() * eventsPerCompany;
        int tests = 0;
        Random random = new Random();
        while ((temp1 == 0 && temp2 == 0) || temp1 == temp2) {
            rand1 = random.nextInt(interval);
            rand2 = random.nextInt(interval);

            rtt1 = rtts[rand1 / eventsPerCompany];
            rtt2 = rtts[rand2 / eventsPerCompany];
            rtt1day = (rand1 % eventsPerCompany) / CompanyTimeTable.NUM_TIMESLOTS; 
            rtt2day = (rand2 % eventsPerCompany) / CompanyTimeTable.NUM_TIMESLOTS; 
            rtt1timeslot = (rand1 % eventsPerCompany) % CompanyTimeTable.NUM_TIMESLOTS;
            rtt2timeslot = (rand2 % eventsPerCompany) % CompanyTimeTable.NUM_TIMESLOTS;
            temp1 = rtt1.getEvent(rtt1day, rtt1timeslot);
            temp2 = rtt2.getEvent(rtt2day, rtt2timeslot);
            tests++;
        }
        rtt1.setEvent(rtt1day, rtt1timeslot, temp2);
        rtt2.setEvent(rtt2day, rtt2timeslot, temp1);
        newSolution.putCompanyTimeTable(rand1 / eventsPerCompany, rtt1);
        newSolution.putCompanyTimeTable(rand2 / eventsPerCompany, rtt2);
        
        //System.out.println("Event 1: " + temp1 + ". Event 2: " + temp2 + ".");

        // TODO Check not to break capacity limits
        return newSolution;
    }

    public TimeTable createTrivialSolution() {
        Map<Integer, Company> companies = unipr.getCompanies();
        int numComp = unipr.getCompanies().size();

        ArrayList<TimeSlot> availableTimeSlots = new ArrayList<TimeSlot>();
        for (int companyId : companies.keySet()) {
            for (int d = 0; d < CompanyTimeTable.NUM_DAYS; d++) {
                for (int t = 0; t < CompanyTimeTable.NUM_TIMESLOTS; t++) {
                    availableTimeSlots.add(new TimeSlot(companyId, d, t));
                }
            }
        }

        TimeTable tt = new TimeTable(numComp);
        for (int companyId : companies.keySet()) {
            Company company = companies.get(companyId);
            CompanyTimeTable rtt = new CompanyTimeTable(company);
            tt.putCompanyTimeTable(companyId, rtt);
        }

        // index variables
        int rttId = 0;
        int day = 0;
        int timeSlot = 0;

        // assign all event to any randomly selected available timeslot
        Random rand = new Random(System.currentTimeMillis());
        for (Event e : unipr.getEvents().values()) {
            TimeSlot availableTimeSlot = availableTimeSlots.get(rand.nextInt(availableTimeSlots.size()));
            CompanyTimeTable rtt = tt.getCompanyTimeTables()[availableTimeSlot.companyId];
            rtt.setEvent(availableTimeSlot.day, availableTimeSlot.timeSlot, e.getId());
            availableTimeSlots.remove(availableTimeSlot);
        }
        availableTimeSlots.clear();

        return tt;
    }

    public void printConf() {
        System.out.println("Algorithm:           \tSimulated annealing");
        System.out.println("Number of iterations:\t" + runLaps);
        System.out.println("Best solution:       \t" + bestSol);
        System.out.println("Desired fitness:     \t" + DESIRED_FITNESS);
        System.out.println("Final temperature:   \t" + temp);
    }


    public void loadData(String dataFileUrl) {
        unipr.clear(); // reset all previous data before loading

        try {
            File file = new File(dataFileUrl);
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = null;
            // input data sections are read in the following order separated by #
            // #companies <name> <capacity> <position>
            // #courses <id> <name> <nJuniors> <nSeniors> <nTrainees>
            // #interviewers <name> <job>+
            // #studentgroups <name> <numStudents> <job>+
            String readingSection = null;
            String companyName = null;
            String jobName = null;
            String interviewerName = null;
            String studentGroupName = null;
            HashMap<String, Integer> jobNameToId = new HashMap<String, Integer>();
            while((line = in.readLine()) != null) {
                String[] data = line.split(" ");
                if(data[0].charAt(0) == '#') {
                    readingSection = data[1];
                    data = in.readLine().split(" ");
                }
                if(readingSection.equals("COMPANIES")) {
                  companyName = data[0];
                  int cap = Integer.parseInt(data[1]);
                  Event.Type type = Event.generateType(Integer.parseInt(data[2]));
                  Company company = new Company(companyName, cap, type);
                  unipr.addCompany(company);
              } else if(readingSection.equals("JOBS")) {
                  jobName = data[0];
                  int nJuniors = Integer.parseInt(data[1]);
                  int nSeniors = Integer.parseInt(data[2]);
                  int nTrainees = Integer.parseInt(data[3]);
                  Job job = new Job(jobName, nJuniors, nSeniors, nTrainees);
                  jobNameToId.put(jobName, job.getId());
                  unipr.addJob(job);
              } else if(readingSection.equals("INTERVIEWERS")) {
                  interviewerName = data[0];
                  Interviewer interviewer = new Interviewer(interviewerName);
                  for(int i = 1; i < data.length; i++) {
            // register all jobs for which this interviewer may interview
                    jobName = data[i];
                    interviewer.addJob(unipr.getJobs().get(jobNameToId.get(jobName)));
                }
                unipr.addInterviewer(interviewer);
            } else if(readingSection.equals("STUDENTGROUPS")) {
              studentGroupName = data[0];
              int size = Integer.parseInt(data[1]);
              StudentGroup studentGroup = new StudentGroup(studentGroupName, size);
              for(int i = 2; i < data.length; i++) {
                jobName = data[i];
                studentGroup.addJob(unipr.getJobs().get(jobNameToId.get(jobName)));
            }
            unipr.addStudentGroup(studentGroup);
        }
    }
      unipr.createEvents(); // create all events
      in.close();
  } catch (FileNotFoundException e) {
      e.printStackTrace();
  } catch (IOException e) {
      e.printStackTrace();
  }
}
}