package com.community.commu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RestController
public class Neo4jController {

    private static String  neo4j_server = "http://192.168.37.54:50005" ;

    @GetMapping("/neo4j/graph")
    public int getGraph() throws JsonProcessingException {
        System.out.println("OK") ;
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = neo4j_server + "/graph";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl , String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Objects.requireNonNull(response.getBody()));

        return 200 ;
    }

    @GetMapping("/neo4j/import")
    public int importGraphCSV(@RequestParam String file) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        String serverUrl = neo4j_server + "/test" ;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        return 200;
    }

}
