package com.community.commu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CommuDispatcher {

    @GetMapping("neo4j/louvain")
    public String getStreamLouvain(@RequestParam String name, @RequestParam String fields) {
        return Neo4jClient.getLouvain(name, fields) ;
    }

    @GetMapping("neo4j/labelPropagation")
    public String getStreamLabelPropagation(@RequestParam String name, @RequestParam String fields) {
        return Neo4jClient.getLabelP(name, fields) ;
    }

    @GetMapping("neo4j/triangle")
    public String getStreamTriangle(@RequestParam String name) {
        return Neo4jClient.getTriangle(name) ;
    }

    //work only for dataset email with nodes which has department on property
    @GetMapping("neo4j/louvain/analyse")
    public String getStreamLouvainAnalyse(@RequestParam String name) {
        return Neo4jClient.getAnalyseStream(Neo4jClient.getLouvain(name, "[department]")) ;
    }

    @GetMapping("neo4j/labelPropagation/analyse")
    public String getStreamLPAnalyse(@RequestParam String name) {
        return Neo4jClient.getAnalyseStream(Neo4jClient.getLabelP(name, "[department]")) ;
    }

    @GetMapping("neo4j/louvain/analyseTriangle")
    public String getNodeLouvainAnalyse(@RequestParam String name) {
        //creation graphInMemory
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("nodetype", "Person");
        map.put("relation", "Send");
        map.put("directed", false);
        map.put("wasOriented", true);
        Neo4jClient.createGraphIn(map);
        //creation properties
        Map<String, Object> put = new HashMap<>();
        put.put("algo", "louvain") ;
        put.put("property", "communityId") ;
        put.put("name", "java") ;
        Neo4jClient.createProperty(put);
        put.replace("algo", "triangle");
        put.replace("property", "triangles");
        Neo4jClient.createProperty(put);
        //analyse
        return Neo4jClient.getAnalyseNP(Neo4jClient.getNodeProperties(name, "[department]", "[communityId, triangles]")) ;
    }

    //make all
    @GetMapping("neo4j/labelPropagation/analyseTriangle")
    public String getNodeLabelPAnalyse(@RequestParam String name) {
        //creation graphInMemory
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("nodetype", "Person");
        map.put("relation", "Send");
        map.put("directed", false);
        map.put("wasOriented", true);
        Neo4jClient.createGraphIn(map);
        //creation properties
        Map<String, Object> put = new HashMap<>();
        put.put("algo", "label") ;
        put.put("property", "communityId") ;
        put.put("name", name) ;
        Neo4jClient.createProperty(put);
        put.replace("algo", "triangle");
        put.replace("property", "triangles");
        Neo4jClient.createProperty(put);
        //analyse
        return Neo4jClient.getAnalyseNP(Neo4jClient.getNodeProperties(name, "[department]", "[communityId, triangles]")) ;
    }

}
