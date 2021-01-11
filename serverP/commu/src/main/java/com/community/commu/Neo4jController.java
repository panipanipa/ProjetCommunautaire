package com.community.commu;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RestController
public class Neo4jController {

    @GetMapping("/neo4j/graph")
    public int getGraph() {
        System.out.println("OK") ;
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "http://192.168.37.54:50005/graph";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl , String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        return 200 ;
    }

}
