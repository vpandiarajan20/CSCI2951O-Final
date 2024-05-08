package solver.ls.heuristics;

import java.util.ArrayList;

import solver.ls.Customer;

public abstract class Heuristic {
    public abstract void applyHeuristic(Customer customer, ArrayList<Customer> route);
    public abstract void applyHeuristicRoute(ArrayList<Customer> route);
}