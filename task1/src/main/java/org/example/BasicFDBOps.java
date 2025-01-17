package org.example;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.apple.foundationdb.*;
import com.apple.foundationdb.async.AsyncIterable;
import com.apple.foundationdb.tuple.Tuple;

public class BasicFDBOps {
    protected FDB fdb = FDB.selectAPIVersion(620);
    
    public boolean setKey(Database db,String key, String value) {
        boolean return_value=false;
        return_value=db.run(tr -> {
            tr.set(Tuple.from(key.toLowerCase()).pack(), Tuple.from(value).pack());
            return true;
        });
        return return_value;
    }
    public boolean setKey(Database db,String[] key, String[] value) {
        boolean return_value=false;
        return_value=db.run(tr -> {
            for(int i=0;i<key.length;i++){
                tr.set(Tuple.from(key[i].toLowerCase()).pack(), Tuple.from(value[i]).pack());
            }
            return true;
        });
        return return_value;
    }
    public String getValue(Database db,String key) {
        String value = null;
        value = db.run(tr -> {
            byte[] result = tr.get(Tuple.from(key.toLowerCase()).pack()).join();
            return result != null ? Tuple.fromBytes(result).getString(0) : null;
        });
        return value;
    }

    public CompletableFuture<List<KeyValue>> getRange(Database db, String start, String end) {
        CompletableFuture<List<KeyValue>> futureResults = new CompletableFuture<>();
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
        return futureResults;
    }

    public CompletableFuture<List<KeyValue>> getRange(Database db,byte[] start, byte[] end, StreamingMode sm , int size ,boolean debug) {
        CompletableFuture<List<KeyValue>> futureResults = new CompletableFuture<>();
            Transaction tr = db.createTransaction();
            Range range = new Range(start,end);
            AsyncIterable<KeyValue> currResults = tr.getRange(range,Integer.MAX_VALUE,false,sm);
            currResults.asList().thenAccept(list -> {
                if(debug){
                    if (list.isEmpty()) {
                        System.out.println("No results found in the specified range.");
                    } else {
                        System.out.println("Results found:"+list.size());
                    }
                }
                if (list.size() != size) {
                    System.out.println("GetRangeSize mismatch (size: " + list.size() + "). Abort Experiment...");
                    futureResults.completeExceptionally(new RuntimeException("Error: Results size not equal to "+size+"."));
                } else {
                    futureResults.complete(list);
                }
                futureResults.complete(list);
                tr.close();
            }).exceptionally(e -> {
                futureResults.completeExceptionally(e);
                return null;
            });
        return futureResults;
    }

    public boolean clearKey(Database db,String key) {
        boolean return_value=false;
        return_value=db.run(tr -> {
                tr.clear(Tuple.from(key.toLowerCase()).pack());
                return true;
            });
        return return_value;
    } 

    public boolean clearRange(Database db,String start, String end) {
        boolean return_value=false;
        return_value=db.run(tr -> {
            Range range = new Range(Tuple.from(start.toLowerCase()).pack(), Tuple.from(end.toLowerCase()).pack());
            tr.clear(range);
            return true;
        });
        return return_value;
    }

    public boolean deleteAll(Database db) {
        boolean return_value=false;
        return_value=db.run(tr -> {
                tr.clear( new byte[]{ 0x00} , new byte[]{ (byte) 0xFF});
                return true;
            });
        return return_value;
    }
    public static void main(String[] args) {
        BasicFDBOps FDB = new BasicFDBOps();
        try (Database db = FDB.fdb.open()) {
            FDB.setKey(db,"apple", "fruit");
            FDB.setKey(db,"banana", "fruit");
            FDB.setKey(db,"cherry", "fruit");
            FDB.setKey(db,"grape", "fruit");
            FDB.setKey(db,"orange", "fruit");
            FDB.setKey(db,"pear", "fruit");
            FDB.setKey(db,"strawberry", "fruit");
            System.out.println(FDB.getValue(db,"strawberry"));
            System.out.println(FDB.getValue(db,"tomato"));
            FDB.getRange(db,"banana", "pear").thenAccept(results -> {
                for (KeyValue kv : results) {
                    System.out.println("Key: " + new String(kv.getKey()) + ", Value: " + new String(kv.getValue()));
                }
            }).exceptionally(e -> {
                System.err.println("Error fetching range: " + e.getMessage());
                return null;
            }).join();
            System.out.println(FDB.getValue(db,"grape"));
            System.out.println(FDB.getValue(db,"key_5000"));
            FDB.deleteAll(db);
            System.out.println(FDB.getValue(db,"grape"));
        }
    }
}