package solver.ls;
import java.util.ArrayList;
import java.util.Random;

import solver.ls.insertionHeuristics.InsertionHeuristic;
import solver.ls.removalHeuristics.RemoveHeuristic;

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
  public static int vehicleCapacity;
  static double[][] distanceMatrix; 
  static ArrayList<Tuple<Double, Integer>>[] nearestNeighborsMatrix;
  static ArrayList<Tuple<Double, Integer>> angleList;
  static ArrayList<Customer> customers;
  static double penalty = 0.0;

  InsertionHeuristic[] insertionHeuristics;
  RemoveHeuristic removalHeuristic;
  ArrayList<Customer>[] schedule;

  public Solution() {
  }
  
  @SuppressWarnings("unchecked")
  public Solution(VRPInstance instance, InsertionHeuristic[] insertionHeuristics, RemoveHeuristic removalHeuristic) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Customer>();
    }
    this.insertionHeuristics = insertionHeuristics;
    this.removalHeuristic = removalHeuristic;
    // sweepGenerateInitialSolution();
    naiveGenerateInitialSolution();
    // penalty = evalSolution(schedule)*0.6;
    // penalty = evalSolution(schedule)*1.8;
    // penalty = 1800;
  }

  public static void initializeFields(VRPInstance instance) {
    Solution.vehicleCapacity = instance.vehicleCapacity;
    Solution.customers = new ArrayList<Customer>();
    for (int i = 0; i < instance.numCustomers; i++) {
      Solution.customers.add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i], i));
    }
    // sort customers by demand, descending
    Solution.customers.sort((a, b) -> b.getDemand() - a.getDemand());
    computeDistanceMatrix(instance);
    computeAngleList(instance);
  }
  

  
  @SuppressWarnings("unchecked")
  public static void computeDistanceMatrix(VRPInstance instance){
        double[][] distanceMatrix = new double[instance.numCustomers][instance.numCustomers];
        Solution.nearestNeighborsMatrix = new ArrayList[instance.numCustomers];
        double maxDistance = 0.0;
        double maxDemand = 0.0;
        double totalDemand = 0.0;
        double totalCapacity = instance.numVehicles * instance.vehicleCapacity;
        for (int i = 0; i < instance.numCustomers; i++) {
          totalDemand += instance.demandOfCustomer[i];
          maxDemand = Math.max(maxDemand, instance.demandOfCustomer[i]);
          nearestNeighborsMatrix[i] = new ArrayList<Tuple<Double, Integer>>();
            for (int j = 0; j < instance.numCustomers; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    // System.out.print("i: " + i + " j: " + j + " x: " + instance.xCoordOfCustomer[i] + " y: " + instance.yCoordOfCustomer[i] + " x: " + instance.xCoordOfCustomer[j] + " y: " + instance.yCoordOfCustomer[j] + "\n");
                    distanceMatrix[i][j] = Math.pow(instance.xCoordOfCustomer[i] - instance.xCoordOfCustomer[j], 2) + Math.pow(instance.yCoordOfCustomer[i] - instance.yCoordOfCustomer[j], 2);
                    nearestNeighborsMatrix[i].add(new Tuple<Double, Integer>(distanceMatrix[i][j], j));
                    maxDistance = Math.max(maxDistance, distanceMatrix[i][j]);
                }
            }
            nearestNeighborsMatrix[i].sort((a, b) -> a.getFirst().compareTo(b.getFirst()));
        }
        Solution.distanceMatrix = distanceMatrix;
        Solution.penalty = Math.max(0.1, Math.min(1000, maxDistance/maxDemand));
        // System.out.println("Max Distance: " + maxDistance + " Max Demand: " + maxDemand + " Total Demand: " + totalDemand + " Total Capacity: " + totalCapacity + " Ratio: " + totalDemand/totalCapacity);
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
    
    // for (int i = 0; i < customers.size(); i++) {
    //   demand = customers.get(i).getDemand();
    //   while (demandLeft[vehicleNum] + demand > Solution.vehicleCapacity) {
    //     vehicleNum++;
    //     if (vehicleNum >= schedule.length) {
    //       // no more vehicles
    //       vehicleNum = 0;
    //     }
    //   }
      
    //   demandLeft[vehicleNum] += demand;
    //   schedule[vehicleNum].add(customers.get(i));
    // }
    for (int i = 1; i < customers.size(); i++) {
      demand = customers.get(i).getDemand();
      for (int j = 0; j < demandLeft.length; j++) {
        if (demandLeft[j] + demand <= Solution.vehicleCapacity) {
          schedule[j].add(customers.get(i));
          demandLeft[j] += demand;
          break;
        }
      }
    }
    
    for (int i = 0; i < schedule.length; i ++) {
      insertionHeuristics[0].applyHeuristicRoute(schedule[i]);
    }
  }
  
    public void sweepGenerateInitialSolution() {
    // generate a solution using the sweep algorithm
    // start from the depot and add customers in order of angle
    // if the demand exceeds the vehicle capacity, start a new route
    // return the solution
    // System.out.println("Vehicle capacity: " + Solution.vehicleCapacity);
    // pick random starting point
    Random rand = new Random();
    int start = rand.nextInt(Solution.angleList.size());
    double capacityMultiplier = 1.0;
    outer: while (true) {
      int vehicleNum = 0;
      for (int j = 0; j < schedule.length; j++) {
        schedule[j] = new ArrayList<Customer>();
      }
      if (capacityMultiplier > 1.18) {
        start = rand.nextInt(Solution.angleList.size());
        capacityMultiplier = 1.1;
        // System.out.println("Restarting");
      }
      // System.out.println("Start: " + start);
      for (int i = 0; i < Solution.angleList.size(); i++) {
        start++;
        if (start >= Solution.angleList.size()) {
          start = 0;
        }
        Customer c = Solution.customers.get(Solution.angleList.get(start).getSecond());
        // System.out.println("Added customer:" + start);
        // System.out.println("vehicleNum:" + vehicleNum + " scheduleLength:" + schedule.length);
        if (computeRouteDemand(schedule[vehicleNum]) + c.getDemand() < capacityMultiplier * Solution.vehicleCapacity) {
          this.schedule[vehicleNum].add(c);
        } else {
          vehicleNum++;
          // System.out.println("VehicleNum: " + vehicleNum);
          if (vehicleNum >= schedule.length) {
            // System.out.println("Restarting");
            capacityMultiplier += 0.01;
            continue outer;
          }
          this.schedule[vehicleNum] = new ArrayList<Customer>();
          this.schedule[vehicleNum].add(c);
        }
      }
      break;
    }
    // System.out.println("Capacity Multiplier: " + capacityMultiplier);
    for (int i = 0; i < schedule.length; i ++) {
      insertionHeuristics[0].applyHeuristicRoute(schedule[i]);
    }
  }



  public static double evalSolution(ArrayList<Customer>[] solution) {
    double totalDistance = 0.0;
    // TODO: do we want penalize for each route or just if any route exceeds capacity?
    // boolean penalize = false;
    for (int i = 0; i < solution.length; i++) {
      totalDistance += computeRouteValue(solution[i]);
    }
    // if (penalize) {
    //   totalDistance += penalty;
    // }
    return totalDistance;
  }

  public static void incrementPenalty() {
    penalty *= 1.2;
    penalty = Math.min(penalty, 100000);
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

  public static double computeRouteValue(ArrayList<Customer> solution) {
    // System.out.println("Route distance: " + computeRouteDistance(solution) + " Route demand: " + computeRouteDemand(solution) + " Penalty: " + penalty);
    return computeRouteDistance(solution) + Math.max(0.0, computeRouteDemand(solution) - Solution.vehicleCapacity) * penalty;
  }

  public void perturbSolution(double temperature) {
    // generate a random neighbor
    // calculate acceptance probability
    // take a step in the direction based on acceptance probability
    double energyCurrent = evalSolution(schedule);
    ArrayList<Customer>[] scheduleNew = takeRandomStep(schedule);
    // ArrayList<Customer>[] scheduleNew = takeRandomStepSwap(schedule);
    // ArrayList<Customer>[] scheduleNew = takeRandomStepRemoveAndInsert(schedule);
    double energyNew = evalSolution(scheduleNew);
    double acceptProb = Solution.acceptanceProbability(energyCurrent, energyNew, temperature);
    // System.out.println("Acceptance Prob: " + acceptProb + " Energy Current: " + energyCurrent + " Energy New: " + energyNew);
    if (acceptProb > Math.random()) {
      schedule = scheduleNew;
    }
  }


  // @SuppressWarnings("unchecked")
  // public ArrayList<Customer>[] takeRandomStepLoop(ArrayList<Customer>[] schedule) {
  //   boolean searchCompleted = false;
  //   Random rand = new Random();
  //   int numNodesToSearch = 10;
  //   int numNeighborsToSearch = 10;
    
  //   // ArrayList<Customers> shuffled Customers = new ArrayList<Customers>(customers);
  //   // Collections.shuffle(shuffledCustomers);
  //   for (int loopID = 0; !searchCompleted; loopID++) {
  //     if (loopID > 1) {
  //       searchCompleted = true;
  //     }
  //     for (int i = 0; i < numNodesToSearch; i++) {
  //       int vehicle1 = rand.nextInt(schedule.length);
  //       ArrayList<Customer> vehicle1Route = (ArrayList<Customer>) schedule[vehicle1].clone();
  //       while (vehicle1Route.size() == 0) {
  //         vehicle1 = rand.nextInt(schedule.length);
  //       }
  //       Customer customer1 = vehicle1Route.get(rand.nextInt(schedule[vehicle1].size()));
  //       for (int j = 0; j < numNeighborsToSearch; j++) {
  //         // selecting customer 2
  //         int customer2ID = Solution.nearestNeighborsMatrix[customer1.getId()].get(j).getSecond();
  //         if (customer2ID == customer1.getId()) {
  //           throw new RuntimeException("Customer 2 is the same as customer 1, you're an idiot");
  //         }
  //         Customer customer2 = customers.get(customer2ID);
  //         ArrayList<Customer> vehicle2Route = null;
  //         // this is mega dumb, but we need to change customer class
  //         for (int k = 0; k < schedule.length; k++) {
  //           if (schedule[k].contains(customer2)) {
  //             vehicle2Route = (ArrayList<Customer>) schedule[k].clone();
  //             break;
  //           }
  //         }


  //         // apply moves
  //         if (loopID == 0) {
  //           if (move1(customer1, customer2, vehicle1Route, vehicle2Route)) {
  //             searchCompleted = false;
  //             continue;
  //           }
  //         }
  //       }
  //       // 10 is a magic number needs to be configured

  //     }
  //   }
  //   return null;
  // }

  // @SuppressWarnings("unchecked")
  // public boolean move1(Customer customer1, Customer customer2, ArrayList<Customer> route1, ArrayList<Customer> route2) {
  //   // dude if they're the same route this might be totally fucked
  //   if (route1 == route2) {
  //     ArrayList<Customer> copyRoute = (ArrayList<Customer>) route1.clone();
  //     copyRoute.remove(customer1);
  //     int idxOfCustomer2 = copyRoute.indexOf(customer2);
  //     copyRoute.add(idxOfCustomer2, customer1);
  //     if (computeRouteValue(copyRoute) < computeRouteValue(route1)) {
  //       route1 = copyRoute1;
  //       return true;
  //     }
  //     return false;
  //   }
  //   ArrayList<Customer> copyRoute1 = (ArrayList<Customer>)route1.clone();
  //   ArrayList<Customer> copyRoute2 = (ArrayList<Customer>) route2.clone();
  //   copyRoute1.remove(customer1);
  //   int idxOfCustomer2 = copyRoute2.indexOf(customer2);
  //   copyRoute2.add(idxOfCustomer2, customer1);

  //   if (computeRouteValue(copyRoute2) + computeRouteValue(copyRoute1) < computeRouteValue(route1) + computeRouteValue(route2)) {
  //     route1 = copyRoute1;
  //     route2 = copyRoute2;
  //     return true;
  //   }
  //   return false;
  //   //TODO:
  // }

  // public boolean move2(Customer customer1, Customer customer2, ArrayList<Customer> route1, ArrayList<Customer> route2) {
  //   if (route1 == route2) {
  //     ArrayList<Customer> copyRoute = (ArrayList<Customer>) route1.clone();
  //     int idxOfCustomer2 = copyRoute2.indexOf(customer2);
  //     copyRoute.add(idxOfCustomer2, customer1);
  //     if (computeRouteValue(copyRoute) < computeRouteValue(route1)) {
  //       route1 = copyRoute1;
  //       return true;
  //     }
  //     return false;
  //   }
  // }

  public ArrayList<Customer>[] takeRandomStep(ArrayList<Customer>[] schedule) {
    ArrayList<Customer>[] schedule1 = copySchedule(schedule);
    schedule1 = takeRandomStepRemoveAndInsert(schedule1);
    ArrayList<Customer>[] schedule2 = copySchedule(schedule);
    schedule2 = takeRandomStepSwap(schedule2);
    if (evalSolution(schedule1) < evalSolution(schedule2)) {
      return schedule1;
    }
    return schedule2;
  }



  @SuppressWarnings("unchecked")
  public ArrayList<Customer>[] takeRandomStepRemoveAndInsert(ArrayList<Customer>[] schedule) {
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
    // System.out.println("Old Schedule: " + printSchedule(schedule));
    ArrayList<Customer>[] scheduleNew = copySchedule(schedule);
    Customer customerRemove = removalHeuristic.removeHeuristic(scheduleNew[vehicle1]);
    ArrayList<Customer> bestRoute = (ArrayList<Customer>) scheduleNew[vehicle2].clone();
    bestRoute.add(customerRemove);
    double bestDistance = computeRouteDistance(bestRoute);
    for (int i = 0; i < insertionHeuristics.length; i++) {
      ArrayList<Customer> newRoute = (ArrayList<Customer>) scheduleNew[vehicle2].clone();
      insertionHeuristics[i].applyHeuristic(customerRemove, newRoute);
      if (computeRouteDistance(newRoute) < bestDistance) {
        bestRoute = newRoute;
        bestDistance = computeRouteDistance(newRoute);
      }
    }
    scheduleNew[vehicle2] = bestRoute;
    // TODO: for solutions that are infeasible, remove the smallest customer such that the solution is feasible
    return scheduleNew;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Customer>[] takeRandomStepSwap(ArrayList<Customer>[] schedule) {
    // randomly select a vehicle and a customer and move that customer to a different vehicle
    // needs to be improved lol
    Random rand = new Random();
    int vehicle1 = rand.nextInt(schedule.length);
    while (schedule[vehicle1].size() == 0) {
      vehicle1 = rand.nextInt(schedule.length);
    }
    int vehicle2 = rand.nextInt(schedule.length);
    while (vehicle1 == vehicle2 || schedule[vehicle2].size() == 0) {
      vehicle2 = (vehicle2 + 1) % schedule.length;
    }
    // System.out.println("Old Schedule: " + printSchedule(schedule));
    ArrayList<Customer>[] scheduleNew = copySchedule(schedule);

    Customer customer1Remove = removalHeuristic.removeHeuristic(scheduleNew[vehicle1]);
    Customer customer2Remove = removalHeuristic.removeHeuristic(scheduleNew[vehicle2]);

    ArrayList<Customer> bestRoute1 = (ArrayList<Customer>) scheduleNew[vehicle1].clone();
    ArrayList<Customer> bestRoute2 = (ArrayList<Customer>) scheduleNew[vehicle2].clone();
    
    bestRoute1.add(customer2Remove);
    bestRoute2.add(customer1Remove);
    
    double bestDistance1 = computeRouteDistance(bestRoute1);
    double bestDistance2 = computeRouteDistance(bestRoute2);

    for (int i = 0; i < insertionHeuristics.length; i++) {
      ArrayList<Customer> newRoute1 = (ArrayList<Customer>) scheduleNew[vehicle1].clone();
      insertionHeuristics[i].applyHeuristic(customer2Remove, newRoute1);
      if (computeRouteDistance(newRoute1) < bestDistance1) {
        bestRoute1 = newRoute1;
        bestDistance1 = computeRouteDistance(newRoute1);
      }

      ArrayList<Customer> newRoute2 = (ArrayList<Customer>) scheduleNew[vehicle2].clone();
      insertionHeuristics[i].applyHeuristic(customer1Remove, newRoute2);
      if (computeRouteDistance(newRoute2) < bestDistance2) {
        bestRoute2 = newRoute2;
        bestDistance2 = computeRouteDistance(newRoute2);
      }
    }
    scheduleNew[vehicle1] = bestRoute1;
    scheduleNew[vehicle2] = bestRoute2;
    // TODO: for solutions that are infeasible, remove the smallest customer such that the solution is feasible
    return scheduleNew;
  }

  @SuppressWarnings("unchecked")
  public static ArrayList<Customer>[] copySchedule(ArrayList<Customer>[] schedule) {
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
    clonedSolution.insertionHeuristics = this.insertionHeuristics;
    clonedSolution.removalHeuristic = this.removalHeuristic;
    
    return clonedSolution;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Vehicle Capacity: " + Solution.vehicleCapacity + "\n");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < schedule[i].size(); j++) {
        Customer c = schedule[i].get(j);
        sb.append(c);
      }
      sb.append(", Distance: " + computeRouteDistance(schedule[i]) + ", Demand: " + computeRouteDemand(schedule[i]) + "\n");
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

  public String submissionFormat() {
    StringBuilder sb = new StringBuilder();
    sb.append(Math.round(evalSolution(schedule)) + " 0 ");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("0 ");
      for (int j = 0; j < schedule[i].size(); j++) {
        sb.append(schedule[i].get(j).getId() + " ");
      }
      sb.append("0 ");
    }
    // remove last space
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public String fileOutputFormat() {
    StringBuilder sb = new StringBuilder();
    sb.append(Math.round(evalSolution(schedule)) + " 0\n");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("0 ");
      for (int j = 0; j < schedule[i].size(); j++) {
        sb.append(schedule[i].get(j).getId() + " ");
      }
      sb.append("0\n");
    }
    // remove last space
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
