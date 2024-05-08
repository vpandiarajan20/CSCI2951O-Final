package solver.ls.removalHeuristics;
import java.util.ArrayList;

import solver.ls.Customer;

public class RandomRemoval extends RemoveHeuristic {

  public Customer removeHeuristic(ArrayList<Customer> route) {
    int randomIndex = (int) (Math.random() * route.size());
    return route.remove(randomIndex);
    }

}