import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

abstract class Room {
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }

    public void displayRoomDetails() {
        System.out.println("Beds: " + numberOfBeds);
        System.out.println("Size: " + squareFeet + " sqft");
        System.out.println("Price per night: " + pricePerNight);
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super(1, 250, 1500.0);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super(2, 400, 2500.0);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super(3, 750, 5000.0);
    }
}

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

class BookingRequestQueue {
    private Queue<Reservation> requests;

    public BookingRequestQueue() {
        requests = new LinkedList<>();
    }

    public void addRequest(Reservation reservation) {
        requests.add(reservation);
    }

    public Queue<Reservation> getRequests() {
        return requests;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Booking Request Intake\n");

        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        bookingQueue.addRequest(new Reservation("Alice", "Single"));
        bookingQueue.addRequest(new Reservation("Bob", "Double"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite"));

        System.out.println("Current Booking Requests in Queue:");
        for (Reservation res : bookingQueue.getRequests()) {
            System.out.println("Guest: " + res.getGuestName() + " | Requested: " + res.getRoomType());
        }

        System.out.println("\nRequests are stored in arrival order (FIFO).");
        System.out.println("No rooms have been allocated yet.");
    }
}