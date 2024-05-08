package solver.ls.heuristics;
import java.util.ArrayList;

import solver.ls.Customer;
import solver.ls.Solution;

public class TwoOpt extends Heuristic {

    private static void twoOptSwap(ArrayList<Customer> route, int i, int k) {
        while (i < k) {
          Customer tmp = route.get(i).clone();
          route.set(i, route.get(k));
          route.set(k, tmp);
          i ++;
          k --;
        }
    }

    // The main 2-opt algorithm
    public static void performTwoOpt(ArrayList<Customer> route) {
        boolean improvement = true;
        while (improvement) {
          double bestDistance = Solution.computeRouteDistance(route);
          improvement = false;
          for (int i = 0; i < route.size() - 1; i++) {
            for (int k = i + 1; k < route.size(); k++) {
              twoOptSwap(route, i, k);
              double newDistance = Solution.computeRouteDistance(route);
              if (newDistance < bestDistance) {
                bestDistance = newDistance;
                improvement = true;
              } else { 
                // If no improvement, revert the swap
                twoOptSwap(route, i, k);
              }
            }
          }
        }
    }
    
    public void applyHeuristic(Customer customer, ArrayList<Customer> route) {
      route.add(customer);
      performTwoOpt(route);
    }

    public void applyHeuristicRoute( ArrayList<Customer> route) {
      performTwoOpt(route);
    }

}