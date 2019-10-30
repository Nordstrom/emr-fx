#!/usr/local/bin/python

#################################################################################
# aws emr cli
# Usage: ./aws-emrV2-cli.py -e prod -p myProfile -i s3://bucket/path-to-data-files/ -n myEMRName -u myUserName
# 		-e environment dev|prod (default is dev) [optional]
# 		-p aws profile to use to deploy [optional]
#		-i input full s3:// path
#		-n emr job name (default is nordstrom-emr) [optional]
#       -u user (default is test) [optional]
#################################################################################

import subprocess
import sys
import os
from optparse import OptionParser

PROFILE="default"
NAME="nordstrom-emr"
INPUT="s3://{s3-input-location}/"
OUTPUT="s3://{s3-output-location}/"
USER="test"
EMR_env="dev"
EMR_Cores=2

# Default to non-prod
EMR_subnet="subnet-TBD"
EMR_MasterSG="sg-TBD"
EMR_ServiceAccessSG="sg-TBD"
EMR_SlaveSG="sg-TBD"
EMR_ServiceRole="arn:aws:iam::{your-AWS-account}:role/EMR_DefaultRole"

EMR_InstanceProfile="EMR_EC2_DefaultRole"
EMR_InstanceType="m3.2xlarge"

# Parse input from command-line
parser = OptionParser()
parser.add_option("-e",action="store",type="string",dest="env")
parser.add_option("-p",action="store",type="string",dest="profile")
parser.add_option("-i",action="store",type="string",dest="input")
parser.add_option("-o",action="store",type="string",dest="output")
parser.add_option("-n",action="store",type="string",dest="name")
parser.add_option("-u",action="store",type="string",dest="user")
parser.add_option('-r',action='store',type='string',dest='role')
parser.add_option('--ep',action='store',type='string',dest='ec2_profile')

(options,sys.argv) = parser.parse_args(sys.argv)

if options.env:
    print("Setting environment: {}".format(options.env))
    EMR_env=options.env
if options.profile:
    print("Setting profile: {}".format(options.profile))
    PROFILE=options.profile
if options.input:
    print("Setting input: {}".format(options.input))
    INPUT=options.input
if options.name:
    print("Setting name: {}".format(options.name))
    NAME=options.name
if options.user:
    print("Setting user: {}".format(options.user))
    USER=options.user
if options.role:
    print("Setting role: {}".format(options.role))
    EMR_ServiceRole = options.role
if options.ec2_profile:
    print("Setting role: {}".format(options.ec2_profile))
    EMR_InstanceProfile = options.ec2_profile
if options.output:
    print("Setting output: {}".format(options.output))
    OUTPUT = options.output


print("env is " + EMR_env)

# If prod, change base values
if EMR_env == "prod":
    OUTPUT="s3://{s3-output-location}/emr/"
    EMR_Cores=2
    EMR_subnet="subnet-{TBD}"
    EMR_MasterSG="sg-{TBD}"
    EMR_ServiceAccessSG="sg-{TBD}"
    EMR_SlaveSG="sg-{TBD}"
    EMR_ServiceRole="arn:aws:iam::{TBD}:role/EMR_DefaultRole"

EMR_Jar=OUTPUT + "nordstrom-emr-" + USER + ".jar"

# Print relevant data
print("\nProfile: " + PROFILE)
print("Input Path: " + INPUT)
print("JAR file: " + EMR_Jar + "\n")

# Copy JAR file to S3
cmd = "aws --profile " + PROFILE + " s3 cp ../../build/libs/amp-emr-"+USER+".jar "+ OUTPUT
print("\n{}".format(cmd))
try :
    procExe = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    while procExe.poll() is None:
        line = procExe.stdout.readline()
        err = procExe.stderr.readline()
        if err:
            print(err)
        if line:
            print(line)
        print(procExe.stdout.read())
        print(procExe.stderr.read())
except :
    sys.exit(subprocess.CalledProcessError)
c
# Create cluster in AWS
cmd = "aws --profile " + PROFILE \
    + " emr create-cluster --release-label emr-5.14.0 --instance-groups InstanceGroupType=CORE,InstanceCount=" + str(EMR_Cores) \
    + ",InstanceType=" + EMR_InstanceType \
    + " InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge --applications Name=Hadoop --configurations file://./config.json" \
    + " --tags \"Owner={YourEMAIL}\" --enable-debugging --log-uri " + OUTPUT + "logs/" \
    + " --name " + NAME \
    + " --ec2-attributes InstanceProfile=" + EMR_InstanceProfile \
    + ",SubnetId=" + EMR_subnet \
    + ",EmrManagedSlaveSecurityGroup=" + EMR_SlaveSG \
    + ",EmrManagedMasterSecurityGroup=" + EMR_MasterSG \
    + ",ServiceAccessSecurityGroup=" + EMR_ServiceAccessSG \
    + " --service-role " + EMR_ServiceRole \
    + " --steps Name=" + NAME \
    + ",Args=" + EMR_env + "," + INPUT + "," + OUTPUT + ",Jar=" + EMR_Jar \
    + ",ActionOnFailure=CONTINUE,Type=CUSTOM_JAR,MainClass={YourMainClassName} --region us-west-2 --auto-terminate"
print("\n{}".format(cmd))
try :
    procExe = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    while procExe.poll() is None:
        line = procExe.stdout.readline()
        if line:
            print(line)
        err = procExe.stderr.readline()
        if err:
            print("ERROR: {}".format(err))

    remainder_out = procExe.stdout.read()
    remainder_err = procExe.stderr.read()    
    if remainder_out:
        print(remainder_out)
    if remainder_err:
        print(remainder_err)

except :
    sys.exit(subprocess.CalledProcessError)
