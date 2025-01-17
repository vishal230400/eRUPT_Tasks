package org.example;

import com.apple.foundationdb.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class SingleGetRange extends BasicFDBOps{
    
    public static void main(String[] args) {
        String[] keys =new String[10000];
        String[] values= new String[10000];
        DecimalFormat keyFormatter = new DecimalFormat("0000");
        DecimalFormat valueFormatter = new DecimalFormat("0000");
        for(int i=0;i<10000;i++){
            keys[i] = String.format("key_%s", keyFormatter.format(i));
            values[i] = String.format("value_%s", valueFormatter.format(i));
        }
        SingleGetRange FDB = new SingleGetRange();
        String filename = "task1/results/SingleGetRange.txt";
        try (Database db = FDB.fdb.open()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (int experiment = 0; experiment < 50; experiment++) {
                    long startSetTime = System.currentTimeMillis();
                    // for (int i = 0; i < 10000; i++) {
                    //     FDB.setKey(db,keys[i], values[i]);
                    // }
                    FDB.setKey(db, keys, values);
                    long endSetTime = System.currentTimeMillis();
                    long durationSetTime = (endSetTime - startSetTime);
                    writer.write("Experiment " + (experiment + 1) + " : Creating 10000 Keys time in ms: " + durationSetTime + "\n");
                    final int tempExp=experiment+1;
                    FDB.getRange(db,new byte[]{0x00}, new byte[]{(byte) 0xFF}, StreamingMode.SERIAL, 10000,false).thenAccept(results -> {
                        long keySize=0;
                        long valueSize=0;
                        for (KeyValue kv: results){
                            keySize+=kv.getKey().length;
                            valueSize+=kv.getValue().length;
                        }
                        try {
                            writer.write("Experiment " + tempExp + " : KeySize in Bytes: " + keySize + "\n");
                            writer.write("Experiment " + tempExp + " : ValueSize in Bytes: " + valueSize + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).exceptionally(e -> {
                        System.err.println("Error fetching range: " + e.getMessage());
                        return null;
                    }).join();
                    for (StreamingMode mode : StreamingMode.values()) {
                        long startGetRangeTime = System.currentTimeMillis();
                        FDB.getRange(db,new byte[]{0x00}, new byte[]{(byte) 0xFF}, mode,10000, false).thenAccept(results -> {
                            long endGetRangeTime = System.currentTimeMillis();
                            long durationGetRangeTime = (endGetRangeTime - startGetRangeTime);
                            try {
                                writer.write("Experiment " + tempExp+ " : GetRange : " + mode + " : 10000 Keys time in ms: " + durationGetRangeTime + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).exceptionally(e -> {
                            System.err.println("Error fetching range: " + e.getMessage());
                            return null;
                        }).join();
                    }
                    FDB.deleteAll(db);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
