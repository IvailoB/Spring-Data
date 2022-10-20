import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

public class Main {

    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final String TABLE_NAME = "minions_db";
    public static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static Connection connection;


    public static void main(String[] args) throws SQLException, IOException {

        connection = getConnection();
        System.out.println("Enter exercise number:");
        int exNum = Integer.parseInt(reader.readLine());

        switch (exNum) {
            case 2 -> exTwo();
            case 3 -> exThree();
            case 5 -> exFive();
            case 6 -> exSix();
            case 7 -> exSEven();
            case 9 -> exNine();
        }
    }

    private static void exSix() throws IOException, SQLException {
        System.out.println("Enter villain id:");
        int villainId = Integer.parseInt(reader.readLine());

        int affectedEntities = deleteMinionsByVillainId(villainId);

        String villainName = findEntityNameById("villains", villainId);
        deleteVillainById(villainId);

        System.out.printf("%s was deleted%n" +
                "%d minions released%n", villainName, affectedEntities);

    }

    private static void deleteVillainById(int villainId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM villains WHERE id = ?"
        );
        preparedStatement.setInt(1, villainId);

        preparedStatement.executeUpdate();
    }

    private static int deleteMinionsByVillainId(int villainId) throws SQLException {

        PreparedStatement preparedStatement = connection.
                prepareStatement("DELETE FROM minions_villains WHERE villain_id = ?");
        preparedStatement.setInt(1, villainId);
        return preparedStatement.executeUpdate();
    }

    private static void exNine() throws IOException, SQLException {

        System.out.println("Enter minion id");
        int minionId = Integer.parseInt(reader.readLine());

        CallableStatement callableStatement = connection.prepareCall("CALL usp_get_older(?)");
        callableStatement.setInt(1, minionId);
        int affected = callableStatement.executeUpdate();

        PreparedStatement preparedStatement = connection.prepareStatement(
                "Select name, age from minions"
        );
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s %d %n", resultSet.getString("name"),
                    resultSet.getInt("age"));
        }
    }

    private static void exSEven() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("Select name From minions");

        ResultSet resultSet = preparedStatement.executeQuery();

        List<String> allMinionsNames = new ArrayList<>();

        while (resultSet.next()) {
            allMinionsNames.add(resultSet.getString(1));
        }

        int start = 0;
        int end = allMinionsNames.size() - 1;

        for (int i = 0; i < end; i++) {
            System.out.println(i % 2 == 0
                    ? allMinionsNames.get(start++)
                    : allMinionsNames.get(end--));
        }


    }

    private static void exFive() throws IOException, SQLException {
        System.out.println("Enter country name: ");
        String countryName = reader.readLine();

        PreparedStatement preparedStatement = connection.
                prepareStatement("Update towns Set name = UPPER(name) where country = ?");
        preparedStatement.setString(1, countryName);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows == 0) {
            System.out.println("No town name");
            return;
        }

        System.out.printf("%d town names were affected%n", affectedRows);

        PreparedStatement preparedStatementTowns = connection
                .prepareStatement("select name from towns where country = ?");

        preparedStatementTowns.setString(1, countryName);
        ResultSet resultSet = preparedStatementTowns.executeQuery();

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }

    }

    private static void exThree() throws IOException, SQLException {
        System.out.println("Enter villain id:");
        int villainId = Integer.parseInt(reader.readLine());

        // String villainName = findVillainNameById(villainId);
        String villainName = findEntityNameById("villains", villainId);
        System.out.println("Villain: " + villainName);
        // getAllMinionsByVillainId(villainId);
        PreparedStatement preparedStatement = connection.prepareStatement("select m.name , m.age from minions as m\n" +
                "join minions_villains as mv on m.id = mv.minion_id\n" +
                "where mv.villain_id = ?;");

        preparedStatement.setInt(1, villainId);

        ResultSet resultSet = preparedStatement.executeQuery();
        int counter = 0;
        while (resultSet.next()) {
            System.out.printf("%d. %s %d%n",
                    ++counter,
                    resultSet.getString("name"),
                    resultSet.getInt("age"));
        }

    }

    private static Set<String> getAllMinionsByVillainId(int villainId) throws SQLException {
        Set<String> result = new LinkedHashSet<>();

        PreparedStatement preparedStatement = connection.prepareStatement("select m.name , m.age from minions as m\n" +
                "join minions_villains as mv on m.id = mv.minion_id\n" +
                "where mv.villain_id = ?;");

        preparedStatement.setInt(1, villainId);

        ResultSet resultSet = preparedStatement.executeQuery();
        int counter = 0;

        while (resultSet.next()) {
            result.add(String.format("%d. %s %d%n",
                    ++counter,
                    resultSet.getString("name"),
                    resultSet.getInt("age")
            ));
        }
        return null;
    }

    private static String findEntityNameById(String tableName, int entityId) throws SQLException {

        String query = "SELECT name FROM " + tableName + " WHERE id = ?";
        PreparedStatement preparedStatement = connection
                .prepareStatement(query);
        preparedStatement.setInt(1, entityId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }

    private static String findVillainNameById(int villainId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT \n" +
                        "    name\n" +
                        "FROM\n" +
                        "    villains\n" +
                        "WHERE\n" +
                        "    id = ?;");
        preparedStatement.setInt(1, villainId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString("name");

    }

    private static void exTwo() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT \n" +
                "    V.NAME, COUNT(DISTINCT mv.minion_id) AS 'm_count'\n" +
                "FROM\n" +
                "    villains AS V\n" +
                "        JOIN\n" +
                "    minions_villains mv ON v.id = mv.villain_id\n" +
                "GROUP BY v.name\n" +
                "HAVING m_count > ?;");

        preparedStatement.setInt(1, 15);

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s %d %n", resultSet.getString(1), resultSet.getInt(2));
        }
    }

    private static Connection getConnection() throws IOException, SQLException {
//        System.out.println("Enter user:");
//        String user = reader.readLine();
//        System.out.println("Enter password: ");
//        String password = reader.readLine();

        //ToDo: Please enter password
        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "123456");

        return DriverManager
                .getConnection(CONNECTION_STRING + TABLE_NAME, properties);
    }
}
