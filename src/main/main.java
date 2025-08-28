
package main;

import config.config;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
      config cf = new config();
      cf.connectDB();
      Scanner sc = new Scanner(System.in);
      
        System.out.print("Enter id: ");
        int id = sc.nextInt();
        System.out.print("Enter First name: ");
        String fn = sc.next();        
        System.out.print("Enter Last Name: ");
        String ln = sc.next();
        String sql = "INSERT INTO tbl_student(s_id,s_fname,s_lname) VALUES (?, ?, ?)";
        cf.addRecord(sql, id, fn,ln);
    }
    
}