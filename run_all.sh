#!/bin/sh
set -e
cd "$(dirname "$0")"
./mvnw -q clean package
./mvnw -q exec:exec@benchmark
./mvnw -q exec:exec@memory
python3 scripts/plot_results.py
