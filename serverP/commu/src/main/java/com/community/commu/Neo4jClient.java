package com.community.commu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jClient {

    private static final String baseURL = "http://localhost:8080/" ;

    private static HttpEntity<Map<String, Object>> request(Map<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return  new HttpEntity<>(map, headers);
    }

    public static void createGraphIn(Map<String, Object> map ) {
        String url =  baseURL + "create" ;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity( url, entity , String.class );
        System.out.println(response) ;
    }

    public static void createProperty(Map<String, Object> map ) {
        String url =  baseURL + "/detectComu" ;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange( url, HttpMethod.PUT, entity , String.class);
        System.out.println(response) ;
    }

    public static String getLouvain(String name, String fields) {
        String url = baseURL + "community/louvain/"+name+"/"+fields ;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> map = new HashMap<>();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class) ;
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        else {
            return response.getStatusCode().toString() ;
        }

    }

    public static String getLabelP(String name) {
        String url = baseURL + "community/labelPropagation/"+name ;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> map = new HashMap<>();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class) ;
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        else {
            return response.getStatusCode().toString() ;
        }
    }

    public static String getNodeProperties(String name, String fields, String properties) {
        String url = baseURL + "properties/"+name+"/"+fields+"/"+properties ;
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class) ;
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        else {
            return response.getStatusCode().toString() ;
        }
    }

    public static String getTriangle(String name) {
        String url = baseURL + "community/triangle/" + name;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> map = new HashMap<>();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        } else {
            return response.getStatusCode().toString();
        }
    }

    //Must have department on stream
    public static String getAnalyseStream(String res) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type type = new TypeToken<List<Map<String, Object>> >(){}.getType();
        List<Map<String, Object>> tab = gson.fromJson(res, type);

        StringBuilder result = new StringBuilder("Louvain Analysis \n") ;

        for ( Map<String, Object> one:tab)
        {
            Object community = one.get("communityId");
            result.append("Community : ").append(community).append("\n") ;
            HashMap<Object, Double> presence = new HashMap<>() ;
            int total = 0 ;
            for (Object id : (Collection<?>) one.get("f0")) {
                presence.merge(id, 1.0, Double::sum) ;
                total++ ;
            }
            if(total>1) {
                for(Map.Entry<Object, Double> id:presence.entrySet()) {
                    Double taux = id.getValue() / total * 100;
                    if(taux>10.0)
                        result.append(id.getKey()).append(" : ").append(taux).append("% | ") ;
                }
            }
        }
        return result.toString() ;
    }


    //Analyse do on getNodeProperties
    //Complicated because I didn't figure out how to make a group by
    //so for 2 properties , i got 2 lines
    public static String getAnalyseNP(String body) {

        StringBuilder result = new StringBuilder("Analyse Nodes Properties");
        //Cast
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> res = gson.fromJson(body, type);


        //get node properties
        //List<Map<String, Object>> res3 = service.getNodeProperty("email_undirected", Arrays.asList("name", "department") ,Arrays.asList("communityId", "triangle")) ;
        HashMap<Object, HashMap<String, Object>> stat = new HashMap<>();
        Object key = null;
        for (Map<String, Object> one : res) {
            Object what = one.get("what");
            if (what.equals("communityId")) {
                key = one.get("val");
                Object commu = one.get("department");
                if (!stat.containsKey(key)) {
                    stat.put(key, new HashMap<>());
                    stat.get(key).put("presence", new HashMap<>());
                }
                HashMap tab = (HashMap) stat.get(key).get("presence");
                tab.merge(String.valueOf(commu), 1.0, (oldValue, newValue) -> (Double) oldValue + (Double) newValue);
            } else if (what.equals("triangles")) {
                stat.get(key).merge("triangles", (Double) one.get("val"), (oldValue, newValue) -> (Double)oldValue + (Double) newValue);
                stat.get(key).merge("size", 1.0, (oldValue, newValue) -> (Double) oldValue + (Double) newValue);
            }
        }


        //Affichage des résultats
        for (Map.Entry<Object, HashMap<String, Object>> entry : stat.entrySet()) {
            Object commu = entry.getKey();
            HashMap values = entry.getValue();
            Double size = (Double) values.get("size");
            Double triangle = (Double) values.get("triangles");
            HashMap<Object, Double> presence = (HashMap<Object, Double>) values.get("presence");

            double max = 0.0;
            String max_label = "";
            for (Map.Entry<Object, Double> sol : presence.entrySet()) {
                double taux = sol.getValue();
                taux = taux / size * 100;
                if (taux > max) {
                    max = taux;
                    max_label = sol.getKey().toString();
                }
            }
            if (size > 1.0) {
                result.append(commu).append(" size : ").append(size).append(" & avg_triangle : ").append((triangle / size)).append("\n");
                result.append("taux de presence max pour ").append(max_label).append(" : ").append(max).append("\n");
            }
        }
        return result.toString();
    }


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "java");
        map.put("nodetype", "Test");
        map.put("relation", "Send");
        map.put("directed", false);
        map.put("wasOriented", true);

        Map<String, Object> put = new HashMap<>();
        put.put("algo", "louvain") ;
        put.put("property", "communityId") ;
        put.put("name", "java") ;
        //createProperty(put);
        put.replace("algo", "triangle");
        put.replace("property", "triangles");
        //createProperty(put);
        String res = getNodeProperties("java", "[department]", "[communityId, triangles]");
        //createGraphIn(map) ;
        //String res = getLouvain("java", "[department]") ;

        System.out.print(getAnalyseNP(res));

        //System.out.println(res);
    }

}
