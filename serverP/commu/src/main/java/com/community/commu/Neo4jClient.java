package com.community.commu;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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

    public static void getLouvain(String name) {
        String url = baseURL + "community/labelPropagation/"+name ;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> map = new HashMap<>();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class) ;
        System.out.println(response) ;

    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "java2");
        map.put("nodetype", "Person");
        map.put("relation", "Send");
        map.put("directed", false);
        map.put("wasOriented", true);
       // createGraphIn(map);
        getLouvain("java2"); ;
    }

}
