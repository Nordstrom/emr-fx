# Nordstrom emr-fx
An open-source framework for writing Java-based EMR jobs.

An EMR job consists of two-phases: map and reduce. During the ```map```, phase you will do the main portion of your processing. During the ```reduce``` phase, you will be consolidating results.

In this framework, the ```main()``` function sets up the execution environment (local, dev, integ, prod), input directory which contains the files the EMR job will process, and the output path which will contain the log files.

```mapreduce.job.reduce.slowstart.completedmaps``` is set to 1.0 (100%) to indicate that the reduce process starts when the map process is 100% complete.

## Creating an EMR Job
1. Create a new class derived from Module.
1. Override process( )

## Running an EMR Job Locally
1. Run the project with the following arguments:
```Program {local|dev|integ|prod} {input-path} {output-path}```
1. In Program.java, update ```getModule()``` to return your new Module-derived class.
1. Place breakpoints

## Deploying an EMR Job to Amazon Web Services
TBD

## Lessons Learned
1. EMR phases (map, reduce) can run for a long time. And in the Amazon environment if your phase doesn't report back (```mapWriteContext() or reduceWriteContext()``) in a "timely" fashion, it will get terminated.
1. Do not create a single data file for the EMR job to process. Create lots of smaller files that can be distributed across multiple machines.