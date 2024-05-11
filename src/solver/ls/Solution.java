package solver.ls;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Solution {


  // ---------------------------------------------------------- Fields ------------------------------------------------
  public static int vehicleCapacity;
  static double[][] distanceMatrix; 
  static ArrayList<Tuple<Double, Integer>>[] nearestNeighborsMatrix;
  static Customer[] customers;
  static ArrayList<Customer> sortedCustomers;
  static double penalty = 0.0;
  static boolean testing = false; // TODO: Remember to disable

  // InsertionHeuristic[] insertionHeuristics;
  // RemoveHeuristic removalHeuristic;
  
  ArrayList<Integer>[] schedule;
  double[] routeDistances;
  double[] routeDemands;
  double[] routePenalties;

  // ---------------------------------------------------------- Constructors ------------------------------------------------
  public Solution() {
  }
  
  @SuppressWarnings("unchecked")
  public Solution(VRPInstance instance) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Integer>();
    }
    routeDemands = new double[instance.numVehicles];
    routeDistances = new double[instance.numVehicles];
    routePenalties = new double[instance.numVehicles];
    // this.insertionHeuristics = insertionHeuristics;
    // this.removalHeuristic = removalHeuristic;

    boolean naiveSolution = naiveGenerateInitialSolution();
    
    if (!naiveSolution) {
      for (int i = 0; i < schedule.length; i ++) {
        schedule[i] = new ArrayList<Integer>();
      }
      naiveGenerateGreedyInitialSolution();
      // System.out.println("Naive Solution Failed, Generated Greedy Solution");
    }
  }
  

  // ------------------------ old take random step ------------------------ 

  public void takeRandomStep() {
    Random rand = new Random();
    int randNum = rand.nextInt(2);
    Customer customer1 = customers[rand.nextInt(customers.length-1) + 1];
    if (randNum == 0) {
      int vehicleNum = rand.nextInt(schedule.length);
      while (vehicleNum != customer1.getRouteId() && routeDemands[vehicleNum] + customer1.getDemand() > vehicleCapacity) {
        customer1 = customers[rand.nextInt(customers.length-1) + 1];
        vehicleNum = rand.nextInt(schedule.length);
        // System.out.println("Trying to add customers:" + customer1 + " to vehicle " + vehicleNum);
      }
      removeCustomer(customer1);
      // if (testing) {
      //   sanityCheck();
      // }
      int idx = 0;
      if (schedule[vehicleNum].size() > 0) {
        idx = rand.nextInt(schedule[vehicleNum].size());
      }
      insertCustomer(customer1, vehicleNum, idx);
      if (testing) {
        sanityCheck();
      }
    } else if (randNum == 1) {
      Customer customer2 = customers[rand.nextInt(customers.length-1) + 1];
      // System.out.println("Customer 1 Route " + customer1.getRouteId() + " Customer 2 Route " +  customer2.getRouteId());
      while (customer1 == customer2
        || routeDemands[customer1.getRouteId()] + customer2.getDemand() - customer1.getDemand() > vehicleCapacity
        || routeDemands[customer2.getRouteId()] + customer1.getDemand() - customer2.getDemand() > vehicleCapacity) {
        customer1 = customers[rand.nextInt(customers.length-1) + 1];
        customer2 = customers[rand.nextInt(customers.length-1) + 1];
        // System.out.println("Customer 1 Route IN " + customer1.getRouteId() + " Customer 2 Route IN " +  customer2.getRouteId());

        // System.out.println("Trying to swap customers:" + customer1 + " " + customer2);
      }
      swapCustomers(customer1, customer2);
      if (testing) {
        sanityCheck();
      }
    }
  }

  // public ArrayList<Integer>[] takeRandomStep(ArrayList<Integer>[] schedule) {
  //   ArrayList<Customer>[] schedule1 = copySchedule(schedule);
  //   schedule1 = takeRandomStepRemoveAndInsert(schedule1);
  //   ArrayList<Customer>[] schedule2 = copySchedule(schedule);
  //   schedule2 = takeRandomStepSwap(schedule2);
  //   if (evalSolution(schedule1) < evalSolution(schedule2)) {
  //     return schedule1;
  //   }
  //   return schedule2;
  // }

  public static void incrementPenalty() {
    penalty *= 1.2;
    penalty = Math.min(penalty, 5000);
    
  }

  // ---------------------------------------------------------- Misc Helpers ------------------------------------------------

  public int[] getPreviousAndNextCustomer(int routeId, int customerIndex) {
    ArrayList<Integer> route = schedule[routeId];
    int[] prevAndNext = new int[2];
    // System.out.println("Route: " + route + " Customer Index: " + customerIndex);
    assert customerIndex >= 0 : "Customer Index is negative";
    assert route.size() > 0 : "Route is empty";
    assert customerIndex < route.size() : "Customer Index is out of bounds";
    if (route.size() <= 1) {
      prevAndNext[0] = 0;
      prevAndNext[1] = 0;
    } else if (customerIndex == 0) {
      prevAndNext[0] = 0;
      prevAndNext[1] = route.get(1);
    } else if (customerIndex == route.size() - 1) {
      prevAndNext[0] = route.get(customerIndex - 1);
      prevAndNext[1] = 0;
    } else {
      prevAndNext[0] = route.get(customerIndex - 1);
      prevAndNext[1] = route.get(customerIndex + 1);
    }
    return prevAndNext;
  }

  public static double acceptanceProbability(double energyCurrent, double energyNew, double temperature) {
    if (energyNew < energyCurrent) {
      return 1.0;
    }
    return Math.exp((energyCurrent - energyNew) / temperature);
  }

  public boolean sanityCheck() {
    for (int i = 0; i < schedule.length; i++) {
      assert Math.round(routeDemands[i]) == Math.round(computeRouteDemand(schedule[i])) : "Schedule" + this + "Route " + i + " Demand: " + routeDemands[i] + " Computed Demand: " + computeRouteDemand(schedule[i]);
      assert Math.round(routeDistances[i]) == Math.round(computeRouteDistance(schedule[i])) : "Schedule" + this + "Route " + i + " Distance: " + routeDistances[i] + " Computed Distance: " + computeRouteDistance(schedule[i]);
      // assert Math.round(routePenalties[i]) == Math.round(computePenalty(routeDemands[i])) : "Schedule" + this + "Route " + i + " Penalty: " + routePenalties[i] + " Computed Penalty: " + computePenalty(routeDemands[i]);
      for (int j = 0; j < schedule[i].size(); j++) {
        assert customers[schedule[i].get(j)].getRouteId() == i;
      }
    }
    for (int i = 1; i < customers.length; i++) {
      assert customers[i].getRouteId() != -1;
    }
    // assert false

    return true;
  }


  // ---------------------------------------------------------- Moving Customer Helpers ------------------------------------------------
  
  public void swapCustomers(Customer c1, Customer c2) {
    if (c1 == c2) {
      return;
    }
    int routeId1 = c1.getRouteId();
    int routeId2 = c2.getRouteId();
    int c1Index = schedule[routeId1].indexOf(c1.getId());
    int c2Index = schedule[routeId2].indexOf(c2.getId());
    int[] c1NeighborIDs = getPreviousAndNextCustomer(routeId1, c1Index);
    int[] c2NeighborIDs = getPreviousAndNextCustomer(routeId2, c2Index);

    schedule[routeId1].set(c1Index, c2.getId());
    schedule[routeId2].set(c2Index, c1.getId());
    c1.setRouteId(routeId2);
    c2.setRouteId(routeId1);

    if (routeId1 == routeId2 && Math.abs(c2Index - c1Index) == 1) {
      routeDistances[routeId1] = computeRouteDistance(schedule[routeId1]);
    } else {
      routeDistances[routeId1] = routeDistances[routeId1] - distanceMatrix[c1.getId()][c1NeighborIDs[0]] - distanceMatrix[c1.getId()][c1NeighborIDs[1]] + distanceMatrix[c2.getId()][c1NeighborIDs[0]] + distanceMatrix[c2.getId()][c1NeighborIDs[1]];
      routeDistances[routeId2] = routeDistances[routeId2] - distanceMatrix[c2.getId()][c2NeighborIDs[0]] - distanceMatrix[c2.getId()][c2NeighborIDs[1]] + distanceMatrix[c1.getId()][c2NeighborIDs[0]] + distanceMatrix[c1.getId()][c2NeighborIDs[1]];
    }
    routeDemands[routeId1] = routeDemands[routeId1] - c1.getDemand() + c2.getDemand();
    routeDemands[routeId2] = routeDemands[routeId2] - c2.getDemand() + c1.getDemand();
    routePenalties[routeId1] = computePenalty(routeDemands[routeId1]);
    routePenalties[routeId2] = computePenalty(routeDemands[routeId2]);
  }

  public void removeCustomer(Customer c1) {
    int routeId = c1.getRouteId();
    int c1Index = schedule[routeId].indexOf(c1.getId());
    int[] neighborIDs = getPreviousAndNextCustomer(routeId, c1Index);
    schedule[routeId].remove(c1Index);
    c1.setRouteId(-1);
    routeDistances[routeId] = routeDistances[routeId] - distanceMatrix[c1.getId()][neighborIDs[0]] - distanceMatrix[c1.getId()][neighborIDs[1]] + distanceMatrix[neighborIDs[0]][neighborIDs[1]];
    routeDemands[routeId] -= c1.getDemand();
    routePenalties[routeId] = computePenalty(routeDemands[routeId]);
  }

  public void insertCustomer(Customer c1, int routeId, int index) {
    schedule[routeId].add(index, c1.getId());
    int[] neighborIDs = getPreviousAndNextCustomer(routeId, index);
    c1.setRouteId(routeId);
    routeDistances[routeId] = routeDistances[routeId] - distanceMatrix[neighborIDs[0]][neighborIDs[1]] + distanceMatrix[neighborIDs[0]][c1.getId()] + distanceMatrix[c1.getId()][neighborIDs[1]];
    routeDemands[routeId] += c1.getDemand();
    routePenalties[routeId] = computePenalty(routeDemands[routeId]);
  }


  // ---------------------------------------------------------- Initialization Functions ------------------------------------------------
  public static void initializeFields(VRPInstance instance) {
    Solution.vehicleCapacity = instance.vehicleCapacity;
    Solution.customers = new Customer[instance.numCustomers];
    Solution.sortedCustomers = new ArrayList<Customer>();
    for (int i = 0; i < instance.numCustomers; i++) {
      customers[i] = new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i], i, -1);
      if (i != 0) {
        sortedCustomers.add(customers[i]);
      }
    }
    // sort customers by demand, descending
    Solution.sortedCustomers.sort((a, b) -> b.getDemand() - a.getDemand());
    computeDistanceMatrix(instance);
  }
  

  
  @SuppressWarnings("unchecked")
  public static void computeDistanceMatrix(VRPInstance instance){
        double[][] distanceMatrix = new double[customers.length][customers.length];
        Solution.nearestNeighborsMatrix = new ArrayList[customers.length];
        double maxDistance = 0.0;
        double maxDemand = 0.0;
        double totalDemand = 0.0;
        double totalCapacity = instance.numVehicles * instance.vehicleCapacity;
        for (int i = 0; i < customers.length; i++) {
          Customer customerI = customers[i];
          totalDemand += customerI.getDemand();
          maxDemand = Math.max(maxDemand, customerI.getDemand());
          nearestNeighborsMatrix[i] = new ArrayList<Tuple<Double, Integer>>();
            for (int j = 0; j < customers.length ; j++) {
                Customer customerJ = customers[j];
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    // System.out.print("i: " + i + " j: " + j + " x: " + customerI.getX() + " y: " + customerI.getY() + " x: " + customerJ.getX() + " y: " + customerJ.getY() + "\n");
                    distanceMatrix[i][j] = Math.sqrt(Math.pow(customerI.getX() - customerJ.getX(), 2) + Math.pow(customerI.getY() - customerJ.getY(), 2));
                    // assert distanceMatrix[i][j] == customerI.distanceTo(j);
                    if (j != 0) {
                      nearestNeighborsMatrix[i].add(new Tuple<Double, Integer>(distanceMatrix[i][j], j));
                    }
                    maxDistance = Math.max(maxDistance, distanceMatrix[i][j]);
                }
            }
            nearestNeighborsMatrix[i].sort((a, b) -> a.getFirst().compareTo(b.getFirst()));
        }
        Solution.distanceMatrix = distanceMatrix;
        // TODO: fix this damn penalty
        Solution.penalty = Math.max(0.1, Math.min(1000, maxDistance/maxDemand));
        // System.out.println("Max Distance: " + maxDistance + " Max Demand: " + maxDemand + " Total Demand: " + totalDemand + " Total Capacity: " + totalCapacity + " Ratio: " + totalDemand/totalCapacity);
  }
  public boolean naiveGenerateInitialSolution() {
    int demand = 0;
    int[] demandLeft = new int[schedule.length];
    for (int i = 0; i < customers.length; i++) {
      demand = customers[i].getDemand();
      for (int j = 0; j < demandLeft.length; j++) {
        if (demandLeft[j] + demand <= Solution.vehicleCapacity) {
          schedule[j].add(i);
          customers[i].setRouteId(j);
          assert customers[i].getRouteId() == j;
          demandLeft[j] += demand;
          break;
        }
      }
    }
    for (int i = 0; i < schedule.length; i++) {
      routeDemands[i] = computeRouteDemand(schedule[i]);
      // System.out.println("Route " + i + " Demand: " + routeDemands[i] + " Capacity: " + Solution.vehicleCapacity);
      assert routeDemands[i] <= Solution.vehicleCapacity;
      routeDistances[i] = computeRouteDistance(schedule[i]);
      assert routeDistances[i] >= 0;
      routePenalties[i] = computePenalty(routeDemands[i]);
      assert routePenalties[i] == 0;
    }
    // for (int i = 0; i < customers.length; i++) {
    //   System.out.println("Customer " + i + " " + customers[i]);
    // }
    // System.out.println("Initial Solution: " + this);
    for (int i = 1; i < customers.length; i++) {
      if (customers[i].getRouteId() == -1) {
        return false;
      }
    }
    return true;
    //TODO: Replace this
    // for (int i = 0; i < schedule.length; i ++) {
    //   insertionHeuristics[0].applyHeuristicRoute(schedule[i]);
    // }
  }

  public void naiveGenerateGreedyInitialSolution() {
    int demand = 0;
    int[] demandLeft = new int[schedule.length];
    for (int i = 0; i < sortedCustomers.size(); i++) {
      demand = sortedCustomers.get(i).getDemand();
      for (int j = 0; j < demandLeft.length; j++) {
        if (demandLeft[j] + demand <= Solution.vehicleCapacity) {
          schedule[j].add(sortedCustomers.get(i).getId());
          sortedCustomers.get(i).setRouteId(j);
          assert sortedCustomers.get(i).getRouteId() == j;
          demandLeft[j] += demand;
          break;
        }
      }
    }
    for (int i = 0; i < schedule.length; i++) {
      routeDemands[i] = computeRouteDemand(schedule[i]);
      assert routeDemands[i] <= Solution.vehicleCapacity;
      routeDistances[i] = computeRouteDistance(schedule[i]);
      assert routeDistances[i] >= 0;
      routePenalties[i] = computePenalty(routeDemands[i]);
      assert routePenalties[i] == 0;
    }
    // for (int i = 0; i < customers.length; i++) {
    //   System.out.println("Customer " + i + " " + customers[i]);
    // }
    // System.out.println("Initial Solution: " + this);
    if (testing) {
      sanityCheck();
    }
    //TODO: Replace this
    // for (int i = 0; i < schedule.length; i ++) {
    //   insertionHeuristics[0].applyHeuristicRoute(schedule[i]);
    // }
  }


  public void applyTwoOpt(int vehichleNum) {
    ArrayList<Integer> route = schedule[vehichleNum];
    int routeSize = route.size();
    boolean improved = true;
    while (improved) {
      improved = false;
      for (int i = 0; i < routeSize - 1; i++) {
        for (int j = i + 1; j < routeSize; j++) {
          ArrayList<Integer> newRoute = new ArrayList<Integer>();
          for (int k = 0; k < i; k++) {
            newRoute.add(route.get(k));
          }
          for (int k = j; k >= i; k--) {
            newRoute.add(route.get(k));
          }
          for (int k = j + 1; k < routeSize; k++) {
            newRoute.add(route.get(k));
          }
          double newRouteDistance = computeRouteDistance(newRoute);
          if (newRouteDistance < routeDistances[vehichleNum]) {
            schedule[vehichleNum] = newRoute;
            routeDistances[vehichleNum] = newRouteDistance;
            routeDemands[vehichleNum] = computeRouteDemand(newRoute);
            routePenalties[vehichleNum] = computePenalty(routeDemands[vehichleNum]);
            improved = true;
          }
        }
      }
    }
  }
  
  // ---------------------------------------------------------- Evaluation Functions ------------------------------------------------

  public double evalSolution() {
    double totalDistance = 0.0;
    for (int i = 0; i < schedule.length; i++) {
      totalDistance += computeRouteValue(schedule[i]);
    }
    return totalDistance;
  }

  public double computeRouteDistance(ArrayList<Integer> solution) {
    double totalDistance = 0.0;
    for (int j = 0; j < solution.size(); j++) {
        if (j == 0) {
          totalDistance += customers[solution.get(j)].distanceToDepot();
          // System.out.println("Distance from customer " + customers[solution.get(j)] + " to depot: " + customers[solution.get(j)].distanceToDepot());
        } else {
          totalDistance += customers[solution.get(j)].distanceTo(solution.get(j - 1));
          // System.out.println("Distance from customer " + customers[solution.get(j)] + " to customer " + customers[solution.get(j - 1)] + ": " + customers[solution.get(j)].distanceTo(solution.get(j - 1)));
        }
      }
    if (solution.size() > 0) {
      totalDistance += customers[solution.get(solution.size() - 1)].distanceToDepot();
      // System.out.println("Distance from customer " + customers[solution.get(solution.size() - 1)] + " to depot: " + customers[solution.get(solution.size() - 1)].distanceToDepot());
    }
    return totalDistance;
  }

  public double computeRouteDemand(ArrayList<Integer> solution) {
    double totalDemand = 0.0;
    for (int j = 0; j < solution.size(); j++) {
      totalDemand += customers[solution.get(j)].getDemand();
    }
    return totalDemand;
  }

  public double computeRouteValue(ArrayList<Integer> solution) {
    return computeRouteDistance(solution) + Math.max(0.0, computeRouteDemand(solution) - Solution.vehicleCapacity) * penalty;
  }

  public double computePenalty(double demand) {
    return Math.max(0.0, demand - vehicleCapacity) * penalty;
  }

  public void recomputePenalties() {
    for (int i = 0; i < schedule.length; i++) {
      routePenalties[i] = computePenalty(routeDemands[i]);
    }
  }

  // ---------------------------------------------------------- Clone Functions ------------------------------------------------
  @SuppressWarnings("unchecked")
  public static ArrayList<Integer>[] copySchedule(ArrayList<Integer>[] schedule) {
    ArrayList<Integer>[] scheduleNew = new ArrayList[schedule.length];
    for (int i = 0; i < schedule.length; i++) {
      scheduleNew[i] = (ArrayList<Integer>)schedule[i].clone();
    }
    return scheduleNew;
  }

  @SuppressWarnings("unchecked")
  public static ArrayList<Integer> copyRoute(ArrayList<Integer> route) {
    return (ArrayList<Integer>)route.clone();
  }
  
  @SuppressWarnings("unchecked")
  public Solution clone() {
    Solution clonedSolution = new Solution();
    // Copy schedule array
    clonedSolution.schedule = new ArrayList[this.schedule.length];
    for (int i = 0; i < this.schedule.length; i++) {
        clonedSolution.schedule[i] = new ArrayList<Integer>(this.schedule[i]);
    }
    // clonedSolution.insertionHeuristics = this.insertionHeuristics;
    // clonedSolution.removalHeuristic = this.removalHeuristic;
    clonedSolution.routeDemands = this.routeDemands.clone();
    clonedSolution.routeDistances = this.routeDistances.clone();
    clonedSolution.routePenalties = this.routePenalties.clone();
    
    return clonedSolution;
  }

  public void syncCustomerRouteIDs() {
    for (int i = 0; i < schedule.length; i++) {
      for (int j = 0; j < schedule[i].size(); j++) {
        customers[schedule[i].get(j)].setRouteId(i);
      }
    }
  }

  //----------------------------------------------------------------- toString Functions ------------------------------------------------
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Vehicle Capacity: " + Solution.vehicleCapacity + "\n");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < schedule[i].size(); j++) {
        Customer c = customers[schedule[i].get(j)];
        sb.append(c);
      }
      sb.append(", Distance: " + computeRouteDistance(schedule[i]) + ", Demand: " + computeRouteDemand(schedule[i]) + "\n");
    }
    sb.append("total_distance: " + evalSolution() + "\n");
    return sb.toString();
  }

  public String printSchedule(ArrayList<Integer>[] newSchedule) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < newSchedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < newSchedule[i].size(); j++) {
        Customer c = customers[newSchedule[i].get(j)];
        sb.append(c);
      }
      sb.append("\n");
    }
    sb.append("total_distance: " + evalSolution() + "\n");
    return sb.toString();
  }

  public String submissionFormat() {
    StringBuilder sb = new StringBuilder();
    sb.append("0 ");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("0 ");
      for (int j = 0; j < schedule[i].size(); j++) {
        sb.append(schedule[i].get(j) + " ");
      }
      sb.append("0 ");
    }
    // remove last space
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public String fileOutputFormat() {
    StringBuilder sb = new StringBuilder();
    sb.append(Math.round(evalSolution()) + " 0\n");
    for (int i = 0; i < schedule.length; i++) {
      sb.append("0 ");
      for (int j = 0; j < schedule[i].size(); j++) {
        sb.append(schedule[i].get(j) + " ");
      }
      sb.append("0\n");
    }
    // remove last space
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
