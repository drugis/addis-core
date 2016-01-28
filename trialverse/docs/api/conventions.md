---
layout: default
title: Dataset conventions
---

Fills in specific conventions of how Trialverse datasets subsist on the versioning mechanism defined previously.

Dataset meta-data
-----------------

Dataset meta-data is stored in the default graph of the dataset in the versioning store.
In addition, some of it is derived from the versioning meta-data.

 - the `dcterms:date` of the dataset in Trialverse is the `dcterms:date` of the dataset in the versioning store.
 - the `dcterms:creator` of the dataset in Trialverse is the `dcterms:creator` of the dataset in the versioning store.
 - the `dcterms:modified` of the dataset in Trialverse is the `dcterms:date` of the `es:head` version.
 - the `void:subset` of the dataset are the graphs contained in the `es:head` version.
 - the type of the dataset in Trialverse and any `void:subset` is `void:Dataset`.
 - the default graph of the versioning dataset should not contain triples matching these patterns.

It is presented to the user through the default graph of the Trialverse dataset.
This default graph, however, is enriched by additonal meta-data, such as a VoID description as described in the [main specification](index.html).

TODO: define a SPARQL pattern for which triples are allowed to be modified by user queries.
TODO: define whether a non-enriched version is available.

Mapping Trialverse datasets to versioning datasets
--------------------------------------------------

TODO: where do we track which versioning dataset is which Trialverse dataset?

Mapping queries
---------------

When creating a new graph, or when the `X-Trialverse-Subject` header is present on a `POST` or `PUT` request to a graph URI, the resulting transaction on the versioning store affects both the default graph and the graph in question.
Therefore, the request is translated to a SPARQL query as described in the SPARQL graph store specification, with an additional query to update the `dcterms:subject` in the default graph.

Versions
--------

As versions have a known URL pattern in the versioning store, the `:version-id` is taken from that URI and used as the `:version-id` fragment described in the [main specification](index.html).
Note that the `/users/:user-id/datasets/:dataset-id/graphs/:graph-id/versions` route does not list revisions of the graph, but rather the graph as it existed in different versions of the dataset.
The Memento timemaps are based directly on the linear history defined by the `es:previous` predicate in each dataset, both for the dataset and for the graphs contained in it (i.e. graphs change only when the dataset changes).

Creator identification & provenance
-----------------------------------

The versioning store expects a single creator URI.
However, these URIs may potentially represent more complex provenance information, for example that the request was made by a computational agent acting on behalf of a user as authenticated through an API key.
Therefore, a convention is needed on how URIs are generated in these scenarios, and where their descriptions are stored.

TODO: Users versus API keys versus groups.

TODO: [W3C PROV](http://www.w3.org/TR/2013/NOTE-prov-primer-20130430/).

 - Describing histories in W3C PROV
 - Describing relationships among agents in W3C PROV
