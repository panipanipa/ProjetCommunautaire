package example.movies.backend;

import example.movies.Environment;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {

    public static void main(String[] args) {
        //log in neo4j
        Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "neo4j"));
        try (Session session = driver.session()) {
            var service = new CommunityService(driver, Environment.getNeo4jDatabase()) ;
            //create the graph and launches Louvain
            service.create_graph("email_directed", "Person", "Send", true) ;
            List<Map<String, Object>> res = service.Louvain("email_undirected", "stream") ;
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
        driver.close();
    }
}

