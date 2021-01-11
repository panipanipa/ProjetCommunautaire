package example.movies.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static spark.Spark.get;
import static spark.Spark.path;

public class DbRoutes implements SparkApplication {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final CommunityService service;

    public DbRoutes(CommunityService service) {
        this.service = service;
    }

    public void init() {

        get("/dest/:person", (req, res) -> gson.toJson(service.findDestinators(URLDecoder.decode(req.params("person"), StandardCharsets.UTF_8))));
        get("/search", (req, res) -> gson.toJson(service.search(req.queryParams("q"))));
        get("/graph", (req, res) -> {
            int limit = req.queryParams("limit") != null ? Integer.parseInt(req.queryParams("limit")) : 100;
            return gson.toJson(service.graph(limit));
        });
        get("/path/:start", (req,res) -> gson.toJson(service.findDestinators(URLDecoder.decode(req.params("start"), StandardCharsets.UTF_8))));
        path("/community", () -> {
            get("/louvain/:name", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                if(!service.graph_exists(name))
                    service.create_graph(name, "Person", "Send", false);
                return gson.toJson(service.Louvain(name, "stream")) ;
            }) ;

            get("/louvain/:name/:directed", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                Boolean directed = Boolean.parseBoolean(req.params("directed")) ;
                if(!service.graph_exists(name))
                    service.create_graph(name, "Person", "Send", directed);
                return gson.toJson(service.Louvain(name, "stream")) ;
            }) ;

            get("/labelPropagation/:name", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                if(!service.graph_exists(name))
                    service.create_graph(name, "Person", "Send", false);
                return gson.toJson(service.labelPropagation(name, "stream")) ;
            }) ;

            get("/labelPropagation/:name/:directed", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                Boolean directed = Boolean.parseBoolean(req.params("directed")) ;
                if(!service.graph_exists(name))
                    service.create_graph(name, "Person", "Send", directed);
                return gson.toJson(service.labelPropagation(name, "stream")) ;
            }) ;
        });

        get("/community/labelPropagation/:name", (req,res) -> gson.toJson(service.labelPropagation((URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8)), "stream"))) ;
    }
}
