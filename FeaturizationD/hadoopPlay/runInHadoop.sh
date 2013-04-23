#!/bin/bash

hadoop fs -rmr data/taste2.csv
hadoop fs -put ../data/taste2.csv data/taste2.csv
hadoop fs -rmr hadoopPlay/output

export HADOOP_CLASSPATH="../lib/CSVInputFormat.jar"

hadoop jar ../SparseMatrixBuilder.jar "SparseMatrixBuilder" -libjars "../lib/CSVInputFormat.jar" "SparseMatrixBuilder" data/taste2.csv output 
