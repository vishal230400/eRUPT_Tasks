cmake_minimum_required(VERSION 3.10)
project(Task3)

# Set the C standard to C11
set(CMAKE_C_STANDARD 11)

# Find the FoundationDB library
find_library(FDB_LIBRARY NAMES fdb_c PATHS /usr/local/lib /usr/lib /usr/lib64)

# Add the executable
add_executable(BasicFDBOps basic_ops.c)

# Link the FoundationDB library
target_link_libraries(BasicFDBOps ${FDB_LIBRARY})

# Link the pthread library
target_link_libraries(BasicFDBOps pthread)

# Include the FoundationDB headers
target_include_directories(BasicFDBOps PRIVATE /usr/local/include)

# Add the executable
add_executable(SingleGetRange single_get_range.c)

# Link the FoundationDB library
target_link_libraries(SingleGetRange ${FDB_LIBRARY})

# Link the pthread library
target_link_libraries(SingleGetRange pthread)

# Include the FoundationDB headers
target_include_directories(SingleGetRange PRIVATE /usr/local/include)

# Add the executable
add_executable(SingleVSMultiRange single_vs_multi_ranges.c)

# Link the FoundationDB library
target_link_libraries(SingleVSMultiRange ${FDB_LIBRARY})

# Link the pthread library
target_link_libraries(SingleVSMultiRange pthread)

# Include the FoundationDB headers
target_include_directories(SingleVSMultiRange PRIVATE /usr/local/include)

# Add the executable
add_executable(ReadSnapshot read_snapshot.c)

# Link the FoundationDB library
target_link_libraries(ReadSnapshot ${FDB_LIBRARY})

# Link the pthread library
target_link_libraries(ReadSnapshot pthread)

# Include the FoundationDB headers
target_include_directories(ReadSnapshot PRIVATE /usr/local/include)