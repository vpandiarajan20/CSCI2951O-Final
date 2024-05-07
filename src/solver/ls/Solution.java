package solver.ls;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  static double[][] distanceMatrix;
  static int vehicleCapacity;
  static ArrayList<Tuple<Double, Integer>> angleList;
  static ArrayList<Customer> customers;

  public Solution() {
  }
  
  @SuppressWarnings("unchecked")
  public Solution(VRPInstance instance) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Customer>();
    }

    // sweepGenerateInitialSolution();
    naiveGenerateInitialSolution();
    
  }

  public static void initializeFields(VRPInstance instance) {
    Solution.vehicleCapacity = instance.vehicleCapacity;
    Solution.customers = new ArrayList<Customer>();
    for (int i = 0; i < instance.numCustomers; i++) {
      Solution.customers.add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i]));
    }
    // sort customers by demand, descending
    Solution.customers.sort((a, b) -> b.getDemand() - a.getDemand());
    computeDistanceMatrix(instance);
    computeAngleList(instance);
  }
  
  @SuppressWarnings("unchecked")
  public int OptimalAddition(int vehicle, Customer c) {
    // find the best position to add customer c to vehicle
    // return the position
    // System.out.println("OptimalAddition called");
    int bestPosition = -1;
    double bestDistance = Double.MAX_VALUE;
    int routeSize = schedule[vehicle].size();
    if (routeSize == 0) {
      return 0;
    }
    // int demand =
    for (int i = 0; i < routeSize; i++) {
      ArrayList<Customer> scheduleNew = (ArrayList<Customer>) schedule[vehicle].clone();
      scheduleNew.add(i, c);
      double distance = computeRouteDistance(scheduleNew);
      double demand = computeRouteDemand(scheduleNew);
      if (distance < bestDistance && demand <= Solution.vehicleCapacity) {
        bestDistance = distance;
        bestPosition = i;
      }
    }
    return bestPosition;
  }
  
  public static void computeDistanceMatrix(VRPInstance instance){
        double[][] distanceMatrix = new double[instance.numCustomers][instance.numCustomers];
        for (int i = 0; i < instance.numCustomers; i++) {
            for (int j = 0; j < instance.numCustomers; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    // System.out.print("i: " + i + " j: " + j + " x: " + instance.xCoordOfCustomer[i] + " y: " + instance.yCoordOfCustomer[i] + " x: " + instance.xCoordOfCustomer[j] + " y: " + instance.yCoordOfCustomer[j] + "\n");
                    distanceMatrix[i][j] = Math.pow(instance.xCoordOfCustomer[i] - instance.xCoordOfCustomer[j], 2) + Math.pow(instance.yCoordOfCustomer[i] - instance.yCoordOfCustomer[j], 2);
                }
            }
        }
        Solution.distanceMatrix = distanceMatrix;
    }
  
  public static void computeAngleList(VRPInstance instance) {
    ArrayList<Tuple<Double, Integer>> angleList = new ArrayList<Tuple<Double, Integer>>();
    for (int i = 0; i < instance.numCustomers; i++) {
      double angle = Math.atan2(customers.get(i).getY(), customers.get(i).getX());
      angleList.add(new Tuple<Double, Integer>(angle, i));
    }
    angleList.sort((a, b) -> a.getFirst().compareTo(b.getFirst()));
    Solution.angleList = angleList;
  }

  public void naiveGenerateInitialSolution() {
    // select customers by size
    int demand = 0;
    int vehicleNum = 0;
    int[] demandLeft = new int[schedule.length];
    
    for (int i = 0; i < customers.size(); i++) {
      demand = customers.get(i).getDemand();
      while (demandLeft[vehicleNum] + demand > Solution.vehicleCapacity) {
        vehicleNum++;
        if (vehicleNum >= schedule.length) {
          // no more vehicles
          vehicleNum = 0;
        }
      }
      
      demandLeft[vehicleNum] += demand;
      schedule[vehicleNum].add(customers.get(i));
    }
  }


  public void sweepGenerateInitialSolution() {
    // generate a solution using the sweep algorithm
    // start from the depot and add customers in order of angle
    // if the demand exceeds the vehicle capacity, start a new route
    // return the solution
    int vehicleNum = 0;
    System.out.println("Vehicle capacity: " + Solution.vehicleCapacity);
    int skip = 4;
    for (int i = 0; i < Solution.angleList.size(); i++) {
      Customer c = Solution.customers.get(Solution.angleList.get(i).getSecond());
      System.out.println("Customer: " + c);
      if (vehicleNum > 0 && skip < 2) {
        int addPositionPrev = OptimalAddition(vehicleNum-1, c);
        if (addPositionPrev != -1) {
          this.schedule[vehicleNum-1].add(addPositionPrev, c);
          continue;
        }
      }
      int addPosition = OptimalAddition(vehicleNum, c);
      if (addPosition == -1) {
        System.out.println("Demand: " + computeRouteDemand(this.schedule[vehicleNum]) + " Distance: " + computeRouteDistance(this.schedule[vehicleNum]) + " Vehicle: " + vehicleNum + " Route: " + this.schedule[vehicleNum]);
        System.out.println("New customer demand: " + c.getDemand());
        vehicleNum++;
        this.schedule[vehicleNum] = new ArrayList<Customer>();
        this.schedule[vehicleNum].add(c);
        skip = -1;
      } else {
        this.schedule[vehicleNum].add(addPosition, c);
      }
      skip++;
    }
  }

  public static double evalSolution(ArrayList<Customer>[] solution) {
    double totalDistance = 0.0;
    for (int i = 0; i < solution.length; i++) {
      totalDistance += computeRouteDistance(solution[i]);
      double demand = computeRouteDemand(solution[i]);
      
      if (demand > Solution.vehicleCapacity) {
        // penalize the solution
        totalDistance += 10000.0;
      }
    }
    return totalDistance;
  }

  public static double computeRouteDistance(ArrayList<Customer> solution) {
    double totalDistance = 0.0;
    for (int j = 0; j < solution.size(); j++) {
        // System.out.println("Customer: " + solution.get(j));
        if (j == 0) {
          totalDistance += solution.get(j).distanceToDepot();
        } else {
          totalDistance += solution.get(j).distanceTo(solution.get(j - 1));
        }
      }
    if (solution.size() > 0) {
      totalDistance += solution.get(solution.size() - 1).distanceToDepot();
    }
    return totalDistance;
  }

  public static double computeRouteDemand(ArrayList<Customer> solution) {
    double totalDemand = 0.0;
    for (int j = 0; j < solution.size(); j++) {
      totalDemand += solution.get(j).getDemand();
    }
    return totalDemand;
  }

  public void perturbSolution(double temperature) {
    // generate a random neighbor
    // calculate acceptance probability
    // take a step in the direction based on acceptance probability
    double energyCurrent = evalSolution(schedule);
    ArrayList<Customer>[] scheduleNew = takeRandomStep(schedule);
    double energyNew = evalSolution(scheduleNew);
    double acceptProb = Solution.acceptanceProbability(energyCurrent, energyNew, temperature);
    System.out.println("Acceptance Prob: " + acceptProb + " Energy Current: " + energyCurrent + " Energy New: " + energyNew);
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
    if (vehicle1 == vehicle2) {
      vehicle2 = (vehicle2 + 1) % schedule.length;
    }
    int customer1 = rand.nextInt(schedule[vehicle1].size());
    ArrayList<Customer>[] scheduleNew = copySchedule(schedule);
    int addPosition = OptimalAddition(vehicle2, schedule[vehicle1].get(customer1));
    // System.out.println("Vehicle1: " + vehicle1 + " Vehicle2: " + vehicle2 + " Customer1(M): " + schedule[vehicle1].get(customer1) + " AddPosition: " + addPosition);
    if (addPosition == -1) {
      addPosition = rand.nextInt(schedule[vehicle2].size());
    }
    scheduleNew[vehicle2].add(addPosition, schedule[vehicle1].get(customer1));
    scheduleNew[vehicle1].remove(customer1);

    return scheduleNew;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Customer>[] copySchedule(ArrayList<Customer>[] schedule) {
    ArrayList<Customer>[] scheduleNew = new ArrayList[schedule.length];
    for (int i = 0; i < schedule.length; i++) {
      scheduleNew[i] = (ArrayList<Customer>)schedule[i].clone();
    }
    return scheduleNew;
  }
  
  @SuppressWarnings("unchecked")
  public Solution clone() {
    Solution clonedSolution = new Solution();

    // Copy schedule array
    clonedSolution.schedule = new ArrayList[this.schedule.length];
    for (int i = 0; i < this.schedule.length; i++) {
        clonedSolution.schedule[i] = new ArrayList<Customer>(this.schedule[i]);
    }
    
    return clonedSolution;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < schedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < schedule[i].size(); j++) {
        Customer c = schedule[i].get(j);
        sb.append(c);
      }
      sb.append("\n");
    }
    sb.append("total_distance: " + Solution.evalSolution(schedule) + "\n");
    return sb.toString();
  }

  public static String printSchedule(ArrayList<Customer>[] newSchedule) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < newSchedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < newSchedule[i].size(); j++) {
        Customer c = newSchedule[i].get(j);
        sb.append(c);
      }
      sb.append("\n");
    }
    sb.append("total_distance: " + Solution.evalSolution(newSchedule) + "\n");
    return sb.toString();
  }
}
