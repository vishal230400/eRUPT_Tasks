#include <foundationdb/fdb_c.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Helper function to check FDB status and handle errors
void check_fdb_error(fdb_error_t err) {
    if (err) {
        fprintf(stderr, "FDB error: %s\n", fdb_get_error(err));
        exit(EXIT_FAILURE);
    }
}

// Function to set a key-value pair
void set_key(FDBTransaction* tr, const char* key, const char* value) {
    fdb_transaction_set(tr, (const uint8_t*)key, strlen(key), (const uint8_t*)value, strlen(value));
}

// Function to get a value for a given key
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

// Function to clear a key
void clear_key(FDBTransaction* tr, const char* key) {
    fdb_transaction_clear(tr, (const uint8_t*)key, strlen(key));
}

// Function to clear a range
void clear_range(FDBTransaction* tr, const char* start, const char* end) {
    fdb_transaction_clear_range(tr, (const uint8_t*)start, strlen(start), (const uint8_t*)end, strlen(end));
}

int main() {
    fdb_error_t err = fdb_select_api_version(FDB_API_VERSION);
    check_fdb_error(err);

    FDBDatabase* db;
    err = fdb_setup_network();
    check_fdb_error(err);

    err = fdb_create_database(NULL, &db);
    check_fdb_error(err);

    err = fdb_run_network();
    check_fdb_error(err);

    FDBTransaction* tr;
    err = fdb_database_create_transaction(db, &tr);
    check_fdb_error(err);

    set_key(tr, "apple", "fruit");
    set_key(tr, "banana", "fruit");
    set_key(tr, "cherry", "fruit");
    set_key(tr, "grape", "fruit");
    set_key(tr, "orange", "fruit");
    set_key(tr, "pear", "fruit");
    set_key(tr, "strawberry", "fruit");

    char* value = get_value(tr, "strawberry");
    printf("Value of 'strawberry': %s\n", value);
    free(value);

    value = get_value(tr, "tomato");
    printf("Value of 'tomato': %s\n", value ? value : "null");
    if (value) free(value);

    clear_range(tr, "a", "z");

    value = get_value(tr, "grape");
    printf("Value of 'grape' after clear range: %s\n", value ? value : "null");
    if (value) free(value);

    fdb_transaction_destroy(tr);
    fdb_database_destroy(db);

    return 0;
}
