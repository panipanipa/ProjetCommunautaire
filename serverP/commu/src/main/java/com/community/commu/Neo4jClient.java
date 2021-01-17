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

    public static void createGraphIn() {
        String url = "http://localhost:8080/create" ;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "java");
        map.put("nodetype", "Test");
        map.put("relation", "Send");
        map.put("directed", false);
        map.put("wasOriented", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity( url, entity , String.class );
        System.out.println(response) ;
    }

    public static void main(String[] args) {
        createGraphIn() ;
    }

}
