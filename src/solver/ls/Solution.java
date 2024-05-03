
package solver.ls;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

public class Solution {
// function to evaluate solution
// function to calculate neighborhood
// take a step in the direction based on acceptance probability

  // 2D array, each row is a vehicle route, each column is a customer
  ArrayList<Customer>[] schedule;

  public Solution(VRPInstance instance) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Customer>();
    }
    for (int i = 0; i < instance.numCustomers; i++) {
      schedule[0].add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i]));
    }
  }
  
  public double evalSolution(ArrayList<Customer>[] solution) {
    double totalDistance = 0.0; 
    for (int i = 0; i < solution.length; i++) {
      for (int j = 0; j < solution[i].size(); j++) {
        if (j == 0) {
          totalDistance += solution[i].get(j).distanceToDepot();
        } else {
          totalDistance += solution[i].get(j).distanceTo(solution[i].get(j-1));
        }
      }
      totalDistance += solution[i].get(solution[i].size()-1).distanceToDepot();
    }
    return totalDistance;
  }
  
  public void perturbSolution(double temperature) {
    // generate a random neighbor
    // calculate acceptance probability
    // take a step in the direction based on acceptance probability
    double energyCurrent = evalSolution(schedule);
    ArrayList<Customer>[] scheduleNew = takeRandomStep(schedule);
    double energyNew = 0.0;
    double acceptProb = Solution.acceptanceProbability(energyCurrent, energyNew, temperature);
    if (acceptProb > Math.random()) {
      schedule = scheduleNew;    
    } 
  }

  public ArrayList<Customer>[] takeRandomStep(ArrayList<Customer>[] schedule) {
    // randomly select a vehicle and a customer and move that customer to a different vehicle
    // needs to be improved lol
    Random rand = new Random();
    int vehicle1 = rand.nextInt(schedule.length);
    int vehicle2 = rand.nextInt(schedule.length);
    int customer1 = rand.nextInt(schedule[vehicle1].size());
    ArrayList<Customer>[] scheduleNew = schedule;
    scheduleNew[vehicle2].add(schedule[vehicle1].get(customer1));
    scheduleNew[vehicle1].remove(customer1);
    return scheduleNew;
  }
  

  public static double acceptanceProbability(double energyCurrent, double energyNew, double temperature) {
    if (energyNew < energyCurrent) {
        return 1.0;
    }
    return Math.exp((energyCurrent - energyNew) / temperature);
  }

  // public double runSimulatedAnnealing(double tMin, double t, double alpha, int maxIter) {
  //   double temperature = t;
  //   double energy = evalSolution(schedule);
  //   double bestEnergy = energy;
  //   for (int i = 0; i < maxIter; i++) {
  //     perturbSolution(temperature);
  //     if (energy < bestEnergy) {
  //       bestEnergy = energy;
  //     }
  //     temperature *= alpha;
  //   }
  //   return bestEnergy;
  // }
}