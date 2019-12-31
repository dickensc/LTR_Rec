#!/bin/bash

function main() {
   local name=$1
   local data_url=$2
   local data_file=$3
   local data_dir=$4
   local data_sub_dir=$5
   local predicate_construction_script=$6

   trap exit SIGINT

   check_requirements

   pushd . > /dev/null

   cd "${data_dir}" || exit 1

   fetch_file "${data_url}" "${data_file}"
   extract_zip "${data_file}" "${data_sub_dir}" "${name}"
   construct_predicates "${predicate_construction_script}" "${data_sub_dir}" "${name}"

   popd > /dev/null || exit 1
}

function check_requirements() {
   local hasWget
   local hasCurl

   type wget > /dev/null 2> /dev/null
   hasWget=$?

   type curl > /dev/null 2> /dev/null
   hasCurl=$?

   if [[ "${hasWget}" -ne 0 ]] && [[ "${hasCurl}" -ne 0 ]]; then
      echo 'ERROR: wget or curl required to download dataset'
      exit 10
   fi

   type tar > /dev/null 2> /dev/null
   if [[ "$?" -ne 0 ]]; then
      echo 'ERROR: tar required to extract dataset'
      exit 11
   fi
}

function get_fetch_command() {
   type curl > /dev/null 2> /dev/null
   if [[ "$?" -eq 0 ]]; then
      echo "curl -o"
      return
   fi

   type wget > /dev/null 2> /dev/null
   if [[ "$?" -eq 0 ]]; then
      echo "wget -O"
      return
   fi

   echo 'ERROR: wget or curl not found'
   exit 20
}

function fetch_file() {
   local url=$1
   local file_name=$2

   if [[ -e "${file_name}" ]]; then
      echo "${file_name} file found cached, skipping download."
      return
   fi

   echo "Downloading ${file_name} file with command: $FETCH_COMMAND"
   $(get_fetch_command) "${file_name}" "${url}"
   if [[ "$?" -ne 0 ]]; then
      echo "ERROR: Failed to download ${file_name} file"
      exit 30
   fi
}

function extract_zip() {
   local zipped_data_file=$1
   local expectedDir=$2
   local name=$3

   if [[ -e "${expectedDir}" ]]; then
      echo "Extracted ${name} zip found cached, skipping extract."
      return
   fi

   echo "Extracting the ${name} zip"
   unzip "${zipped_data_file}"
   if [[ "$?" -ne 0 ]]; then
      echo "ERROR: Failed to extract ${name} zip"
      exit 40
   fi
}

function construct_predicates() {
  local predicate_construction_script=$1
  local expectedDir=$2
  local name=$3

#  if [[ -e "${expectedDir}" ]]; then
#    echo "${name} predicates found cached, skipping predicate construction."
#    return
#  fi

  python3 "${predicate_construction_script}"
  if [[ "$?" -ne 0 ]]; then
    echo "ERROR: Failed to run ${predicate_construction_script}"
    exit 60
  fi
}

main "$@"