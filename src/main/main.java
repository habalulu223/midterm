package main;

import dbConnect.dbConnect;
import java.util.InputMismatchException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        // Initialize the database connection manager
        dbConnect cf = new dbConnect();

        System.out.println("Initializing Application...");
        
        // --- SYSTEM SETUP ---
        // Ensure database tables exist before running the application
        cf.createTables(); 
        
        // Setup initial admin if the user table is empty
        if (cf.getSingleValue("SELECT COUNT(user_id) FROM tbl_user", 0) == 0) {
            System.out.println("--- SYSTEM SETUP: Creating initial admin account (Username: admin, Email: admin@shop.com, Pass: adminpass) ---");
            // Register and approve the initial admin
            cf.registerUser("admin", "adminpass", "admin@shop.com");
            cf.approveUser("admin"); 
            cf.makeUserAdmin("admin", "system_init"); // Promote to admin
        }
        // --- END SETUP ---


        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Welcome! Choose an option ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = -1;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    // Register new user
                    System.out.print("Enter desired username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter Email: ");
                    String Gmail = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    boolean registered = cf.registerUser(username, password, Gmail);
                    if (registered) {
                        System.out.println("Registration successful! Awaiting admin approval.");
                    } else {
                        System.out.println("Registration failed. Username or Email may already exist.");
                    }
                    break;

                case 2:
                    // Login existing user
                    System.out.print("Enter Email: ");
                    String loginUser = sc.nextLine();
                    System.out.print("Enter password: ");
                    String loginPass = sc.nextLine();
                    boolean loggedIn = cf.loginUser(loginUser, loginPass);

                    if (loggedIn) {
                        System.out.println("Login successful!");
                        if (cf.isAdmin(loginUser)) {
                            adminMenu(sc, cf, loginUser);
                        } else {
                            userMenu(sc, cf, loginUser);
                        }
                    } else {
                        System.out.println("Login failed. Invalid credentials or account pending approval.");
                    }
                    break;

                case 3:
                    System.out.println("Exiting program. Goodbye! 👋");
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // --------------------------------------------------------------------------------------------------
    // Regular user menu
    // --------------------------------------------------------------------------------------------------
    private static void userMenu(Scanner sc, dbConnect cf, String username) {
        while (true) {
            System.out.println("\n--- User Menu for " + username + " ---");
            System.out.println("1. View Products");
            System.out.println("2. Make Transaction");
            System.out.println("3. Make Payment");
            System.out.println("4. View Transactions");
            System.out.println("5. Update Transaction Quantity");
            System.out.println("6. Delete Transaction");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");

            int option = -1;
            try {
                option = sc.nextInt();
                sc.nextLine(); // consume newline
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear the invalid input
                continue;
            }

            switch (option) {
                case 1:
                    cf.viewProducts();
                    break;
                case 2:
                    System.out.println("Available Products:");
                    cf.viewProducts(); // Display products before transaction

                    int user_id = cf.getUserId(username);
                    if (user_id <= 0) {
                        System.out.println("Error: User ID not found. Cannot proceed with transaction.");
                        break;
                    }

                    try {
                        System.out.print("Enter product id to buy: ");
                        int productId = sc.nextInt();
                        System.out.print("Enter quantity: ");
                        int quantity = sc.nextInt();
                        sc.nextLine(); // consume newline

                        cf.makeTransaction(username, user_id, productId, quantity);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input for ID or Quantity. Please enter numbers.");
                        sc.nextLine();
                    }
                    break;
                case 3:
                    cf.viewTransactions(username);
                    try {
                        System.out.print("Enter transaction id to pay: ");
                        int transactionId = sc.nextInt();
                        sc.nextLine();
                        cf.makePayment(username, transactionId);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input for Transaction ID. Please enter a number.");
                        sc.nextLine();
                    }
                    break;
                case 4:
                    cf.viewTransactions(username);
                    break;
                case 5:
                    cf.viewTransactions(username);
                    try {
                        System.out.print("Enter transaction id to update: ");
                        int transactionid = sc.nextInt();
                        System.out.print("Enter new quantity: ");
                        int newquantity = sc.nextInt();
                        sc.nextLine();
                        cf.updateTransaction(transactionid, newquantity, username);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input for ID or Quantity. Please enter numbers.");
                        sc.nextLine();
                    }
                    break;
                case 6:
                    cf.viewTransactions(username);
                    try {
                        System.out.print("Enter transaction id to delete: ");
                        int deletetransactionid = sc.nextInt();
                        sc.nextLine();
                        cf.deleteTransaction(deletetransactionid, username);
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input for Transaction ID. Please enter a number.");
                        sc.nextLine();
                    }
                    break;
                case 7:
                    System.out.println("Logging out " + username + "...");
                    return; // Exit user menu loop
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // --------------------------------------------------------------------------------------------------
    // Product Management Methods (for Admin Menu)
    // --------------------------------------------------------------------------------------------------

    private static void addProduct(Scanner sc, dbConnect cf, String adminUsername) {
        System.out.println("\n--- Add Product ---");
        System.out.print("Enter product name: ");
        String name = sc.nextLine();
        try {
            System.out.print("Enter product price: ");
            double price = sc.nextDouble();
            System.out.print("Enter product stock quantity: ");
            int stock = sc.nextInt();
            sc.nextLine(); // consume newline
            cf.addProduct(name, price, stock, adminUsername);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input for Price or Stock. Please enter numbers.");
            sc.nextLine(); // Clear the invalid input
        }
        viewProducts(cf);
    }

    private static void updateProduct(Scanner sc, dbConnect cf, String adminUsername) {
        viewProducts(cf);
        try {
            System.out.println("\n--- Update Product ---");
            System.out.print("Enter ID of product to update: ");
            int updateId = sc.nextInt();
            sc.nextLine(); // consume newline
            System.out.print("Enter new product name: ");
            String newName = sc.nextLine();
            System.out.print("Enter new product price: ");
            double newPrice = sc.nextDouble();
            System.out.print("Enter new product stock quantity: ");
            int newStock = sc.nextInt();
            sc.nextLine(); // consume newline
            cf.updateProduct(updateId, newName, newPrice, newStock, adminUsername);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input for ID, Price, or Stock. Please enter numbers.");
            sc.nextLine(); // Clear the invalid input
        }
        viewProducts(cf);
    }

    private static void deleteProduct(Scanner sc, dbConnect cf, String adminUsername) {
        viewProducts(cf);
        try {
            System.out.println("\n--- Delete Product ---");
            System.out.print("Enter product id to delete: ");
            int deleteId = sc.nextInt();
            sc.nextLine(); // consume newline
            cf.deleteProduct(deleteId, adminUsername);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input for ID. Please enter a number.");
            sc.nextLine(); // Clear the invalid input
        }
        viewProducts(cf);
    }

    private static void viewProducts(dbConnect cf) {
        System.out.println("\n--- View All Products ---");
        cf.viewProducts(); 
    }

    // --------------------------------------------------------------------------------------------------
    // User Account Management Methods (for Admin Menu)
    // --------------------------------------------------------------------------------------------------

    private static void viewPendingUsers(dbConnect cf) {
        System.out.println("\n--- View Pending Users ---");
        cf.viewPendingUsers(); 
    }

    private static void approveUser(Scanner sc, dbConnect cf) {
        viewPendingUsers(cf);
        System.out.println("\n--- Approve User ---");
        System.out.print("Enter username to approve: ");
        String userToApprove = sc.nextLine();
        boolean approved = cf.approveUser(userToApprove);
        if (approved) {
            System.out.println("User '" + userToApprove + "' approved successfully! They can now log in.");
        } else {
            System.out.println("Failed to approve user. User may not exist or already approved.");
        }
    }

    private static void promoteUserToAdmin(Scanner sc, dbConnect cf, String adminUsername) {
        viewUsers(cf);
        System.out.println("\n--- Promote User to Admin ---");
        System.out.print("Enter account name to promote as admin: ");
        String userToPromote = sc.nextLine();
        boolean success = cf.makeUserAdmin(userToPromote, adminUsername);
        if (success) {
            System.out.println("User '" + userToPromote + "' has been successfully promoted to Admin. 🎉");
        } else {
            System.out.println("Failed to promote user. Check if the user exists or is not attempting to demote themselves.");
        }
    }

    private static void viewUsers(dbConnect cf) {
        System.out.println("\n--- View All Accounts ---");
        cf.viewUsers(); 
    }

    // --------------------------------------------------------------------------------------------------
    // Transaction Management Methods (for Admin Menu)
    // --------------------------------------------------------------------------------------------------
    private static void deleteAnyTransaction(Scanner sc, dbConnect cf, String adminUsername) {
        System.out.println("\n--- Force Delete ANY Transaction ---");
        try {
            System.out.print("Enter transaction ID to forcefully delete: ");
            int deleteTxnId = sc.nextInt();
            sc.nextLine(); // consume newline
            cf.adminDeleteTransaction(deleteTxnId, adminUsername);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number for the Transaction ID.");
            sc.nextLine(); // Clear the invalid input
        }
    }


    // --------------------------------------------------------------------------------------------------
    // Admin menu
    // --------------------------------------------------------------------------------------------------
    private static void adminMenu(Scanner sc, dbConnect cf, String username) {
        while (true) {
            System.out.println("\n--- Admin Menu for " + username + " (ADMIN) ---");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product");
            System.out.println("3. Delete Product");
            System.out.println("--- User Account Management ---");
            System.out.println("4. View Pending Users");
            System.out.println("5. Approve User");
            System.out.println("6. Promote User to Admin");
            System.out.println("7. View All Accounts");
            System.out.println("--- Transaction Management ---");
            System.out.println("8. Delete ANY Transaction (No Stock Restore!)");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");

            int option = -1;
            try {
                option = sc.nextInt();
                sc.nextLine(); // consume newline
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear the invalid input
                continue;
            }

            switch (option) {
                case 1:
                    addProduct(sc, cf, username);
                    break;
                case 2:
                    updateProduct(sc, cf, username);
                    break;
                case 3:
                    deleteProduct(sc, cf, username);
                    break;
                case 4:
                    viewPendingUsers(cf);
                    break;
                case 5:
                    approveUser(sc, cf);
                    break;
                case 6:
                    promoteUserToAdmin(sc, cf, username);
                    break;
                case 7:
                    viewUsers(cf);
                    break;
                case 8: 
                    deleteAnyTransaction(sc, cf, username);
                    break;
                case 9:
                    System.out.println("Logging out " + username + "...");
                    return; // Exit admin menu loop
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
