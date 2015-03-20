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

This document defines the Trialverse HTTP API. It is aligned as much as possible with existing standards, especially [SPARQL 1.1 graph store](http://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/) and [SPARQL 1.1 protocol](http://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/).
In addition, we try to adhere to [REST](http://en.wikipedia.org/wiki/Representational_state_transfer) principles, although exceptions may be made to statelessness (for browser sessions) and to HATEOAS (especially where it is unclear how RDF could be used to specify the actions that are available on a resource).

Even though this main specification is sufficient to enable clients to query and manipulate data in Trialverse, the versioning mechanism is centrally important to the correct implementation of this API.
Therefore, versioning is discussed in [an accompanying document](versioning.html).
This should help verify that the Trialverse API can indeed be implemented using the proposed versioning mechanism and service.
The layered architecture looks like:

```
Client  <-->  Trialverse  <-->  Versioning
```

Base URI
--------

All URLs in the document are relative to a base URI. By default, this is `https://trialverse.org`.
Turtle snippets, in particular, should be read as if `@base <https://trialverse.org/> .` is present.

ID pattern
----------

In some cases, the API allows user specified IDs. Such IDs may only contain lowercase ASCII characters, digits, and dashes ('-').

Personal workspace
------------------

TBD: versioning of personal workspaces.

TBD: supported vocabularies / information for profiles, and the updateable subset.
The most likely vocabulary is [FOAF](http://xmlns.com/foaf/spec/).

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

Returns a dataset description using the [VoID](http://www.w3.org/TR/2011/NOTE-void-20110303/) vocabulary:

```turtle
</users/:user-id/datasets/:dataset-id> a void:Dataset ;
  dcterms:title "My dataset" ;
  dcterms:description "It contains a bunch of studies" ;
  dcterms:creator </users/:user-id> ;
  dcterms:date "2015-02-05T15:02:22+0100"^^xsd:dateTime ;
  dcterms:modified "2015-02-19T14:55:28+0100"^^xsd:dateTime ;
  void:sparqlEndpoint </users/:user-id/datasets/:dataset-id/query> ,
                      </users/:user-id/datasets/:dataset-id/update> ;
  void:subset </users/:user-id/datasets/:dataset-id/graphs/:graph-id> .

</users/:user-id/datasets/:dataset-id/graphs/:graph-id> a void:Dataset ;
  dcterms:subject <some-study-uri> .
```

The query endpoints referenced in the dataset description must conform to the [SPARQL Service Description](http://www.w3.org/TR/sparql11-service-description/) requirements (that is, they respond to a GET request without query parameters by returning a service description in RDF).
The graph above is derived from the versioning meta-data, combined with triples from a meta-data graph, in this case containing:

```turtle
</users/:user-id/datasets/:dataset-id>
  dcterms:title "My dataset" ;
  dcterms:description "It contains a bunch of studies" ;

</users/:user-id/datasets/:dataset-id/graphs/:graph-id>
  dcterms:subject <some-study-uri> .
```

The `rdf:type`, `dcterms:creator`, `dcterms:date`, `dcterms:modified`, `void:sparqlEndpoint` and `void:subset` properties of the dataset are maintained by Trialverse and can not be changed by the client.
Trying to do so will result in a `400 Bad Request` response (TODO: or `403 Forbidden`?).
Similarly, the `rdf:type` of any `void:subset` of the dataset may not be changed by the client.

Creating a dataset can be done through a `POST` to its intended new location.
For this to succeed, the new location must match `/users/:user-id/datasets/:dataset-id`, where `:user-id` is the current user (or the user that authorized API access in case of API key authentication), and `:dataset-id` matches the allowed ID pattern.
The former results in a `403 Forbidden` response, and the latter in a `400 Bad Request` response.
Because the dataset URI is known at the time the request is made, dataset meta-data in an RDF format can be specified in the request body (see [conventions](conventions.html)).

```
/users/:user-id/datasets/:dataset-id/query
```

SPARQL protocol query endpoint (read only) for the latest version of this dataset. May `302 Found` to a specific version (see the Versioning section).

```
/users/:user-id/datasets/:dataset-id/update
```

SPARQL protocol update endpoint for the latest version of this dataset.
Expects the request headers `X-EventSource-Title` (required) and `X-EventSource-Description` (optional) containing a human-readable short- and long-form description of the change (see [Versioning](versioning.html)).
The server returns a response header `X-Trialverse-Version` containing the URI of the created version.
Note that this URI will be different from the one specified under [Versioning](versioning.html), as the Trialverse API exposes versions through a different endpoint.
In general, SPARQL update queries should not be used to create or delete graphs, and the store's behaviour is not defined in that case.

Both the `./query` and `./update` endpoint define `/users/:user-id/datasets/:dataset-id/graphs/` as their base URI, and hence graphs can be referred to using only their ID in SPARQL: `<:graph-id>`.

```
/users/:user-id/datasets/:dataset-id/graphs/:graph-id
```

SPARQL graph store API for the latest version of this dataset, using the direct graph identification pattern (i.e. the graph URI resolves to an RDF document of the graph).
POST, PUT, and DELETE requests must specify the `X-EventSource-Title` header and may specify the `X-EventSource-Description` header.
The response to all successful requests contains an `X-Trialverse-Version` header indicating the version returned or the newly created version.

Creating graphs
---------------

Graphs can be created using a POST request to the graph's desired URI.

When creating or replacing graphs in a dataset, it is necessary to know the core subject of a graph (e.g. we want to know which study is described in the graph).
This could be done by giving the subject the same URI as the graph, but this has two problems:

 - It becomes impossible to say something about the graph itself, without confusing it with the subject.
 - When copying the graph to a graph with a new URI, one would have to replace all occurences of the old URI in the copy with the URI of the new graph. This would interfere with the goal of having a clear audit trail.

Therefore, we choose not to assign the same URI to the graph and its subject.
Instead, the subject of the graph is associated with the graph using the `dcterms:subject` predicate in the dataset meta-data graph.
The topic URI is also returned using the `Link:` HTTP header, again using the `dcterms:subject` predicate:

```http
HTTP/1.1 200 Ok
Link: <some-study-uri>;rel=http://purl.org/dc/terms/subject
...
```

When creating a graph using the SPARQL graph store API, an initial value for the topic URI must be determined. There are two possible mechanisms:

 - The topic URI is provided using the HTTP header `X-Trialverse-Subject`
 - The topic URI is inferred from the RDF content, as the unique root of the RDF graph

It is likely that the latter is a good default, and the former will be needed for more advanced use cases.
If the `X-TrialVerse-Subject` header is not present, and the graph has no unique root, the server responds with `400 Bad Request`.

In the future, not all Trialverse graphs may describe studies. The type of resource described in the graph is determined from the content of the graph itself.
In particular, a graph describing a study will have a `dcterms:subject ?subject`, where `?subject` has an `rdf:type` that indicates it is a study.
For the precise class to be used we defer to the Trialverse data model specification.

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
The mementos should just be the ones at `/users/:user-id/datasets/:dataset-id/versions/:version-id/graph/:graph-id`.

Copying graphs from other datasets
----------------------------------

```
/users/:user-id/datasets/:dataset-id/graphs/:new-graph-id?copyFrom=$URI
```

The meta-data associated with the graph (importantly, the `dcterms:subject`) is copied along with it.

Staging changes
---------------

TODO: needed for commit pages. Start by creating a "working copy" of a graph, then make changes to it, and retrieve those changes as a set of additions and retractions. Note that these do not need to be portable, and so can make reference to blank nodes. As such, the format need not be TurtlePatch, and the graph need not be skolemized (see [Versioning](versioning.html)).

Authentication
--------------

Authentication to use the API can happen in two ways:

 - By logging in to the Trialverse application using an OAuth provider, establishing a session through a cookie.
 - Through a previously generated API key, supplied in the request URL.

API keys may be limited to access a single dataset, and may have an expiration date.

Cross site request forgery
--------------------------

Stateless CSRF using a token generated by the client ([source](http://www.javacodegeeks.com/2014/10/stateless-spring-security-part-1-stateless-csrf-protection.html), approach 3).

Provenance information
----------------------

TODO: W3C prov.

Searching for datasets / studies
--------------------------------

TODO: define a mechanism. Should this be versioned too? Probably not.
