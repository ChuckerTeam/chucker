#!/bin/bash

# GraphQL Schema Update
# Open Android Studio terminal
# Run command `./downloadApolloSchema.sh`
# Format the file (`ctrl+option+i` && `option+cmd+L`), if not formatted
# Replace `"null"` to `null`
# Go to the top of the file and click the play icon ▶️ "Generate GraphQL SDL schema file". (It normally can be visible on Android Studio and IntelliJ IDE)
# Be happy.

./gradlew downloadApolloSchema --endpoint="https://rickandmortyapi.com/graphql" --schema="sample/src/main/graphql/com/chuckerteam/chucker/sample/schema.json"
