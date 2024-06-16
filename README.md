# Interview Scheduling Problem

## Overview
This project focuses on solving the interview scheduling problem using two different algorithms: Genetic Algorithm and Simulated Annealing. The goal is to create optimal interview schedules based on given constraints and preferences.

## Algorithms

### Genetic Algorithm
The Genetic Algorithm (GA) is a search heuristic that mimics the process of natural selection. It is used to generate high-quality solutions for optimization and search problems.

- **Initialization**: Generate an initial population of random schedules.
- **Selection**: Select the fittest schedules to be parents.
- **Crossover**: Combine parents to create offspring.
- **Mutation**: Apply random changes to offspring to maintain diversity.
- **Iteration**: Repeat until a satisfactory solution is found.

### Simulated Annealing
Simulated Annealing (SA) is a probabilistic technique for approximating the global optimum of a given function.

- **Initialization**: Start with an initial schedule.
- **Cooling Schedule**: Decrease the probability of accepting worse solutions over time.
- **Neighbor Selection**: Generate a neighboring schedule by making a small change.
- **Acceptance Criteria**: Decide whether to accept the new schedule based on temperature and change in fitness.
- **Iteration**: Repeat until the system cools down.

## Implementation
The project includes the implementation of both algorithms in Java. The code reads data from CSV files and processes it to generate and evaluate schedules.

## How to Use
1. Clone the repository.
2. Compile and run the Java programs.
3. Modify the datasets as needed and re-run the algorithms to see different results.
