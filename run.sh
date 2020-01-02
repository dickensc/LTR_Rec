#!/usr/bin/env bash

readonly BASE_NAME='LTR_Recc'
readonly CLASSPATH_FILE='classpath.out'
readonly TARGET_CLASS="org.linqs.psl.${BASE_NAME}.Run"
readonly DATA_PATH="./data"
readonly FETCH_DATA_SCRIPT='fetchData.sh'

declare datasets=("movie_lens" "Jester" "yelp" "lastfm")

declare -A FINAL_DATA_PATH
FINAL_DATA_PATH['movie_lens']='data'
FINAL_DATA_PATH['Jester']='jester/0/eval'
FINAL_DATA_PATH['yelp']='yelp/0/eval'
FINAL_DATA_PATH['lastfm']='lastfm/0/eval'

declare -A DATA_URL
DATA_URL['movie_lens']='https://files.grouplens.org/datasets/movielens/ml-100k.zip'
DATA_URL['Jester']='https://linqs-data.soe.ucsc.edu/public/psl-infinite-experiments/jester.zip'
DATA_URL['yelp']='https://linqs-data.soe.ucsc.edu/public/dickens-ranking/yelp.zip'
DATA_URL['lastfm']='https://linqs-data.soe.ucsc.edu/public/dickens-ranking/lastfm.zip'

declare -A DATA_FILE
DATA_FILE['movie_lens']='ml-100k.zip'
DATA_FILE['Jester']='jester.zip'
DATA_FILE['yelp']='yelp.zip'
DATA_FILE['lastfm']='lastfm.zip'

declare -A DATA_SUB_DIR
DATA_SUB_DIR['movie_lens']='ml-100k'
DATA_SUB_DIR['Jester']='jester'
DATA_SUB_DIR['yelp']='yelp'
DATA_SUB_DIR['lastfm']='lastfm'

declare -A PREDICATE_CONSTRUCTION_SCRIPTS
PREDICATE_CONSTRUCTION_SCRIPTS['movie_lens']='predicate_construction.py'
PREDICATE_CONSTRUCTION_SCRIPTS['Jester']='predicate_construction.py'
PREDICATE_CONSTRUCTION_SCRIPTS['yelp']='predicate_construction.py'
PREDICATE_CONSTRUCTION_SCRIPTS['lastfm']='predicate_construction.py'

function main() {
   trap exit SIGINT

   parseArgs "$@"
   getData
   check_requirements
   compile
   buildClasspath
   run
}

function getData() {
   pushd . > /dev/null
   cd "${DATA_PATH}" || exit 1

   for datasetName in "${datasets[@]}"
   do
     bash "${FETCH_DATA_SCRIPT}" "${datasetName}" "${DATA_URL[$datasetName]}" "${DATA_FILE[$datasetName]}" "${datasetName}" "${DATA_SUB_DIR[$datasetName]}" "${PREDICATE_CONSTRUCTION_SCRIPTS[$datasetName]}"
   done

   popd > /dev/null || exit 1
}

function parseArgs() {
  while getopts 'ad:' opt;
  do
     case "$opt" in
        a ) datasets=("movie_lens" "Jester" "yelp" "lastfm") ;;
        d ) datasets=("$OPTARG") ;;
        ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
     esac
  done

  shift $((OPTIND - 1))
}

function helpFunction()
{
   echo ""
   echo "Usage: $0 -a -d parameterD"
   echo -e "\t-a run inference on all datasets"
   echo -e "\t-d the name of the dataset you would like to run inference on: ${datasets[*]}"
   exit 1 # Exit script after printing help
}

function run() {
   echo "Running PSL"

   for datasetName in "${datasets[@]}"
   do
     java -Xms4000m -Xmx30000m -cp ./target/classes:"$(cat "${CLASSPATH_FILE}")" "${TARGET_CLASS}" "${datasetName}" "${FINAL_DATA_PATH[$datasetName]}"
     if [[ "$?" -ne 0 ]]; then
        echo 'ERROR: Failed to run'
        exit 60
     fi
   done
}

function check_requirements() {
   type mvn > /dev/null 2> /dev/null
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: maven required to build project'
      exit 10
   fi

   type java > /dev/null 2> /dev/null
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: java required to run project'
      exit 11
   fi
}

function buildClasspath() {
   # Rebuild every time.
   # It is hard for new users to know when to rebuild.

   mvn dependency:build-classpath -Dmdep.outputFile="${CLASSPATH_FILE}"
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: Failed to build classpath'
      exit 50
   fi
}

function compile() {
   mvn compile
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: Failed to compile'
      exit 40
   fi
}

main "$@"
