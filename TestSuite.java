import java.io.*;
import java.util.*;
import java.lang.*;

public class TestSuite {
    Scanner scanner;

    long start, end;

    // genetic algorithm
    Algorithm algorithm;

    TimeTable bestTimeTable;

    public TestSuite() {
        System.out.println("[***] Which algorithm do you want to run?");
        System.out.println("Your alternatives:");
        System.out.println("0. Genetic algorithm");
        System.out.println("1. Simulated annealing");
        System.out.print("> ");
        scanner = new Scanner(System.in);
        int ans = scanner.nextInt();
        scanner.nextLine(); // Consume to next line break
        System.out.println();

        if (ans == 0) {
            setupGA();
        } else {
            setupSA();
        }

        // Display countdown
        countdown(0);

        start = System.currentTimeMillis();
        System.out.println();

        System.out.println(":::::::::::::::::::::::::::::::");
        System.out.println(":::::::::: Start run ::::::::::");
        System.out.println(":::::::::::::::::::::::::::::::");
        System.out.println("Start time: " + start);
        run();
        end = System.currentTimeMillis();

        System.out.println(":::::::::::::::::::::::::::::::");
        System.out.println("::::: Generated timetable :::::");
        System.out.println(":::::::::::::::::::::::::::::::");
        printTimeTable(bestTimeTable);
        System.out.println();
        System.out.println(":::::::::::::::::::::::::::::::");
        System.out.println("::::::::::::: Info ::::::::::::");
        System.out.println(":::::::::::::::::::::::::::::::");
        algorithm.printConf();
        System.out.println("Start timestamp:\t" + start);
        System.out.println("End timestamp:  \t" + end);
        System.out.println("Total time:     \t" + (end - start) / 1000.0 + "s");
    }

    private void countdown(int seconds) {
        System.out.print("Starting in " + seconds);
        for (int i = seconds - 1; i >= 0; i--) {
            for (int j = 0; j < 5; j++) {
                sleep(200);
                System.out.print(".");
            }
            System.out.print(i);
        }
    }

    /**
     * Splits sleep up into 50 small parts.
     */
    private void sleep(int milliseconds) {
        long now = System.currentTimeMillis();
        long stop = now + milliseconds;
        while (stop > now) {
            try {
                Thread.sleep(milliseconds / 50);
            } catch (InterruptedException e) {}
            now = System.currentTimeMillis();
        }
    }

    private void setupSA() {
        System.out.println("Simulated annealing selected.");
        SA sa = new SA();
        sa.loadData(getString("path to data set", "../input/unipr_L"));
        algorithm = sa;
    }

    private void setupGA() {
        System.out.println("Genetic algorithm selected.");
        GA ga = new GA();
        ga.loadData(getString("path to data set", "./TESTER/unipr_L"));
        ga.setMutationProbability(getInt("mutation probability", 30));
        ga.setCrossoverProbability(getInt("crossover probability", 30));
        ga.setPopulationSize(getInt("population size", 30));
        ga.setSelectionSize(getInt("selection size", 50));
        ga.setSelectionType(getInt("selection type", 1));
        ga.setMutationType(getInt("mutation type", 0));
        algorithm = ga;
    }

    private int getInt(String name, int def) {
        // Ask user
        System.out.println("[***] Specify " + name + ". (Press enter to use " + def + ")");
        System.out.print("> ");
        int data;
        try {
            data = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("Using " + name + " " + data + ".");
        } catch (NumberFormatException e) {
            data = def;
            System.out.println("Using standard " + name + " (" + data+ ").");
        }
        System.out.println();
        return data;
    }

    private String getString(String name, String def) {
        // Ask user
        System.out.println("[***] Specify " + name + ". (Press enter to use '" + def + "')");
        System.out.print("> ");
        String data1 = scanner.nextLine().trim();
        if (data1.length() == 0) {
            System.out.println("Using standard data set ('" + def + "').");
            data1 = def;
        } else {
            System.out.println("Using dataset '" + data1 + "'.");
        }
        System.out.println();
        return data1;
    }

    private void run() {
        bestTimeTable = algorithm.generateTimeTable();
    }

    public void printTimeTable(TimeTable tt) {
        StringBuilder sb = new StringBuilder();
        int nrSlots = 0;
        int nrEvents = 0;
       
        for(CompanyTimeTable rtt : tt.getCompanyTimeTables()) {
            sb.append("============ ");    
            sb.append("Company: " + rtt.getCompany().getName() + " Capacity: " + rtt.getCompany().getCapacity());
            sb.append(" ============\n");   
            for (int timeslot = 0; timeslot < CompanyTimeTable.NUM_TIMESLOTS; timeslot++) {
                for (int day = 0; day < CompanyTimeTable.NUM_DAYS; day++) {
                    int eventId = rtt.getEvent(day, timeslot);
         
                    if(eventId > nrEvents) {
                        nrEvents = eventId; 
                    }
                    nrSlots++;
                    sb.append("[\t" + eventId + "\t]");
                }
                sb.append("\n");
            }    
        }
        System.out.println(sb.toString());
        System.out.println("Fitness:         \t" + tt.getFitness());
        System.out.println("Number of slots: \t" + nrSlots);
        System.out.println("Number of events:\t" + nrEvents);
        System.out.println("Sparseness:      \t" + ((double)nrEvents/(double)nrSlots));
    }
}