package solver.ls;

public class Customer {
  private double x;
  private double y;
  private int demand;
  private int id;

  public Customer(double x, double y, int demand, int id) {
    this.x = x;
    this.y = y;
    this.demand = demand;
    this.id = id;
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

  public double distanceTo(Customer p) {
    return Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2));
  }

  public double distanceToDepot() {
    return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
  }

  public String toString() {
    return "(" + x + ", " + y + ", " + demand + ")";
  }

  public Customer clone() {
    return new Customer(x,y,demand,id);
  }

}
