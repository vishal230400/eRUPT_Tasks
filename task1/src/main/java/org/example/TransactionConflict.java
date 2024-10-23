package org.example;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.tuple.Tuple;

public class TransactionConflict {

    public static void main(String[] args) throws InterruptedException {

        FDB fdb = FDB.selectAPIVersion(620);

        Database db = fdb.open();

        try (Transaction tx = db.createTransaction()) {
            tx.set(Tuple.from("K1").pack(), Tuple.from("Value1").pack());
            tx.set(Tuple.from("K2").pack(), Tuple.from("Value2").pack());
            tx.commit().join();
        }

        Thread transactionT1 = new Thread(() -> {
            try (Transaction tx1 = db.createTransaction()) {
                byte[] valueK1 = tx1.get(Tuple.from("K1").pack()).join();
                System.out.println("T1 reads K1: " + Tuple.fromBytes(valueK1).getString(0));

                tx1.set(Tuple.from("K2").pack(), Tuple.from("UpdatedByT1").pack());

                Thread.sleep(3000);

                tx1.commit().join();
                System.out.println("T1 committed.");
            } catch (Exception e) {
                System.out.println("T1 aborted: " + e.getMessage());
            }
        });

        Thread transactionT2 = new Thread(() -> {
            try (Transaction tx2 = db.createTransaction()) {

                byte[] valueK2 = tx2.get(Tuple.from("K2").pack()).join();
                System.out.println("T2 reads K2: " + Tuple.fromBytes(valueK2).getString(0));

                tx2.set(Tuple.from("K1").pack(), Tuple.from("UpdatedByT2").pack());

                Thread.sleep(1000);

                tx2.commit().join();
                System.out.println("T2 committed.");
            } catch (Exception e) {
                System.out.println("T2 aborted: " + e.getMessage());
            }
        });

        transactionT1.start();
        transactionT2.start();

        transactionT1.join();
        transactionT2.join();
    }
}