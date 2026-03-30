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

class AddOnServiceManager {
    private Map<String, List<String>> reservationAddOns = new HashMap<>();
    private Map<String, Double> servicePrices = new HashMap<>();

    public AddOnServiceManager() {
        servicePrices.put("Breakfast", 500.0);
        servicePrices.put("WiFi", 200.0);
        servicePrices.put("Gym", 300.0);
    }

    public void addService(String reservationId, String serviceName) {
        reservationAddOns.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(serviceName);
    }

    public double calculateTotalAddOnCost(String reservationId) {
        double total = 0;
        List<String> services = reservationAddOns.get(reservationId);
        if (services != null) {
            for (String service : services) {
                total += servicePrices.getOrDefault(service, 0.0);
            }
        }
        return total;
    }

    public void displayServices(String reservationId) {
        List<String> services = reservationAddOns.get(reservationId);
        System.out.println("Add-ons: " + (services != null ? services : "None"));
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Reservation with Add-On Services\n");

        // Simulating a confirmed reservation from Use Case 6
        Reservation res = new Reservation("Alice", "Single");
        res.setReservationId("S101");

        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Guest selects services
        serviceManager.addService(res.getReservationId(), "Breakfast");
        serviceManager.addService(res.getReservationId(), "WiFi");

        // Display results
        System.out.println("Guest: " + res.getGuestName());
        System.out.println("Reservation ID: " + res.getReservationId());
        serviceManager.displayServices(res.getReservationId());

        double extraCost = serviceManager.calculateTotalAddOnCost(res.getReservationId());
        System.out.println("Total Add-on Cost: " + extraCost);
    }
}