public class Node {
    private int id;
    private Node previous;
    private Node next;

    // Constructor
    public Node(int id) {
        this.id = id;
        this.previous = null;
        this.next = null;
    }

    // Getter and setter methods for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and setter methods for previous node
    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    // Getter and setter methods for next node
    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
