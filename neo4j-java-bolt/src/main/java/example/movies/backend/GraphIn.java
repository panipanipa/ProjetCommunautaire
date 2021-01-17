package example.movies.backend;

import java.util.List;

public class GraphIn {

    private String name ;
    private boolean directed ;
    private boolean wasOriented ;
    private String nodetype ;
    private String relation ;

    public GraphIn(String name, boolean directed, boolean wasOriented, String nodetype, String relation) {
        this.name = name;
        this.directed = directed;
        this.wasOriented = wasOriented;
        this.nodetype = nodetype;
        this.relation = relation;
    }

    public String getNodetype() {
        return nodetype;
    }

    public void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public boolean isWasOriented() {
        return wasOriented;
    }

    public void setWasOriented(boolean wasOriented) {
        this.wasOriented = wasOriented;
    }

}
