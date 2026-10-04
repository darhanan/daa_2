#!/bin/sh
# Builds the project, runs the tests, the benchmark and the memory measurement, and redraws the charts.
set -e
cd "$(dirname "$0")"
./mvnw -q clean package
./mvnw -q exec:exec@benchmark
./mvnw -q exec:exec@memory
python3 scripts/plot_results.py
