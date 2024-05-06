package solver.ls;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Solution {
  // function to evaluate solution
  // function to calculate neighborhood
  // take a step in the direction based on acceptance probability

  public static double acceptanceProbability(double energyCurrent, double energyNew, double temperature) {
    if (energyNew < energyCurrent) {
      return 1.0;
    }
    return Math.exp((energyCurrent - energyNew) / temperature);
  }
  
  // 2D array, each row is a vehicle route, each column is a customer
  ArrayList<Customer>[] schedule;
  static double[][] distanceMatrix;
  static int vehicleCapacity;
  static ArrayList<Tuple<Double, Integer>> angleList;
  static ArrayList<Customer> customers;

  public Solution() {
  }
  
  @SuppressWarnings("unchecked")
  public Solution(VRPInstance instance) {
    schedule = new ArrayList[instance.numVehicles];
    for (int i = 0; i < instance.numVehicles; i++) {
      schedule[i] = new ArrayList<Customer>();
    }

    // sweepGenerateInitialSolution();
    naiveGenerateInitialSolution();
  }
  
  @SuppressWarnings("unchecked")
  public int OptimalAddition(int vehicle, Customer c) {
    // find the best position to add customer c to vehicle
    // return the position
    System.out.println("OptimalAddition called");
    int bestPosition = -1;
    double bestDistance = Double.MAX_VALUE;
    int routeSize = schedule[vehicle].size();
    if (routeSize == 0) {
      return 0;
    }
    // int demand =
    for (int i = 0; i < routeSize; i++) {
      ArrayList<Customer> scheduleNew = (ArrayList<Customer>) schedule[vehicle].clone();
      scheduleNew.add(i, c);
      double distance = computeRouteDistance(scheduleNew);
      double demand = computeRouteDemand(scheduleNew);
      if (distance < bestDistance && demand <= Solution.vehicleCapacity) {
        bestDistance = distance;
        bestPosition = i;
      }
    }
    return bestPosition;
  }

  public static void initializeFields(VRPInstance instance) {
    Solution.vehicleCapacity = instance.vehicleCapacity;
    customers = new ArrayList<Customer>();
    for (int i = 0; i < instance.numCustomers; i++) {
      customers.add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i], instance.demandOfCustomer[i]));
    }
    computeDistanceMatrix(instance);
    computeAngleList(instance);
  }

  public static void computeDistanceMatrix(VRPInstance instance){
        double[][] distanceMatrix = new double[instance.numCustomers][instance.numCustomers];
        for (int i = 0; i < instance.numCustomers; i++) {
            for (int j = 0; j < instance.numCustomers; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    // System.out.print("i: " + i + " j: " + j + " x: " + instance.xCoordOfCustomer[i] + " y: " + instance.yCoordOfCustomer[i] + " x: " + instance.xCoordOfCustomer[j] + " y: " + instance.yCoordOfCustomer[j] + "\n");
                    distanceMatrix[i][j] = Math.pow(instance.xCoordOfCustomer[i] - instance.xCoordOfCustomer[j], 2) + Math.pow(instance.yCoordOfCustomer[i] - instance.yCoordOfCustomer[j], 2);
                }
            }
        }
        Solution.distanceMatrix = distanceMatrix;
    }
  
  public static void computeAngleList(VRPInstance instance) {
    ArrayList<Tuple<Double, Integer>> angleList = new ArrayList<Tuple<Double, Integer>>();
    for (int i = 0; i < instance.numCustomers; i++) {
      double angle = Math.atan2(customers.get(i).getY(), customers.get(i).getX());
      angleList.add(new Tuple<Double, Integer>(angle, i));
    }
    angleList.sort((a, b) -> a.getFirst().compareTo(b.getFirst()));
    Solution.angleList = angleList;
  }

  public void naiveGenerateInitialSolution() {
    int vehicleNum = 0;
    int demand = 0;
    for (int i = 0; i < customers.size(); i++) {
      demand += customers.get(i).getDemand();
      if (demand > Solution.vehicleCapacity) {
        vehicleNum++;
        if (vehicleNum >= schedule.length) {
          // no more vehicles
          throw new RuntimeException("No more vehicles");
        }
        demand = customers.get(i).getDemand();
      }
      schedule[vehicleNum].add(customers.get(i));
    }
  }

  public void sweepGenerateInitialSolution() {
    // generate a solution using the sweep algorithm
    // start from the depot and add customers in order of angle
    // if the demand exceeds the vehicle capacity, start a new route
    // return the solution
    int vehicleNum = 0;
    System.out.println("Vehicle capacity: " + Solution.vehicleCapacity);
    for (int i = 0; i < Solution.angleList.size(); i++) {
      Customer c = Solution.customers.get(Solution.angleList.get(i).getSecond());
      if (vehicleNum > 0) {
        for (int j = 0; j < vehicleNum; j++) {
          // try to add to existing routes (except the last one
          int addPositionPrev = OptimalAddition(j, c);
          if (addPositionPrev != -1) {
            this.schedule[j].add(addPositionPrev, c);
            continue;
          }
        }
      }
      int addPosition = OptimalAddition(vehicleNum, c);
      if (addPosition == -1) {
        System.out.println("Demand: " + computeRouteDemand(this.schedule[vehicleNum]) + " Distance: " + computeRouteDistance(this.schedule[vehicleNum]) + " Vehicle: " + vehicleNum + " Route: " + this.schedule[vehicleNum]);
        System.out.println("New customer demand: " + c.getDemand());
        vehicleNum++;
        this.schedule[vehicleNum] = new ArrayList<Customer>();
        this.schedule[vehicleNum].add(c);
      } else {
        this.schedule[vehicleNum].add(addPosition, c);
      }
    }
  }

  public static double evalSolution(ArrayList<Customer>[] solution) {
    // System.out.println("evalSolution called");
    double totalDistance = 0.0;
    for (int i = 0; i < solution.length; i++) {
      totalDistance += computeRouteDistance(solution[i]);
      double demand = computeRouteDemand(solution[i]);
      
      if (demand > Solution.vehicleCapacity) {
        // penalize the solution
        totalDistance += 1000000.0;
      }
    }
    return totalDistance;
  }

  public static double computeRouteDistance(ArrayList<Customer> solution) {
    double totalDistance = 0.0;
    for (int j = 0; j < solution.size(); j++) {
        if (j == 0) {
          totalDistance += solution.get(j).distanceToDepot();
        } else if (j == solution.size() - 1) {
          totalDistance += solution.get(j).distanceToDepot();
        } else {
          totalDistance += solution.get(j).distanceTo(solution.get(j - 1));
        }
      }
    return totalDistance;
  }

  public static double computeRouteDemand(ArrayList<Customer> solution) {
    double totalDemand = 0.0;
    for (int j = 0; j < solution.size(); j++) {
      totalDemand += solution.get(j).getDemand();
    }
    return totalDemand;
  }

  public void perturbSolution(double temperature) {
    // generate a random neighbor
    // calculate acceptance probability
    // take a step in the direction based on acceptance probability
    double energyCurrent = evalSolution(schedule);
    ArrayList<Customer>[] scheduleNew = takeRandomStep(schedule);
    double energyNew = 0.0;
    double acceptProb = Solution.acceptanceProbability(energyCurrent, energyNew, temperature);
    if (acceptProb > Math.random()) {
      schedule = scheduleNew;
    }
  }

  public ArrayList<Customer>[] takeRandomStep(ArrayList<Customer>[] schedule) {
    // randomly select a vehicle and a customer and move that customer to a different vehicle
    // needs to be improved lol
    Random rand = new Random();
    int vehicle1 = rand.nextInt(schedule.length);
    while (schedule[vehicle1].size() == 0) {
      vehicle1 = rand.nextInt(schedule.length);
    }
    int vehicle2 = rand.nextInt(schedule.length);
    int customer1 = rand.nextInt(schedule[vehicle1].size());
    ArrayList<Customer>[] scheduleNew = schedule;
    int addPosition = OptimalAddition(vehicle2, schedule[vehicle1].get(customer1));
    // while (addPosition == -1) {
    //   vehicle2 = rand.nextInt(schedule.length);
    //   addPosition = OptimalAddition(vehicle2, schedule[vehicle1].get(customer1));
    // }
    if (addPosition == -1) {
      addPosition = rand.nextInt(schedule[vehicle2].size());
    }
    scheduleNew[vehicle2].add(addPosition, schedule[vehicle1].get(customer1));
    scheduleNew[vehicle1].remove(customer1);
    return scheduleNew;
  }
  
  @SuppressWarnings("unchecked")
  public Solution clone() {
    Solution clonedSolution = new Solution();

    // Copy schedule array
    clonedSolution.schedule = new ArrayList[this.schedule.length];
    for (int i = 0; i < this.schedule.length; i++) {
        clonedSolution.schedule[i] = new ArrayList<Customer>(this.schedule[i]);
    }
    
    return clonedSolution;
  }

  @SuppressWarnings("unchecked")
  public static Solution initializeSolution(VRPInstance instance, int numClusters) {
        // Create a list of customers
        System.out.println("Creating customers list");
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < instance.numCustomers; i++) {
            customers.add(new Customer(instance.xCoordOfCustomer[i], instance.yCoordOfCustomer[i],
                    instance.demandOfCustomer[i]));
        }
        System.out.println("Shuffling customers list");

        // Shuffle the list to randomize customer order
        Collections.shuffle(customers);

        System.out.println("Performing k-means clustering");
        // Perform k-means clustering
        List<List<Customer>> clusters = kMeansClustering(customers, numClusters);

        // Assign clusters to vehicles
        Solution initialSolution = new Solution();
        initialSolution.schedule = new ArrayList[instance.numVehicles];
        for (int i = 0; i < instance.numVehicles; i++) {
            initialSolution.schedule[i] = new ArrayList<>();
        }
        int vehicleIndex = 0;
        int currDemand = 0;
        System.out.println("Assigning clusters to vehicles " + clusters);
        for (List<Customer> cluster : clusters) {
            for (Customer customer : cluster) {
                currDemand += customer.getDemand();
                if (currDemand <= instance.vehicleCapacity) {
                    initialSolution.schedule[vehicleIndex].add(customer);
                } else {
                    currDemand = customer.getDemand();
                    vehicleIndex++;
                    initialSolution.schedule[vehicleIndex].add(customer);
                }
            }
        }
        System.out.println("Initial solution: " + initialSolution);
        return initialSolution;
    }

    private static List<List<Customer>> kMeansClustering(List<Customer> customers, int k) {
        // Initialize cluster centers randomly
        Random rand = new Random();
        List<Customer> centers = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centers.add(customers.get(rand.nextInt(customers.size())));
        }

        // Initialize clusters
        List<List<Customer>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        boolean converged = false;
        int maxIterations = 100;
        while (!converged && maxIterations-- > 0) {
            // Assign each point to the nearest cluster
            System.out.println("KMeans iter num" + maxIterations);
            for (Customer customer : customers) {
                int nearestClusterIndex = findNearestCluster(customer, centers);
                clusters.get(nearestClusterIndex).add(customer);
            }
            // Update cluster centers
            List<Customer> newCenters = new ArrayList<>();
            for (List<Customer> cluster : clusters) {
                if (!cluster.isEmpty()) {
                    Customer centroid = calculateCentroid(cluster);
                    newCenters.add(centroid);
                }
            }
            // Check for convergence
            converged = newCenters.equals(centers);

            // Update cluster centers
            centers = newCenters;

            // Clear clusters
            for (List<Customer> cluster : clusters) {
                cluster.clear();
            }
        }
        for (Customer customer : customers) {
          int nearestClusterIndex = findNearestCluster(customer, centers);
          clusters.get(nearestClusterIndex).add(customer);
      }

        return clusters;
    }

    private static int findNearestCluster(Customer customer, List<Customer> centers) {
        int nearestClusterIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < centers.size(); i++) {
            double distance = customer.distanceTo(centers.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                nearestClusterIndex = i;
            }
        }
        return nearestClusterIndex;
    }

    private static Customer calculateCentroid(List<Customer> cluster) {
        double totalX = 0;
        double totalY = 0;
        int size = cluster.size();
        for (Customer customer : cluster) {
            totalX += customer.getX();
            totalY += customer.getY();
        }
        double centroidX = totalX / size;
        double centroidY = totalY / size;
        return new Customer(centroidX, centroidY, 0);
    }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < schedule.length; i++) {
      sb.append("Vehicle " + i + ": ");
      for (int j = 0; j < schedule[i].size(); j++) {
        Customer c = schedule[i].get(j);
        sb.append("(" + c.getDemand() + ", " + c.getX() + ", " + c.getY() + ") ");
      }
      sb.append("\n");
    }
    sb.append("total_distance: " + Solution.evalSolution(schedule) + "\n");
    return sb.toString();
  }
}
