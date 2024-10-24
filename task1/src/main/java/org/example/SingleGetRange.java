package org.example;

import com.apple.foundationdb.*;
import com.apple.foundationdb.async.AsyncIterable;
import com.apple.foundationdb.tuple.Tuple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class SingleGetRange {
    private FDB fdb = FDB.selectAPIVersion(620);

    private boolean setKey(String key, String value){
        try(Database db=fdb.open()){
            db.run(tr->{
                tr.set(Tuple.from(key.toLowerCase()).pack(),Tuple.from(value).pack());
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

    public CompletableFuture<List<KeyValue>> getRange(byte[] start, byte[] end, StreamingMode sm ) {
        CompletableFuture<List<KeyValue>> futureResults = new CompletableFuture<>();
    
        try (Database db = fdb.open()) {
            Transaction tr = db.createTransaction();
            Range range = new Range(start,end);
            AsyncIterable<KeyValue> currResults = tr.getRange(range,Integer.MAX_VALUE,false,sm);
            currResults.asList().thenAccept(list -> {
                if (list.isEmpty()) {
                    System.out.println("No results found in the specified range.");
                } else {
                    System.out.println("Results found:"+list.size());
                }
                futureResults.complete(list);
                tr.close();
            }).exceptionally(e -> {
                futureResults.completeExceptionally(e);
                return null;
            });
        }
        return futureResults;
    }

    public boolean deleteAll() {
        try (Database db = fdb.open()) {
            db.run(tr -> {
                tr.clear( new byte[]{ 0x00} , new byte[]{ (byte) 0xFF});
                return true;
            });
        }
        return false;
    }

    public static void main(String[] args) {
        SingleGetRange FDB = new SingleGetRange();
        String filename = "task1/results/SingleGetRange.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int experiment = 0; experiment < 10; experiment++) {
                long startSetTime = System.nanoTime();
                for (int i = 0; i < 10000; i++) {
                    FDB.setKey("key_" + i, "value_" + i);
                }
                long endSetTime = System.nanoTime();
                long durationSetTime = (endSetTime - startSetTime);
                writer.write("Experiment " + (experiment + 1) + " : Creating 10000 Keys time in ns: " + durationSetTime + "\n");
                final int tempExp=experiment+1;
                for (StreamingMode mode : StreamingMode.values()) {
                    long startGetRangeTime = System.nanoTime();
                    FDB.getRange(new byte[]{0x00}, new byte[]{(byte) 0xFF}, mode).thenAccept(results -> {
                        long endGetRangeTime = System.nanoTime();
                        long durationGetRangeTime = (endGetRangeTime - startGetRangeTime);
                        try {
                            writer.write("Experiment " + tempExp+ " : GetRange : " + mode + " : 10000 Keys time in ns: " + durationGetRangeTime + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).exceptionally(e -> {
                        System.err.println("Error fetching range: " + e.getMessage());
                        return null;
                    }).join();
                }
                FDB.deleteAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
