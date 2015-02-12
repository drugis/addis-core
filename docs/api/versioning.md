---
layout: default
title: Versioning mechanism
---

Defines a mechanism for versioning of RDF datasets, which are collections of graphs.
This serves as a back-end to the datasets offered through the Trialverse API.
It is less specific and constrained in a number of ways.
Specifically, it is not concerned with authentication or authorization and will execute any request given to it, as long as it is a valid request.

Creating datasets
-----------------

A new dataset is created though a POST request to /datasets.
The X-EventSource-Author header can be used to specify a URI identifying the author.

```
POST /datasets HTTP/1.1
Host: example.com
X-EventSource-Author: http://example.com/GreenGoblin
```

This will result in a `201 Created response` indicating both the location of the new dataset and the initial version ID.

```
HTTP/1.1 201 Created
Location: http://example.com/datasets/ubb245f8sz
X-EventSource-Version: http://example.com/versions/3ucq3j5c7u
```

Updating and querying datasets
------------------------------

Under `/datasets/:dataset-id` reside a SPARQL protocol query (`./query`) and update (`./update`) endpoint, as well as a SPARQL graph store (`./data`) using the indirect graph identification pattern.
The protocol is modified in three ways:

 1. All requests can specify the `X-Accept-EventSource-Version` header, and the server will `Vary: X-Accept-EventSource-Version`.
For requests that do not alter the state (i.e. requests to the query endpoint and `GET` requests to the graph store), the header indicates that the contents of a previous version are being queried.
For requests that do alter state, the header indicates that the requests expects the latest version to be the version specified in the header.
If it does not, the request fails with a `409 Conflict` response.
If the header is not specified, the action is executed against the latest version.

 2. All write requests can specify the `X-EventSource-Author` header to set the author attribute of the new version.

 3. All requests return a `X-EventSource-Version` header. For read requests this indicates the version being returned, while for write requests it indicates the new version created.

Example
-------

Say the Green Goblin creates a dataset to state that, in fact, Peter Parker goes by the names "Peter Parker" and "Spiderman".

To do this, we first create the new dataset:

```
POST /datasets HTTP/1.1
Host: example.com
X-EventSource-Author: http://example.com/GreenGoblin
```

And the server responds with a newly created dataset, and indicates the initial version ID:

```
HTTP/1.1 201 Created
Location: http://example.com/datasets/ubb245f8sz
X-EventSource-Version: http://example.com/versions/3ucq3j5c7u
```

After this, the triple store looks like:

```turtle
@prefix dc:   <http://purl.org/dc/elements/1.1/> .
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

@prefix es: <http://drugis.org/eventSourcing/es#> .
@prefix dataset: <http://example.com/datasets/> .

es:log {

  dataset:ubb245f8sz a es:Dataset ;
    dc:date "2014-09-24T12:40:03+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/GreenGoblin> ;
    es:head version:3ucq3j5c7u .

  version:3ucq3j5c7u a es:DatasetVersion ;
    dc:date "2014-09-24T12:40:03+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/GreenGoblin> .

}
```

Then, we post new contents for the graph describing Peter Parker:

```
POST /datasets/ubb245f8sz/data?graph=http://example.com/PeterParker HTTP/1.1
Host: example.com
Content-Type: text/turtle
X-EventSource-Author: http://example.com/GreenGoblin
X-EventSource-Previous: http://example.com/versions/3ucq3j5c7u

@prefix foaf: <http://xmlns.com/foaf/0.1/> .

<http://example.com/PeterParker> a foaf:Person ;
  foaf:name "Peter Parker", "Spiderman" .
```

The response has no content, and contains a header indicating the created version:

```
HTTP/1.1 204 No Content
X-EventSource-Version: http://example.com/versions/7wi4xglx1c
```

Now, Peter Parker wants to dispute this claim made by the Green Goblin, and starts by creating a copy of his dataset:

```
POST /datasets?copyOf=http://example.com/versions/7wi4xglx1c HTTP/1.1
X-EventSource-Author: http://example.com/PeterParker
```

The response, again, indicates the location and version of the newly created dataset:

```
HTTP/1.1 201 Created
Location: http://example.com/datasets/qmk2x16nz1
X-EventSource-Version: http://example.com/versions/7wi4xglx1c
```

Then Peter procedes to run a SPARQL update query to set the record straight:

```
POST /datasets/qmk2x16nz1/update HTTP/1.1
Content-Type: application/sparql-query
X-EventSource-Author: http://example.com/PeterParker
X-EventSource-Previous: http://example.com/versions/7wi4xglx1c

PREFIX foaf: <http://xmlns.com/foaf/0.1/>

DELETE DATA {
  GRAPH <http://example.com/PeterParker> {
    <http://example.com/PeterParker> foaf:name "Spiderman"
  }
};
INSERT DATA {
  GRAPH <http://example.com/Spiderman> {
    <http://example.com/Spiderman> a foaf:Person ;
      foaf:name "Spiderman" .
  }
  GRAPH <http://example.com/PeterParker> {
    <http://example.com/PeterParker> foaf:homepage <http://www.okcupid.com/profile/PeterParker> .
  }
}
```

Which results in a newly created version:

```
HTTP/1.1 204 No Content
X-EventSource-Version: http://example.com/versions/g05ri5qvvq
```


### Complete turtle of final store

```turtle
@prefix dc:   <http://purl.org/dc/elements/1.1/> .
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .

@prefix es: <http://drugis.org/eventSourcing/es#> .

@prefix dataset: <http://example.com/datasets/> .
@prefix version: <http://example.com/versions/> .
@prefix revision: <http://example.com/revisions/> .
@prefix assert: <http://example.com/assertions/> .
@prefix retract: <http://example.com/retractions/> .

es:log {

  dataset:qmk2x16nz1 a es:Dataset ;
    dc:date "2014-09-24T12:49:18+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/Spiderman> ;
    es:head version:g05ri5qvvq .

  dataset:ubb245f8sz a es:Dataset ;
    dc:date "2014-09-24T12:40:03+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/GreenGoblin> ;
    es:head version:7wi4xglx1c .

  version:g05ri5qvvq a es:DatasetVersion ;
    dc:date "2014-09-24T12:58:16,835290832+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/Spiderman> ;
    es:previous version:7wi4xglx1c ;
    es:has_graph_version [
      es:graph <http://example.com/Spiderman> ;
      es:revision revision:302431f4-43e8-11e4-8745-c72e64fa66b1 .
    ] ;
    es:has_graph_version [
      es:graph <http://example.com/PeterParker> ;
      es:revision revision:44ea0618-43e8-11e4-bcfb-bba47531d497 .
    ] .

  version:7wi4xglx1c a es:DatasetVersion ;
    dc:date "2014-09-24T12:45:25,048366032+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/GreenGoblin> ;
    es:previous version:3ucq3j5c7u ;
    es:has_graph_version [
      es:graph <http://example.com/PeterParker> ;
      es:revision revision:38fc1de7a-43ea-11e4-a12c-3314171ce0bb .
    ] .

  version:3ucq3j5c7u a es:DatasetVersion ;
    dc:date "2014-09-24T12:40:03+0000"^^xsd:dateTime ;
    dc:creator <http://example.com/GreenGoblin> .

  revision:302431f4-43e8-11e4-8745-c72e64fa66b1 a es:Revision ;
    es:event version:g05ri5qvvq ;
    es:assertions assert:302431f4-43e8-11e4-8745-c72e64fa66b1 .

  revision:44ea0618-43e8-11e4-bcfb-bba47531d497 a es:Revision ;
    es:event version:g05ri5qvvq ;
    es:previous revision:38fc1de7a-43ea-11e4-a12c-3314171ce0bb ;
    es:assertions assert:44ea0618-43e8-11e4-bcfb-bba47531d497 ;
    es:retractions retract:44ea0618-43e8-11e4-bcfb-bba47531d497 .

  revision:38fc1de7a-43ea-11e4-a12c-3314171ce0bb a es:Revision ;
    es:event version:7wi4xglx1c ;
    es:assertions assert:844908ec-43eb-11e4-ac51-6b523949084e .

}

assert:844908ec-43eb-11e4-ac51-6b523949084e {

  <http://example.com/PeterParker> a foaf:Person ;
    foaf:name "Peter Parker", "Spiderman" .

}

retract:44ea0618-43e8-11e4-bcfb-bba47531d497 {

  <http://example.com/PeterParker> foaf:name "Spiderman" .

}

assert:44ea0618-43e8-11e4-bcfb-bba47531d497 {

  <http://example.com/PeterParker> foaf:homepage <http://www.okcupid.com/profile/PeterParker> .

}

assert:302431f4-43e8-11e4-8745-c72e64fa66b1 {

  <http://example.com/Spiderman> a foaf:Person;
    foaf:name "Spiderman" .

}
```
