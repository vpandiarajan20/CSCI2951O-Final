package solver.ls;

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
    VRPInstance instance;

    public Solver(float t, float tMin, float alpha, int maxIter, VRPInstance instance) {
        this.t = t;
        this.tMin = tMin;
        this.alpha = alpha;
        this.maxIter = maxIter;
        this.instance = instance;
    }

    public Solution solve() {
        Solution currSolution = new Solution(instance);
        double currEnergy = currSolution.evalSolution(currSolution.schedule);
        double bestEnergy = currEnergy;
        Solution bestSolution = currSolution;
        int iter = 0;
        while (t > tMin && iter < maxIter) {
            currSolution.perturbSolution(t);
            currEnergy = currSolution.evalSolution(currSolution.schedule);
            if (currEnergy < bestEnergy) {
                bestEnergy = currEnergy;
                bestSolution = new Solution(currSolution);
            }
            iter++;
            System.out.println("Iteration: " + iter + " Temperature: " + t + " Energy: " + currEnergy + " Best Energy: " + bestEnergy);
            t = t * alpha;
        }
        return bestSolution;
    }
}

