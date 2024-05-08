package solver.ls;

public class Customer {
  private double x;
  private double y;
  private int demand;

  public Customer(double x, double y, int demand) {
    this.x = x;
    this.y = y;
    this.demand = demand;
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
    return new Customer(x,y,demand);
  }

}
