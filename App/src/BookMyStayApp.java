import java.util.*;

// --- CUSTOM EXCEPTIONS ---
class InvalidRoomException extends Exception { public InvalidRoomException(String m) { super(m); } }
class NoAvailabilityException extends Exception { public NoAvailabilityException(String m) { super(m); } }
class ReservationNotFoundException extends Exception { public ReservationNotFoundException(String m) { super(m); } }

// --- DOMAIN MODELS ---
abstract class Room {
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }
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

// --- SERVICES ---
class RoomInventory {
    private Map<String, Integer> roomAvailability = new HashMap<>();
    public RoomInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }
    public boolean isValidRoomType(String type) { return roomAvailability.containsKey(type); }
    public int getAvailability(String roomType) { return roomAvailability.getOrDefault(roomType, 0); }
    public void updateAvailability(String roomType, int count) { roomAvailability.put(roomType, count); }
}

class BookingHistory {
    private List<Reservation> history = new ArrayList<>();
    public void addRecord(Reservation res) { history.add(res); }
    public void removeRecord(Reservation res) { history.remove(res); }
    public List<Reservation> getHistory() { return history; }
}

class CancellationService {
    private Stack<String> releasedRoomIds = new Stack<>();

    public void cancelBooking(String reservationId, BookingHistory history, RoomInventory inventory) throws ReservationNotFoundException {
        Reservation found = null;
        for (Reservation res : history.getHistory()) {
            if (res.getReservationId().equals(reservationId)) {
                found = res;
                break;
            }
        }

        if (found == null) {
            throw new ReservationNotFoundException("Reservation ID " + reservationId + " not found.");
        }

        // 1. Rollback Room ID to Stack
        releasedRoomIds.push(found.getReservationId());

        // 2. Increment Inventory
        int currentCount = inventory.getAvailability(found.getRoomType());
        inventory.updateAvailability(found.getRoomType(), currentCount + 1);

        // 3. Remove from History
        history.removeRecord(found);

        System.out.println("CANCELLED: " + found.getGuestName() + "'s booking. ID " + reservationId + " returned to pool.");
    }
}

class BookingService {
    private BookingHistory bookingHistory;
    private Random random = new Random();

    public BookingService(BookingHistory history) {
        this.bookingHistory = history;
    }

    public void processBooking(Reservation request, RoomInventory inventory) {
        try {
            validateBooking(request, inventory);
            String roomId = request.getRoomType().substring(0, 1).toUpperCase() + (random.nextInt(900) + 100);
            request.setReservationId(roomId);

            inventory.updateAvailability(request.getRoomType(), inventory.getAvailability(request.getRoomType()) - 1);
            bookingHistory.addRecord(request);
            System.out.println("SUCCESS: Confirmed " + request.getGuestName() + " (ID: " + roomId + ")");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private void validateBooking(Reservation res, RoomInventory inventory) throws InvalidRoomException, NoAvailabilityException {
        if (!inventory.isValidRoomType(res.getRoomType())) throw new InvalidRoomException("Invalid type.");
        if (inventory.getAvailability(res.getRoomType()) <= 0) throw new NoAvailabilityException("No vacancy.");
    }
}

// --- MAIN APPLICATION ---
public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(history);
        CancellationService cancelService = new CancellationService();

        // 1. Create a booking
        Reservation aliceRes = new Reservation("Alice", "Single");
        bookingService.processBooking(aliceRes, inventory);
        String idToCancel = aliceRes.getReservationId();

        System.out.println("Inventory before cancellation: " + inventory.getAvailability("Single"));

        // 2. Perform Cancellation
        try {
            cancelService.cancelBooking(idToCancel, history, inventory);
        } catch (ReservationNotFoundException e) {
            System.err.println(e.getMessage());
        }

        // 3. Verify state
        System.out.println("Inventory after cancellation: " + inventory.getAvailability("Single"));
        System.out.println("History records: " + history.getHistory().size());
    }
}