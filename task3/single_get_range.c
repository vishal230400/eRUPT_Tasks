#define FDB_API_VERSION 630
#include <foundationdb/fdb_c.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>

int  NUM_KEYS=10000;
int NUM_EXPERIMENTS=50;

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
    int rowLimit = limit > 0 ? limit : 1000;
    int byteLimit = 0;
    int iteration = 1;
    fdb_bool_t snapshot = 0;
    fdb_bool_t reverse = 0;
    int total_count = 0;
    int more = 1;
    uint8_t* allocated_key = NULL;
    while (more) {
        FDBFuture* future = fdb_transaction_get_range(
            tr, 
            FDB_KEYSEL_FIRST_GREATER_OR_EQUAL(begin_key_name, begin_key_name_length), 
            FDB_KEYSEL_FIRST_GREATER_OR_EQUAL(end_key_name, end_key_name_length), 
            rowLimit, 
            byteLimit, 
            mode, 
            iteration, 
            snapshot, 
            reverse
        );
        fdb_error_t err = fdb_future_block_until_ready(future);
        check_fdb_error(err);
        const FDBKeyValue* keyvalues;
        int count;
        err = fdb_future_get_keyvalue_array(future, &keyvalues, &count, &more);
        fdb_future_destroy(future);
        check_fdb_error(err);
        total_count += count;
        if (more) {
            if (allocated_key) {
                free(allocated_key);
                allocated_key = NULL;
            }
            size_t next_key_length = keyvalues[count - 1].key_length + 1;
            allocated_key = (uint8_t*)malloc(next_key_length);
            memcpy(allocated_key, keyvalues[count - 1].key, keyvalues[count - 1].key_length);
            allocated_key[keyvalues[count - 1].key_length] = '\0';
            begin_key_name = allocated_key;
            begin_key_name_length = keyvalues[count - 1].key_length + 1;
            iteration++;
        }
    }
    if (allocated_key) {
        free(allocated_key);
    }
    if (total_count != NUM_KEYS) {
        fprintf(stderr, "Error: Expected %d key-value pairs, but found %d.\n", NUM_KEYS, total_count);
        exit(EXIT_FAILURE);
    }
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
        //printf("Transaction committed successfully.\n");
    }
    fdb_future_destroy(commitFuture);
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
    FILE* file = fopen("results/SingleGetRange.txt", "w");
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
            char key[9], value[11];
            sprintf(key, "key_%04d", i);
            sprintf(value, "value_%04d", i);
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
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_ITERATOR);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_ITERATOR : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 1:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_SMALL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_SMALL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 2:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_MEDIUM);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_MEDIUM : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 3:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_LARGE);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_LARGE : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 4:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_SERIAL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_SERIAL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;                
                case 5:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_WANT_ALL);
                    end_set_time = clock();
                    duration_set_time = (double)(end_set_time - start_set_time) / CLOCKS_PER_SEC;
                    fprintf(file, "Experiment %d : GetRange : Mode FDB_STREAMING_MODE_WANT_ALL : 10000 Keys time in s: %.6f\n", experiment + 1, duration_set_time);
                    break;
                case 6:
                    start_set_time = clock();
                    get_range(tr1,"\x00","\xFF",NUM_KEYS,FDB_STREAMING_MODE_EXACT);
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
