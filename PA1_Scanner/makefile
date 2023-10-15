JC := javac
SRC_DIR := ./src
DEPS := ../lib/commons-cli-1.5.0.jar
TESTS_DIR := ../../engr-cse-compiler-design-f23/PA1
# For a different testfile, use TEST=testXXX as CLI argument
TEST ?= test000
SRC_FILE := $(TEST).txt

build: $(SRC_DIR)/coco/*.java
	@$(JC) -cp ".:$(DEPS)" $(SRC_DIR)/coco/*.java

run: build
	@(cd ./src && java -cp ".:../$(DEPS)" coco.ScannerTester -s ../$(TESTS_DIR)/$(SRC_FILE))

zip: clean
	@(cd ./src && zip -r PA1 coco)

clean:
	@(rm -f $(SRC_DIR)/coco/*.class $(SRC_DIR)/*.zip)