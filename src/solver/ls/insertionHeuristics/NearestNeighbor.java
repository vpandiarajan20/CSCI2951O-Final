package solver.ls.insertionHeuristics;

import java.util.ArrayList;

import solver.ls.Customer;
import solver.ls.Solution;

public class NearestNeighbor extends InsertionHeuristic{

    public void applyHeuristic(Customer newCustomer, ArrayList<Customer> route) {
        int addPosition = OptimalAddition(route, newCustomer);
        route.add(addPosition, newCustomer);
    }

    @SuppressWarnings("unchecked")
    public void applyHeuristicRoute(ArrayList<Customer> route) {
        ArrayList<Customer> customers = (ArrayList<Customer>) route.clone();
        route = new ArrayList<>();
        Customer depot = new Customer(0, 0, 0, -1);
        Customer lastCustomer = depot;
        while (customers.size() > 0) {
            int nearestNeighbor = findNearestNeighbor(lastCustomer, customers);
            route.add(lastCustomer = customers.get(nearestNeighbor));
            customers.remove(nearestNeighbor);
        }
    }

  @SuppressWarnings("unchecked")
  public int OptimalAddition(ArrayList<Customer> route, Customer c) {
    // find the best position to add customer c to vehicle
    // return the position
    // System.out.println("OptimalAddition called");
    int bestPosition = -1;
    double bestDistance = Double.MAX_VALUE;
    int routeSize = route.size();
    if (routeSize == 0) {
      return 0;
    }
    for (int i = 0; i < routeSize; i++) {
      ArrayList<Customer> scheduleNew = (ArrayList<Customer>) route.clone();
      scheduleNew.add(i, c);
      double distance = Solution.computeRouteDistance(scheduleNew); // could be optimized
      if (distance < bestDistance) {
        bestDistance = distance;
        bestPosition = i;
      }
    }
    return bestPosition;
  }

  public int findNearestNeighbor(Customer customer, ArrayList<Customer> route) {
      int nearestNeighbor = -1;
      double minDistance = Double.MAX_VALUE;
      for (int i = 0; i < route.size(); i++) {
          if (route.get(i).distanceTo(customer) < minDistance) {
              minDistance = route.get(i).distanceTo(customer);
              nearestNeighbor = i;
          }
      }
      return nearestNeighbor;
  }

}
