package example.movies.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static spark.Spark.get;

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
    }
}
