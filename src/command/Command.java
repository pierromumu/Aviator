package command;

import java.sql.*;


public class Command {

    private static Connection c;
    private static Statement stmt;

    public static void connect(){

        try {
            Class.forName("org.postgresql.Driver");
            // dbname, username & password = aviator
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/aviator", "aviator", "aviator");
            System.out.println("Database opened successfully.");

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage());
            System.exit(0);
        }
    }

    public static void executeU(String text){

        try {
            stmt = c.createStatement();
            stmt.executeUpdate(text);
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQ(String text){

        ResultSet result = null;

        try {
            stmt = c.createStatement();
            result = stmt.executeQuery(text);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void closeStmt(){
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
