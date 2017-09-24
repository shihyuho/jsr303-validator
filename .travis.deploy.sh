#!/bin/bash

gpg --import .travis.key.gpg
mvn versions:set -DnewVersion=${TRAVIS_TAG}
mvn clean deploy -P release --settings .travis.settings.xml