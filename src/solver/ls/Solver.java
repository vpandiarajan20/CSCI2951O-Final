package solver.ls;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
    Solution solution = new Solution(instance);
    int iter = 0;
    while (t > tMin && iter < maxIter) {
        solution.perturbSolution(t);
        iter++;
        t = t * alpha;
    }
    return solution;
    }

}

