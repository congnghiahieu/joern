#! /bin/env bash

SCRIPT_ABS_PATH=$(readlink -f $0)
SCRIPT_ABS_DIR=$(dirname $SCRIPT_ABS_PATH)
ROOT_DIR=$(dirname $(dirname $SCRIPT_ABS_DIR))
# OUTPUT_DIR=out_$(date +%s)
OUTPUT_DIR=$ROOT_DIR/out
CPG_PATH=$ROOT_DIR/cpg.bin
DATABASE="neo4j"
DATABASE_USER="neo4j"
DATABASE_PASSWORD="12345678"

EXTRA_ARGS=""

case $1 in
"rust")
  INPUT_DIR=$ROOT_DIR/rust/tests/rust/
  EXTRA_ARGS=""
  ;;
"rustdownload")
  INPUT_DIR=$ROOT_DIR/rust/tests/rustdownload/
  ;;
"go")
  INPUT_DIR=$ROOT_DIR/rust/tests/go/
  ;;
"js")
  INPUT_DIR=$ROOT_DIR/rust/tests/js/
  ;;
"py")
  INPUT_DIR=$ROOT_DIR/rust/tests/python/
  ;;
"java")
  INPUT_DIR=$ROOT_DIR/rust/tests/java/
  ;;
"c")
  INPUT_DIR=$ROOT_DIR/rust/tests/c/
  ;;
*)
  INPUT_DIR=$ROOT_DIR/rust/tests/rust/
  ;;
esac

rm -rf $OUTPUT_DIR

$ROOT_DIR/joern-parse -J-Xmx25G $INPUT_DIR --output $CPG_PATH $EXTRA_ARGS --language RUSTLANG --frontend-args --rust-parser-path /home/hieucien/Workspace/joern/joern-cli/frontends/rustsrc2cpg/bin/rust-parser/rust-parser

$ROOT_DIR/joern-export --repr=all --format=neo4jcsv --out=$OUTPUT_DIR $CPG_PATH

# Remove old import file
docker exec neo4j bash -c "rm -rf /var/lib/neo4j/import/*"

# Remove old graph
docker exec neo4j bash -c "cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE \"MATCH (n) DETACH DELETE n\""

# # Create Joern database in Neo4j
# docker exec neo4j bash -c "cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE 'CREATE DATABASE $DATABASE'"

docker cp $OUTPUT_DIR/. neo4j:/var/lib/neo4j/import

docker exec neo4j bash -c "find /var/lib/neo4j/import/ -name 'nodes_*_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;"
docker exec neo4j bash -c "find /var/lib/neo4j/import/ -name 'edges_*_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;"

# export DATABASE="neo4j"
# export DATABASE_USER="neo4j"
# export DATABASE_PASSWORD="12345678"
# find /var/lib/neo4j/import/ -name 'edges_AST_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;
