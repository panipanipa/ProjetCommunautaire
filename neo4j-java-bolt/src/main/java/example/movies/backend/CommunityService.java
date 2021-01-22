package example.movies.backend;

import javafx.util.Pair;
import org.neo4j.driver.Driver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommunityService extends DatabaseService {

    private static final List<String> modes = Arrays.asList("stats", "write", "mutate") ;

    public CommunityService(Driver driver, String database) {
        super(driver, database);
    }

    public void create_graph(String name, String nodetype, String relation, boolean directed, boolean wasOriented) {
        String conf_relation ;
        if(!directed) {
            conf_relation = "{" + relation+": { orientation: 'Undirected'" +
                    (wasOriented ? ", aggregation : 'SINGLE'" : "") +
                    "} }" ;
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

    // oriented by default
    public void create_graph(String name, String nodetype, String relation) {
        String conf_relation ;
        conf_relation = "$relation" ;
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

    public List<Map<String, Object>> getNodeProperty(String name,List<String> fields,  List<String> properties) {
        StringBuilder query = new StringBuilder("Call gds.graph.streamNodeProperties($name, [") ;
        boolean first = true ;
        for (String prop:properties) {
            if(!first) {
                query.append(", ");
            }
            query.append("'").append(prop).append("'");
            first = false ;
        }
        query.append("]) Yield nodeId as id, nodeProperty as what, propertyValue as val ") ;
        first = true ;
        if(!fields.isEmpty()) {
            query.append("return " );
            for (String field : fields) {
                if(!first) {
                    query.append(", ");
                }
                query.append("gds.util.asNode(id).").append(field).append(" as ").append(field) ;
                first = false ;
            }
            query.append(", what, val") ;
        }
        //query.append("return gds.util.asNode(id).name as name, gds.util.asNode(id).department as solution, what, val") ;
        var result = query(
                query.toString(),
                Map.of("name" , name)
        ) ;
        return result ;
    }

    /*
        Call gds.labelPropagation.stream("email_undirected")
    YIELD nodeId, communityId
    RETURN communityId,
    collect(gds.util.asNode(nodeId).personId) as members,
    collect(gds.util.asNode(nodeId).department) as solution
    ORDER BY communityId ASC
     */

    public List<Map<String, Object>> louvain(String name, String mode, List<String> fields) {
        String callLouvain = null ;
        List<Map<String, Object>> res = null;
        String query = "" ;
        StringBuilder query2 = new StringBuilder("Call ") ;
        if ("stream".equals(mode)) {
            query2 .append("gds.louvain.stream($name) YIELD nodeId, communityId RETURN communityId") ;
            int i = 0 ;
            for (String field:fields) {
                query2.append(", collect(gds.util.asNode(nodeId).").append(field).append(") as f").append(i) ;
                i++ ;
            }
            /*
            query = "Call gds.louvain.stream($name) " +
                    "YIELD nodeId, communityId " +
                    "RETURN gds.util.asNode(nodeId).personId AS name, gds.util.asNode(nodeId).department AS solution, communityId \n" +
                    "ORDER BY communityId, name ASC" ;

             */
            res = query(query2.toString(), Map.of("name", name)) ;
        } else if (modes.contains(mode)) {
            query = "Call gds.louvain." + mode + "('" + name + "'" +
                    ("stats".equals(mode) ? "" : ", { "+mode+"Property"+": '"+ fields.get(0) +"' }")+ ") " +
                    "YIELD communityCount, createMillis, computeMillis, communityDistribution " +
                    "Return communityCount, createMillis, computeMillis, communityDistribution";
            res = write_query(query) ;
        }
        return res;
    }

    public List<Map<String, Object>> labelPropagation(String name, String mode, List<String> fields) {
        List<Map<String, Object>> res = null;
        String query = "" ;
        StringBuilder query2 = new StringBuilder("Call ") ;
        if ("stream".equals(mode)) {
            query2.append("gds.labelPropagation.stream($name) YIELD nodeId, communityId RETURN communityId");
            int i = 0;
            for (String field : fields) {
                query2.append(",collect(gds.util.asNode(nodeId).").append(field).append(") as f").append(i);
                i++ ;
            }
            res = query(query2.toString(), Map.of("name", name)) ;
            /*
            query = "Call gds.labelPropagation.stream($name) " +
                    "YIELD nodeId, communityId " +
                    "RETURN gds.util.asNode(nodeId).personId AS name, gds.util.asNode(nodeId).department AS solution, communityId " +
                    "ORDER BY communityId, name ASC";
            res = query(query, Map.of("name", name)) ;
             */
        } else if (modes.contains(mode)) {
            query = "Call gds.labelPropagation." + mode + "('" + name + "'" +
                    ("stats".equals(mode) ? "" : ", { "+mode+"Property"+": '"+ fields.get(0) +"' }")+ ") " +
                    "YIELD communityCount, createMillis, computeMillis, communityDistribution " +
                    "Return communityCount, createMillis, computeMillis, communityDistribution";
            res = write_query(query) ;
        }
        return res;
    }

    public List<Map<String, Object>> triangle(String name, String mode, List<String> fields) {
        List<Map<String, Object>> res = null;
        String query = "" ;
        //Triangle count node by node.
        if ("stream".equals(mode)) {
            query = "CALL gds.triangleCount.stream($name) " +
                    "YIELD   nodeId, triangleCount " +
                    "RETURN gds.util.asNode(nodeId).personId AS name, triangleCount";
            //Stream mode = info + Global triangle Count
            res = query(query, Map.of("name", name)) ;
        }
        else if (modes.contains(mode)) {
            query = "Call gds.triangleCount." + mode + "('" + name + "'" +
                    ("stats".equals(mode) ? "" : ", { "+mode+"Property"+": '"+ fields.get(0) +"' }")+ ") " +
                    "YIELD globalTriangleCount, createMillis, computeMillis " +
                    "Return globalTriangleCount, createMillis, computeMillis";
            res = write_query(query) ;
        }
        return res;
    }

    public List<Map<String, Object>> localClusteringCoef(String name, String mode,  List<String> fields) {
        List<Map<String, Object>> res = null ;
        String query = "" ;
        if ("stream".equals(mode)) {
            query = "CALL  gds.localClusteringCoefficient.stream($name) " +
                    "YIELD   nodeId, localClusteringCoefficient as coef " +
                    "RETURN gds.util.asNode(nodeId).personId AS name, coef";
            //Stream mode = info + Global triangle Count
            res = query(query, Map.of("name", name)) ;
        }
        else if (modes.contains(mode)) {
            query = "Call gds.localClusteringCoefficient." + mode + "('" + name + "'" +
                    ("stats".equals(mode) ? "" : ", { "+mode+"Property"+": '"+ fields.get(0) +"' }")+ ") " +
                    "YIELD averageClusteringCoefficient as avCef, nodeCount, createMillis, computeMillis " +
                    "Return avCef, nodeCount, createMillis, computeMillis";
            res = write_query(query) ;
        }
        return res;
    }


}