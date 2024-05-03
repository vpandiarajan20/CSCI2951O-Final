package solver.ls;
import java.util.ArrayList;
import java.util.Random;

public class Solution {
  // function to evaluate solution
  // function to calculate neighborhood
  // take a step in the direction based on acceptance probability

  public static double acceptanceProbability(double energyCurrent, double energyNew, double temperature) {
    if (energyNew < energyCurrent) {
      return 1.0;
    }
    return Math.exp((energyCurrent - energyNew) / temperature);
  }

  // 2D array, each row is a vehicle route, each column is a customer
  ArrayList<Customer>[] schedule;

  int vehicleDemand;

  @SuppressWarnings("unchecked")
  public Solution(VRPInstance instance) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Customer>();
    }
    for (int i = 0; i < instance.numCustomers; i++) {
      schedule[0].add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i]));
    }
    vehicleDemand = instance.vehicleCapacity;
  }

  @SuppressWarnings("unchecked")
  public Solution(Solution s) {
    schedule = new ArrayList[s.schedule.length];
    for (int i = 0; i < s.schedule.length; i++) {
      schedule[i] = new ArrayList<Customer>(s.schedule[i]);
    }
    vehicleDemand = s.vehicleDemand;
  }

  public double evalSolution(ArrayList<Customer>[] solution) {
    double totalDistance = 0.0;
    for (int i = 0; i < solution.length; i++) {
      for (int j = 0; j < solution[i].size(); j++) {
        if (j == 0) {
          totalDistance += solution[i].get(j).distanceToDepot();
        } else if (j == solution[i].size() - 1) {
          totalDistance += solution[i].get(j).distanceToDepot();
        } else {
          totalDistance += solution[i].get(j).distanceTo(solution[i].get(j - 1));
        }
      }
      double demand = 0.0;
      for (int j = 0; j < solution[i].size(); j++) {
        demand += solution[i].get(j).getDemand();
      }
      if (demand > this.vehicleDemand) {
        // penalize the solution
        totalDistance += 1000000.0;
      }
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
    while (schedule[vehicle1].size() == 0) {
      vehicle1 = rand.nextInt(schedule.length);
    }
    int vehicle2 = rand.nextInt(schedule.length);
    int customer1 = rand.nextInt(schedule[vehicle1].size());
    ArrayList<Customer>[] scheduleNew = schedule;
    scheduleNew[vehicle2].add(schedule[vehicle1].get(customer1));
    scheduleNew[vehicle1].remove(customer1);
    return scheduleNew;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < schedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < schedule[i].size(); j++) {
        Customer c = schedule[i].get(j);
        sb.append("(" + c.getDemand() + ", " + c.getX() + ", " + c.getY() + ") ");
      }
      sb.append("\n");
    }
    sb.append("total_distance: " + this.evalSolution(schedule) + "\n");
    return sb.toString();
  }
}
