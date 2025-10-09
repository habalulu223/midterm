package main;

import config.config; 
import java.util.InputMismatchException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        config cf = new config();
        
       
        System.out.println("Initializing Application...");

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
                        System.out.println("Registration failed. Username may already exist.");
                    }
                    break;

                case 2:
                    // Login existing user
                    System.out.print("Enter Gmail: ");
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
    private static void userMenu(Scanner sc, config cf, String username) {
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
                    cf.viewProducts();  // Display products before transaction
                    
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
                        // ⚠️ Corrected call to the 3-parameter updateTransaction method
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

   
private static void adminMenu(Scanner sc, config cf, String username) {
    while (true) {
        System.out.println("\n--- Admin Menu for " + username + " (ADMIN) ---");
        System.out.println("1. Add Product");
        System.out.println("2. Update Product");
        System.out.println("3. Delete Product");
        System.out.println("4. View Pending Users");
        System.out.println("5. Approve User");
        System.out.println("6. Delete ANY Transaction");
        System.out.println("7. Add user as admin");
        System.out.println("8 View accounts");
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
                // ... (existing code for Add Product) ...
                System.out.print("Enter product name: ");
                String name = sc.nextLine();
                try {
                    System.out.print("Enter product price: ");
                    double price = sc.nextDouble();
                    System.out.print("Enter product stock quantity: ");
                    int stock = sc.nextInt();
                    sc.nextLine();
                    cf.addProduct(name, price, stock, username);
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input for Price or Stock. Please enter numbers.");
                    sc.nextLine();
                }
                break;
            case 2:
                // ... (existing code for Update Product) ...
                cf.viewProducts();
                try {
                    System.out.print("Enter id to update: ");
                    int updateId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter new product name: ");
                    String newName = sc.nextLine();
                    System.out.print("Enter new product price: ");
                    double newPrice = sc.nextDouble();
                    System.out.print("Enter new product stock quantity: ");
                    int newStock = sc.nextInt();
                    sc.nextLine();
                    cf.updateProduct(updateId, newName, newPrice, newStock, username);
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input for ID, Price, or Stock. Please enter numbers.");
                    sc.nextLine();
                }
                break;
            case 3:
                // ... (existing code for Delete Product) ...
                cf.viewProducts();
                try {
                    System.out.print("Enter product id to delete: ");
                    int deleteId = sc.nextInt();
                    sc.nextLine();
                    cf.deleteProduct(deleteId, username);
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input for ID. Please enter a number.");
                    sc.nextLine();
                }
                break;
            case 4:
                // ... (existing code for View Pending Users) ...
                cf.viewPendingUsers();
                break;
            case 5:
                // ... (existing code for Approve User) ...
                cf.viewPendingUsers();
                System.out.print("Enter username to approve: ");
                String userToApprove = sc.nextLine();
                boolean approved = cf.approveUser(userToApprove);
                if (approved) {
                    System.out.println("User '" + userToApprove + "' approved successfully! They can now log in.");
                } else {
                    System.out.println("Failed to approve user. User may not exist or already approved.");
                }
                break;
            case 6: // <-- NEW ADMIN OPTION: Delete ANY Transaction
                // Optional: You might want a 'view all transactions' method here for admin
                System.out.print("Enter transaction ID to forcefully delete: ");
                try {
                    int deleteTxnId = sc.nextInt();
                    sc.nextLine();
                    cf.adminDeleteTransaction(deleteTxnId, username);
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number for the Transaction ID.");
                    sc.nextLine();
                }
                break;
            case 7:
                cf.viewUsers();
                System.out.print("Enter account name to prromote as admin: ");
                String userToPromote = sc.nextLine();
                boolean success = cf.makeUserAdmin(userToPromote, username); 
                if (success) {
                    System.out.println("User '" + userToPromote + "' has been successfully promoted to Admin. 🎉");
                } else {
                    System.out.println("Failed to promote user. Check if the user exists and is not already an admin.");
                }
                break;
            case 8:
                cf.viewUsers();
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