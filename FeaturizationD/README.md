CSVInputFormat Example Code
===========================

This is an SBT Project that runs a csv modified input format example with sample data provided from the CSVInputFormat github

in order to run just "startup hadoop"
`cd hadoopPlay`
`./runInHadoop`


IGNORE ALL THE REST BELOW. I'll just show you in person how to make your own copy of this code and modify it yourself.
=============
This refers to the `lib/CSVInputFormat.jar` which comes from jarring up the following project: `https://github.com/derrickcheng/CSVInputFormat/tree/master/src/main`. This project is just: `https://github.com/mvallebr/CSVInputFormat` but in SBT Project Form

Things you need to download first:
1)
2)

If you want to modify to this skeleton to do your own bidding:
1) Do not modify this and make a copy of this project somewhere else
2) go to the top level directory of the copied project. then type `sbt eclipse`. Which will analyze the build.sbt file and make all the libraries available on path to the project when you open it up in sbt
3) go to eclipse and do that `import existing project from workspace deal` and import over that newly create project

