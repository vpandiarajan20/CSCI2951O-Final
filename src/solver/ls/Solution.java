package solver.ls;
import java.util.ArrayList;
import java.util.Random;


public class Solution {


  // ---------------------------------------------------------- Fields ------------------------------------------------
  public static int vehicleCapacity;
  static double[][] distanceMatrix; 
  static ArrayList<Tuple<Double, Integer>>[] nearestNeighborsMatrix;
  static Customer[] customers;
  static ArrayList<Customer> sortedCustomers;
  static double penalty = 0.0;
  static boolean testing = true; // TODO: Remember to disable

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
    naiveGenerateInitialSolution();
  }


  // ---------------------------------------------------------- Iterative Functions ------------------------------------------------




  public void takeRandomStep() {
    boolean searchCompleted = false;
    Random rand = new Random();
    int numNodesToSearch = Math.min(6, customers.length-1);
    int numNeighborsToSearch = Math.min(10, nearestNeighborsMatrix[1].size());
    

    while (!searchCompleted) {
      searchCompleted = true;
      for (int i = 0; i < numNodesToSearch; i++) {
        Customer customer1 = customers[rand.nextInt(customers.length-1) + 1];
        // TODO: should not select the same customer twice
        for (int j = 0; j < numNeighborsToSearch; j++) {
          // selecting customer 2
          int customer2ID = Solution.nearestNeighborsMatrix[customer1.getId()].get(j).getSecond();
          if (customer2ID == customer1.getId()) {
            throw new RuntimeException("Customer 2 is the same as customer 1, you're an idiot");
          }
          Customer customer2 = customers[customer2ID];
          assert customer2.getId() == customer2ID;
          
          // System.out.println("Attempting Moves");
          if (move1(customer1, customer2)) {
            searchCompleted = false;
            continue;
          }
        }
      }
    }
  }

  // @SuppressWarnings("unchecked")
  public boolean move1(Customer customer1, Customer customer2) {
    // remove customer1 from route1, add customer1 to route2, after customer2
    
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];

    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    double removeC1Delta = distanceMatrix[customer1NeighborIDs[0]][customer1NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
    double addC1Delta = distanceMatrix[customer1.getId()][customer2.getId()] + distanceMatrix[customer1.getId()][customer2NeighborIDs[1]] - distanceMatrix[customer2.getId()][customer2NeighborIDs[1]];

    if (customer1.getRouteId() == customer2.getRouteId()) {
      if (addC1Delta + removeC1Delta < 0) {
        route1.remove(customer1Index);
        customer2Index = route2.indexOf(customer2.getId());
        route1.add(customer2Index + 1, customer1.getId());
        routeDistances[customer1.getRouteId()] += addC1Delta + removeC1Delta;
        return true;
      }
      return false;
    } else {
      if (addC1Delta + removeC1Delta < routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()]) {
        double removeC1Penalties = computePenalty(routeDemands[customer1.getRouteId()] - customer1.getDemand()) - routePenalties[customer1.getRouteId()];
        double addC1Penalties = computePenalty(routeDemands[customer2.getRouteId()] + customer1.getDemand()) - routePenalties[customer2.getRouteId()];
        if (addC1Delta + removeC1Delta < removeC1Penalties + addC1Penalties) {
          route1.remove(customer1Index);
          route2.add(customer2Index + 1, customer1.getId());
          routeDistances[customer1.getRouteId()] += removeC1Delta;
          routeDistances[customer2.getRouteId()] += addC1Delta;
          routeDemands[customer1.getRouteId()] -= customer1.getDemand();
          routeDemands[customer2.getRouteId()] += customer1.getDemand();
          routePenalties[customer1.getRouteId()] = computePenalty(routeDemands[customer1.getRouteId()]);
          routePenalties[customer2.getRouteId()] = computePenalty(routeDemands[customer2.getRouteId()]);
          customer1.setRouteId(customer2.getRouteId());
          return true;
        }
      }
      return false;
    }
  }

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
    penalty = Math.min(penalty, 100000);
  }


  // ---------------------------------------------------------- Misc Helpers ------------------------------------------------

  public int[] getPreviousAndNextCustomer(int routeId, int customerIndex) {
    ArrayList<Integer> route = schedule[routeId];
    int[] prevAndNext = new int[2];
    // System.out.println("Route: " + route + " Customer Index: " + customerIndex);
    assert route.size() > 0;
    if (route.size() == 1) {
      prevAndNext[0] = 0;
      prevAndNext[1] = 0;
    } else if (customerIndex == 0) {
      prevAndNext[0] = 0;
      prevAndNext[1] = route.get(1);
    } else if (customerIndex == route.size() - 1) {
      prevAndNext[0] = route.get(route.size() - 2);
      prevAndNext[1] = 0;
    } else {
      prevAndNext[0] = route.get(customerIndex - 1);
      prevAndNext[1] = route.get(customerIndex + 1);
    }
    prevAndNext[0] = Math.max(0, prevAndNext[0]);
    return prevAndNext;
  }

  public double computePenalty(double demand) {
    return Math.max(0.0, demand - vehicleCapacity) * penalty;
  }

  public static double acceptanceProbability(double energyCurrent, double energyNew, double temperature) {
    if (energyNew < energyCurrent) {
      return 1.0;
    }
    return Math.exp((energyCurrent - energyNew) / temperature);
  }

  public void sanityCheck() {
    for (int i = 0; i < schedule.length; i++) {
      assert routeDemands[i] == computeRouteDemand(schedule[i]);
      assert routeDistances[i] == computeRouteDistance(schedule[i]);
      assert routePenalties[i] == computePenalty(routeDemands[i]);
      for (int j = 0; j < schedule[i].size(); j++) {
        assert customers[schedule[i].get(j)].getRouteId() == i;
      }
    }

  }


  // ---------------------------------------------------------- Initialization Functions ------------------------------------------------
  public static void initializeFields(VRPInstance instance) {
    Solution.vehicleCapacity = instance.vehicleCapacity;
    Solution.customers = new Customer[instance.numCustomers + 1];
    Solution.sortedCustomers = new ArrayList<Customer>();
    customers[0] = new Customer(0, 0, 0, 0, 0);
    for (int i = 1; i < instance.numCustomers + 1; i++) {
      customers[i] = new Customer(instance.xCoordOfCustomer[i-1], instance.yCoordOfCustomer[i-1], instance.demandOfCustomer[i-1], i, -1);
      sortedCustomers.add(customers[i]);
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
                    // System.out.print("i: " + i + " j: " + j + " x: " + instance.xCoordOfCustomer[i] + " y: " + instance.yCoordOfCustomer[i] + " x: " + instance.xCoordOfCustomer[j] + " y: " + instance.yCoordOfCustomer[j] + "\n");
                    distanceMatrix[i][j] = Math.sqrt(Math.pow(customerI.getX() - customerJ.getX(), 2) + Math.pow(customerI.getY() - customerJ.getY(), 2));
                    if (j != 0) {
                      nearestNeighborsMatrix[i].add(new Tuple<Double, Integer>(distanceMatrix[i][j], j));
                    }
                    maxDistance = Math.max(maxDistance, distanceMatrix[i][j]);
                }
            }
            nearestNeighborsMatrix[i].sort((a, b) -> a.getFirst().compareTo(b.getFirst()));
        }
        Solution.distanceMatrix = distanceMatrix;
        Solution.penalty = Math.max(0.1, Math.min(1000, maxDistance/maxDemand));
        // System.out.println("Max Distance: " + maxDistance + " Max Demand: " + maxDemand + " Total Demand: " + totalDemand + " Total Capacity: " + totalCapacity + " Ratio: " + totalDemand/totalCapacity);
  }
  
  public void naiveGenerateInitialSolution() {
    int demand = 0;
    int[] demandLeft = new int[schedule.length];
    for (int i = 1; i < customers.length; i++) {
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
      assert routeDemands[i] <= Solution.vehicleCapacity;
      routeDistances[i] = computeRouteDistance(schedule[i]);
      assert routeDistances[i] >= 0;
      routePenalties[i] = computePenalty(routeDemands[i]);
      assert routePenalties[i] == 0;
    }
    // TODO: Fill demand, distance, and penalty arrays
    
    //TODO: Replace this
    // for (int i = 0; i < schedule.length; i ++) {
    //   insertionHeuristics[0].applyHeuristicRoute(schedule[i]);
    // }
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
        } else {
          totalDistance += customers[solution.get(j)].distanceTo(solution.get(j - 1));
        }
      }
    if (solution.size() > 0) {
      totalDistance += customers[solution.get(solution.size() - 1)].distanceToDepot();
    }
    return totalDistance;
  }

  public double computeRouteDemand(ArrayList<Integer> solution) {
    double totalDemand = 0.0;
    for (int j = 0; j < solution.size(); j++) {
      totalDemand += customers[solution.get(j)].getDemand();
    }
    assert totalDemand == routeDemands[customers[solution.get(0)].getRouteId()];
    assert customers[solution.get(0)].getRouteId() == customers[solution.get(solution.size() - 1)].getRouteId();
    return totalDemand;
  }

  public double computeRouteValue(ArrayList<Integer> solution) {
    return computeRouteDistance(solution) + Math.max(0.0, computeRouteDemand(solution) - Solution.vehicleCapacity) * penalty;
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
    
    return clonedSolution;
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
    sb.append(Math.round(evalSolution()) + " 0 ");
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
