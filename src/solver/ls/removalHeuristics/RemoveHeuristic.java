package solver.ls.removalHeuristics;

import java.util.ArrayList;
import solver.ls.Customer;

public abstract class RemoveHeuristic {
    public abstract Customer removeHeuristic(ArrayList<Customer> route);
}