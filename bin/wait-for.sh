#!/usr/bin/env bash

set -eu

selected_service=${@}

service_base_urls=${CIVIL_SERVICE_URL}
if [[ "${selected_service}" == 'manage-case' ]]; then
  service_base_urls=${URL}
fi

max_health_check_attempts=30

function checkHealth {
  for service_base_url in ${service_base_urls}; do
    echo -e "\n Service base url: ${service_base_url}"
    curl -k --fail --silent --output /dev/null --head ${service_base_url}/health
    if [ $? -ne 0 ]; then
      exit 1
    fi
  done
}

until $(checkHealth); do
  current_health_check_attempt=$((${current_health_check_attempt:-1} + 1))

  if [ ${current_health_check_attempt} -gt ${max_health_check_attempts} ]; then
    echo -e "\nMax number of attempts reached"
    exit 1
  fi

  if [ ${current_health_check_attempt} -eq 2 ]; then
    printf 'Awaiting healthy services'
  else
    printf '.'
  fi

  sleep 10
done
