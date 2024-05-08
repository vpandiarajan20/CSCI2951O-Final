package solver.ls.removalHeuristics;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;

import solver.ls.Customer;

public class TopkRemoval extends RemoveHeuristic {

  int K = 5;
  public Customer removeHeuristic(ArrayList<Customer> route) {
    if ((route.size() <= K) || (Math.random() < 0.1)) {
      int randomIndex = (int) (Math.random() * route.size());
      return route.remove(randomIndex);
    }
    PriorityQueue<CustomerDistance> maxHeap = new PriorityQueue<>(K, Comparator.comparingDouble(cd -> -cd.distance));

    maxHeap.add(new CustomerDistance(0, route.get(0).distanceToDepot()));
    for (int i = 1; i < route.size(); i++) {
        double distance = route.get(i-1).distanceTo(route.get(i));
        maxHeap.add(new CustomerDistance(i, distance));
        if (maxHeap.size() > K) {
            maxHeap.poll();
        }
    }

    int randomIndex = (int) (Math.random() * maxHeap.size());
    return route.remove(maxHeap.toArray(new CustomerDistance[0])[randomIndex].index);
  }

  // Helper class to store customers and their respective distances
  private static class CustomerDistance {
    int index;
    double distance;

    CustomerDistance(int index, double distance) {
        this.index = index;
        this.distance = distance;
    }
  }

}