// Copyright (c) 2024 IBM Corporation and others.
// Licensed under Creative Commons Attribution-NoDerivatives
// 4.0 International (CC BY-ND 4.0)
//   https://creativecommons.org/licenses/by-nd/4.0/
//
// Contributors:
//   IBM Corporation
:projectid: concurrency-intro
:page-layout: guide-multipane
:page-duration: 20 minutes
:page-releasedate: 2024-10-31
:page-description: Learn how to run tasks asynchronously or parallelly in Java microservices by using Jakarta Concurrency APIs.
:page-tags: ['jakartaee']
:page-permalink: /guides/{projectid}
:imagesdir: /img/guide/{projectid}
:page-related-guides: ['reactive-rest-client', 'microprofile-rest-client-async']
:common-includes: https://raw.githubusercontent.com/OpenLiberty/guides-common/prod
:source-highlighter: prettify
:page-seo-title: Running tasks asynchronously or parallelly in Java microservices by using Jakarta Concurrency APIs
:page-seo-description: A getting started tutorial with examples on how to run tasks asynchronously or parallelly in Java microservices by using Jakarta Concurrency APIs.
= Running tasks asynchronously or parallelly in Java microservices

[.hidden]
NOTE: This repository contains the guide documentation source. To view the guide in published form, view it on the https://openliberty.io/guides/{projectid}.html[Open Liberty website].

Learn how to run tasks asynchronously or parallelly in Java microservices by using Jakarta Concurrency APIs.

== What you'll learn

Introduction

- explain Jakarta Concurrency APIs
- describe the application

image::architecture.png[Application architecture where inventory service uses the Jakarta Concurrency APIs to call the system service.,align="center"]


// =================================================================================================
// Getting started
// =================================================================================================
[role='command']
include::{common-includes}/gitclone.adoc[]

=== Try what you'll build

Polish the following instruction.

Navigate to the finish directory.
```
cd finish
```

Start `inventory` service in dev mode:
```
mvn -pl inventory liberty:run
```

Start `system` service in dev mode:
```
mvn -pl system liberty:run
```

Visit http://localhost:9080/openapi/ui for inventory REST APIs that perform asynchronous or parallel tasks

== Creating asynchronous tasks in the InventoryManager bean

- start the dev mode
- create the InventoryAsyncTask utility class
  - create and explain asynchronous task
- replace the InventoryResource class
  - add/update REST endpoint to call asynchronous task

== Calling the system service in parallel

- start the dev mode
- replace the InventoryAsyncTask utility class
  - create and explain parallel task
- replace the InventoryResource class
  - add/update REST endpoint to call parallel task  

== Running the application

- similar to the https://openliberty.io/guides/cdi-intro.html#running-the-application

== Testing the inventory application

- similar to https://openliberty.io/guides/cdi-intro.html#testing-the-inventory-application
- explain the test

=== Running the tests

- similar to https://openliberty.io/guides/cdi-intro.html#running-the-tests
- stop the dev mode of the system and inventory services

== Great work! You're done!

You have just ... by using Jakarta Concurrency APIs in Open Liberty.

include::{common-includes}/attribution.adoc[subs="attributes"]
