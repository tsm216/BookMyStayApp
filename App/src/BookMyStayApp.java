import java.io.*;
import java.util.*;

// --- DOMAIN MODELS (Must implement Serializable) ---
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String guestName;
    private String roomType;
    private String reservationId;

    public Reservation(String guestName, String roomType, String reservationId) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.reservationId = reservationId;
    }

    @Override
    public String toString() {
        return "ID: " + reservationId + " | Guest: " + guestName + " | Room: " + roomType;
    }
}

// --- SERVICES ---
class RoomInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    public void updateAvailability(String type, int count) {
        roomAvailability.put(type, count);
    }

    public void displayInventory() {
        System.out.println("Current Inventory: " + roomAvailability);
    }
}

class BookingHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Reservation> history = new ArrayList<>();

    public void addRecord(Reservation res) { history.add(res); }
    public List<Reservation> getHistory() { return history; }
}

class PersistenceService {
    private static final String FILE_NAME = "hotel_state.ser";

    public void saveState(RoomInventory inventory, BookingHistory history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(inventory);
            oos.writeObject(history);
            System.out.println("SYSTEM: State successfully persisted to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("SAVE ERROR: " + e.getMessage());
        }
    }

    public Object[] loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("SYSTEM: No previous state found. Starting fresh.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            RoomInventory inventory = (RoomInventory) ois.readObject();
            BookingHistory history = (BookingHistory) ois.readObject();
            System.out.println("SYSTEM: State successfully recovered from " + FILE_NAME);
            return new Object[]{inventory, history};
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("RECOVERY ERROR: " + e.getMessage());
            return null;
        }
    }
}

// --- MAIN APPLICATION ---
public class BookMyStayApp {
    public static void main(String[] args) {
        PersistenceService persistence = new PersistenceService();
        RoomInventory inventory;
        BookingHistory history;

        // 1. Try to recover system state
        Object[] recovered = persistence.loadState();
        if (recovered != null) {
            inventory = (RoomInventory) recovered[0];
            history = (BookingHistory) recovered[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
            // Simulate initial booking to have something to save
            Reservation res = new Reservation("John Doe", "Suite", "S999");
            inventory.updateAvailability("Suite", 1);
            history.addRecord(res);
        }

        // 2. Display current state (Recovered or New)
        System.out.println("\n--- Current System State ---");
        inventory.displayInventory();
        System.out.println("Bookings in History: " + history.getHistory().size());
        for (Reservation r : history.getHistory()) System.out.println(r);

        // 3. Simulate a change and persist (Preparation for Shutdown)
        System.out.println("\nUpdating state and saving...");
        inventory.updateAvailability("Single", 2);
        persistence.saveState(inventory, history);

        System.out.println("\nApplication Terminated. Run again to see recovery in action.");
    }
}