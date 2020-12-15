package example.movies.backend;

import example.movies.Environment;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;

public class test {

    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "neo4j"));
        try (Session session = driver.session()) {
            var service = new EmailService(driver, Environment.getNeo4jDatabase()) ;
            List<Map<String, Object>> res = service.findDestinators("0") ;
            if(res.isEmpty()){
                System.out.println("Vide !");
            }
            else {
                for ( Map<String, Object> one:res)
                {
                    for (Map.Entry<String, Object> entry : one.entrySet()) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                }
            }
            Map<String, Object> graph = service.graph(10) ;
        }
        driver.close();
    }
}

