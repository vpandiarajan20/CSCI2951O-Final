package solver.ls.heuristics;

import java.util.ArrayList;

import solver.ls.Customer;

public class NearestNeighbor extends Heuristic{

    public void applyHeuristic(Customer newCustomer, ArrayList<Customer> route) {
        return;
    }

    public void applyHeuristicRoute(ArrayList<Customer> route) {
        return;
    }

    // public ArrayList<Customer> nearestNeighborRoute(Customer depot, ArrayList<Customer> customers) {
        // ArrayList<Customer> route = new ArrayList<>();
        // Customer lastCustomer = depot;
        // while (customers.size() > 0) {
        //     int nearestNeighbor = findNearestNeighbor(lastCustomer, customers);
        //     route.add(lastCustomer = customers.get(nearestNeighbor));
        //     customers.remove(nearestNeighbor);
        // }
        // return route;
        // ArrayList<Customer> customers = (ArrayList<Customer>) route.clone();
        // customers.add(newCustomer);
        // ArrayList<Customer> newRoute = new ArrayList<>();
        // Customer depot = new Customer(0, 0, 0);
        // Customer lastCustomer = depot;
        // while (customers.size() > 0) {
        //     int nearestNeighbor = findNearestNeighbor(lastCustomer, customers);
        //     newRoute.add(lastCustomer = customers.get(nearestNeighbor));
        //     customers.remove(nearestNeighbor);
        // }
        // route = newRoute;
    // }

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
