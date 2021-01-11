package example.movies.backend;

import example.movies.Environment;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import spark.Spark;

import static spark.Spark.externalStaticFileLocation;

/**
 * @author Michael Hunger @since 22.10.13
 */
public class DBServer {

    public static void main(String[] args) {
        Spark.port(Environment.getPort());
        externalStaticFileLocation("src/main/webapp");
        var driver = GraphDatabase.driver(
                Environment.getNeo4jUrl(),
                AuthTokens.basic(Environment.getNeo4jUsername(), Environment.getNeo4jPassword())
        );

        var service = new CommunityService(driver, Environment.getNeo4jDatabase());
        new DbRoutes(service).init();

        Runtime.getRuntime().addShutdownHook(new Thread(driver::close));
    }
}
