# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.28

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Disable VCS-based implicit rules.
% : %,v

# Disable VCS-based implicit rules.
% : RCS/%

# Disable VCS-based implicit rules.
% : RCS/%,v

# Disable VCS-based implicit rules.
% : SCCS/s.%

# Disable VCS-based implicit rules.
% : s.%

.SUFFIXES: .hpux_make_needs_suffix_list

# Command-line flag to silence nested $(MAKE).
$(VERBOSE)MAKESILENT = -s

#Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E rm -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/vishal/github/eRUPT_Tasks/task3

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/vishal/github/eRUPT_Tasks/task3/build

# Include any dependencies generated for this target.
include CMakeFiles/TransactionConflict.dir/depend.make
# Include any dependencies generated by the compiler for this target.
include CMakeFiles/TransactionConflict.dir/compiler_depend.make

# Include the progress variables for this target.
include CMakeFiles/TransactionConflict.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/TransactionConflict.dir/flags.make

CMakeFiles/TransactionConflict.dir/tx_conflict.c.o: CMakeFiles/TransactionConflict.dir/flags.make
CMakeFiles/TransactionConflict.dir/tx_conflict.c.o: /home/vishal/github/eRUPT_Tasks/task3/tx_conflict.c
CMakeFiles/TransactionConflict.dir/tx_conflict.c.o: CMakeFiles/TransactionConflict.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --progress-dir=/home/vishal/github/eRUPT_Tasks/task3/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/TransactionConflict.dir/tx_conflict.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -MD -MT CMakeFiles/TransactionConflict.dir/tx_conflict.c.o -MF CMakeFiles/TransactionConflict.dir/tx_conflict.c.o.d -o CMakeFiles/TransactionConflict.dir/tx_conflict.c.o -c /home/vishal/github/eRUPT_Tasks/task3/tx_conflict.c

CMakeFiles/TransactionConflict.dir/tx_conflict.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Preprocessing C source to CMakeFiles/TransactionConflict.dir/tx_conflict.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/vishal/github/eRUPT_Tasks/task3/tx_conflict.c > CMakeFiles/TransactionConflict.dir/tx_conflict.c.i

CMakeFiles/TransactionConflict.dir/tx_conflict.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green "Compiling C source to assembly CMakeFiles/TransactionConflict.dir/tx_conflict.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/vishal/github/eRUPT_Tasks/task3/tx_conflict.c -o CMakeFiles/TransactionConflict.dir/tx_conflict.c.s

# Object files for target TransactionConflict
TransactionConflict_OBJECTS = \
"CMakeFiles/TransactionConflict.dir/tx_conflict.c.o"

# External object files for target TransactionConflict
TransactionConflict_EXTERNAL_OBJECTS =

TransactionConflict: CMakeFiles/TransactionConflict.dir/tx_conflict.c.o
TransactionConflict: CMakeFiles/TransactionConflict.dir/build.make
TransactionConflict: /usr/lib/libfdb_c.so
TransactionConflict: CMakeFiles/TransactionConflict.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color "--switch=$(COLOR)" --green --bold --progress-dir=/home/vishal/github/eRUPT_Tasks/task3/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable TransactionConflict"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/TransactionConflict.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/TransactionConflict.dir/build: TransactionConflict
.PHONY : CMakeFiles/TransactionConflict.dir/build

CMakeFiles/TransactionConflict.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/TransactionConflict.dir/cmake_clean.cmake
.PHONY : CMakeFiles/TransactionConflict.dir/clean

CMakeFiles/TransactionConflict.dir/depend:
	cd /home/vishal/github/eRUPT_Tasks/task3/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/vishal/github/eRUPT_Tasks/task3 /home/vishal/github/eRUPT_Tasks/task3 /home/vishal/github/eRUPT_Tasks/task3/build /home/vishal/github/eRUPT_Tasks/task3/build /home/vishal/github/eRUPT_Tasks/task3/build/CMakeFiles/TransactionConflict.dir/DependInfo.cmake "--color=$(COLOR)"
.PHONY : CMakeFiles/TransactionConflict.dir/depend

