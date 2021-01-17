package example.movies.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.servlet.SparkApplication;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


import static spark.Spark.get;
import static spark.Spark.post ;
import static spark.Spark.path;


public class DbRoutes implements SparkApplication {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final CommunityService service;

    private GraphCreate graphCreate ;

    public DbRoutes(CommunityService service) {
        this.service = service;
        this.graphCreate = new GraphCreate(service) ;
    }

    public void init() {

        post("/create", (request, response) -> {
            response.type("application/json");
            GraphIn graph = new Gson().fromJson(request.body(), GraphIn.class);
            service.create_graph(graph.getName(), graph.getNodetype(), graph.getRelation(), graph.isDirected(), graph.isWasOriented());
            return "OK" ;
        });

        post("/test", (request, response) -> {
            String location = "/home/denis/5A/"; // the directory location where files will be stored
            String answer= "OK";
            long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
            long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
            int fileSizeThreshold = 1024;       // the size threshold after which files will be written to disk

            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    location, maxFileSize, maxRequestSize, fileSizeThreshold);
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig",
                    multipartConfigElement);

            Collection<Part> parts = request.raw().getParts();
            /*
            for (Part part : parts) {
                System.out.println("Name: " + part.getName());
                System.out.println("Size: " + part.getSize());
                System.out.println("Filename: " + part.getSubmittedFileName());
            }
             */

            String fName = request.raw().getPart("file").getSubmittedFileName();
            System.out.println("File: " + fName);

            Part uploadedFile = request.raw().getPart("file");

            File dest = new File("/home/denis/5A/testUpload.txt");
            if(dest.createNewFile() ) {
                Path out = dest.toPath() ;
                //Path out = Paths.get("home/denis/5A/" + fName+ "Server");
                try (final InputStream in = uploadedFile.getInputStream()) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                    uploadedFile.delete();
                }
                //service.importCSV("/home/denis/5A/testUpload.txt", " ", true) ;
            }
            else
                answer = "NOK" ;
            return answer;
        }) ;

        //return a node in the graph and all his neighboors.
        get("/dest/:person", (req, res) -> gson.toJson(service.findDestinators(URLDecoder.decode(req.params("person"), StandardCharsets.UTF_8))));

       //search a node in the graph given his name. .
        get("/search", (req, res) -> gson.toJson(service.search(req.queryParams("q"))));

        //return the whole graph, if too bug, only return 100 first nodes. (+connexion)
        get("/graph", (req, res) -> {
            int limit = req.queryParams("limit") != null ? Integer.parseInt(req.queryParams("limit")) : 100;
            return gson.toJson(service.graph(limit));
        });

        //return nodes reachable from start
        get("/path/:start", (req,res) -> gson.toJson(service.findDestinators(URLDecoder.decode(req.params("start"), StandardCharsets.UTF_8))));

        //launche community detection algorithm
        path("/community", () -> {

            get("/louvain/:name", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                if(!service.graph_exists(name))
                    return "graph does not exists !" ;
                else
                    return gson.toJson(service.louvain(name, "stream", Arrays.asList("personId", "department"))) ;
            }) ;

            //launch labelPropagation one.
            get("/labelPropagation/:name", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                if(!service.graph_exists(name))
                    return "graph does not exists !" ;
                else
                    return gson.toJson(service.labelPropagation(name, "stream", Arrays.asList("personId", "department"))) ;
            }) ;

            get("/triangle/:name", (req,res)-> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                Object result ;
                if(service.graph_exists(name)) {
                     result = gson.toJson(service.triangle(name, "stream")) ;
                }
                else {
                    result = "Graph does not exists" ;
                }
                return result ;
            }) ;
        });

        //@deprecated
        //: Old method.
        //get("/community/labelPropagation/:name", (req,res) -> gson.toJson(service.labelPropagation((URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8)), "stream"))) ;
    }
}
