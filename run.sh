#!/usr/bin/env bash

readonly BASE_NAME='LTR_Recc'
readonly CLASSPATH_FILE='classpath.out'
readonly TARGET_CLASS="org.linqs.psl.${BASE_NAME}.Run"
readonly DATA_PATH="./data"
readonly FETCH_DATA_SCRIPT='fetchData.sh'

declare -A DATA_SUB_PATH=(['movie_lens']='data' ['Jester']='jester/0/eval')
DATASETS=("movie_lens")

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

   for datasetName in "${DATASETS[@]}"
   do
     cd "${DATA_PATH}/${datasetName}" || exit 1
     bash "${FETCH_DATA_SCRIPT}"
   done

   popd > /dev/null || exit 1
}

function parseArgs() {
  while getopts 'ad:' opt;
  do
     case "$opt" in
        a ) DATASETS=("movie_lens") ;;
        d ) DATASETS=("$OPTARG") ;;
        ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
     esac
  done

  shift $((OPTIND - 1))
}

function helpFunction()
{
   echo ""
   echo "Usage: $0 -a parameterA -b parameterB -c parameterC"
   echo -e "\t-a Description of what is parameterA"
   echo -e "\t-d Name of dataset you would like to run inference on: movie_lens"
   exit 1 # Exit script after printing help
}

function run() {
   echo "Running PSL"

   for datasetName in "${DATASETS[@]}"
   do
     java -cp ./target/classes:"$(cat "${CLASSPATH_FILE}")" "${TARGET_CLASS}" "${datasetName}" "${DATA_SUB_PATH[$datasetName]}"
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
