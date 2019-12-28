#!/bin/bash

readonly PREDICATE_CONSTRUCTION_SCRIPT='predicate_construction.py'
readonly EXPECTED_DATA_DIR='./data'

function main() {
   trap exit SIGINT

   check_requirements

   construct_predicates "${DATA_FILE}" "${DATA_DIR}" 'data'
}

function check_requirements() {
   type python3 > /dev/null 2> /dev/null
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: python3 required to run project'
      exit 11
   fi
}

function construct_predicates() {
  if [[ -e "${EXPECTED_DATA_DIR}" ]]; then
    echo "movie_lens predicates found cached, skipping construction."
    return
  fi

  python3 "${PREDICATE_CONSTRUCTION_SCRIPT}"
  if [[ "$?" -ne 0 ]]; then
    echo 'ERROR: Failed to run'
    exit 60
  fi
}

main "$@"