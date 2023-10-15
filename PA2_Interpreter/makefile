JC := javac
SRC_DIR := ./src
DEPS := ../lib/commons-cli-1.5.0.jar
TESTS_DIR := ../../engr-cse-compiler-design-f23/PA2
# For a different testfile, use TEST=testXXX as CLI argument
TEST ?= test000
INPUT ?= dummy
SRC_FILE := $(TEST).txt
INPUT_FILE := $(INPUT).in

build: $(SRC_DIR)/coco/*.java
	@$(JC) -cp ".:$(DEPS)" $(SRC_DIR)/coco/*.java

run: build
	@(cd ./src && java -cp ".:../$(DEPS)" coco.InterpreterTester -i ../$(TESTS_DIR)/$(INPUT_FILE) -s ../$(TESTS_DIR)/$(SRC_FILE))

zip: clean
	@(cd ./src && zip -r PA4 coco)

clean:
	@(rm -f $(SRC_DIR)/coco/*.class $(SRC_DIR)/*.zip)