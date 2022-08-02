export MAVEN_OPTS="-Xmx1G"

# rm -rf ~/.m2/repository/org/antlr*

cd ..
mvn clean
mvn -DskipTests install

find ~/.m2/repository/org/antlr -name 'antlr4*.jar'
