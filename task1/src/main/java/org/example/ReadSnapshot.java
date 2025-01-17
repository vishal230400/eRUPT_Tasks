package org.example;

import java.util.concurrent.Semaphore;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.tuple.Tuple;

public class ReadSnapshot {
    
    private static final Semaphore semaphoreT4 = new Semaphore(0);

    public static void main(String[] args) throws InterruptedException {
        FDB fdb = FDB.selectAPIVersion(620);
        Database db = fdb.open();

        try (Transaction tx = db.createTransaction()) {
            tx.set(Tuple.from("K1").pack(), Tuple.from("Value1").pack());
            tx.set(Tuple.from("K2").pack(), Tuple.from("Value2").pack());
            tx.set(Tuple.from("K3").pack(), Tuple.from("Value3").pack());
            tx.set(Tuple.from("K4").pack(), Tuple.from("Value4").pack());
            tx.commit().join();
        }

        Thread transactionT1 = new Thread(() -> {
            try (Transaction tx1 = db.createTransaction()) {
                byte[] valueK1 = tx1.get(Tuple.from("K1").pack()).join();
                byte[] valueK2 = tx1.get(Tuple.from("K2").pack()).join();
                semaphoreT4.release();
                byte[] valueK3 = tx1.get(Tuple.from("K3").pack()).join();
                System.out.println("T1 reads K1: " + Tuple.fromBytes(valueK1).getString(0));
                System.out.println("T1 reads K2: " + Tuple.fromBytes(valueK2).getString(0));
                System.out.println("T1 reads K3: " + Tuple.fromBytes(valueK3).getString(0));
                tx1.commit().join();
                System.out.println("T1 committed.");
            } catch (Exception e) {
                System.out.println("T1 aborted: " + e.getMessage());
            }
        });

        Thread transactionT4 = new Thread(() -> {
            try {
                semaphoreT4.acquire();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try (Transaction tx4 = db.createTransaction()) {

                tx4.set(Tuple.from("K2").pack(), Tuple.from("NewValue2").pack());
                tx4.set(Tuple.from("K4").pack(), Tuple.from("NewValue4").pack());
                tx4.commit().join();
                System.out.println("T4 committed.");
                semaphoreT4.release();
            } catch (Exception e) {
                System.out.println("T4 aborted: " + e.getMessage());
            }
        });

        transactionT1.start();
        transactionT4.start();

        transactionT1.join();
        transactionT4.join();
    }
}