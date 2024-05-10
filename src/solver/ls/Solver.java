package solver.ls;


public class Solver {
    float t;
    float tMin;
    float alpha;
    int maxIter;
    int restartIter;
    int penaltyIncrementIter; //TODO: incorporate this
    float penaltyIncrementFactor; //TODO: incorporate this
    boolean randomRestart; //TODO: incorporate this
    VRPInstance instance;
    static boolean testing = true; //TODO: Remember to set this to false
    // InsertionHeuristic[] insertionHeuristics;
    // RemoveHeuristic removalHeuristic;

    public Solver(float t, float tMin, float alpha, int maxIter, int restartIter, 
    VRPInstance instance) {
        this.t = t;
        this.tMin = tMin;
        this.alpha = alpha;
        this.maxIter = maxIter;
        this.restartIter = restartIter;
        this.instance = instance;
        // this.insertionHeuristics = insertionHeuristics;
        // this.removalHeuristic = removalHeuristic;
    }

    public Solution solve() {
        Solution.initializeFields(instance);
        Solution currSolution = new Solution(instance);
        System.out.println("Initial solution: " + currSolution);
        double currEnergy = currSolution.evalSolution();
        // TODO: explore using different values for the temperature, such as the initial energy
        t = (float) currEnergy;
        double bestEnergy = currEnergy;
        Solution bestSolution = currSolution.clone();
        int iter = 0;
        int noImprovIter = 0;
        while (t > tMin && iter < maxIter) {
            if (iter % 100 == 0) {
                // TODO: 100 needs to be a hyperparam
                Solution.incrementPenalty();
                bestEnergy = bestSolution.evalSolution();
                if (testing) {
                    currSolution.sanityCheck();
                }
                // System.out.println("Solution: " + currSolution);
            }
            // TODO: explore using a group of solutions to perturb
            perturbSolution(currSolution, t);
            currEnergy = currSolution.evalSolution();
            if (currEnergy < bestEnergy) {
                bestEnergy = currEnergy;
                bestSolution = currSolution.clone();
                System.out.println("New best solution found: " + bestSolution);
                noImprovIter = 0;
            } else {
                noImprovIter++;
                if (noImprovIter > restartIter) {
                    currSolution = bestSolution.clone();
                    currSolution.syncCustomerRouteIDs();
                    noImprovIter = 0;
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

    public void perturbSolution(Solution sol, double temperature) {
        Solution copySol = sol.clone();
        double energyCurrent = sol.evalSolution();
        sol.takeRandomStep();
        double energyNew = sol.evalSolution();
        double acceptProb = Solution.acceptanceProbability(energyCurrent, energyNew, temperature);
        if (acceptProb > Math.random()) {
            // accept the new solution
        } else {
            // reject the new solution
            sol = copySol;
        }
        // System.out.println("Acceptance Prob: " + acceptProb + " Energy Current: " + energyCurrent + " Energy New: " + energyNew);
    }
}

