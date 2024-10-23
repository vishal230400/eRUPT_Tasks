package org.example;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.apple.foundationdb.*;
import com.apple.foundationdb.async.AsyncIterable;
import com.apple.foundationdb.tuple.Tuple;

public class BasicFDBOps {
    private FDB fdb = FDB.selectAPIVersion(620);
    
    public boolean setKey(String key, String value) {
        try (Database db = fdb.open()) {
            db.run(tr -> {
                tr.set(Tuple.from(key.toLowerCase()).pack(), Tuple.from(value).pack());
                return true;
            });
        }
        return false;
    }

    public String getValue(String key) {
        String value = null;
        try (Database db = fdb.open()) {
            value = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(key.toLowerCase()).pack()).join();
                return result != null ? Tuple.fromBytes(result).getString(0) : null;
            });
        }
        return value;
    }

    public CompletableFuture<List<KeyValue>> getRange(String start, String end) {
        CompletableFuture<List<KeyValue>> futureResults = new CompletableFuture<>();

        try (Database db = fdb.open()) {
            db.run(tr -> {
                Range range = new Range(Tuple.from(start.toLowerCase()).pack(), Tuple.from(end.toLowerCase()).pack());
                AsyncIterable<KeyValue> currResults = tr.getRange(range);
                
                currResults.asList().thenAccept(list -> {
                    if (list.isEmpty()) {
                        System.out.println("No results found in the specified range.");
                    } else {
                        System.out.println("Results found in the specified range:");
                    }
                    futureResults.complete(list);
                }).exceptionally(e -> {
                    futureResults.completeExceptionally(e);
                    return null;
                });
                return null;
            });
        }
        return futureResults;
    }

    public boolean clearKey(String key) {
        try (Database db = fdb.open()) {
            db.run(tr -> {
                tr.clear(Tuple.from(key.toLowerCase()).pack());
                return true;
            });
        }
        return false;
    } 

    public boolean clearRange(String start, String end) {
        try (Database db = fdb.open()) {
            db.run(tr -> {
                Range range = new Range(Tuple.from(start.toLowerCase()).pack(), Tuple.from(end.toLowerCase()).pack());
                tr.clear(range);
                return true;
            });
        }
        return false;
    }

    public static void main(String[] args) {
        BasicFDBOps FDB = new BasicFDBOps();
        FDB.setKey("apple", "fruit");
        FDB.setKey("banana", "fruit");
        FDB.setKey("cherry", "fruit");
        FDB.setKey("grape", "fruit");
        FDB.setKey("orange", "fruit");
        FDB.setKey("pear", "fruit");
        FDB.setKey("strawberry", "fruit");
        System.out.println(FDB.getValue("strawberry"));
        System.out.println(FDB.getValue("tomato"));
        FDB.getRange("banana", "pear").thenAccept(results -> {
            for (KeyValue kv : results) {
                System.out.println("Key: " + new String(kv.getKey()) + ", Value: " + new String(kv.getValue()));
            }
        }).exceptionally(e -> {
            System.err.println("Error fetching range: " + e.getMessage());
            return null;
        }).join();
        System.out.println(FDB.getValue("grape"));
        System.out.println(FDB.getValue("key_5000"));
        FDB.clearRange("a", "z");
        System.out.println(FDB.getValue("grape"));
    }
}