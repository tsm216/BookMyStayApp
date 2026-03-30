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

    public double getPricePerNight() { return pricePerNight; }
}

class SingleRoom extends Room { public SingleRoom() { super(1, 250, 1500.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super(2, 400, 2500.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super(3, 750, 5000.0); } }

class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
}

class RoomInventory {
    private Map<String, Integer> roomAvailability = new HashMap<>();
    public RoomInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }
    public int getAvailability(String roomType) { return roomAvailability.getOrDefault(roomType, 0); }
    public void updateAvailability(String roomType, int count) { roomAvailability.put(roomType, count); }
}

class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addRecord(Reservation res) {
        history.add(res);
    }

    public List<Reservation> getHistory() {
        return history;
    }
}

class BookingReportService {
    public void generateSummaryReport(BookingHistory bookingHistory) {
        System.out.println("\n--- Administrative Booking Report ---");
        List<Reservation> records = bookingHistory.getHistory();

        if (records.isEmpty()) {
            System.out.println("No confirmed bookings found.");
            return;
        }

        int totalBookings = records.size();
        System.out.println("Total Confirmed Bookings: " + totalBookings);
        System.out.println("Detailed Audit Trail:");

        for (Reservation res : records) {
            System.out.println("- ID: " + res.getReservationId() + " | Guest: " + res.getGuestName() + " | Room: " + res.getRoomType());
        }
        System.out.println("------------------------------------\n");
    }
}

class BookingService {
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private BookingHistory bookingHistory;
    private Random random = new Random();

    public BookingService(BookingHistory history) {
        this.bookingHistory = history;
        allocatedRooms.put("Single", new HashSet<>());
        allocatedRooms.put("Double", new HashSet<>());
        allocatedRooms.put("Suite", new HashSet<>());
    }

    public void processBookings(Queue<Reservation> queue, RoomInventory inventory) {
        while (!queue.isEmpty()) {
            Reservation request = queue.poll();
            String type = request.getRoomType();
            int currentStock = inventory.getAvailability(type);

            if (currentStock > 0) {
                String roomId = generateUniqueRoomId(type);
                request.setReservationId(roomId);

                allocatedRooms.get(type).add(roomId);
                inventory.updateAvailability(type, currentStock - 1);

                // Add to persistent history
                bookingHistory.addRecord(request);

                System.out.println("SUCCESS: Confirmed booking for " + request.getGuestName() + " (ID: " + roomId + ")");
            } else {
                System.out.println("FAILED: No availability for " + request.getGuestName() + " (" + type + ")");
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
        // Initialize Components
        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(history);
        BookingReportService reportService = new BookingReportService();

        Queue<Reservation> requestQueue = new LinkedList<>();

        // 1. Intake Requests
        requestQueue.add(new Reservation("Alice", "Single"));
        requestQueue.add(new Reservation("Bob", "Suite"));
        requestQueue.add(new Reservation("Charlie", "Suite"));
        requestQueue.add(new Reservation("David", "Suite")); // Should fail (only 2 suites available)

        // 2. Process and Allocate
        bookingService.processBookings(requestQueue, inventory);

        // 3. Admin generates report from history
        reportService.generateSummaryReport(history);
    }
}