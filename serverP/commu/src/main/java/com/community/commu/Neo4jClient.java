package com.community.commu;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestTemplate;

public class Neo4jClient {

    public static void createGraphIn() {
        String url = "http://localhos:8080/create" ;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("name", "java");
        map.add("nodetype", "Test");
        map.add("relation", "Send");
        map.add("directed", "false");
        map.add("wasOriented", "false");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
        System.out.println(response) ;
    }

    public static void main(String[] args) {
        createGraphIn() ;
    }

}
