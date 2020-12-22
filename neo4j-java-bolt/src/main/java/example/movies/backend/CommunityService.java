package example.movies.backend;

import org.neo4j.driver.Driver;

import java.util.List;
import java.util.Map;

public class CommunityService extends DatabaseService {

    public CommunityService(Driver driver, String database) {
        super(driver, database) ;
    }

    public void create_graph(String name, String nodetype, String relation, boolean directed) {
        String conf_relation ;
        if(!directed) {
            conf_relation = "{" +
                    "$relation : {" +
                    "orientation: 'UNDIRECTED'" +
                    "}" +
                    "}" ;
        }
        else {
            conf_relation = ", $relation" ;
        }
        var result = query(
                "CALL gds.graph.create(" +
                        "$name," +
                        "$nodetype" +
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
            case "write":
                callLouvain = "gds.louvain.write" ;
        }
        if (callLouvain != null) {
            res = query(
                    "Call gds.louvain.stream($name)\n" +
                            "YIELD nodeId, communityId, intermediateCommunityIds\n" +
                            "RETURN gds.util.asNode(nodeId).personId AS name, gds.util.asNode(nodeId).department AS solution, communityId, intermediateCommunityIds\n" +
                            "ORDER BY communityId, name ASC",
                    Map.of("name", name)
            ) ;
        }
        return res;
    }
}