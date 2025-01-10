package org.example;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.Cardinality;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SampleGraphAppBerkley {
    public static void main(String[] args) {
        JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje").set("storage.directory", "data/graph").open();
        initializeSchema(graph);
        long startSetTime = System.nanoTime();
        loadGraphData(graph, "./task2/src/resources/air-routes-latest-nodes.txt", "./task2/src/resources/air-routes-latest-edges.txt");
        long endSetTime = System.nanoTime();
        long durationSetTime = (endSetTime - startSetTime);
        System.out.println("Time taken to load to berkley db in ns is: "+durationSetTime);
        graph.close();
    }

    private static void initializeSchema(JanusGraph graph) {
        JanusGraphManagement mgmt = graph.openManagement();
        
        if (!mgmt.containsVertexLabel("airport")) {
            mgmt.makeVertexLabel("airport").make();
        }
        if (!mgmt.containsVertexLabel("country")) {
            mgmt.makeVertexLabel("country").make();
        }
        if (!mgmt.containsVertexLabel("continent")) {
            mgmt.makeVertexLabel("continent").make();
        }

        if (!mgmt.containsEdgeLabel("route")) {
            mgmt.makeEdgeLabel("route").multiplicity(Multiplicity.MULTI).make();
        }
        if (!mgmt.containsEdgeLabel("contains")) {
            mgmt.makeEdgeLabel("contains").multiplicity(Multiplicity.MULTI).make();
        }

        if (!mgmt.containsPropertyKey("city")) {
            mgmt.makePropertyKey("city").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("lat")) {
            mgmt.makePropertyKey("lat").dataType(Double.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("lon")) {
            mgmt.makePropertyKey("lon").dataType(Double.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("dist")) {
            mgmt.makePropertyKey("dist").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("identity")) {
            mgmt.makePropertyKey("identity").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("type")) {
            mgmt.makePropertyKey("type").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("code")) {
            mgmt.makePropertyKey("code").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("icao")) {
            mgmt.makePropertyKey("icao").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("desc")) {
            mgmt.makePropertyKey("desc").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("region")) {
            mgmt.makePropertyKey("region").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("runways")) {
            mgmt.makePropertyKey("runways").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("longest")) {
            mgmt.makePropertyKey("longest").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("elev")) {
            mgmt.makePropertyKey("elev").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        }
        if (!mgmt.containsPropertyKey("country")) {
            mgmt.makePropertyKey("country").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        }
        if (mgmt.getGraphIndex("Idx_comidx_Vertex_identity_unique") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_identity_unique", Vertex.class)
                .addKey(mgmt.getPropertyKey("identity"))
                .unique()
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Vertex_type_airport") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_type_airport", Vertex.class)
                .addKey(mgmt.getPropertyKey("type"))
                .indexOnly(mgmt.getVertexLabel("airport"))
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Vertex_code") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_code", Vertex.class)
                .addKey(mgmt.getPropertyKey("code"))
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Vertex_icao") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_icao", Vertex.class)
                .addKey(mgmt.getPropertyKey("icao"))
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Vertex_country") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_country", Vertex.class)
                .addKey(mgmt.getPropertyKey("country"))
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Vertex_city") == null) {
            mgmt.buildIndex("Idx_comidx_Vertex_city", Vertex.class)
                .addKey(mgmt.getPropertyKey("city"))
                .buildCompositeIndex();
        }

        if (mgmt.getGraphIndex("Idx_comidx_Edge_identity") == null) {
            mgmt.buildIndex("Idx_comidx_Edge_identity", Edge.class)
                .addKey(mgmt.getPropertyKey("identity"))
                .buildCompositeIndex();
        }
        
        mgmt.commit();
    }

    private static void loadGraphData(JanusGraph graph, String nodesFile, String edgesFile) {
        GraphTraversalSource g = graph.traversal();
    
        try (BufferedReader br = new BufferedReader(new FileReader(nodesFile))) {
            String line;
            br.readLine(); 
    
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",",-1);
                
                if (parts[1].equals("version") || parts[1].equals("~label")) {
                    continue;
                }
                if (parts.length == 16) {
                    String id = parts[0].strip();
                    String type = parts[2];
                    String code = parts[3];
                    String icao = parts[4];
                    String desc = parts[5];
                    String region = parts[6];
                    String country = parts[10];
                    String city = parts[11];
                    if (type.equals("airport")) {
                        int runways = Integer.parseInt(parts[7]);
                        int longest = Integer.parseInt(parts[8]);
                        int elev = Integer.parseInt(parts[9]);
                        double lat = Double.parseDouble(parts[12]);
                        double lon = Double.parseDouble(parts[13]);
                        g.addV(type).property("code", code)
                                .property("identity",id)
                                .property("icao", icao)
                                .property("desc", desc)
                                .property("region", region)
                                .property("runways", runways)
                                .property("longest", longest)
                                .property("elev", elev)
                                .property("country", country)
                                .property("city", city)
                                .property("lat", lat)
                                .property("lon", lon)
                                .next();
                    } else if (type.equals("country") || type.equals("continent")) {
                        g.addV(type).property("code", code)
                                .property("identity",id)
                                .property("desc", desc)
                                .property("country", country)
                                .property("city", city)
                                .next();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        try (BufferedReader br = new BufferedReader(new FileReader(edgesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length == 5) {
                    String fromID = parts[1].strip();
                    String toID = parts[2].strip();
                    String edgeLabel = parts[3];
                    String dist = parts[4];
                    
                    GraphTraversal<Vertex, Vertex> fromTraversal = g.V().has("identity", fromID);
                    GraphTraversal<Vertex, Vertex> toTraversal = g.V().has("identity", toID);
                    if (fromTraversal.hasNext() && toTraversal.hasNext()) {
                        Vertex from = fromTraversal.next();
                        Vertex to = toTraversal.next();
                        
                        g.V(from)
                            .addE(edgeLabel)
                            .property("dist", dist)
                            .to(to)
                            .iterate();
                    } else {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long vertexCount = g.V().count().next();
        long edgeCount = g.E().count().next();
        
        System.out.println("Total number of vertices: " + vertexCount);
        System.out.println("Total number of edges: " + edgeCount);
    }    
}
