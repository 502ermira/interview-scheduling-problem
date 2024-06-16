import java.util.*;

//keeps all the TimeTables for a generation
public class Population {

// should be ordered when selecting the best individuals
private LinkedList<TimeTable> individuals;

public Population() {
 individuals = new LinkedList<TimeTable>();
}

public void createRandomIndividuals(int numIndividuals, UniPR unipr) {
 Map<Integer, Company> companies = unipr.getCompanies();
 int numComp = unipr.getCompanies().size();

 for(int i = 0; i < numIndividuals; i++) {
   // register all available timeslots
   ArrayList<TimeSlot> availableTimeSlots = new ArrayList<TimeSlot>();
   for(int companyId : companies.keySet()) {
     for(int d = 0; d < CompanyTimeTable.NUM_DAYS; d++) {
       for(int t = 0; t < CompanyTimeTable.NUM_TIMESLOTS; t++) {
         availableTimeSlots.add(new TimeSlot(companyId, d, t));
       }
     }
   }

   TimeTable tt = new TimeTable(numComp);
   for(int companyId : companies.keySet()) {
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
   for(Event e : unipr.getEvents().values()) {
     TimeSlot availableTimeSlot = availableTimeSlots.get(rand.nextInt(availableTimeSlots.size()));
     CompanyTimeTable rtt = tt.getCompanyTimeTables()[availableTimeSlot.companyId];
     rtt.setEvent(availableTimeSlot.day, availableTimeSlot.timeSlot, e.getId());
     availableTimeSlots.remove(availableTimeSlot);
     /* DEBUG
     System.out.println("==============");
     System.out.println("COMPANY TIME TABLE ID: " + rtt.getCompany().getName());
     System.out.println("Day: " + availableTimeSlot.day + " Timeslot: " + availableTimeSlot.timeSlot + " Event ID: " + e.getId());
     */
   }
   individuals.add(tt);
   availableTimeSlots.clear();
 }
}

// assumes sorted
public TimeTable getTopIndividual() {
 return individuals.get(0);
}

public TimeTable getWorstIndividual() {
 return individuals.getLast();
}

public void addIndividual(TimeTable tt) {
 individuals.add(tt);
}

public TimeTable getIndividual(int i) {
 return individuals.get(i);
}

public void addIndividualSorted(TimeTable tt) {
 ListIterator<TimeTable> it = individuals.listIterator();
 ListIterator<TimeTable> it2 = individuals.listIterator();

 while (it.hasNext()) {
   if (it.next().getFitness() < tt.getFitness()) {
     it2.add(tt);
     break;
   }

   it2.next();
 }
}

public ListIterator<TimeTable> listIterator() {
 return individuals.listIterator();
}

public void sortIndividuals() {
 Collections.sort(individuals);
}

public int size() {
 return individuals.size();
}
}