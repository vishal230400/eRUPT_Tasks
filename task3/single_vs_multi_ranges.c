#define FDB_API_VERSION 630
#include <foundationdb/fdb_c.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>

int  NUM_KEYS=10000;
int NUM_EXPERIMENTS=10;

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

char start0[20] = "key_0";
char end0[20] = "key_1898";
char start1[20] = "key_1899";
char end1[20] = "key_2798";
char start2[20] = "key_2799";
char end2[20] = "key_3698";
char start3[20] = "key_3699";
char end3[20] = "key_4598";
char start4[20] = "key_4599";
char end4[20] = "key_5498";
char start5[20] = "key_5499";
char end5[20] = "key_6398";
char start6[20] = "key_6399";
char end6[20] = "key_7298";
char start7[20] = "key_7299";
char end7[20] = "key_8198";
char start8[20] = "key_8199";
char end8[20] = "key_9098";
char start9[20] = "key_9099";
char end9[20] = "key_9999";

void get_multi_range(FDBTransaction* tr1, FDBStreamingMode mode){
    get_range(tr1, start0, end0, NUM_KEYS/10, mode);
    get_range(tr1, start1, end1, NUM_KEYS/10, mode);
    get_range(tr1, start2, end2, NUM_KEYS/10, mode);
    get_range(tr1, start3, end3, NUM_KEYS/10, mode);
    get_range(tr1, start4, end4, NUM_KEYS/10, mode);
    get_range(tr1, start5, end5, NUM_KEYS/10, mode);
    get_range(tr1, start6, end6, NUM_KEYS/10, mode);
    get_range(tr1, start7, end7, NUM_KEYS/10, mode);
    get_range(tr1, start8, end8, NUM_KEYS/10, mode);
    get_range(tr1, start9, end9, NUM_KEYS/10, mode);
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
    FILE* file = fopen("results/SingleVSMultiRanges.txt", "w");
    if (!file) {
        perror("Error opening file");
        exit(EXIT_FAILURE);
    }
    for (int experiment = 0; experiment < NUM_EXPERIMENTS; experiment++) {
        FDBTransaction* tr;
        err = fdb_database_create_transaction(db, &tr);
        check_fdb_error(err);

        clock_t start_set_time = clock();
        for (int i = 0; i < NUM_KEYS; i++) {
            char key[20], value[20];
            sprintf(key, "key_%d", i);
            sprintf(value, "value_%d", i);
            set_key(tr, key, value);
        }
        check_transaction_commit(tr);
        fdb_transaction_destroy(tr);
        clock_t end_set_time = clock();
        double duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
        fprintf(file, "Experiment %d : Creating 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);


        FDBTransaction* tr1;
        err = fdb_database_create_transaction(db, &tr1);
        check_fdb_error(err);
        for(int i=0;i<7;i++){
            switch (i) {
                case 0:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_ITERATOR);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_ITERATOR : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 1:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_SMALL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_SMALL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 2:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_MEDIUM);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_MEDIUM : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 3:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_LARGE);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_LARGE : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 4:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_SERIAL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_SERIAL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;                
                case 5:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_WANT_ALL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_WANT_ALL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 6:
                    start_set_time = clock();
                    get_multi_range(tr1,FDB_STREAMING_MODE_EXACT);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_EXACT : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
            }
        }
        check_transaction_commit(tr1);
        fdb_transaction_destroy(tr1);
    }
    FDBTransaction* tr3;
    err = fdb_database_create_transaction(db, &tr3);
    check_fdb_error(err);
    clear_range(tr3, "a", "z");
    check_transaction_commit(tr3);
    fdb_transaction_destroy(tr3);

    fdb_database_destroy(db);
    err = fdb_stop_network();
    check_fdb_error(err);
    pthread_join(network_thread, NULL);
    return 0;
}
