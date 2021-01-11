package example.movies.backend;

import org.neo4j.driver.Driver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommunityService extends DatabaseService {

    private static final List<String> modes = Arrays.asList("stats", "write", "mutate") ;

    public CommunityService(Driver driver, String database) {
        super(driver, database) ;
    }

    public void create_graph(String name, String nodetype, String relation, boolean directed) {
        String conf_relation ;
        if(!directed) {
            conf_relation = "{" +
                    relation+": {" +
                    "orientation: 'Undirected'" +
                    "}" +
                    "}" ;
        }
        else {
            conf_relation = "$relation" ;
        }
        System.out.println(conf_relation);
        var result = query(
                "CALL gds.graph.create(" +
                        "$name," +
                        "$nodetype," +
                        conf_relation +
                        ")",
                Map.of("name", name, "nodetype", nodetype, "relation", relation)
        );
    }

    public boolean graph_exists(String name) {
        var result = query(
                "Call gds.graph.exists($name)",
                Map.of("name" , name)
        ) ;
        return (Boolean) result.get(0).get("exists") ;
    }

    public List<Map<String, Object>> Louvain(String name, String mode) {
        String callLouvain = null ;
        List<Map<String, Object>> res = null;
        switch (mode) {
            case "stream" :
                callLouvain = "gds.louvain.stream" ;
                break;
            case "write":
                callLouvain = "gds.louvain.write" ;
                break;
        }
        if (callLouvain != null) {
            res = query(
                    "Call "+ callLouvain +"($name)\n" +
                            "YIELD nodeId, communityId \n" +
                            "RETURN gds.util.asNode(nodeId).personId AS name, gds.util.asNode(nodeId).department AS solution, communityId \n" +
                            "ORDER BY communityId, name ASC",
                    Map.of("name", name)
            ) ;
        }
        return res;
    }

    public List<Map<String, Object>> labelPropagation(String name, String mode) {
        List<Map<String, Object>> res = null;
        String query = "" ;
        if ("stream".equals(mode)) {
            query = "Call gds.labelPropagation.stream($name) " +
                    "YIELD nodeId, communityId " +
                    "RETURN gds.util.asNode(nodeId).personId AS name, gds.util.asNode(nodeId).department AS solution, communityId " +
                    "ORDER BY communityId, name ASC";
        } else if (modes.contains(mode)) {
            query = "Call gds.labelPropagation." + mode + "($name) " +
                    "YIELD communityCount, ranIterations, didConverge,  computeMillis, communityDistribution";
        }
        if (modes.contains(mode)) {
            res = query(query, Map.of("name", name)) ;
        }
        return res;
    }


}