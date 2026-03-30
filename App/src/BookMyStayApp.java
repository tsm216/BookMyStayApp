import java.util.*;

// --- CUSTOM EXCEPTIONS ---
class InvalidRoomException extends Exception {
    public InvalidRoomException(String message) { super(message); }
}

class NoAvailabilityException extends Exception {
    public NoAvailabilityException(String message) { super(message); }
}

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

    public boolean isValidRoomType(String type) {
        return roomAvailability.containsKey(type);
    }

    public int getAvailability(String roomType) {
        return roomAvailability.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}

class BookingHistory {
    private List<Reservation> history = new ArrayList<>();
    public void addRecord(Reservation res) { history.add(res); }
    public List<Reservation> getHistory() { return history; }
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

            int currentStock = inventory.getAvailability(request.getRoomType());
            inventory.updateAvailability(request.getRoomType(), currentStock - 1);
            bookingHistory.addRecord(request);

            System.out.println("SUCCESS: Confirmed for " + request.getGuestName() + " (ID: " + roomId + ")");

        } catch (InvalidRoomException | NoAvailabilityException e) {
            System.err.println("VALIDATION ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("SYSTEM ERROR: An unexpected error occurred.");
        }
    }

    private void validateBooking(Reservation res, RoomInventory inventory) throws InvalidRoomException, NoAvailabilityException {
        if (!inventory.isValidRoomType(res.getRoomType())) {
            throw new InvalidRoomException("Room type '" + res.getRoomType() + "' does not exist.");
        }
        if (inventory.getAvailability(res.getRoomType()) <= 0) {
            throw new NoAvailabilityException("No vacancy for room type: " + res.getRoomType());
        }
    }
}

// --- MAIN EXECUTION ---
public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(history);

        System.out.println("--- Booking System with Validation ---\n");

        // Valid Request
        bookingService.processBooking(new Reservation("Alice", "Single"), inventory);

        // Invalid Request: Wrong Room Type
        bookingService.processBooking(new Reservation("Bob", "Penthouse"), inventory);

        // Invalid Request: Out of Stock
        bookingService.processBooking(new Reservation("Charlie", "Suite"), inventory);
        bookingService.processBooking(new Reservation("David", "Suite"), inventory);
        bookingService.processBooking(new Reservation("Eve", "Suite"), inventory); // Should trigger NoAvailabilityException

        System.out.println("\nFinal History Size: " + history.getHistory().size());
    }
}