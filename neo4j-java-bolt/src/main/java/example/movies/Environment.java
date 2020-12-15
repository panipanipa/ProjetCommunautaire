package example.movies;

/**
 * @author Michael Hunger @since 22.10.13
 */
public class Environment {

    private static final String DEFAULT_URL = "bolt://localhost";


    private static final String DEFAULT_DATABASE = "email";

    private static final String DEFAULT_USER = "neo4j";

    private static final String DEFAULT_PASS = "neo4j";

    public static int getPort() {
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            return 8080;
        }
        return Integer.parseInt(webPort);
    }

    public static String getNeo4jUrl() {
        String url = System.getenv("NEO4J_URI");
        if (url == null || url.isEmpty()) {
            return DEFAULT_URL;
        }
        return url;
    }

    public static String getNeo4jDatabase() {
        String database = System.getenv("NEO4J_DATABASE");
        if (database == null || database.isEmpty()) {
            return DEFAULT_DATABASE;
        }
        return database;
    }

    public static String getNeo4jUsername() {
        String user = System.getenv("NEO4J_USER");
        if (user == null || user.isEmpty()) {
            return DEFAULT_USER;
        }
        return user;
    }

    public static String getNeo4jPassword() {
        String password = System.getenv("NEO4J_PASSWORD");
        if (password == null || password.isEmpty()) {
            return DEFAULT_PASS;
        }
        return password;
    }
}
