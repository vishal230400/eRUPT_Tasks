package org.example;

import com.apple.foundationdb.*;
import com.apple.foundationdb.async.AsyncIterable;
import com.apple.foundationdb.tuple.Tuple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SingleVsMultiRanges {
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
                    System.out.println("Results found: "+list.size());
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
        SingleVsMultiRanges FDB = new SingleVsMultiRanges();
        String filename = "task1/results/SingleVsMultiRanges.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int experiment = 0; experiment < 10; experiment++) {
                long startSetTime = System.nanoTime();
                for (int i = 0; i < 10000; i++) {
                    FDB.setKey("key_" + i, "value_" + i);
                }
                long endSetTime = System.nanoTime();
                long durationSetTime = (endSetTime - startSetTime);
                writer.write("Experiment " + (experiment + 1) + " : Creating 10000 Keys time in ns: " + durationSetTime + "\n");
                String start0="key_0";
                String end0="key_1898";
                String start1="key_1899";
                String end1="key_2798";
                String start2="key_2799";
                String end2="key_3698";
                String start3="key_3699";
                String end3="key_4598";
                String start4="key_4599";
                String end4="key_5498";
                String start5="key_5499";
                String end5="key_6398";
                String start6="key_6399";
                String end6="key_7298";
                String start7="key_7299";
                String end7="key_8198";
                String start8="key_8199";
                String end8="key_9098";
                String start9="key_9099";
                String end9="key_9999";
                final int tempExp=experiment+1;
                for (StreamingMode mode : StreamingMode.values()) {
                    long startGetRangeTime = System.nanoTime();
                    CompletableFuture<List<KeyValue>> result0=FDB.getRange(Tuple.from(start0).pack(),Tuple.from(end0).pack(), mode);
                    CompletableFuture<List<KeyValue>> result1=FDB.getRange(Tuple.from(start1).pack(),Tuple.from(end1).pack(), mode);
                    CompletableFuture<List<KeyValue>> result2=FDB.getRange(Tuple.from(start2).pack(),Tuple.from(end2).pack(), mode);
                    CompletableFuture<List<KeyValue>> result3=FDB.getRange(Tuple.from(start3).pack(),Tuple.from(end3).pack(), mode);
                    CompletableFuture<List<KeyValue>> result4=FDB.getRange(Tuple.from(start4).pack(),Tuple.from(end4).pack(), mode);
                    CompletableFuture<List<KeyValue>> result5=FDB.getRange(Tuple.from(start5).pack(),Tuple.from(end5).pack(), mode);
                    CompletableFuture<List<KeyValue>> result6=FDB.getRange(Tuple.from(start6).pack(),Tuple.from(end6).pack(), mode);
                    CompletableFuture<List<KeyValue>> result7=FDB.getRange(Tuple.from(start7).pack(),Tuple.from(end7).pack(), mode);
                    CompletableFuture<List<KeyValue>> result8=FDB.getRange(Tuple.from(start8).pack(),Tuple.from(end8).pack(), mode);
                    CompletableFuture<List<KeyValue>> result9=FDB.getRange(Tuple.from(start9).pack(),Tuple.from(end9).pack(), mode);
                    CompletableFuture.allOf(result0,result1,result2,result3,result4,result5,result6,result7,result8,result9).join();
                    long endGetRangeTime = System.nanoTime();
                    long durationGetRangeTime = (endGetRangeTime - startGetRangeTime);
                    try {
                        writer.write("Experiment " + tempExp+ " : GetRange : " + mode + " : 10000 Keys time in ns: " + durationGetRangeTime + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FDB.deleteAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


