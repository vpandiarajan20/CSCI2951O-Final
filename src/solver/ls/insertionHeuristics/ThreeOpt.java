package solver.ls.insertionHeuristics;
import java.util.ArrayList;
import solver.ls.Customer;
import solver.ls.Solution;

public class ThreeOpt extends InsertionHeuristic {

    private static void threeOptSwap(ArrayList<Customer> route, int i, int j, int k) {
        ArrayList<Customer> newRoute = new ArrayList<>();
        // Reverse segment [i, j]
        for (int a = 0; a < i; a++) {
            newRoute.add(route.get(a));
        }
        for (int a = j; a >= i; a--) {
            newRoute.add(route.get(a));
        }
        for (int a = j + 1; a <= k; a++) {
            newRoute.add(route.get(a));
        }
        for (int a = k + 1; a < route.size(); a++) {
            newRoute.add(route.get(a));
        }
        route.clear();
        route.addAll(newRoute);
    }

    // The main 3-opt algorithm
    public static void performThreeOpt(ArrayList<Customer> route) {
        boolean improvement = true;
        while (improvement) {
            double bestDistance = Solution.computeRouteDistance(route);
            improvement = false;
            for (int i = 0; i < route.size() - 2; i++) {
                for (int j = i + 1; j < route.size() - 1; j++) {
                    for (int k = j + 1; k < route.size(); k++) {
                        threeOptSwap(route, i, j, k);
                        double newDistance = Solution.computeRouteDistance(route);
                        if (newDistance < bestDistance) {
                            bestDistance = newDistance;
                            improvement = true;
                        } else {
                            // If no improvement, revert the swap
                            threeOptSwap(route, i, j, k);
                        }
                    }
                }
            }
        }
    }

    public void applyHeuristic(Customer customer, ArrayList<Customer> route) {
        route.add(customer);
        performThreeOpt(route);
    }

    public void applyHeuristicRoute(ArrayList<Customer> route) {
        performThreeOpt(route);
    }
}