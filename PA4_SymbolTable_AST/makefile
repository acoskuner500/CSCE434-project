JC := javac
SRC_DIR := ./src
DEPS := ../lib/commons-cli-1.5.0.jar
TESTS_DIR := ../../engr-cse-compiler-design-f23/PA4
# For a different testfile, use TEST=testXXX as CLI argument
TEST ?= test000
SRC_FILE := $(TEST).txt

build: $(SRC_DIR)/coco/*.java $(SRC_DIR)/ast/*.java $(SRC_DIR)/types/*.java
	@$(JC) -cp ".:$(DEPS)" $(SRC_DIR)/coco/*.java $(SRC_DIR)/ast/*.java $(SRC_DIR)/types/*.java

run: build
	@(cd ./src && java -cp ".:../$(DEPS)" coco.CompilerTester -s ../$(TESTS_DIR)/$(SRC_FILE) -a)

zip: clean
	@(cd ./src && zip -r PA4 coco ast types)

clean:
	@(rm -f $(SRC_DIR)/coco/*.class $(SRC_DIR)/ast/*.class $(SRC_DIR)/types/*.class $(SRC_DIR)/*.zip)