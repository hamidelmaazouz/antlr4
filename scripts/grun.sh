# mkdir -p /tmp/antlr/grun
# export OUTPUT_DIR=/tmp/antlr/grun
export OUTPUT_DIR=/home/hamidelmaazouz/repositories/antlr/antlr4/runtime/Java/src/org/antlr/v4/runtime/pal/evaluation
export ANTLR_REPO=/home/hamidelmaazouz/repositories/antlr/antlr4
export CLASSPATH=$OUTPUT_DIR:$ANTLR_REPO/tool/target/antlr4-4.10.2-SNAPSHOT-complete.jar
export WORKING_DIR=$(pwd)

java org.antlr.v4.Tool -no-listener -visitor -o $OUTPUT_DIR $WORKING_DIR/example/java/Java8.g4
# javac -d $OUTPUT_DIR $OUTPUT_DIR/*.java
# java org.antlr.v4.gui.TestRig Java8 compilationUnit -cfg $WORKING_DIR/example/java/Fibonacci.java
