import java.util.*;

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
        System.out.println("Beds: " + numberOfBeds + " | Size: " + squareFeet + " sqft | Price: " + pricePerNight);
    }
}

class SingleRoom extends Room { public SingleRoom() { super(1, 250, 1500.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super(2, 400, 2500.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super(3, 750, 5000.0); } }

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

class RoomInventory {
    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    public int getAvailability(String roomType) {
        return roomAvailability.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}

class BookingService {
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private Random random = new Random();

    public BookingService() {
        allocatedRooms.put("Single", new HashSet<>());
        allocatedRooms.put("Double", new HashSet<>());
        allocatedRooms.put("Suite", new HashSet<>());
    }

    public void processBookings(Queue<Reservation> queue, RoomInventory inventory) {
        System.out.println("Processing Booking Requests...\n");

        while (!queue.isEmpty()) {
            Reservation request = queue.poll();
            String type = request.getRoomType();
            int currentStock = inventory.getAvailability(type);

            if (currentStock > 0) {
                String roomId = generateUniqueRoomId(type);

                // Add to the specific set for that room type
                allocatedRooms.get(type).add(roomId);

                // Update centralized inventory
                inventory.updateAvailability(type, currentStock - 1);

                System.out.println("CONFIRMED: " + request.getGuestName() + " | Room: " + type + " | ID: " + roomId);
            } else {
                System.out.println("FAILED: " + request.getGuestName() + " | Room: " + type + " (Fully Booked)");
            }
        }
    }

    private String generateUniqueRoomId(String type) {
        String id;
        String prefix = type.substring(0, 1).toUpperCase();
        do {
            id = prefix + (random.nextInt(900) + 100);
        } while (isRoomIdTaken(id));
        return id;
    }

    private boolean isRoomIdTaken(String id) {
        for (Set<String> ids : allocatedRooms.values()) {
            if (ids.contains(id)) return true;
        }
        return false;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        BookingService bookingService = new BookingService();
        Queue<Reservation> requestQueue = new LinkedList<>();

        requestQueue.add(new Reservation("Alice", "Single"));
        requestQueue.add(new Reservation("Bob", "Double"));
        requestQueue.add(new Reservation("Charlie", "Suite"));
        requestQueue.add(new Reservation("David", "Suite"));
        requestQueue.add(new Reservation("Eve", "Suite"));

        bookingService.processBookings(requestQueue, inventory);

        System.out.println("\nFinal Inventory Status:");
        System.out.println("Single: " + inventory.getAvailability("Single"));
        System.out.println("Double: " + inventory.getAvailability("Double"));
        System.out.println("Suite: " + inventory.getAvailability("Suite"));
    }
}