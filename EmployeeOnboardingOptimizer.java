import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Scanner;

public class EmployeeOnboardingOptimizer {

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("onboardingDB");
        MongoCollection<Document> collection = database.getCollection("employees");

        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n=== Employee Onboarding Optimizer ===");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Update Onboarding Status");
            System.out.println("4. Delete Employee");
            System.out.println("5. Calculate Average Onboarding Days");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } else {
                sc.nextLine(); // Discard invalid input
                System.out.println("Invalid input! Enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Employee Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Department: ");
                    String dept = sc.nextLine();
                    System.out.print("Enter Onboarding Days: ");
                    int days;
                    if (sc.hasNextInt()) {
                        days = sc.nextInt();
                        sc.nextLine();
                    } else {
                        sc.nextLine();
                        System.out.println("Invalid number! Employee not added.");
                        break;
                    }
                    Document doc = new Document("name", name)
                            .append("department", dept)
                            .append("onboardingDays", days)
                            .append("status", "Pending");
                    collection.insertOne(doc);
                    System.out.println("Employee added successfully!");
                    break;

                case 2:
                    System.out.println("\n--- Employee List ---");
                    MongoCursor<Document> cursor = collection.find().iterator();
                    try {
                        while (cursor.hasNext()) {
                            Document emp = cursor.next();
                            System.out.println(emp.toJson());
                        }
                    } finally {
                        cursor.close();
                    }
                    break;

                case 3:
                    System.out.print("Enter Employee Name to Update: ");
                    String empName = sc.nextLine();
                    System.out.print("Enter new Status (Pending/Completed): ");
                    String status = sc.nextLine();

                    Bson filter = Filters.eq("name", empName);
                    Document update = new Document("$set", new Document("status", status));
                    if (collection.updateOne(filter, update).getMatchedCount() > 0) {
                        System.out.println("Status updated successfully!");
                    } else {
                        System.out.println("Employee not found!");
                    }
                    break;

                case 4:
                    System.out.print("Enter Employee Name to Delete: ");
                    String delName = sc.nextLine();
                    Bson delFilter = Filters.eq("name", delName);
                    if (collection.deleteOne(delFilter).getDeletedCount() > 0) {
                        System.out.println("Employee deleted successfully!");
                    } else {
                        System.out.println("Employee not found!");
                    }
                    break;

                case 5:
                    double totalDays = 0;
                    long count = 0;
                    MongoCursor<Document> avgCursor = collection.find().iterator();
                    try {
                        while (avgCursor.hasNext()) {
                            Document emp = avgCursor.next();
                            Object val = emp.get("onboardingDays");
                            if (val instanceof Integer) {
                                totalDays += (int) val;
                                count++;
                            }
                        }
                    } finally {
                        avgCursor.close();
                    }
                    if (count > 0)
                        System.out.println("Average Onboarding Time: " + (totalDays / count) + " days");
                    else
                        System.out.println("No employees found.");
                    break;

                case 6:
                    System.out.println("Exiting program...");
                    sc.close();
                    mongoClient.close();
                    System.exit(0);

                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}
