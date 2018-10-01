SHELL := /bin/bash
NAME := jenkins-cwp-quickstart01
OS := $(shell uname)
ARTIFACT_ID = jenkinsfile-runner-demo
VERSION = 256.0-test

all: build

check: build test

build:
	java \
		-jar /opt/cwp/custom-war-packager.jar \
		-configPath packager-config.yml
	git config --get remote.origin.url

test: 
	echo "do nothing"

install:
	echo "do nothing"

clean:
	rm -rf build release

linux:
	echo "do nothing"

.PHONY: release clean
