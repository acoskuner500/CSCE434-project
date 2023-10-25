SRC_DIR := src
BUILD_DIR := build
DEPS := lib/commons-cli-1.5.0.jar
TESTS_DIR := ../engr-cse-compiler-design-f23/PA7

# For a different testfile, use TEST=testXXX as CLI argument
TEST ?= test000
INPUT_FILE := $(TEST).txt

# Find all Java source files recursively within the SRC_DIR
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Convert source file paths to class file paths in the BUILD_DIR
CLASSES := $(patsubst $(SRC_DIR)/%.java,$(BUILD_DIR)/%.class,$(SOURCES))

# Define the Java compiler and its flags
JAVAC := javac
JAVACFLAGS := -d $(BUILD_DIR) -sourcepath $(SRC_DIR) -cp ".:../$(DEPS)"

build_dir:
	@(mkdir -p build)

build: build_dir $(CLASSES)

# Compile Java source files into classes
$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java
	@($(JAVAC) $(JAVACFLAGS) $<)

run: build
	@(cd $(BUILD_DIR) && java -cp ".:../../$(DEPS)" coco.CompilerTester -s ../$(TESTS_DIR)/$(INPUT_FILE))

ast: build
	@(cd $(BUILD_DIR) && java -cp ".:../../$(DEPS)" coco.CompilerTester -s ../$(TESTS_DIR)/$(INPUT_FILE) -a)

zip: $(SOURCES)
	@(cd $(SRC_DIR) && zip -r PA7 *)

clean:
	@(rm -rf $(BUILD_DIR))
	@(rm -f $(SRC_DIR)/*.zip)

# PHONY targets (targets that don't represent files)
.PHONY: build run ast zip clean