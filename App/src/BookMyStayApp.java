import java.util.*;

// --- DOMAIN MODELS ---
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
    public void setReservationId(String id) { this.reservationId = id; }
}

// --- THREAD-SAFE SERVICES ---
class RoomInventory {
    private Map<String, Integer> roomAvailability = new HashMap<>();

    public RoomInventory() {
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    // Synchronized to prevent two threads from reading/writing simultaneously
    public synchronized int getAvailability(String roomType) {
        return roomAvailability.getOrDefault(roomType, 0);
    }

    public synchronized void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
    }
}

class BookingHistory {
    private List<Reservation> history = Collections.synchronizedList(new ArrayList<>());
    public void addRecord(Reservation res) { history.add(res); }
    public int getCount() { return history.size(); }
}

class ConcurrentBookingProcessor implements Runnable {
    private Queue<Reservation> requestQueue;
    private RoomInventory inventory;
    private BookingHistory history;
    private Random random = new Random();

    public ConcurrentBookingProcessor(Queue<Reservation> queue, RoomInventory inventory, BookingHistory history) {
        this.requestQueue = queue;
        this.inventory = inventory;
        this.history = history;
    }

    @Override
    public void run() {
        while (true) {
            Reservation request;

            // Synchronize on the queue to safely poll requests
            synchronized (requestQueue) {
                if (requestQueue.isEmpty()) break;
                request = requestQueue.poll();
            }

            processRequest(request);
        }
    }

    private void processRequest(Reservation request) {
        String type = request.getRoomType();

        // Critical Section: Ensure inventory check and update are atomic
        synchronized (inventory) {
            int currentStock = inventory.getAvailability(type);
            if (currentStock > 0) {
                String roomId = type.substring(0, 1).toUpperCase() + (random.nextInt(900) + 100);
                request.setReservationId(roomId);

                inventory.updateAvailability(type, currentStock - 1);
                history.addRecord(request);

                System.out.println(Thread.currentThread().getName() + " CONFIRMED: " +
                        request.getGuestName() + " (Room ID: " + roomId + ")");
            } else {
                System.out.println(Thread.currentThread().getName() + " FAILED: No rooms for " +
                        request.getGuestName());
            }
        }
    }
}

// --- MAIN APPLICATION ---
public class BookMyStayApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Concurrent Booking Simulation ---\n");

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        Queue<Reservation> requestQueue = new LinkedList<>();

        // Add 10 simultaneous requests
        for (int i = 1; i <= 10; i++) {
            requestQueue.add(new Reservation("Guest_" + i, (i % 2 == 0) ? "Single" : "Suite"));
        }

        // Create two worker threads (simulating two booking servers processing the same queue)
        Thread thread1 = new Thread(new ConcurrentBookingProcessor(requestQueue, inventory, history), "Processor_1");
        Thread thread2 = new Thread(new ConcurrentBookingProcessor(requestQueue, inventory, history), "Processor_2");

        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        thread1.join();
        thread2.join();

        System.out.println("\nSimulation Complete.");
        System.out.println("Total Bookings in History: " + history.getCount());
        System.out.println("Final Suite Inventory: " + inventory.getAvailability("Suite"));
    }
}