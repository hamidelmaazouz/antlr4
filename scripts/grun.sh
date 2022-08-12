export ANTLR_REPO=/home/hamidelmaazouz/repositories/antlr/antlr4
export PWD=$(pwd)

### Build ANTLR from source - build once and comment these lines to save time
#cd $ANTLR_REPO/scripts
#source install.sh
#cd $PWD
#exit

### Use the previously built ANTLR to generate target language artefacts
export RUST_GRAMMAR_DIR=$PWD/rust
export ANTLR_OUTPUT_DIR=$RUST_GRAMMAR_DIR/Java
export JAVA_OUTPUT_DIR=$RUST_GRAMMAR_DIR/target/classes
export CLASSPATH=$ANTLR_REPO/tool/target/antlr4-4.10.2-SNAPSHOT-complete.jar

java org.antlr.v4.Tool -listener -visitor -cfgbuilder -o $ANTLR_OUTPUT_DIR \
     $RUST_GRAMMAR_DIR/RustLexer.g4 $RUST_GRAMMAR_DIR/RustParser.g4
javac -d $JAVA_OUTPUT_DIR $ANTLR_OUTPUT_DIR/*.java

export CLASSPATH=$CLASSPATH:$JAVA_OUTPUT_DIR
java org.antlr.v4.gui.TestRig Rust crate -cfg $RUST_GRAMMAR_DIR/examples/hello.rs
