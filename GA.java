import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Performs the Genetic Algorithm(GA) on the UniPR data set.
 */
public class GA implements Algorithm {

  public enum SELECTION_TYPE {
    NORMAL,
    ROULETTE_WHEEL,
    TOURNAMENT;

    public static String[] getNames() {
      GA.SELECTION_TYPE[] states = values();
      String[] names = new String[states.length];
      for (int i = 0; i < states.length; i++) {
        names[i] = states[i].name();
      }
      return names;
    }
  };

  public enum MUTATION_TYPE {
    NORMAL;

    public static String[] getNames() {
      GA.MUTATION_TYPE[] states = values();
      String[] names = new String[states.length];
      for (int i = 0; i < states.length; i++) {
        names[i] = states[i].name();
      }
      return names;
    }
  };

  // algorithm parameters
  private int DESIRED_FITNESS = 0;
  private int MAX_POPULATION_SIZE;
  private int MUTATION_PROBABILITY; // compared with 1000
  private int CROSSOVER_PROBABILITY; // compared with 1000
  private int SELECTION_SIZE;
  private int TOURNAMENT_POOL_SIZE = 5;
  private SELECTION_TYPE selectionType = SELECTION_TYPE.NORMAL;
  private MUTATION_TYPE mutationType = MUTATION_TYPE.NORMAL;

  private Population population;
  private UniPR unipr;

  public GA() {
    unipr = new UniPR();
  }
  
  /*
  * Returns a schedule based on the given constraints
  */
  public TimeTable generateTimeTable() {
    DecimalFormat numberFormat = new DecimalFormat("#.00");
    long startTime = System.currentTimeMillis();
    // run until the fitness is high enough
    // high enough should at least mean that
    // all hard constraints are solved
    // adjust for the number of soft constraints to be solved too
    // use another stop criteria too, in order to not run forever?

    // create the initial random population
    createRandomPopulation();
    ListIterator<TimeTable> it = population.listIterator();
    while(it.hasNext()) {
      TimeTable tt = it.next();
      tt.fitness(unipr);
    }

    population.sortIndividuals();

    int numGenerations = 1;
    while (population.getTopIndividual().getFitness() < DESIRED_FITNESS) {
      Population children = breed(population, MAX_POPULATION_SIZE);
      population = selection(population, children);

      // sort the population by their fitness
      // not needed
      population.sortIndividuals(); 
      
      numGenerations++;
      System.out.println("#TIME: " + numberFormat.format((System.currentTimeMillis()-startTime)/1000.0) + ", GENERATIONS: " + numGenerations + " FITNESS: " + population.getTopIndividual().getFitness());
    }

    return population.getTopIndividual();
  }


  //////////////////////////
  // SETUP
  //////////////////////////

  public void loadData(String dataFileUrl) {
    unipr.clear(); // reset all previous data before loading

    try {
      File file = new File(dataFileUrl);
      BufferedReader in = new BufferedReader(new FileReader(file));
      String line = null;
      // input data sections are read in the following order separated by #
      // #rooms <name> <capacity> <type>
      // #courses <id> <name> <numLectures> <numClasses> <numLabs>
      // #lecturers <name> <course>+
      // #studentgroups <name> <numStudents> <course>+
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
          int nJunior = Integer.parseInt(data[1]);
          int nSenior = Integer.parseInt(data[2]);
          int nTrainee = Integer.parseInt(data[3]);
          Job job = new Job(jobName,nJunior, nSenior, nTrainee);
          jobNameToId.put(jobName, job.getId());
          unipr.addJob(job);
        
       
        } else if(readingSection.equals("INTERVIEWERS")) {
          interviewerName = data[0];
          Interviewer interviewer = new Interviewer(interviewerName);
          for(int i = 1; i < data.length; i++) {
            // register all job for which this interviewer may interview
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

  //////////////////////////
  // GENETIC ALGORITHMS
  //////////////////////////

  private Population createRandomPopulation() {
    population = new Population();
    population.createRandomIndividuals(MAX_POPULATION_SIZE, unipr);
    return population;
  }

  private TimeTable next(ListIterator<TimeTable> it) {
    return it.hasNext() ? it.next() : null;
  }

  /////////////////////////////
  
  // uses another implementation of roulette selection of parents
  private Population breed(Population population, int N) {
    Population children = new Population();

    // calculate the pseudofitness of each individual
    // used in the roulette selection
    int[] pseudoFitness = new int[population.size()];
    int smallestFitness = population.getWorstIndividual().getFitness();
    smallestFitness = smallestFitness >= 0 ? 0 : smallestFitness;
    
    int i = 0;
    ListIterator<TimeTable> it = population.listIterator();
    int fitnessSum = 0;
    while (it.hasNext()) {
      // the smallest possible is 1, this saves us from weird behaviour in
      // cases where all individuals have the same fitness
      pseudoFitness[i] = it.next().getFitness() + -1 * smallestFitness + 1;
      fitnessSum += pseudoFitness[i];
      i++;
    }

    // create alias index
    int[] alias = new int[fitnessSum];
    
    // add the individual indexes a proportionate amount of times 
    int aliasIndex = 0;
    it = population.listIterator();
    for (int individual = 0; individual < population.size(); individual++) {
      for (int j = 0; j < pseudoFitness[individual]; j++) {
        alias[aliasIndex] = individual;
        aliasIndex++;
      }
    }
    
    Random rand = new Random(System.currentTimeMillis());
    
    while (children.size() < N) {
      if (alias.length == 0) {
        break;
      }

      int pi1 = alias[rand.nextInt(alias.length)];
      int numPi1 = pseudoFitness[pi1];
      int aIndex = rand.nextInt(alias.length - numPi1);

      int ai = 0;
      int j = 0;
      for (; j < alias.length && ai < aIndex; j++) {
        // skip ahead if we are at the span of the first parent's index
        while (j < (alias.length - 1) && alias[j] == pi1) {
          j++; 
        }

        ai++;
      }

      int pi2 = alias[j];

      TimeTable t1 = population.getIndividual(pi1);
      TimeTable t2 = population.getIndividual(pi2);

      TimeTable child = crossoverWithPoint(t1, t2);
      mutate(child);
      repairTimeTable(child);
      child.fitness(unipr);

      children.addIndividual(child);
    }
    
    return children;
  }

  private Population selection(Population population, Population children) {
    // population is already sorted
    children.sortIndividuals(); 

    Population nextPopulation = new Population();

    ListIterator<TimeTable> itParents = population.listIterator();
    ListIterator<TimeTable> itChildren = children.listIterator();
    TimeTable nextParent = next(itParents);
    TimeTable nextChild = next(itChildren);

    while (nextPopulation.size() < MAX_POPULATION_SIZE) {
      if (nextChild != null) {
        if (nextChild.getFitness() > nextParent.getFitness()) {
          nextPopulation.addIndividual(nextChild);

          nextChild = next(itChildren);

        } else {
          nextPopulation.addIndividual(nextParent);

          nextParent = next(itParents);
        }

      } else {
        if (nextParent != null) {
            // add the rest from population
          nextPopulation.addIndividual(nextParent);
          nextParent = next(itParents);
        }
      }
    }

    return nextPopulation;
  }

  /////////////////////////////


  private TimeTable crossoverWithPoint(TimeTable t1, TimeTable t2) {
    TimeTable child = new TimeTable(unipr.getNumCompanies());

    int interval = unipr.getNumCompanies() * CompanyTimeTable.NUM_TIMESLOTS *
    CompanyTimeTable.NUM_DAYS;

    int point = new Random(System.currentTimeMillis()).nextInt(interval);

    CompanyTimeTable[] rtts1 = t1.getCompanyTimeTables();
    CompanyTimeTable[] rtts2 = t2.getCompanyTimeTables();

    int gene = 0;

		// iterate over the genes
    for (int i = 0; i < unipr.getNumCompanies(); i++) {
     CompanyTimeTable rtt = new CompanyTimeTable(rtts1[i].getCompany());

			// for each available time
     for (int timeslot = 0; timeslot < CompanyTimeTable.NUM_TIMESLOTS;
       timeslot++) {
      for (int day = 0; day < CompanyTimeTable.NUM_DAYS; day++) {
       int allele;

       if (gene < point) {
        allele = rtts1[i].getEvent(day, timeslot);
      } else {
        allele = rtts2[i].getEvent(day, timeslot);
      }

      rtt.setEvent(day, timeslot, allele);
      gene++;
    }
  }

  child.putCompanyTimeTable(i, rtt);
}

return child;
}

private void repairTimeTable(TimeTable tt) {
  HashMap<Integer, LinkedList<CompanyDayTime>> locations = new HashMap<Integer,
  LinkedList<CompanyDayTime>>();

  LinkedList<CompanyDayTime> unusedSlots = new LinkedList<CompanyDayTime>();

    // initiate number of bookings to 0
  for (int eventID : unipr.getEvents().keySet()) {
    locations.put(eventID, new LinkedList<CompanyDayTime>());
  }

  CompanyTimeTable[] rtts = tt.getCompanyTimeTables();

  for (int i = 0; i < unipr.getNumCompanies(); i++) {
    CompanyTimeTable rtt = rtts[i];
      // for each available time
    for (int timeslot = 0; timeslot < CompanyTimeTable.NUM_TIMESLOTS;
     timeslot++) {
      for (int day = 0; day < CompanyTimeTable.NUM_DAYS; day++) {
        int bookedEvent = rtt.getEvent(day, timeslot);
        if (bookedEvent == 0) {
            // add to usable slots
          unusedSlots.add(new CompanyDayTime(i, day, timeslot));

        } else {
            // save the location
          locations.get(bookedEvent).add(new CompanyDayTime(i, day, timeslot));
        }
      }
    }
  }

  List<Integer> unbookedEvents = new LinkedList<Integer>();

  for (int eventID : unipr.getEvents().keySet()) {
    if (locations.get(eventID).size() == 0) {
        // this event is unbooked
      unbookedEvents.add(eventID);

    } else if (locations.get(eventID).size() > 1) {
        // this is event is booked more than once
        // randomly make those slots unused until only one remains
      LinkedList<CompanyDayTime> slots = locations.get(eventID);
      Collections.shuffle(slots);

      while (slots.size() > 1) {
        CompanyDayTime rdt = slots.removeFirst();

          // mark this slot as unused
        unusedSlots.add(rdt);
        rtts[rdt.company].setEvent(rdt.day, rdt.time, 0);
      }
    }
  }

    // now put each unbooked event in an unused slot
  Collections.shuffle(unusedSlots);
  for (int eventID : unbookedEvents) {
    CompanyDayTime rdt = unusedSlots.removeFirst();
    rtts[rdt.company].setEvent(rdt.day, rdt.time, eventID);
  }
}

  // wrapper class only used in repair function
private class CompanyDayTime {
  int company;
  int day;
  int time;

  CompanyDayTime(int company, int day, int time) {
    this.company = company;
    this.day = day;
    this.time = time;
  }
}

  //////////////////////////
  // MUTATION
  //////////////////////////

private void mutate(TimeTable tt) {
  Random rand = new Random(System.currentTimeMillis());
  CompanyTimeTable[] rtts = tt.getCompanyTimeTables();

  for (int i = 0; i < unipr.getNumCompanies(); i++) {
    CompanyTimeTable rtt = rtts[i];
      // for each available time
    for (int timeslot = 0; timeslot < CompanyTimeTable.NUM_TIMESLOTS;
      timeslot++) {
      for (int day = 0; day < CompanyTimeTable.NUM_DAYS; day++) {
        if (rand.nextInt(1000) < MUTATION_PROBABILITY) {
            // mutate this gene
          int swapTargetDay = rand.nextInt(CompanyTimeTable.NUM_DAYS);
          int swapTargetTimeslot = rand.nextInt(CompanyTimeTable.NUM_TIMESLOTS);
          int swapTargetEventId = rtt.getEvent(swapTargetDay, swapTargetTimeslot);
          int swapSrcEventId = rtt.getEvent(day, timeslot);
          rtt.setEvent(swapTargetDay, swapTargetTimeslot, swapSrcEventId);
          rtt.setEvent(day, timeslot, swapTargetEventId);
        }
      }
    }
  }
}

public void setMutationProbability(int p) {
  MUTATION_PROBABILITY = p;
}

public void setCrossoverProbability(int p) {
  CROSSOVER_PROBABILITY = p;
}

public void setPopulationSize(int size) {
  MAX_POPULATION_SIZE = size;
}

public void setSelectionSize(int size) {
  SELECTION_SIZE = size;
}

public void setMutationType(int i) {
  mutationType = MUTATION_TYPE.values()[i];
}

public void setSelectionType(int i) {
  selectionType = SELECTION_TYPE.values()[i];
}

public void printConf() {
  System.out.println("Desired fitness:\t" + DESIRED_FITNESS);
  System.out.println("Population size:\t" + MAX_POPULATION_SIZE);
  System.out.println("Selection size: \t" + SELECTION_SIZE);
  System.out.println("Mutation type:  \t" + mutationType);
  System.out.println("Selection type: \t" + selectionType);
  System.out.println("P(Mutation):    \t" + ((double)MUTATION_PROBABILITY / 1000.0d * 100) + "%");
  System.out.println("P(Crossover):   \t" + ((double)CROSSOVER_PROBABILITY / 1000.0d * 100) + "%");
}
}