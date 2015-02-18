---
layout: default
title: Notes
---

Implementation notes
--------------------

 - Base64 encoding: [Apache Commons Codec](http://commons.apache.org/proper/commons-codec/)
 - Flake IDs: [Fauxflake](https://github.com/rholder/fauxflake)
 - [Google Guava Cache](https://code.google.com/p/guava-libraries/wiki/CachesExplained)

Alternative API token approaches
--------------------------------

 - [Principles for Standardized REST Authentication](http://broadcast.oreilly.com/2009/12/principles-for-standardized-rest-authentication.html)
 - [Implementing REST Authentication](http://www.objectpartners.com/2011/06/16/implementing-rest-authentication/)
 - [Stateless authentication](http://www.javacodegeeks.com/2014/10/stateless-spring-security-part-2-stateless-authentication.html)
 - [Implementing HMAC authentication for REST API with Spring Security](http://massimilianosciacco.com/implementing-hmac-authentication-rest-api-spring-security)

Why not use Git?
-----------------

Although this work was in part inspired by Git, and the internals of that system were studied in the process of specifying our own versioning mechanism, our approach diverges due to some important reasons:

 - Our approach to reproducibility requires that we are able to reconstruct the state of a dataset at a previous point in time. Git's fluid branch/merge model makes that impossible.
 - Unlike Git, we allow only a single "head" per dataset, to allow for a simpler mental model of what a dataset actually is. Our audience are not software developers.
 - Adopting single graphs from other datasets, with full meta-data about that operation, is an important use case for us, but not possible in Git.

Future work
-----------

 - Merge strategies
 - Allow multiple deployments of Trialverse (on different URIs) to co-exist, interoperate
 - Allow the versioning backend to be distributed over multiple machines
 - Show how specific forms of collaboration can be implemented based on the basic versioning / merging framework (e.g. multiple data entry).
 - A meta layer for rating of and commenting on datasets, graphs, or specific sets of triples.
 - Making corrections to single graphs / studies, either
   - In the form of specially formatted comments using the "meta layer"
   - In the form of datasets with a special status
 - Renaming and deleting datasets (while preserving history)
