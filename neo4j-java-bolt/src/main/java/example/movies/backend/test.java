package example.movies.backend;

import example.movies.Environment;
import javafx.util.Pair;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;


import java.util.*;

//was tested on email dataset
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

    public static void test_mutate(CommunityService service) {
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Person", "Send", false, true) ;
        List<Map<String, Object>> res = service.louvain("email_undirected", "mutate",
                Collections.singletonList("communityId"));
        for (Map<String, Object> one:res) {
            for (Map.Entry<String, Object> entry : one.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    public static void test_Louvain(CommunityService service) {
        //create the graph and launches Louvain
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Person", "Send", false, true) ;
        List<Map<String, Object>> res = service.labelPropagation("email_undirected", "stream",
                Collections.singletonList("department"));
        if(res.isEmpty()){
            System.out.println("Vide !");
        }
        else {
            //Return every community, and the nodes associated. Return their original department, if the department contributes to at least 10% of the community
            ArrayList<Integer> taux_exact = new ArrayList<Integer>() ;
            Object pred = null ;
            int size_community = 0 ;
            //HashMap<Object, Double> members = new HashMap<Object, Double>() ;
            for ( Map<String, Object> one:res)
            {
                Object community = one.get("communityId");
                System.out.println("Community : " + community) ;
                HashMap<Object, Double> presence = new HashMap<Object, Double>() ;
                int total = 0 ;
                for (Object id : (Collection<?>) one.get("f0")) {
                    presence.merge(id, 1.0, Double::sum) ;
                    total++ ;
                }
                if(total>1) {
                    for(Map.Entry<Object, Double> id:presence.entrySet()) {
                        Double taux = id.getValue() / total * 100;
                        if(taux>10.0)
                            System.out.print(id.getKey() + " : " + taux + "% | ") ;
                    }
                }
            }
                /*
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


                }
                for(Map.Entry<Object, Double> member:members.entrySet()) {
                    Double taux = member.getValue() / size_community * 100;
                    if(taux>10.0)
                        System.out.print(member.getKey() + " : " + taux + "% | ") ;
                }
                if(size_community>1)
                    System.out.println("taille communauté : " + size_community);
                */
            }
    }

    public static void test_triangle(CommunityService service) {
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Person", "Send", false, true) ;
        List<Map<String, Object>> res = service.triangle("email_undirected", "stream", Collections.emptyList()) ;
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

    public  static void test_getProperties(CommunityService service) {
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Test", "Send", false, true) ;

        //Louvain
        List<Map<String, Object>> res = service.louvain("email_undirected", "mutate",
                Collections.singletonList("communityId"));
        for (Map<String, Object> one:res) {
            for (Map.Entry<String, Object> entry : one.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }

        //Triangle
        List<Map<String, Object>> res2 = service.triangle("email_undirected", "mutate",
                Collections.singletonList("triangle"));
        for (Map<String, Object> one:res2) {
            for (Map.Entry<String, Object> entry : one.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }

        //get node properties
        List<Map<String, Object>> res3 = service.getNodeProperty("email_undirected", Arrays.asList("communityId", "triangle")) ;
        HashMap<Object, HashMap<String,Object>> stat = new HashMap<>();
        Object key = null ;
        for (Map<String, Object> one:res3) {
            Object what = one.get("what") ;
            if(what.equals("communityId")) {
                key = one.get("val") ;
                Object commu = one.get("solution");;
                if(!stat.containsKey(key)) {
                    stat.put(key, new HashMap<>());
                    stat.get(key).put("presence", new HashMap<>()) ;
                }
                HashMap tab = (HashMap) stat.get(key).get("presence");
                tab.merge(String.valueOf(commu), 1.0, (oldValue, newValue) -> (Double) oldValue + (Double) newValue) ;
            }
            else if(what.equals("triangle")) {
                stat.get(key).merge("triangle", (Long) one.get("val"), (oldValue, newValue) -> (Long) oldValue + (Long) newValue) ;
                stat.get(key).merge("size", 1.0 , (oldValue, newValue) -> (Double) oldValue + (Double) newValue) ;
            }
        }

        //Affichage des résultats
        for(Map.Entry<Object, HashMap<String, Object>> entry:stat.entrySet()) {
            Object commu = entry.getKey();
            HashMap values = entry.getValue();
            Double size = (Double) values.get("size");
            Long triangle = (Long) values.get("triangle") ;
            HashMap<Object, Double> presence = (HashMap<Object, Double>) values.get("presence");
            System.out.println(commu + " size : " + size + " & avg_triangle : " + (triangle/size)) ;
            for ( Map.Entry<Object, Double> sol: presence.entrySet()) {
                 double taux = sol.getValue() ;
                 taux = taux / size *100;
                 if(taux >10.0)
                     System.out.print(" // " + sol.getKey() + " : " + taux ) ;
            }
            System.out.println("");
        }
    }

    public static void test_localClusteringCoef(CommunityService service) {
        if(!service.graph_exists("email_undirected"))
            service.create_graph("email_undirected", "Person", "Send", false, true) ;
        List<Map<String, Object>> res = service.localClusteringCoef("email_undirected", "stream", Collections.emptyList()) ;
        if(res.isEmpty()){
            System.out.println("Vide !");
        }
        else {
            for ( Map<String, Object> one:res)
            {
                System.out.println("name " + one.get("name") + " coefficient = " + one.get("coef") ) ;
            }
        }
    }

    public static void main(String[] args) {
        //log in neo4j
        Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "neo4j"));
        try (Session session = driver.session()) {
            var service = new CommunityService(driver, Environment.getNeo4jDatabase()) ;
            //test_Louvain(service);
            //test_mutate(service);
            test_getProperties(service) ;
        }
        driver.close();
    }
}

