---
layout: default
title: Main specification
---

This document draws on a number of standards and specifications, see

 - [RFC5988: Web Linking](http://tools.ietf.org/html/rfc5988)
 - [RFC5829: Link Relation Types for Simple Version Navigation between Web Resources](http://tools.ietf.org/html/rfc5829)
 - [RFC7089: HTTP Framework for Time-Based Access to Resource States -- Memento](http://www.mementoweb.org/guide/rfc/)
 - [SPARQL 1.1 Service Description](http://www.w3.org/TR/sparql11-service-description/)
 - [Describing Linked Datasets with the VoID Vocabulary](http://www.w3.org/TR/void/)

TODO: use case of making corrections to single study.

Base URI
--------

All URLs in the document are relative to a base URI. By default, this is `https://trialverse.org`.
Turtle snippets, in particular, should be read as if `@base https://trialverse.org` is present.

Personal workspace
------------------

TBD: versioning of personal workspaces.

TBD: supported vocabularies / information for profiles, and the updateable subset.

```
/users/:user-id
```

A personal workspace for each user.
Created when the user is created in the system, for example after sign-up.

TODO: the API should support non-user agents, which may be groups or organizations (e.g. `drugis.org`).
However, these would probably fall under a different resource (e.g. /groups/).

```
GET /users/:user-id
```

Retrieves basic profile information for this user, in an RDF serialization (other content types TBD).
This also includes a list of datasets owned by this user.

```
POST /users/:user-id
```

May be used by the user to update the basic profile information.


Datasets
--------

```
GET /users/:user-id/datasets/:dataset-id
```

Returns a dataset description using the [VoID](http://www.w3.org/TR/2011/NOTE-void-20110303/) and [SPARQL Service Description] vocabularies. TODO: example.

```
/users/:user-id/datasets/:dataset-id/query
```

SPARQL protocol query endpoint (read only) for the latest version of this dataset. May `302 Found` to a specific version (see the Versioning section).

```
/users/:user-id/datasets/:dataset-id/update
```

SPARQL protocol update endpoint for the latest version of this dataset.
Expects a request header `X-Trialverse-Commit-Message` containing a human-readable description of the change.
The server returns a response header `X-Trialverse-Revision` containing the URI of the created version (TODO: could also be a `Link:`).

```
/users/:user-id/datasets/:dataset-id/graphs/:graph-id
```

SPARQL graph store API for the latest version of this dataset.
POST, PUT, and DELETE requests must specify the `X-Trialverse-Commit-Message` header.

The following RDF description of the dataset is returned on `GET /users/:user-id/datasets/:dataset-id`:

```
</users/:user-id/datasets/:dataset-id> a void:Dataset ;
  dcterms:title "My dataset" ;
  dcterms:description "It contains a bunch of studies" ;
  void:sparqlEndpoint </users/:user-id/datasets/:dataset-id/query> ,
                      </users/:user-id/datasets/:dataset-id/update> ;
  void:subset </users/:user-id/datasets/:dataset-id/graphs/:graph-id> .

</users/:user-id/datasets/:dataset-id/graphs/:graph-id> a void:Dataset ;
  dcterms:subject <some-study-uri> .
```

The query endpoints referenced in the dataset description must conform to the SPARQL service description requirements (that is, they respond to a GET request without query parameters by returning a service description in RDF).
The graph above is derived from the versioning meta-data, combined with triples from a meta-data graph, in this case containing:

```
</users/:user-id/datasets/:dataset-id>
  dcterms:title "My dataset" ;
  dcterms:description "It contains a bunch of studies" ;

</users/:user-id/datasets/:dataset-id/graphs/:graph-id>
  dcterms:subject <some-study-uri> .
```

TODO: expand it with time stamps and author information.

TODO: add bibliographic references.

TODO: specify where it can be accessed and updated, and what kind of restrictions apply.

Creating graphs
---------------

When creating or replacing graphs in a dataset, it is necessary to know the core subject of a graph (e.g. we want to know which study is described in the graph).
This could be done by giving the subject the same URI as the graph, but this has two problems:

 - It becomes impossible to say something about the graph itself, without confusing it with the subject.
 - When copying the graph to a graph with a new URI, one would have to replace all occurences of the old URI in the copy with the URI of the new graph. This would interfere with the goal of having a clear audit trail.

Therefore, the subject of the graph is associated with the graph using the `dcterms:subject` predicate in the dataset meta-data graph (TBD). 
The topic URI is also returned using the `Link:` HTTP header, again using the `dcterms:subject` predicate.

When creating a graph using the SPARQL graph store API, an initial value for the topic URI must be determined. There are two possible mechanisms:

 - The topic URI is provided using a HTTP header (TBD)
 - The topic URI is inferred from the RDF content, as the unique root of the RDF graph

It is likely that the latter is a good default, and the former will be needed for more advanced use cases.

Versions of datasets
--------------------

```
/users/:user-id/datasets/:dataset-id
/users/:user-id/datasets/:dataset-id/query
/users/:user-id/datasets/:dataset-id/graphs/:graph-id
```

These resources act as their own timegate (and thus `Vary: Accept-Datetime`).
This is implemented using Memento pattern 1.1 (URI-R=URI-G; 302-Style Negotiation; Distinct URI-M), to maximize the ability to cache responses.
Note: 404 responses may need to include a timegate `Link:` header if the resource used to exist.


```
/users/:user-id/datasets/:dataset-id/versions
```

This is a timemap for the original resource at `/users/:user-id/:dataset-id`.

```
/users/:user-id/datasets/:dataset-id/versions/:version-id
```

This is a memento for the original resource at `/users/:user-id/:dataset-id`.

```
/users/:user-id/datasets/:dataset-id/versions/:version-id/query
```

SPARQL protocol query endpoint (read only) for a specific version of this dataset.
This is a memento for the original resource at `/users/:user-id/:dataset-id/query` (with identical query parameters).
There is no timemap for this resource.

```
/users/:user-id/datasets/:dataset-id/versions/:version-id/graphs/:graph-id
```

SPARQL graph store API, supporting only GET for a specific version of this dataset.
This is a memento for the original resource at `/users/:user-id/datasets/:dataset-id/graphs/:graph-id`.

```
/users/:user-id/datasets/:dataset-id/graphs/:graph-id/versions
```

TODO: should this be a timemap for `/users/:user-id/datasets/:dataset-id/graph/:graph-id`?
The mementos should just be the ones at `/users/:user-id/datasets/:dataset-id/rev/:version-id/graph/:graph-id`.

Copying graphs from other datasets
----------------------------------

```
/users/:user-id/datasets/:dataset-id/graphs/:new-graph-id?copyFrom=$URI
```

The meta-data associated with the graph (importantly, the `dcterms:subject`) is copied along with it.

Staging changes
---------------

TODO: needed for commit pages. Start by creating a "working copy" of a graph, make changes to it, and retrieve those changes in PATCH fromat (see versioning).

Authentication
--------------

Authentication to use the API can happen in two ways:

 - By logging in to the Trialverse application using an OAuth provider, establishing a session through a cookie.
 - Through a previously generated API key, supplied in the request URL.

API keys may be limited to access a single dataset, and may have an expiration date.

Handling of blank nodes
-----------------------

TODO: should this go under implementation of versioning?

Provenance information
----------------------

TODO: W3C prov.

Searching for datasets / studies
--------------------------------

TODO: define a mechanism. Should this be versioned too? Probably not.
