package HospitalApp;
import java.util.*;
import java.time.LocalDateTime;

/**
 * LabManagementApp.java
 * Single-file Java console application for Hospital Lab Test Management.
 *
 * Compile: javac LabManagementApp.java
 * Run:     java LabManagementApp
 */
public class LabManagementApp {

    // ---------------------------
    // Domain classes
    // ---------------------------

    static class Patient {
        private static int nextId = 1;
        public final int id;
        public String name;
        public String dob; // simple string for demo
        public String phone;

        public Patient(String name, String dob, String phone) {
            this.id = nextId++;
            this.name = name;
            this.dob = dob;
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "Patient{id=" + id + ", name='" + name + "', dob='" + dob + "', phone='" + phone + "'}";
        }
    }

    static class LabTest {
        private static int nextId = 1;
        public final int id;
        public String code;
        public String name;
        public double price;

        public LabTest(String code, String name, double price) {
            this.id = nextId++;
            this.code = code;
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return "LabTest{id=" + id + ", code='" + code + "', name='" + name + "', price=" + price + "}";
        }
    }

    static class TestOrder {
        private static int nextId = 1;
        public final int id;
        public final Patient patient;
        public final LocalDateTime createdAt;
        public List<TestOrderItem> items = new ArrayList<>();
        public boolean invoiced = false;

        public TestOrder(Patient patient) {
            this.id = nextId++;
            this.patient = patient;
            this.createdAt = LocalDateTime.now();
        }

        public void addItem(TestOrderItem item) {
            items.add(item);
        }

        public Optional<TestOrderItem> findItemById(int itemId) {
            return items.stream().filter(i -> i.id == itemId).findFirst();
        }

        @Override
        public String toString() {
            return "TestOrder{id=" + id + ", patient=" + patient.name + ", createdAt=" + createdAt + ", items=" + items.size() + "}";
        }
    }

    static class TestOrderItem {
        private static int nextId = 1;
        public final int id;
        public final LabTest labTest;
        public final TestOrder order;
        public Sample sample; // may be null until collected
        public TestResult result; // may be null until recorded
        public String status = "ORDERED"; // ORDERED -> SAMPLE_COLLECTED -> RESULT_RECORDED

        public TestOrderItem(LabTest labTest, TestOrder order) {
            this.id = nextId++;
            this.labTest = labTest;
            this.order = order;
        }

        @Override
        public String toString() {
            return "TestOrderItem{id=" + id + ", test=" + labTest.name + ", status=" + status + "}";
        }
    }

    static class Sample {
        private static int nextId = 1;
        public final int id;
        public final String sampleType; // e.g., Blood, Urine
        public final LocalDateTime collectedAt;
        public final TestOrderItem linkedItem;

        public Sample(String sampleType, TestOrderItem linkedItem) {
            this.id = nextId++;
            this.sampleType = sampleType;
            this.collectedAt = LocalDateTime.now();
            this.linkedItem = linkedItem;
        }

        @Override
        public String toString() {
            return "Sample{id=" + id + ", type=" + sampleType + ", collectedAt=" + collectedAt + ", linkedItemId=" + linkedItem.id + "}";
        }
    }

    static class TestResult {
        private static int nextId = 1;
        public final int id;
        public final String observation; // free text
        public final String value; // numeric or text value
        public final String unit;
        public final LocalDateTime recordedAt;
        public final TestOrderItem linkedItem;

        public TestResult(String observation, String value, String unit, TestOrderItem linkedItem) {
            this.id = nextId++;
            this.observation = observation;
            this.value = value;
            this.unit = unit;
            this.recordedAt = LocalDateTime.now();
            this.linkedItem = linkedItem;
        }

        @Override
        public String toString() {
            return "TestResult{id=" + id + ", observation='" + observation + "', value='" + value + " " + unit + "', recordedAt=" + recordedAt + ", linkedItemId=" + linkedItem.id + "}";
        }
    }

    static class Invoice {
        private static int nextId = 1;
        public final int id;
        public final TestOrder order;
        public final LocalDateTime issuedAt;
        public final List<TestOrderItem> billedItems;
        public final double totalAmount;

        public Invoice(TestOrder order, List<TestOrderItem> billedItems) {
            this.id = nextId++;
            this.order = order;
            this.issuedAt = LocalDateTime.now();
            this.billedItems = new ArrayList<>(billedItems);
            this.totalAmount = billedItems.stream().mapToDouble(item -> item.labTest.price).sum();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Invoice{id=").append(id).append(", orderId=").append(order.id).append(", patient=").append(order.patient.name)
              .append(", issuedAt=").append(issuedAt).append(", total=").append(totalAmount).append(", items=[");
            for (TestOrderItem it : billedItems) sb.append(it.labTest.name).append("(").append(it.labTest.price).append("), ");
            sb.append("]}");
            return sb.toString();
        }
    }

    // ---------------------------
    // In-memory repositories
    // ---------------------------

    static class DataStore {
        Map<Integer, Patient> patients = new HashMap<>();
        Map<Integer, LabTest> labTests = new HashMap<>();
        Map<Integer, TestOrder> orders = new HashMap<>();
        Map<Integer, Sample> samples = new HashMap<>();
        Map<Integer, TestResult> results = new HashMap<>();
        Map<Integer, Invoice> invoices = new HashMap<>();
    }

    // ---------------------------
    // Application logic and UI
    // ---------------------------

    private static final Scanner scanner = new Scanner(System.in);
    private static final DataStore store = new DataStore();

    public static void main(String[] args) {
        seedSampleData();

        boolean exit = false;
        System.out.println("=== Hospital Lab Test Management ===");
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": createPatient(); break;
                case "2": listPatients(); break;
                case "3": listLabTests(); break;
                case "4": createOrder(); break;
                case "5": collectSample(); break;
                case "6": recordResult(); break;
                case "7": generateInvoice(); break;
                case "8": viewOrderTrace(); break;
                case "9": listOrders(); break;
                case "0": exit = true; break;
                default: System.out.println("Invalid option. Try again."); break;
            }
            System.out.println();
        }
        System.out.println("Exiting. Bye!");
    }

    private static void printMenu() {
        System.out.println("Choose an option:");
        System.out.println("1. Create patient");
        System.out.println("2. List patients");
        System.out.println("3. List lab tests");
        System.out.println("4. Create test order");
        System.out.println("5. Collect sample (associate to order item)");
        System.out.println("6. Record result for an order item");
        System.out.println("7. Generate invoice for an order");
        System.out.println("8. View order trace (order -> items -> sample -> result)");
        System.out.println("9. List orders");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    private static void seedSampleData() {
        // Seed some lab tests
        LabTest cbc = new LabTest("CBC", "Complete Blood Count", 300.0);
        LabTest rbs = new LabTest("RBS", "Random Blood Sugar", 150.0);
        LabTest lft = new LabTest("LFT", "Liver Function Test", 500.0);
        LabTest lipid = new LabTest("LIPID", "Lipid Profile", 800.0);
        store.labTests.put(cbc.id, cbc);
        store.labTests.put(rbs.id, rbs);
        store.labTests.put(lft.id, lft);
        store.labTests.put(lipid.id, lipid);

        // Seed a patient
        Patient p = new Patient("John Doe", "1990-01-01", "9876543210");
        store.patients.put(p.id, p);

        // Seed an order for demo
        TestOrder order = new TestOrder(p);
        TestOrderItem it1 = new TestOrderItem(cbc, order);
        TestOrderItem it2 = new TestOrderItem(rbs, order);
        order.addItem(it1);
        order.addItem(it2);
        store.orders.put(order.id, order);
    }

    // ---------------------------
    // Menu actions
    // ---------------------------

    private static void createPatient() {
        System.out.print("Patient name: ");
        String name = scanner.nextLine().trim();
        System.out.print("DOB (YYYY-MM-DD): ");
        String dob = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        Patient p = new Patient(name, dob, phone);
        store.patients.put(p.id, p);
        System.out.println("Created " + p);
    }

    private static void listPatients() {
        if (store.patients.isEmpty()) {
            System.out.println("No patients.");
            return;
        }
        System.out.println("Patients:");
        store.patients.values().forEach(System.out::println);
    }

    private static void listLabTests() {
        System.out.println("Available Lab Tests:");
        store.labTests.values().forEach(System.out::println);
    }

    private static void createOrder() {
        if (store.patients.isEmpty()) {
            System.out.println("No patients registered. Create a patient first.");
            return;
        }
        System.out.println("Select patient by ID:");
        listPatients();
        int pid = readInt("Patient ID: ");
        Patient p = store.patients.get(pid);
        if (p == null) {
            System.out.println("Invalid patient id.");
            return;
        }
        TestOrder order = new TestOrder(p);

        boolean adding = true;
        while (adding) {
            listLabTests();
            int tid = readInt("Enter LabTest id to add (0 to stop): ");
            if (tid == 0) break;
            LabTest lt = store.labTests.get(tid);
            if (lt == null) {
                System.out.println("Invalid lab test id.");
                continue;
            }
            TestOrderItem item = new TestOrderItem(lt, order);
            order.addItem(item);
            System.out.println("Added " + lt.name + " as order item id " + item.id);
            System.out.print("Add another test? (y/n): ");
            String r = scanner.nextLine().trim();
            if (!r.equalsIgnoreCase("y")) adding = false;
        }
        if (order.items.isEmpty()) {
            System.out.println("Order has no items. Cancelled.");
            return;
        }
        store.orders.put(order.id, order);
        System.out.println("Created order: " + order);
    }

    private static void collectSample() {
        System.out.println("Collect sample for an order item.");
        listOrders();
        int oid = readInt("Order ID: ");
        TestOrder order = store.orders.get(oid);
        if (order == null) {
            System.out.println("Order not found. Business rule: order must exist before sample collection.");
            return;
        }
        System.out.println("Order items:");
        order.items.forEach(System.out::println);
        int itemId = readInt("Enter order item id to collect sample for: ");
        Optional<TestOrderItem> optItem = order.findItemById(itemId);
        if (!optItem.isPresent()) {
            System.out.println("Order item not found in this order.");
            return;
        }
        TestOrderItem item = optItem.get();

        if (item.sample != null) {
            System.out.println("Sample already collected for this item: " + item.sample);
            return;
        }

        System.out.print("Sample type (e.g., Blood, Urine): ");
        String sampleType = scanner.nextLine().trim();
        // Business rule enforced: order exists and item exists before sample collection
        Sample s = new Sample(sampleType, item);
        item.sample = s;
        item.status = "SAMPLE_COLLECTED";
        store.samples.put(s.id, s);
        System.out.println("Sample collected: " + s);
    }

    private static void recordResult() {
        System.out.println("Record result for an order item.");
        listOrders();
        int oid = readInt("Order ID: ");
        TestOrder order = store.orders.get(oid);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }
        System.out.println("Order items:");
        order.items.forEach(System.out::println);
        int itemId = readInt("Enter order item id to record result for: ");
        Optional<TestOrderItem> optItem = order.findItemById(itemId);
        if (!optItem.isPresent()) {
            System.out.println("Order item not found.");
            return;
        }
        TestOrderItem item = optItem.get();

        // Business rule: sample should be collected before result (traceability)
        if (item.sample == null) {
            System.out.println("Cannot record result. Sample not collected for this item. Business rule violated.");
            return;
        }
        if (item.result != null) {
            System.out.println("Result already recorded: " + item.result);
            return;
        }

        System.out.print("Result value (e.g., 5.6 or Negative): ");
        String value = scanner.nextLine().trim();
        System.out.print("Unit (or '-' if none): ");
        String unit = scanner.nextLine().trim();
        System.out.print("Short observation/comment: ");
        String obs = scanner.nextLine().trim();

        TestResult res = new TestResult(obs, value, unit, item);
        item.result = res;
        item.status = "RESULT_RECORDED";
        store.results.put(res.id, res);
        System.out.println("Recorded result: " + res);
    }

    private static void generateInvoice() {
        System.out.println("Generate invoice for order.");
        listOrders();
        int oid = readInt("Order ID: ");
        TestOrder order = store.orders.get(oid);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }
        if (order.invoiced) {
            System.out.println("Order already invoiced. Existing invoices:");
            store.invoices.values().stream().filter(inv -> inv.order.id == order.id).forEach(System.out::println);
            return;
        }

        // For simplicity we bill all items; in a real system we'd support partial billing.
        Invoice inv = new Invoice(order, order.items);
        store.invoices.put(inv.id, inv);
        order.invoiced = true;
        System.out.println("Invoice generated:\n" + inv);
    }

    private static void viewOrderTrace() {
        System.out.println("View order trace.");
        listOrders();
        int oid = readInt("Order ID: ");
        TestOrder order = store.orders.get(oid);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }
        System.out.println("Order: " + order);
        for (TestOrderItem item : order.items) {
            System.out.println("  -> Item: " + item);
            if (item.sample != null) {
                System.out.println("       Sample: " + item.sample);
            } else {
                System.out.println("       Sample: [not collected]");
            }
            if (item.result != null) {
                System.out.println("       Result: " + item.result);
            } else {
                System.out.println("       Result: [not recorded]");
            }
        }
        Optional<Invoice> inv = store.invoices.values().stream().filter(i -> i.order.id == order.id).findFirst();
        if (inv.isPresent()) {
            System.out.println("Invoice: " + inv.get());
        } else {
            System.out.println("Invoice: [not generated]");
        }
    }

    private static void listOrders() {
        if (store.orders.isEmpty()) {
            System.out.println("No orders.");
            return;
        }
        System.out.println("Orders:");
        store.orders.values().forEach(o -> {
            System.out.println(o);
            for (TestOrderItem it : o.items) {
                System.out.println("   " + it);
            }
        });
    }

    // ---------------------------
    // Utility helpers
    // ---------------------------

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String s = scanner.nextLine().trim();
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid integer.");
            }
        }
    }
}