package com.jdbcdemo;

import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class diablo {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "123456");

        Connection connection = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/diablo", properties);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();

        PreparedStatement query = connection.prepareStatement
                ("Select user_name, first_name, last_name, Count(ug.id) " +
                "from users " +
                "join users_games as ug on users.id = ug.user_id" +
                " where user_name =?");
        query.setString(1, username);

        ResultSet result = query.executeQuery();

        if (result.next()){
        String dbUsername = result.getString("user_name");
        String dbFirstName = result.getString("first_name");
        String dbLastName = result.getString("last_name");
        int dbGamesCount = result.getInt("count(ug.id)");

            System.out.printf("User: %s%n%s %s has played %d games",dbUsername,dbFirstName,dbLastName,dbGamesCount);
        }else {
            System.out.println("No such user exists");
        }
    }
}
