package example.movies.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import spark.servlet.SparkApplication;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static spark.Spark.*;


public class DbRoutes implements SparkApplication {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final CommunityService service;

    public DbRoutes(CommunityService service) {
        this.service = service;
    }

    public void init() {

        //create graph in-memory
        post("/create", (request, response) -> {
            response.type("application/json");
            GraphIn graph = new Gson().fromJson(request.body(), GraphIn.class);
            service.create_graph(graph.getName(), graph.getNodetype(), graph.getRelation(), graph.isDirected(), graph.isWasOriented());
            return "OK" ;
        });

        put("/detectComu", (request, response) -> {
            Map<String, Object> json = new Gson().fromJson(request.body(), new TypeToken<Map<String, Object>>() {}.getType());
            String algo = json.get("algo").toString() ;
            String name = json.get("name").toString() ;
            String prop = json.get("property").toString() ;
            List<Map<String, Object>> answer = null;
            switch(algo) {
                case "louvain" :
                     answer = service.louvain(name, "mutate", Collections.singletonList(prop));
                     break;
                case "label" :
                    answer = service.labelPropagation(name, "mutate", Collections.singletonList(prop));
                    break;
                case "triangle" :
                    answer = service.triangle(name, "mutate", Collections.singletonList(prop));
                    break;
                case "clustering" :
                    answer = service.localClusteringCoef(name, "mutate", Collections.singletonList(prop));
                    break;
            }
           return gson.toJson(answer) ;
        });

        get("/properties/:name/:fields/:properties", (req, res) -> {
            String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
            String json = URLDecoder.decode(req.params("fields"),  StandardCharsets.UTF_8) ;
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> fields = gson.fromJson(json, type);
            String json2 = URLDecoder.decode(req.params("properties"),  StandardCharsets.UTF_8) ;
            Type type2 = new TypeToken<List<String>>(){}.getType();
            List<String> properties = gson.fromJson(json2, type2);
            System.out.println(name);
            if(!service.graph_exists(name))
                return "graph does not exists !" ;
            else
                return gson.toJson(service.getNodeProperty(name, fields,properties)) ;
        }
                );

        post("/test", (request, response) -> {
            //String location = "/home/denis/5A/"; // the directory location where files will be stored
            String location = "/home/user";
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

            File dest = new File("/home/user/testUpload.txt");
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

        //Test Jenkins commit
        get("/testJenkins", (req, res) -> {
            return "Work";
        });

        //return the whole graph, if too bug, only return 100 first nodes. (+connexion)
        get("/graph", (req, res) -> {
            int limit = req.queryParams("limit") != null ? Integer.parseInt(req.queryParams("limit")) : 100;
            return gson.toJson(service.graph(limit));
        });

        //return nodes reachable from start
        get("/path/:start", (req,res) -> gson.toJson(service.findDestinators(URLDecoder.decode(req.params("start"), StandardCharsets.UTF_8))));

        //launche community detection algorithm
        path("/community", () -> {

            get("/louvain/:name/:fields", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                String json = URLDecoder.decode(req.params("fields"),  StandardCharsets.UTF_8) ;
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> fields = gson.fromJson(json, type);
                System.out.println(name);
                System.out.println(json);
                if(!service.graph_exists(name))
                    return "graph does not exists !" ;
                else
                    return gson.toJson(service.louvain(name, "stream", fields));
            }) ;

            //launch labelPropagation one.
            get("/labelPropagation/:name/:fields", (req,res) -> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                String json = URLDecoder.decode(req.params("fields"),  StandardCharsets.UTF_8) ;
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> fields = gson.fromJson(json, type);
                System.out.println(name);
                if(!service.graph_exists(name))
                    return "graph does not exists !" ;
                else
                    return gson.toJson(service.labelPropagation(name, "stream", fields)) ;
            }) ;

            get("/triangle/:name", (req,res)-> {
                String name = URLDecoder.decode(req.params("name"),  StandardCharsets.UTF_8) ;
                Object result ;
                if(service.graph_exists(name)) {
                     result = gson.toJson(service.triangle(name, "stream", Collections.emptyList())) ;
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
