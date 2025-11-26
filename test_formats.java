import com.accountbook.service.LedgerService;
import com.accountbook.util.FileFormat;
import java.time.LocalDate;

/**
 * Simple test to verify CSV and JSON file handling functionality
 */
public class test_formats {
    public static void main(String[] args) {
        System.out.println("=== Testing File Format Functionality ===");
        
        // Create a test ledger service
        LedgerService ledger = new LedgerService("test_data");
        
        // Add some test data
        System.out.println("Adding test data...");
        ledger.addItem("지출", LocalDate.of(2025, 10, 15), 50000, "Food", "점심 식사");
        ledger.addItem("지출", LocalDate.of(2025, 10, 16), 30000, "Transport", "지하철 카드 충전");
        ledger.addItem("지출", LocalDate.of(2025, 10, 17), 100000, "Shopping", "옷 구매");
        
        System.out.printf("Added %d items%n", ledger.getItemCount());
        
        // Test CSV format (default)
        System.out.println("\n=== Testing CSV Format ===");
        System.out.printf("Current format: %s%n", ledger.getCurrentFormat().getDescription());
        
        boolean csvSaved = ledger.saveData();
        System.out.printf("CSV save result: %s%n", csvSaved ? "SUCCESS" : "FAILED");
        
        // Test JSON format
        System.out.println("\n=== Testing JSON Format ===");
        boolean formatChanged = ledger.changeFormat(FileFormat.JSON);
        System.out.printf("Format change result: %s%n", formatChanged ? "SUCCESS" : "FAILED");
        System.out.printf("Current format: %s%n", ledger.getCurrentFormat().getDescription());
        
        // Test loading from JSON
        System.out.println("\n=== Testing JSON Load ===");
        LedgerService jsonLedger = new LedgerService("test_data.json");
        System.out.printf("Loaded %d items from JSON%n", jsonLedger.getItemCount());
        
        // Test loading from CSV
        System.out.println("\n=== Testing CSV Load ===");
        LedgerService csvLedger = new LedgerService("test_data.csv");
        System.out.printf("Loaded %d items from CSV%n", csvLedger.getItemCount());
        
        // Verify data integrity
        System.out.println("\n=== Data Integrity Check ===");
        boolean dataMatches = jsonLedger.getItemCount() == csvLedger.getItemCount();
        System.out.printf("Data integrity: %s%n", dataMatches ? "PASSED" : "FAILED");
        
        System.out.println("\n=== Test Complete ===");
    }
}