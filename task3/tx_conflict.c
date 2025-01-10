#define FDB_API_VERSION 630
#include <foundationdb/fdb_c.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>

void check_fdb_error(fdb_error_t err) {
    if (err) {
        fprintf(stderr, "FDB error: %s\n", fdb_get_error(err));
        exit(EXIT_FAILURE);
    }
}

void set_key(FDBTransaction* tr, const char* key, const char* value) {
    fdb_transaction_set(tr, (const uint8_t*)key, strlen(key), (const uint8_t*)value, strlen(value));
}

char* get_value(FDBTransaction* tr, const char* key) {
    FDBFuture* f = fdb_transaction_get(tr, (const uint8_t*)key, strlen(key), 0);
    fdb_error_t err = fdb_future_block_until_ready(f);
    check_fdb_error(err);

    const uint8_t* value;
    int valuelen;
    int present;
    err = fdb_future_get_value(f, &present, &value, &valuelen);
    fdb_future_destroy(f);
    check_fdb_error(err);

    if (present) {
        char* result = (char*)malloc(valuelen + 1);
        memcpy(result, value, valuelen);
        result[valuelen] = '\0';
        return result;
    } else {
        return NULL;
    }
}

void get_range(FDBTransaction* tr, const char* begin_key, const char* end_key, int limit, FDBStreamingMode mode) {
    const uint8_t* begin_key_name = (const uint8_t*)begin_key;
    const uint8_t* end_key_name = (const uint8_t*)end_key;
    int begin_key_name_length = strlen(begin_key);
    int end_key_name_length = strlen(end_key);
    int begin_offset = 0;
    int end_offset = 0;
    int target_bytes = 0;
    int iteration = 1;
    fdb_bool_t snapshot = 0;
    fdb_bool_t reverse = 0;
    FDBFuture* future = fdb_transaction_get_range(tr,begin_key_name, begin_key_name_length,1,begin_offset,end_key_name, 
                                                end_key_name_length,1,end_offset,limit,target_bytes,mode,iteration,
                                                snapshot,reverse);

    fdb_error_t err = fdb_future_block_until_ready(future);
    check_fdb_error(err);

    const FDBKeyValue* keyvalues;
    int count;
    int more;
    err = fdb_future_get_keyvalue_array(future, &keyvalues, &count, &more);
    fdb_future_destroy(future);
    check_fdb_error(err);

    // printf("Found %d key-value pairs in the range:\n", count);
    // for (int i = 0; i < count; ++i) {
    //     printf("Key: %.*s, Value: %.*s\n", keyvalues[i].key_length, keyvalues[i].key, keyvalues[i].value_length, keyvalues[i].value);
    // }
    // if (more) {
    //     printf("There are more key-value pairs to fetch.\n");
    // }
}

void clear_key(FDBTransaction* tr, const char* key) {
    fdb_transaction_clear(tr, (const uint8_t*)key, strlen(key));
}

void clear_range(FDBTransaction* tr, const char* start, const char* end) {
    fdb_transaction_clear_range(tr, (const uint8_t*)start, strlen(start), (const uint8_t*)end, strlen(end));
}

void* network_thread_func(void* arg) {
    fdb_error_t err = fdb_run_network();
    if (err) {
        fprintf(stderr, "Network thread error: %s\n", fdb_get_error(err));
    }
    return NULL;
}

void check_transaction_commit(FDBTransaction* tr) {
    int committed = 0;
    FDBFuture *commitFuture = fdb_transaction_commit(tr);
    check_fdb_error(fdb_future_block_until_ready(commitFuture));

    fdb_error_t err = fdb_future_get_error(commitFuture);
    if (err) {
        fprintf(stderr, "Commit failed: %s\n", fdb_get_error(err));
        fdb_future_destroy(commitFuture);
        exit(EXIT_FAILURE);
    } else {
        committed = 1;
        printf("Transaction committed successfully.\n");
    }
    fdb_future_destroy(commitFuture);
}

void* transaction_t1(void* arg) {
    FDBDatabase* db = (FDBDatabase*)arg;
    FDBTransaction* tr1;
    fdb_error_t err = fdb_database_create_transaction(db, &tr1);
    check_fdb_error(err);
    char* value = get_value(tr1, "K1");
    printf("T1 reads K1: %s\n", value);
    free(value);
    sleep(3);
    set_key(tr1, "K2", "NewValue2");
    check_transaction_commit(tr1);
    fdb_transaction_destroy(tr1);
    printf("T1 committed.\n");
    return NULL;
}

void* transaction_t2(void* arg) {
    FDBDatabase* db = (FDBDatabase*)arg;
    FDBTransaction* tr1;
    fdb_error_t err = fdb_database_create_transaction(db, &tr1);
    check_fdb_error(err);
    char* value = get_value(tr1, "K2");
    printf("T1 reads K2: %s\n", value);
    free(value);
    sleep(1);
    set_key(tr1, "K1", "NewValue2");
    check_transaction_commit(tr1);
    fdb_transaction_destroy(tr1);
    printf("T2 committed.\n");
    return NULL;
}

int main() {
    fdb_error_t err = fdb_select_api_version(FDB_API_VERSION);
    check_fdb_error(err);
    err = fdb_setup_network();
    check_fdb_error(err);
    FDBDatabase* db;
    err = fdb_create_database(NULL, &db);
    check_fdb_error(err);
    
    pthread_t network_thread;
    err = pthread_create(&network_thread, NULL, network_thread_func, NULL);
    if (err != 0) {
        fprintf(stderr, "Error creating network thread: %s\n", strerror(err));
        exit(EXIT_FAILURE);
    }
    FDBTransaction* tr1;
    err = fdb_database_create_transaction(db, &tr1);
    check_fdb_error(err);
    set_key(tr1, "K1", "Value1");
    set_key(tr1, "K2", "Value2");
    check_transaction_commit(tr1);
    fdb_transaction_destroy(tr1);
    pthread_t t1_thread, t2_thread;
    err = pthread_create(&t1_thread, NULL, transaction_t1, (void*)db);
    if (err != 0) {
        fprintf(stderr, "Error creating transaction_t1: %s\n", strerror(err));
        exit(EXIT_FAILURE);
    }
    err = pthread_create(&t2_thread, NULL, transaction_t2, (void*)db);
    if (err != 0) {
        fprintf(stderr, "Error creating transaction_t2: %s\n", strerror(err));
        exit(EXIT_FAILURE);
    }
    pthread_join(t1_thread, NULL);
    pthread_join(t2_thread, NULL);
    fdb_database_destroy(db);
    err = fdb_stop_network();
    check_fdb_error(err);
    pthread_join(network_thread, NULL);
    return 0;
}
