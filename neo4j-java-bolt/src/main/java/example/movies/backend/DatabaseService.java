package example.movies.backend;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import spark.Spark;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DatabaseService {

    private final Driver driver;

    private final String database;

    public DatabaseService(Driver driver, String database) {
        this.driver = driver;
        this.database = database;
        //Spark.ipAddress("10.29.40.63");
    }

    public void importCSV(File file) {
        String query = "LOAD CSV FROM '{csv-dir}/artists.csv' AS line\n" +
                "CREATE (:Test { name: line[1], year: toInteger(line[2])})" ;
    }

    public List<Map<String, Object>> findDestinators(String idsrc) {
        var result = query(
                "Match (p:Person {personId:$idsrc})-[:Send]->(m) return m",
                Map.of("idsrc", idsrc)
        );
        return result.isEmpty() ? new ArrayList<>() : result;
    }

    public List<Map<String, Object>> findShortPath(String start, String end, boolean bi) {
        // friendship because a friendship is a kind of ship that doesn't sink
        // Specially because friendship is a relationship
        String friendship = "-[:Send]-" ;
        if (!bi)
            friendship += ">";
        var result = query(
                "match (start:Person {personId:$start}),\n" +
                        " (end:Person {personId:$end}),\n" +
                        " p=((start)"+friendship+"(end))\n"+
                        " return p",
                Map.of("start", start, "end", end)
        );
        return result.isEmpty() ? new ArrayList<>() : result;
    }

    public Iterable<Map<String, Object>> search(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        return query(
                "MATCH (person:Person)\n" +
                        " WHERE person.personId = $part\n" +
                        " RETURN person",
                Map.of("part", query)
        );
    }

    public Map<String, Object> graph(int limit) {
        var result = query(
                "MATCH (m:Person)<-[:Send]-(a:Person) " +
                        " return m.personId as id, collect(a.personId) as contact " +
                        " LIMIT $limit",
                Map.of("limit", limit)
        );

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> rels = new ArrayList<>();
        int i = 0;
        for (var row : result) {
            nodes.add(Map.of("person", row.get("id")));
            int target = i;
            i++;
            for (Object id : (Collection<?>) row.get("contact")) {
                Map<String, Object> contact_list = Map.of("personId", id);
                int source = nodes.indexOf(contact_list);
                if (source == -1) {
                    nodes.add(contact_list);
                    source = i++;
                }
                rels.add(Map.of("source", source, "target", target));
            }
        }
        return Map.of("nodes", nodes, "links", rels);
    }

    protected List<Map<String, Object>> query(String query, Map<String, Object> params) {
        try (Session session = getSession()) {
           // System.out.println(params);
            return session.readTransaction(
                    tx -> tx.run(query, params).list( r -> r.asMap(DatabaseService::convert))
            );
        }
    }

    private Session getSession() {
        if (database == null || database.isBlank()) return driver.session();
        return driver.session(SessionConfig.forDatabase(database));
    }

    private static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(DatabaseService::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }
        return value.asObject();
    }
}
