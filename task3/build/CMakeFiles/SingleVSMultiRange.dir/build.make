# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.16

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
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
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /workspaces/eRUPT_Tasks/task3

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /workspaces/eRUPT_Tasks/task3/build

# Include any dependencies generated for this target.
include CMakeFiles/SingleVSMultiRange.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/SingleVSMultiRange.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/SingleVSMultiRange.dir/flags.make

CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o: CMakeFiles/SingleVSMultiRange.dir/flags.make
CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o: ../single_vs_multi_ranges.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/workspaces/eRUPT_Tasks/task3/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o   -c /workspaces/eRUPT_Tasks/task3/single_vs_multi_ranges.c

CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /workspaces/eRUPT_Tasks/task3/single_vs_multi_ranges.c > CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.i

CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /workspaces/eRUPT_Tasks/task3/single_vs_multi_ranges.c -o CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.s

# Object files for target SingleVSMultiRange
SingleVSMultiRange_OBJECTS = \
"CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o"

# External object files for target SingleVSMultiRange
SingleVSMultiRange_EXTERNAL_OBJECTS =

SingleVSMultiRange: CMakeFiles/SingleVSMultiRange.dir/single_vs_multi_ranges.c.o
SingleVSMultiRange: CMakeFiles/SingleVSMultiRange.dir/build.make
SingleVSMultiRange: /usr/lib/libfdb_c.so
SingleVSMultiRange: CMakeFiles/SingleVSMultiRange.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/workspaces/eRUPT_Tasks/task3/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable SingleVSMultiRange"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/SingleVSMultiRange.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/SingleVSMultiRange.dir/build: SingleVSMultiRange

.PHONY : CMakeFiles/SingleVSMultiRange.dir/build

CMakeFiles/SingleVSMultiRange.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/SingleVSMultiRange.dir/cmake_clean.cmake
.PHONY : CMakeFiles/SingleVSMultiRange.dir/clean

CMakeFiles/SingleVSMultiRange.dir/depend:
	cd /workspaces/eRUPT_Tasks/task3/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /workspaces/eRUPT_Tasks/task3 /workspaces/eRUPT_Tasks/task3 /workspaces/eRUPT_Tasks/task3/build /workspaces/eRUPT_Tasks/task3/build /workspaces/eRUPT_Tasks/task3/build/CMakeFiles/SingleVSMultiRange.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/SingleVSMultiRange.dir/depend
