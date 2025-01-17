#! /bin/env bash

SCRIPT_ABS_PATH=$(readlink -f "$0")
SCRIPT_ABS_DIR=$(dirname "$SCRIPT_ABS_PATH")
ROOT_DIR=$(dirname "$(dirname "$SCRIPT_ABS_DIR")")
# OUTPUT_DIR=out_$(date +%s)
OUTPUT_DIR=$ROOT_DIR/out
DATABASE="neo4j"
DATABASE_USER="neo4j"
DATABASE_PASSWORD="12345678"

case $1 in
  "rust")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/rustsrc2cpg/cpg.bin
    ;;
  "go")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/gosrc2cpg/cpg.bin
    ;;
  "js")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/jssrc2cpg/cpg.bin
    ;;
  "py")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/pysrc2cpg/cpg.bin
    ;;
  "java")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/javasrc2cpg/cpg.bin
    ;;
  "c")
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/c2cpg/cpg.bin
    ;;
  *)
    CPG_PATH=$ROOT_DIR/joern-cli/frontends/rustsrc2cpg/cpg.bin
    ;;
esac

rm -rf "$OUTPUT_DIR"
"$ROOT_DIR/joern-export" --repr=all --format=neo4jcsv --out="$OUTPUT_DIR" "$CPG_PATH"

# Remove old import file
docker exec neo4j bash -c "rm -rf /var/lib/neo4j/import/*"

# Remove old graph
docker exec neo4j bash -c "cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE \"MATCH (n) DETACH DELETE n\""

# # Create Joern database in Neo4j
# docker exec neo4j bash -c "cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE 'CREATE DATABASE $DATABASE'"

docker cp "$OUTPUT_DIR/." neo4j:/var/lib/neo4j/import

docker exec neo4j bash -c "find /var/lib/neo4j/import/ -name 'nodes_*_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;"
docker exec neo4j bash -c "find /var/lib/neo4j/import/ -name 'edges_*_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;"

# export DATABASE="neo4j"
# export DATABASE_USER="neo4j"
# export DATABASE_PASSWORD="12345678"
# find /var/lib/neo4j/import/ -name 'edges_AST_cypher.csv' -exec cypher-shell -u $DATABASE_USER -p $DATABASE_PASSWORD -d $DATABASE --file {} \;
