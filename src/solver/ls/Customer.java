package solver.ls;

public class Customer {
  private double x;
  private double y;
  private int demand;
  private int id;
  private int routeId;
  // include a private field reference to a node

  public Customer(double x, double y, int demand, int id, int routeId) {
    this.x = x;
    this.y = y;
    this.demand = demand;
    this.id = id;
    this.routeId = routeId;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public int getDemand() {
    return demand;
  }

  public int getId() {
    return id;
  }

  public int getRouteId() {
    return routeId;
  }

  public void setRouteId(int routeId) {
    this.routeId = routeId;
  }

  public double distanceTo(Customer p) {
    return Solution.distanceMatrix[id][p.getId()];
    // return Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2));
  }
  
  public double distanceTo(int p) {
    return Solution.distanceMatrix[id][p];
    // return Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2));
  }

  public double distanceToDepot() {
    return Solution.distanceMatrix[0][id];
    // return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
  }

  public String toString() {
    return "(" + x + ", " + y + ", " + demand + ", " + id + ")";
  }

  public Customer clone() {
    return new Customer(x,y,demand,id,routeId);
  }

}
