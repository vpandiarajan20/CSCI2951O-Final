package solver.ls;

import solver.ls.insertionHeuristics.InsertionHeuristic;
import solver.ls.removalHeuristics.RemoveHeuristic;

public class Solver {
    // function to evaluate solution
    // function to calculate acceptance probability
    // function to generate a random neighbor
    // function to generate initial solution
    // some sort of temperature schedule
    float t;
    float tMin;
    float alpha;
    int maxIter;
    int restartIter;
    VRPInstance instance;
    InsertionHeuristic heuristic;
    RemoveHeuristic removalHeuristic;

    public Solver(float t, float tMin, float alpha, int maxIter, int restartIter, 
    VRPInstance instance, InsertionHeuristic heuristic, RemoveHeuristic removalHeuristic) {
        this.t = t;
        this.tMin = tMin;
        this.alpha = alpha;
        this.maxIter = maxIter;
        this.restartIter = restartIter;
        this.instance = instance;
        this.heuristic = heuristic;
        this.removalHeuristic = removalHeuristic;
    }

    public Solution solve() {
        Solution.initializeFields(instance);
        Solution currSolution = new Solution(instance, heuristic, removalHeuristic);
        // Solution currSolution = Solution.initializeSolution(instance, 10);
        // currSolution.sweepGenerateSolution();
        System.out.println("Initial solution: " + currSolution);
        double currEnergy = Solution.evalSolution(currSolution.schedule);
        double bestEnergy = currEnergy;
        Solution bestSolution = currSolution.clone();
        int iter = 0;
        int noImprovIter = 0;
        while (t > tMin && iter < maxIter) {
            currSolution.perturbSolution(t);
            currEnergy = Solution.evalSolution(currSolution.schedule);
            // System.out.println("Schedule:" + currSolution);
            if (currEnergy < bestEnergy) {
                bestEnergy = currEnergy;
                bestSolution = currSolution.clone();
                noImprovIter = 0;
                // System.out.println("New best solution: " + bestSolution);
            } else {
                noImprovIter++;
                if (noImprovIter > restartIter) {
                    currSolution = bestSolution.clone();
                    noImprovIter = 0;
                    System.out.println("Restarting");
                }
            }
            iter++;
            System.out.println("Iteration: " + iter + " Temperature: " + t + " Energy: " + currEnergy + " Best Energy: " + bestEnergy);
            t = t * alpha;
        }
        // for (int i = 0; i < bestSolution.schedule.length; i++) {
        //     System.out.println("Route " + i + ", Distance: " + Solution.computeRouteDistance(bestSolution.schedule[i]));
        // }
        return bestSolution;
    }

    
}

