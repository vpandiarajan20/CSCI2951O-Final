package solver.ls.removalHeuristics;
import java.util.ArrayList;

import solver.ls.Customer;
import solver.ls.Solution;

public class FeasibleRemoval extends RemoveHeuristic {

  public Customer removeHeuristic(ArrayList<Customer> route) {
    double demand = Solution.computeRouteDemand(route);
    if (demand >= Solution.vehicleCapacity) {
      for (int i = 0; i < route.size(); i++) {
        if (demand - route.get(i).getDemand() <= Solution.vehicleCapacity) {
          return route.remove(i);
        }
      }
    }
    int randomIndex = (int) (Math.random() * route.size());
    return route.remove(randomIndex); 
  }
}