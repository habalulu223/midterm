package main;

import config.config;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        config cf = new config();
        cf.connectDB();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nWelcome! Choose an option:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    // Register new user
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    boolean registered = cf.registerUser (username, password);
                    if (registered) {
                        System.out.println("Registration successful!");
                    } else {
                        System.out.println("Registration failed. Username may already exist.");
                    }
                    break;

                case 2:
                    // Login existing user
                    System.out.print("Enter username: ");
                    String loginUser  = sc.nextLine();
                    System.out.print("Enter password: ");
                    String loginPass = sc.nextLine();
                    boolean loggedIn = cf.loginUser (loginUser , loginPass);
                    if (loggedIn) {
                        System.out.println("Login successful!");
                        if (cf.isAdmin(loginUser )) {
                            adminMenu(sc, cf, loginUser );
                        } else {
                            userMenu(sc, cf, loginUser );
                        }
                    } else {
                        System.out.println("Login failed. Invalid credentials.");
                    }
                    break;

                case 3:
                    System.out.println("Exiting program. Goodbye!");
                    sc.close();
                    System.exit(0);

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Regular user menu
    private static void userMenu(Scanner sc, config cf, String username) {
        while (true) {
            System.out.println("\nUser   Menu - Choose an option:");
            System.out.println("1. View Products");
            System.out.println("2. Make Transaction");
            System.out.println("3. Make Payment");
            System.out.println("4. View Transaction");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            int option = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (option) {
                case 1:
                    cf.viewProducts();
                    break;
                case 2:
                    System.out.println("Available Products:");
                    cf.viewProducts();  // Display products before transaction
                    System.out.print("Enter product id to buy: ");
                    int productId = sc.nextInt();
                    System.out.print("Enter quantity: ");
                    int quantity = sc.nextInt();
                    sc.nextLine();
            {
                int user_id = 0;
                cf.makeTransaction(username,user_id, productId, quantity);
            }
                    break;
                case 3:
                    System.out.print("Enter transaction id to pay: ");
                    int transactionId = sc.nextInt();
                    sc.nextLine();
                    cf.makePayment(username, transactionId);
                    break;
                case 4:
                     cf.viewTransactions(username);  // Call new method to view transactions
                    
                    return;
                case 5:
                System.out.println("Logging out...");
                break;
                    
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

// Admin menu with add, update, delete, exit
private static void adminMenu(Scanner sc, config cf, String username) {
        while (true) {
            System.out.println("\nAdmin Menu - Choose an option:");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product");
            System.out.println("3. Delete Product");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int option = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (option) {
                case 1:
                    System.out.print("Enter product name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter product price: ");
                    double price = sc.nextDouble();
                    System.out.print("Enter product stock quantity: ");
                    int stock = sc.nextInt();
                    sc.nextLine();
                    cf.addProduct(name, price, stock, username);
                    break;
                case 2:
                    cf.viewProducts();
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
                    break;
                case 3:
                    cf.viewProducts();
                    System.out.print("Enter new product stock quantity: ");
                    int deleteId = sc.nextInt();
                    sc.nextLine();
                    cf.deleteProduct(deleteId, username);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}