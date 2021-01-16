package example.movies.backend;

import example.movies.Environment;
import javafx.util.Pair;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {

    public static void test_Import(DatabaseService service) {
            List<Pair<String, DatabaseService.type>>  fields = new ArrayList<Pair<String, DatabaseService.type>>() ;
            fields.add(new Pair<String, DatabaseService.type>("name", DatabaseService.type.INTEGER)) ;
            fields.add(new Pair<String, DatabaseService.type>("department", DatabaseService.type.INTEGER)) ;
            service.importNodeCSV("/home/denis/5A/testUpload.txt", " ", true, "Test", fields);

            service.importEdgeCSV("/home/denis/5A/community/email/email.txt", " ", true, "Test", "Send",
                    new  Pair< Pair<String, DatabaseService.type>, Pair<String, DatabaseService.type> >(
                            new Pair<String, DatabaseService.type>("name", DatabaseService.type.INTEGER),
                            new Pair<String, DatabaseService.type>("name", DatabaseService.type.INTEGER)
                    )
            ) ;
    }

    public static void test_Louvain(CommunityService service) {
        //create the graph and launches Louvain
        service.create_graph("email_directed", "Person", "Send", true) ;
        List<Map<String, Object>> res = service.labelPropagation("email_undirected", "stream") ;
        if(res.isEmpty()){
            System.out.println("Vide !");
        }
        else {
            //Return every community, and the nodes associated. Return their original department, if the department contributes to at least 10% of the community
            ArrayList<Integer> taux_exact = new ArrayList<Integer>() ;
            Object pred = null ;
            int size_community = 0 ;
            HashMap<Object, Double> members = new HashMap<Object, Double>() ;
            for ( Map<String, Object> one:res)
            {
                Object community = one.get("communityId");
                Object solution =  one.get("solution");
                Object name =  one.get("name");
                if(pred==null || pred.equals(community)) {
                    size_community++ ;
                    members.merge(solution, 1.0, Double::sum) ;
                }
                else {
                    for(Map.Entry<Object, Double> member:members.entrySet()) {
                        Double taux = member.getValue() / size_community * 100;
                        if(taux>10.0)
                            System.out.print(member.getKey() + " : " + taux + "% | ") ;
                    }
                    if(size_community>1)
                        System.out.println("taille communauté : " + size_community);
                    size_community = 1 ;
                    members.clear() ;
                }
                pred = community ;
                //System.out.println( name + " : " + community);
                    /*
                    for (Map.Entry<String, Object> entry : one.entrySet()) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                     */

                }
                for(Map.Entry<Object, Double> member:members.entrySet()) {
                    Double taux = member.getValue() / size_community * 100;
                    if(taux>10.0)
                        System.out.print(member.getKey() + " : " + taux + "% | ") ;
                }
                if(size_community>1)
                    System.out.println("taille communauté : " + size_community);
            }
    }

    public static void test_triangle(CommunityService service) {
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Person", "Send", false) ;
        List<Map<String, Object>> res = service.triangle("email_undirected", "stream") ;
        if(res.isEmpty()){
            System.out.println("Vide !");
        }
        else {
            for ( Map<String, Object> one:res)
            {
                System.out.println("name " + one.get("name") + " nb triangle = " + one.get("triangleCount") ) ;
            }
        }
    }

    public static void main(String[] args) {
        //log in neo4j
        Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "neo4j"));
        try (Session session = driver.session()) {
            var service = new CommunityService(driver, Environment.getNeo4jDatabase()) ;
            test_triangle(service) ;
        }
        driver.close();
    }
}

