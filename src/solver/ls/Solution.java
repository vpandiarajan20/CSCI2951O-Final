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
    boolean intraRouteMove = false;
    int numNodesToSearch = Math.min(8, customers.length-1);
    int numNeighborsToSearch = Math.min(15, nearestNeighborsMatrix[1].size());

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
          intraRouteMove = customer1.getRouteId() == customer2.getRouteId();
          // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2);
          // debug move2
          // System.out.println("Attempting Moves");
          if (move1(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              assert customer1.getRouteId() == customer2.getRouteId();
              assert schedule[customer1.getRouteId()].contains(customer1.getId());
              assert schedule[customer1.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          // } else if (move2(customer1, customer2) || (testing && sanityCheck() && !testing)) {
          //   if (testing) {
          //     assert customer1.getRouteId() == customer2.getRouteId();
          //     assert schedule[customer1.getRouteId()].contains(customer1.getId());
          //     assert schedule[customer1.getRouteId()].contains(customer2.getId());
          //     sanityCheck();
          //   }
          //   searchCompleted = false;
          //   continue;
          } else if (move3(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              assert customer1.getRouteId() == customer2.getRouteId();
              assert schedule[customer1.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          } else if (move4(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              // assert customer1.getRouteId() == customer2.getRouteId();
              // assert schedule[customer1.getRouteId()].contains(customer1.getId());
              // assert schedule[customer1.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          } else if (move5(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              // assert customer1.getRouteId() == customer2.getRouteId();
              // assert schedule[customer1.getRouteId()].contains(customer1.getId());
              // assert schedule[customer1.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          } else if (move6(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              // assert customer1.getRouteId() == customer2.getRouteId();
              // assert schedule[customer1.getRouteId()].contains(customer1.getId());
              // assert schedule[customer2.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          } else if (move78(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              assert customer1.getRouteId() == customer2.getRouteId();
              assert schedule[customer1.getRouteId()].contains(customer1.getId());
              assert schedule[customer2.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          } else if (!intraRouteMove && move9(customer1, customer2) || (testing && sanityCheck() && !testing)) {
            if (testing) {
              // assert customer1.getRouteId() == customer2.getRouteId();
              assert schedule[customer1.getRouteId()].contains(customer1.getId());
              assert schedule[customer2.getRouteId()].contains(customer2.getId());
              sanityCheck();
            }
            searchCompleted = false;
            continue;
          }
          
          ArrayList<Integer> route = schedule[customer2.getRouteId()];
          int customer2Index = route.indexOf(customer2ID);         
          if (customer2Index == 0) {
            if (move1b(customer1, customer2)) {
              // System.out.println("Move 1b: " + customer1 + " " + customer2);
              // System.out.println(this);
              if (testing) {
                sanityCheck();
              }
              searchCompleted = false;
              continue;
            }
          }
        }
        for (int j = 0; j < schedule.length; j++) {
          if (schedule[0].size() == 0) {
            if (move1c(customer1, i)) {
              if (testing) {
                sanityCheck();
              }
              searchCompleted = false;
              continue;
            }
            break;
          }
        }
      }
    }
  }


  // ---------------------------------------------------------- Move Functions ------------------------------------------------

  @SuppressWarnings("unchecked")
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

    if (customer2NeighborIDs[1] == customer1.getId()) {
      return false;
    }

    double removeC1Delta = distanceMatrix[customer1NeighborIDs[0]][customer1NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
    double addC1Delta = distanceMatrix[customer1.getId()][customer2.getId()] + distanceMatrix[customer1.getId()][customer2NeighborIDs[1]] - distanceMatrix[customer2.getId()][customer2NeighborIDs[1]];

    if (customer1.getRouteId() == customer2.getRouteId()) {
      if (addC1Delta + removeC1Delta < 0.01f) {
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
        if (addC1Delta + removeC1Delta + removeC1Penalties + addC1Penalties < 0) {
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

  public boolean move1b(Customer customer1, Customer customer2) {
    // remove customer1 from route1, add customer1 before customer2 (at the start of the thing)
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];

    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    if (customer2NeighborIDs[0] == customer1.getId()) {
      return false;
    }

    double removeC1Delta = distanceMatrix[customer1NeighborIDs[0]][customer1NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
    double addC1Delta = distanceMatrix[customer1.getId()][0] + distanceMatrix[customer1.getId()][customer2.getId()] - distanceMatrix[customer2.getId()][0];
    if (removeC1Delta + addC1Delta < routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()]) {
      double removeC1Penalties = computePenalty(routeDemands[customer1.getRouteId()] - customer1.getDemand()) - routePenalties[customer1.getRouteId()];
      double addC1Penalties = computePenalty(routeDemands[customer2.getRouteId()] + customer1.getDemand()) - routePenalties[customer2.getRouteId()];
      if (addC1Delta + removeC1Delta + removeC1Penalties + addC1Penalties < 0.01f) {
        route1.remove(customer1Index);
        route2.add(0, customer1.getId());
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

  public boolean move1c(Customer customer1, int emptyRouteId) {
    // remove customer1 from route1, add customer1 before customer2 (at the start of the thing)
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[emptyRouteId];

    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    double removeC1Delta = distanceMatrix[customer1NeighborIDs[0]][customer1NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
    double addC1Delta = distanceMatrix[customer1.getId()][0] * 2.0;
    if (removeC1Delta + addC1Delta < routePenalties[customer1.getId()]) {
      double removeC1Penalties = computePenalty(routeDemands[customer1.getRouteId()] - customer1.getDemand()) - routePenalties[customer1.getRouteId()];
      if (addC1Delta + removeC1Delta + removeC1Penalties < 0.01f) {
        route1.remove(customer1Index);
        route2.add(0, customer1.getId());
        routeDistances[customer1.getRouteId()] += removeC1Delta;
        routeDistances[emptyRouteId] += addC1Delta;
        routeDemands[customer1.getRouteId()] -= customer1.getDemand();
        routeDemands[emptyRouteId] += customer1.getDemand();
        routePenalties[customer1.getRouteId()] = computePenalty(routeDemands[customer1.getRouteId()]);
        routePenalties[emptyRouteId] = computePenalty(routeDemands[emptyRouteId]);
        customer1.setRouteId(emptyRouteId);
        return true;
      }
    }
    return false;
  }

  public boolean move2(Customer customer1, Customer customer2) {
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];

    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    if (customer2NeighborIDs[1] == customer1.getId() || customer2.getId() == customer1NeighborIDs[1] || customer1NeighborIDs[1] == 0) {
      return false;
    }


    int[] customer3NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index + 1);
    assert customer3NeighborIDs[0] == customer1.getId();
    Customer customer3 = customers[customer1NeighborIDs[1]];

    double removeC1C3Delta = distanceMatrix[customer1NeighborIDs[0]][customer3NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1NeighborIDs[1]][customer3NeighborIDs[1]];
    double addC1C3Delta = distanceMatrix[customer1.getId()][customer2.getId()] + distanceMatrix[customer1NeighborIDs[1]][customer2NeighborIDs[1]] - distanceMatrix[customer2.getId()][customer2NeighborIDs[1]];
    if (customer1.getRouteId() == customer2.getRouteId()) {
      if (addC1C3Delta + removeC1C3Delta < 0.01f) {
        route1.remove(customer1Index); // remove customer1
        route1.remove(customer1Index); // remove customer3
        customer2Index = route2.indexOf(customer2.getId());
        route2.add(customer2Index + 1, customer1.getId());
        route2.add(customer2Index + 2, customer1NeighborIDs[1]);
        assert route2.indexOf(customer3.getId()) != -1;
        assert route2.indexOf(customer1.getId()) != -1;
        assert route2.indexOf(customer3.getId()) - 1 == route2.indexOf(customer1.getId());
        routeDistances[customer1.getRouteId()] += addC1C3Delta + removeC1C3Delta;
        assert Math.abs(routeDistances[customer1.getRouteId()] - computeRouteDistance(route1)) < 0.01f;
        return true;
      }
      return false;
    } else {
      if (removeC1C3Delta + addC1C3Delta < routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()]) {
        double removeC1C3Penalties = computePenalty(routeDemands[customer1.getRouteId()] - customer1.getDemand() - customer3.getDemand()) - routePenalties[customer1.getRouteId()];
        double addC1C3Penalties = computePenalty(routeDemands[customer2.getRouteId()] + customer1.getDemand() + customer3.getDemand()) - routePenalties[customer2.getRouteId()];
        if (addC1C3Delta + removeC1C3Delta + removeC1C3Penalties + addC1C3Penalties < 0) {
          route1.remove(customer1Index);
          route1.remove(customer1Index);
          route2.add(customer2Index + 1, customer1.getId());
          route2.add(customer2Index + 2, customer1NeighborIDs[1]);
          assert route2.indexOf(customer3.getId()) != -1;
          assert route2.indexOf(customer1.getId()) != -1;
          assert route2.indexOf(customer3.getId()) - 1 == route2.indexOf(customer1.getId());
          routeDistances[customer1.getRouteId()] += removeC1C3Delta - distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
          routeDistances[customer2.getRouteId()] += addC1C3Delta + distanceMatrix[customer1.getId()][customer1NeighborIDs[1]];
          assert Math.abs(routeDistances[customer1.getRouteId()] - computeRouteDistance(route1)) < 0.01f;
          assert Math.abs(routeDistances[customer2.getRouteId()] - computeRouteDistance(route2)) < 0.01f;
          routeDemands[customer1.getRouteId()] -= customer1.getDemand() - customer3.getDemand();
          routeDemands[customer2.getRouteId()] += customer1.getDemand() + customer3.getDemand();
          routePenalties[customer1.getRouteId()] = computePenalty(routeDemands[customer1.getRouteId()]);
          routePenalties[customer2.getRouteId()] = computePenalty(routeDemands[customer2.getRouteId()]);
          customer1.setRouteId(customer2.getRouteId());
          customer3.setRouteId(customer2.getRouteId());
          return true;
        }
      }
      return false;
    }
  }

  public boolean move3(Customer customer1, Customer customer2) {
    // remove customer1 from route1, add (customer1 & customer after customer1) after customer2
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];

    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    if (customer1.getId() == customer2NeighborIDs[1] || customer2.getId() == customer1NeighborIDs[1] || customer1NeighborIDs[1] == 0) {
      return false;
    }


    int[] customer3NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index + 1);
    assert customer3NeighborIDs[0] == customer1.getId();
    Customer customer3 = customers[customer1NeighborIDs[1]];

    double removeC1C3Delta = distanceMatrix[customer1NeighborIDs[0]][customer3NeighborIDs[1]] - distanceMatrix[customer1.getId()][customer1NeighborIDs[0]] - distanceMatrix[customer1.getId()][customer3.getId()] - distanceMatrix[customer3.getId()][customer3NeighborIDs[1]];
    double addC1C3Delta = distanceMatrix[customer2.getId()][customer3.getId()] + distanceMatrix[customer3.getId()][customer1.getId()] + distanceMatrix[customer1.getId()][customer2NeighborIDs[1]] - distanceMatrix[customer2.getId()][customer2NeighborIDs[1]];
    if (customer1.getRouteId() == customer2.getRouteId()) {
      if (addC1C3Delta + removeC1C3Delta < 0.01f) {
        route1.remove(customer1Index); // remove customer1
        route1.remove(customer1Index); // remove customer3
        customer2Index = route2.indexOf(customer2.getId());
        route2.add(customer2Index + 1, customer3.getId());
        route2.add(customer2Index + 2, customer1.getId());
        assert route2.indexOf(customer3.getId()) != -1;
        assert route2.indexOf(customer1.getId()) != -1;
        assert route2.indexOf(customer3.getId()) + 1 == route2.indexOf(customer1.getId());
        routeDistances[customer1.getRouteId()] += addC1C3Delta + removeC1C3Delta;
        assert routeDemands[customer1.getRouteId()] == computeRouteDemand(route1);
        assert Math.abs(routeDistances[customer1.getRouteId()] - computeRouteDistance(route1)) < 0.01f;
        return true;
      }
      return false;
    } else {
      if (removeC1C3Delta + addC1C3Delta < routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()]) {
        double removeC1C3Penalties = computePenalty(routeDemands[customer1.getRouteId()] - customer1.getDemand() - customer3.getDemand()) - routePenalties[customer1.getRouteId()];
        double addC1C3Penalties = computePenalty(routeDemands[customer2.getRouteId()] + customer1.getDemand() + customer3.getDemand()) - routePenalties[customer2.getRouteId()];
        if (addC1C3Delta + removeC1C3Delta + removeC1C3Penalties + addC1C3Penalties < 0) {
          route1.remove(customer1Index);
          route1.remove(customer1Index);
          route2.add(customer2Index + 1, customer3.getId());
          route2.add(customer2Index + 2, customer1.getId());
          assert route2.indexOf(customer3.getId()) != -1;
          assert route2.indexOf(customer1.getId()) != -1;
          assert route2.indexOf(customer3.getId()) + 1 == route2.indexOf(customer1.getId());
          routeDistances[customer1.getRouteId()] += removeC1C3Delta;
          routeDistances[customer2.getRouteId()] += addC1C3Delta;
          routeDemands[customer1.getRouteId()] -= customer1.getDemand() + customer3.getDemand();
          routeDemands[customer2.getRouteId()] += customer1.getDemand() + customer3.getDemand();
          assert routeDemands[customer1.getRouteId()] == computeRouteDemand(route1);
          assert routeDemands[customer2.getRouteId()] == computeRouteDemand(route2);
          routePenalties[customer1.getRouteId()] = computePenalty(routeDemands[customer1.getRouteId()]);
          routePenalties[customer2.getRouteId()] = computePenalty(routeDemands[customer2.getRouteId()]);
          customer1.setRouteId(customer2.getRouteId());
          customer3.setRouteId(customer2.getRouteId());
          return true;
        }
      }
      return false;
    }
  }

  public boolean move4(Customer customer1, Customer customer2) {
    // swap customer1 & customer2
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];
    
    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    if (customer1.getId() == customer2NeighborIDs[0] || customer1.getId() == customer2NeighborIDs[1]) {
      return false;
    }

    double beforeCost = routeDistances[customer1.getRouteId()] + routeDistances[customer2.getRouteId()]  + routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()];
    swapCustomers(customer1, customer2);
    double afterCost = routeDistances[customer1.getRouteId()] + routeDistances[customer2.getRouteId()]  + routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()];
    if (beforeCost > afterCost) {
      return true;
    }
    swapCustomers(customer1, customer2);
    return false;
  }

  public boolean move5(Customer customer1, Customer customer2) {
    // if U X -- V -> V -- U X
    ArrayList<Integer> route1 = schedule[customer1.getRouteId()];
    ArrayList<Integer> route2 = schedule[customer2.getRouteId()];
    
    int customer1Index = route1.indexOf(customer1.getId());
    assert customer1Index != -1;
    int customer2Index = route2.indexOf(customer2.getId());
    assert customer1Index != -1;

    int[] customer1NeighborIDs = getPreviousAndNextCustomer(customer1.getRouteId(), customer1Index);
    int[] customer2NeighborIDs = getPreviousAndNextCustomer(customer2.getRouteId(), customer2Index);

    Customer customer3 = customers[customer1NeighborIDs[1]];

    if (customer1.getId() == customer2NeighborIDs[0] || customer3.getId() == customer2NeighborIDs[0] || customer1.getId() == customer2NeighborIDs[1] || customer3.getId() == 0) {
      return false;
    }

    double beforeCost = routeDistances[customer1.getRouteId()] + routeDistances[customer2.getRouteId()]  + routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()];
    swapCustomers(customer1, customer2);
    removeCustomer(customer3);
    customer1Index = route2.indexOf(customer1.getId());
    insertCustomer(customer3, customer1.getRouteId(), customer1Index+1);
    assert route2.indexOf(customer3.getId()) != -1;
    assert route2.indexOf(customer3.getId()) - 1 == route2.indexOf(customer1.getId());
    double afterCost = routeDistances[customer1.getRouteId()] + routeDistances[customer2.getRouteId()]  + routePenalties[customer1.getRouteId()] + routePenalties[customer2.getRouteId()];
    if (beforeCost > afterCost) {
      return true;
    }
    swapCustomers(customer1, customer2);
    removeCustomer(customer3);
    customer1Index = route1.indexOf(customer1.getId());
    insertCustomer(customer3, customer1.getRouteId(), customer1Index+1);
    assert route1.indexOf(customer3.getId()) != -1;
    assert route1.indexOf(customer3.getId()) - 1 == route1.indexOf(customer1.getId());
    return false;
  }






  // ------------------------ old take random step ------------------------ 

  // public void takeRandomStep() {
  //   Random rand = new Random();
  //   int randNum = rand.nextInt(2);
  //   Customer customer1 = customers[rand.nextInt(customers.length-1) + 1];
  //   if (randNum == 0) {
  //     int vehicleNum = rand.nextInt(schedule.length);
  //     while (vehicleNum != customer1.getRouteId() && routeDemands[vehicleNum] + customer1.getDemand() > vehicleCapacity) {
  //       customer1 = customers[rand.nextInt(customers.length-1) + 1];
  //       vehicleNum = rand.nextInt(schedule.length);
  //       // System.out.println("Trying to add customers:" + customer1 + " to vehicle " + vehicleNum);
  //     }
  //     removeCustomer(customer1);
  //     if (testing) {
  //       sanityCheck();
  //     }
  //     int idx = 0;
  //     if (schedule[vehicleNum].size() > 0) {
  //       idx = rand.nextInt(schedule[vehicleNum].size());
  //     }
  //     insertCustomer(customer1, vehicleNum, idx);
  //     if (testing) {
  //       sanityCheck();
  //     }
  //   } else if (randNum == 1) {
  //     Customer customer2 = customers[rand.nextInt(customers.length-1) + 1];
  //     while (customer1 == customer2
  //      || routeDemands[customer1.getRouteId()] + customer2.getDemand() - customer1.getDemand() > vehicleCapacity
  //       || routeDemands[customer2.getRouteId()] + customer1.getDemand() - customer2.getDemand() > vehicleCapacity) {
  //       customer1 = customers[rand.nextInt(customers.length-1) + 1];
  //       customer2 = customers[rand.nextInt(customers.length-1) + 1];
  //       // System.out.println("Trying to swap customers:" + customer1 + " " + customer2);
  //     }
  //     swapCustomers(customer1, customer2);
  //     if (testing) {
  //       sanityCheck();
  //     }
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

  //

  public boolean move6(Customer customerU, Customer customerV) { 
    // if U X -- V Y -> V Y -- U X -- they do something checking if 
    int routeIDU = customerU.getRouteId();
    int routeIDV = customerV.getRouteId();
    
    ArrayList<Integer> routeU = schedule[routeIDU];
    ArrayList<Integer> routeV = schedule[routeIDV];
    // assert routeU == routeV; -- this is only move 7

    int customerUIndex = routeU.indexOf(customerU.getId());
    assert customerUIndex != -1;
    int customerVIndex = routeV.indexOf(customerV.getId());
    assert customerVIndex != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customerUNeighborIDs = getPreviousAndNextCustomer(routeIDU, customerUIndex);
    int[] customerVNeighborIDs = getPreviousAndNextCustomer(routeIDV, customerVIndex);

    // if X is V then switch does nothing or both have nothing or either U or X is at end
    if (customerUNeighborIDs[1] == 0 || customerVNeighborIDs[1] == 0) {
      return false;
    }
    // if route is the same for both, then V == U does nothing 
    if (routeIDU == routeIDV && customerU.getId() == customerV.getId()) {
      return false;
    }

    Customer customerX = customers[customerUNeighborIDs[1]];
    Customer customerY = customers[customerVNeighborIDs[1]];

    // if routes are the same it just doubles the cost
    double costBefore = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];
    swapCustomers(customerU, customerV);
    swapCustomers(customerX, customerY);
    double costAfter = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];

    if (costBefore > costAfter) {
      return true;
    }
    swapCustomers(customerU, customerV);
    swapCustomers(customerX, customerY);
    return false;
  }

  public boolean move78(Customer customerU, Customer customerV) { 
    // if U X -- V Y -> U V -- X Y
    // works for both if on same route or on different routes
    int routeIDU = customerU.getRouteId();
    int routeIDV = customerV.getRouteId();
    
    ArrayList<Integer> routeU = schedule[routeIDU];
    ArrayList<Integer> routeV = schedule[routeIDV];
    // assert routeU == routeV; -- this is only move 7

    int customerUIndex = routeU.indexOf(customerU.getId());
    assert customerUIndex != -1;
    int customerVIndex = routeV.indexOf(customerV.getId());
    assert customerVIndex != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customerUNeighborIDs = getPreviousAndNextCustomer(routeIDU, customerUIndex);
    int[] customerVNeighborIDs = getPreviousAndNextCustomer(routeIDV, customerVIndex);

    // if X is V then switch does nothing or both have nothing or either U or X is at end 
    if (customerUNeighborIDs[1] == 0 || customerVNeighborIDs[1] == 0) {
      return false;
    }
    // if route is the same for both, then V == X does nothing 
    if (routeIDU == routeIDV && customerUNeighborIDs[1] == customerV.getId()) {
      return false;
    }
    
    Customer customerX = customers[customerUNeighborIDs[1]];

    // if routes are the same it just doubles the cost
    double costBefore = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];
    swapCustomers(customerV, customerX);
    double costAfter = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];

    if (costBefore > costAfter) {
      return true;
    }
    swapCustomers(customerX, customerV);
    return false;
  }
  
  public boolean move9(Customer customerU, Customer customerV) { 
    // if U X -- V Y -> U Y -- V X
    // works for both if on same route or on different routes
    int routeIDU = customerU.getRouteId();
    int routeIDV = customerV.getRouteId();
    
    ArrayList<Integer> routeU = schedule[routeIDU];
    ArrayList<Integer> routeV = schedule[routeIDV];
    assert routeIDU != routeIDV;

    int customerUIndex = routeU.indexOf(customerU.getId());
    assert customerUIndex != -1;
    int customerVIndex = routeV.indexOf(customerV.getId());
    assert customerVIndex != -1;

    // System.out.println("Customer 1: " + customer1 + " Customer 2: " + customer2 + " Route 1: " + route1 + " Route 2: " + route2 + " Customer 1 Index: " + customer1Index + " Customer 2 Index: " + customer2Index);
    int[] customerUNeighborIDs = getPreviousAndNextCustomer(routeIDU, customerUIndex);
    int[] customerVNeighborIDs = getPreviousAndNextCustomer(routeIDV, customerVIndex);

    // either U or V is at end then this cant work
    if (customerUNeighborIDs[1] == 0 || customerVNeighborIDs[1] == 0) {
      return false;
    }
    Customer customerX = customers[customerUNeighborIDs[1]];
    Customer customerY = customers[customerVNeighborIDs[1]];

    double costPrune = distanceMatrix[customerU.getId()][customerY.getId()] + distanceMatrix[customerV.getId()][customerX.getId()] - distanceMatrix[customerU.getId()][customerX.getId()] - distanceMatrix[customerV.getId()][customerY.getId()]
		        - routePenalties[routeIDU] - routePenalties[routeIDV]; // pruning but way more jank bc after hella shit

    if (costPrune >= 0) {
      return false;
    }
    
    double costBefore = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];
    swapCustomers(customerX, customerY);
    double costAfter = routeDistances[routeIDU]+ routePenalties[routeIDU] + routeDistances[routeIDV]+ routePenalties[routeIDV];

    if (costBefore > costAfter) {
      return true;
    }
    swapCustomers(customerY, customerX);
    return false;
  }

  
  public static void incrementPenalty() {
    penalty *= 1.2;
    penalty = Math.min(penalty, 5000);
    
  }


  // ---------------------------------------------------------- Misc Helpers ------------------------------------------------

  public int[] getPreviousAndNextCustomer(int routeId, int customerIndex) {
    ArrayList<Integer> route = schedule[routeId];
    int[] prevAndNext = new int[2];
    // System.out.println("Route: " + route + " Customer Index: " + customerIndex);
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
      assert Math.round(routePenalties[i]) == Math.round(computePenalty(routeDemands[i])) : "Schedule" + this + "Route " + i + " Penalty: " + routePenalties[i] + " Computed Penalty: " + computePenalty(routeDemands[i]);
      for (int j = 0; j < schedule[i].size(); j++) {
        assert customers[schedule[i].get(j)].getRouteId() == i;
      }
    }
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
        Solution.penalty = Math.max(15, Math.min(1000, maxDistance/maxDemand));
        System.out.println("Max Distance: " + maxDistance + " Max Demand: " + maxDemand + " Total Demand: " + totalDemand + " Total Capacity: " + totalCapacity + " Ratio: " + totalDemand/totalCapacity);
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
