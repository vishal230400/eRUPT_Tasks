package org.example;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.schema.JanusGraphManagement;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.janusgraph.core.Cardinality;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SampleGraphApp {
    public static void main(String[] args) throws CsvValidationException {
        JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open();
        initializeSchema(graph);
        loadGraphData(graph, "./task2/src/resources/air-routes-latest-nodes.txt", "./task2/src/resources/air-routes-latest-edges.txt");
        verifyGraphData(graph);
        List<Vertex> airportsInUS = graph.traversal().V().hasLabel("airport").has("country", "US").toList();
        System.out.println("Airports in the US :");
        for (Vertex airport : airportsInUS) {
            System.out.println("Airport ID: " + airport.property("identity").value() +", Name: " + airport.property("desc").value() +", ICAO: " + airport.property("icao").value());
        }
        graph.close();
    }

    private static void verifyGraphData(JanusGraph graph) {
        GraphTraversalSource g = graph.traversal();
    
        long vertexCount = g.V().count().next();
        long edgeCount = g.E().count().next();
    
        System.out.println("Vertex count: " + vertexCount);
        System.out.println("Edge count: " + edgeCount);
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

    private static void loadGraphData(JanusGraph graph, String nodesFile, String edgesFile) throws CsvValidationException {
        GraphTraversalSource g = graph.traversal();

        try (CSVReader reader = new CSVReader(new FileReader(nodesFile))) {
            String[] parts;
            reader.readNext();

            while ((parts = reader.readNext()) != null) {
                if (parts[1].equals("version") || parts[1].equals("~label")) {
                    continue;
                }

                if (parts.length != 16) {
                    System.out.println("Invalid row: " + String.join(",", parts));
                    continue;
                }

                String id = parts[0].strip();
                String type = parts[2];
                String code = parts[3];
                String icao = parts[4];
                String desc = parts[5];
                String region = parts[6];
                String country = parts[10];
                String city = parts[11];

                if (type.equals("airport")) {
                    int runways = parts[7].isEmpty() ? 0 : Integer.parseInt(parts[7]);
                    int longest = parts[8].isEmpty() ? 0 : Integer.parseInt(parts[8]);
                    int elev = parts[9].isEmpty() ? 0 : Integer.parseInt(parts[9]);
                    double lat = parts[12].isEmpty() ? 0.0 : Double.parseDouble(parts[12]);
                    double lon = parts[13].isEmpty() ? 0.0 : Double.parseDouble(parts[13]);

                    g.addV(type).property("code", code)
                            .property("identity", id)
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
                            .property("identity", id)
                            .property("desc", desc)
                            .property("country", country)
                            .property("city", city)
                            .next();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (CSVReader reader = new CSVReader(new FileReader(edgesFile))) {
            String[] parts;
            while ((parts = reader.readNext()) != null) {
                if (parts.length == 5) {
                    if (parts[1].equals("~from")) {
                        continue;
                    }
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
                        System.out.println("One or both vertices not found for edge: " + fromID + " -> " + toID);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
}
