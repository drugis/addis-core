---
layout: default
title: Interaction model 
---

The interaction model is based on the following principles:

 - Event sourcing: a record of all changes to the knowledge base is kept, allowing the state of knowledge at any point in time to be reconstructed.
 - Account locality: it should always be easy to identify the set of statements made from a specific account. This may need to be even more fine-grained to allow for organizations, groups, or computational agents.
 - Account merging: it may happen that accounts are merged; this should happen rarely and often one of the accounts will merely be a "shell" account, but it could happen that substantial content has been contributed from both accounts. This content should be merged from that moment on. Merging accounts is a non-reversible action.
 - Topic locality: certain topics (e.g. individual studies, publications, etc.) are recognized as main units of interest, and are treated in an isolated manner. This makes it easy to build pages on each topic, and to carry out separate discussions on each topic.
 - Topic merging: sometimes different identifiers may turn out to describe the same topic, in which case their corresponding topic datasets will need to be merged. This must be a reversible operation so that mistakes can be corrected.

This should be sufficient for any person to create datasets on their own. Then there

 - Limited aggregation: limited aggregation of data across accounts is possible within the scope of a project, using simple rules set out by the project (chosen from a finite list of possibilities?). Q: do we require that statements get added to projects explicitly?
 - Consensus aggregation: overall aggregation of knowledge

### Meta-data model ###

 - Account information: event sourced, uniquely defined at any point in time, once accounts have been merged this is irreversible.
    - Once accounts have been merged, a merged view on any account-local datasets will be presented from then on. It will still be possible to request states of the fact base prior to the merge, where these will be separate.
 - Topics: there are several "classes" of topics, e.g. :Study, :Publication, etc. 
    - Each class has a corresponding meta-data set that deals with the identity of topics, i.e. when two identifiers refer to the same thing.
    - Each meta-data set is event sourced, and account local.
    - Each topic instance is described by a data set that is event sourced, account local, and topic local.
    - Once topic instances have been found identical, their separate data sets are merged.

Now, to construct what account x had claimed about topic y by time t:

 1. (Re)construct the accounts data at time t.
 2. Find the set X of accounts identical to x at time t.
 3. Find the topic class z corresponding to topic y.
 4. (Re)construct the meta-data corresponding to class z, according to accounts X, at time t.
 5. Find the set Y of topics identical to y at time t.
 6. (Re)construct the data corresponding to topics Y, merged into y, according to accounts X, at time t. 

It appears that in this model, merging of accounts can be implemented at any later date without breaking the data model (i.e. this feature can be postponed), as long as steps (4) and (6) are similar for a single account x or a set of accounts X.

Q: How is querying at various time points / on various topics handled? Is this transparent at the graph level and handled though multiple endpoints or will there be a single endpoint and will the client be responsible for constructing the correct graph names? Most of the time the client would be most interested in a single coherent view of the data, given a time t and a 'viewpoint', which may be a single user, a project, or the consensus? An alternative might be to use SPIN?

### Making statements ###

A set of statements is made as a bag of triples with a certain context (e.g. describing a certain study).
Formally, this is represented as a named graph containing the triples, plus annotations on the named graph, i.e. using the [graph annotation pattern](http://patterns.dataincubator.org/book/graph-annotation.html) as well as the [graph per source pattern](http://patterns.dataincubator.org/book/graph-per-source.html); see also the [graph per resource](http://patterns.dataincubator.org/book/graph-per-resource.html) and [graph per aspect](http://patterns.dataincubator.org/book/graph-per-aspect.html) patterns.
When first making a statement this can simply entail POSTing a set of triples to an endpoint, e.g. the following facts about myself:

```turtle
@prefix foaf: <http://xmlns.com/foaf/0.1/>.
@prefix person: <http://test.drugis.org/person/> .

person:Gert a foaf:Person ;
  foaf:name "Gert van Valkenhoef" ;
  foaf:homepage <http://www.ai.rug.nl/~valkenhoef/> ;
  foaf:mbox <mailto:gertvv@hccnet.nl> .
```

Which could be stored at `<http://test.drugis.org/claim/001>`.
However, later I want to amend my record and remove the now invalid e-mail address given above, while also adding another home page.
This can be encapsulated by a set of added and a set of revoked claims.

Additions `<http://test.drugis.org/claim/002>`:

```turtle
@prefix foaf: <http://xmlns.com/foaf/0.1/>.
@prefix person: <http://test.drugis.org/person/> .

person:Gert
  foaf:homepage <http://gertvv.nl/> ;
  foaf:mbox <mailto:gert@gertvv.nl> .
```

Retractions `<http://test.drugis.org/retract/002>`:

```turtle
@prefix foaf: <http://xmlns.com/foaf/0.1/>.
@prefix person: <http://test.drugis.org/person/> .

person:Gert
  foaf:mbox <mailto:gertvv@hccnet.nl> .
```

The following meta-data describes these statement sets:

```turtle
@prefix dc:   <http://purl.org/dc/elements/1.1/>.
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#>.

@prefix stmt: <http://test.drugis.org/ontology/statements#> .
@prefix claim: <http://test.drugis.org/claim/> .
@prefix retract: <http://test.drugis.org/retract/> .
@prefix closure: <http://test.drugis.org/closure/> .
@prefix person: <http://test.drugis.org/person/> .

<http://test.drugis.org/delta/001> a stmt:Delta ;
  stmt:claims claim:001 ;
  stmt:closure closure:001 ;
  stmt:about person:Gert ;
  dc:date "2003-04-17T14:35:03"^^xsd:dateTime ;
  dc:creator person:Gert .

<http://test.drugis.org/delta/002> a stmt:Delta ;
  stmt:claims claim:002 ;
  stmt:retractions retract:002 ;
  stmt:closure closure:002 ;
  stmt:about person:Gert ;
  dc:date "2008-05-13T14:35:03"^^xsd:dateTime ;
  dc:creator person:Gert . 
```

Now, we can retrieve an ordered list of all statements made by me about me, as follows:

```sparql
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

PREFIX stmt: <http://test.drugis.org/ontology/statements#>
PREFIX claim: <http://test.drugis.org/claim/>
PREFIX retract: <http://test.drugis.org/retract/>
PREFIX person: <http://test.drugis.org/person/>

SELECT * WHERE {
  GRAPH <http://test.drugis.org/meta> {
    ?delta dc:creator person:Gert .
    ?delta stmt:about person:Gert .
    ?delta dc:date ?date .
    OPTIONAL { ?delta stmt:claims ?claims } .
    OPTIONAL { ?delta stmt:retractions ?retractions } .
  }
} ORDER BY ASC(?date)
```

```
------------------------------------------------------------------------------------------------------
| delta                              | date                                | claims    | retractions |
======================================================================================================
| <http://test.drugis.org/delta/001> | "2003-04-17T14:35:03"^^xsd:dateTime | claim:001 |             |
| <http://test.drugis.org/delta/002> | "2008-05-13T14:35:03"^^xsd:dateTime | claim:002 | retract:002 |
------------------------------------------------------------------------------------------------------
```

Say we have constructed a closure over all deltas up until `delta:001`, i.e. `closure:001` is just `delta:001`. Then, the following
SPARQL computes the closure up until `delta:002`, i.e. `closure:002`:

```sparql
PREFIX claim: <http://test.drugis.org/claim/>
PREFIX retract: <http://test.drugis.org/retract/>
PREFIX closure: <http://test.drugis.org/closure/>
PREFIX person: <http://test.drugis.org/person/>

CONSTRUCT { ?o ?p ?s }
WHERE {
 {
  { GRAPH closure:001 { ?o ?p ?s } } MINUS { GRAPH retract:002 { ?o ?p ?s } } 
 } UNION {
  GRAPH claim:002 { ?o ?p ?s }
 }
}
```

```turtle
@prefix person: <http://test.drugis.org/person/> .
@prefix closure: <http://test.drugis.org/closure/> .
@prefix claim: <http://test.drugis.org/claim/> .
@prefix retract: <http://test.drugis.org/retract/> .

person:Gert  a  <http://xmlns.com/foaf/0.1/Person> ;
        <http://xmlns.com/foaf/0.1/homepage>
                <http://gertvv.nl/> , <http://www.ai.rug.nl/~valkenhoef/> ;
        <http://xmlns.com/foaf/0.1/mbox>
                <mailto:gert@gertvv.nl> ;
        <http://xmlns.com/foaf/0.1/name>
                "Gert van Valkenhoef" .
```

By recursively applying this query, we can compute what I had said about myself at any point in time.
This can be done explicitly by performing this query and depositing the results, or it can be implemented implicitly e.g. by extending Jena to automatically construct the closure graphs in memory when requested.
A clever implementation could cache closures that are requested frequently and discard unused ones.
Essentially, this is an implementation of the [event sourcing pattern](http://martinfowler.com/eaaDev/EventSourcing.html) and is loosely based on the [Datomic Information Model](http://www.infoq.com/articles/Datomic-Information-Model).

The following query would get my e-mail address as up-to-date on January 23, 2006:

```sparql
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX stmt: <http://test.drugis.org/ontology/statements#>
PREFIX closure: <http://test.drugis.org/closure/>
PREFIX person: <http://test.drugis.org/person/>

SELECT ?mbox ?graph ?date
WHERE {
  {
    SELECT ?graph ?date WHERE {
      GRAPH <http://test.drugis.org/meta> {
        ?delta a stmt:Delta ;
               dc:creator person:Gert ;
               stmt:about person:Gert ;
               dc:date ?date ;
               stmt:closure ?graph .
        FILTER (?date < "2006-01-23T00:00:00"^^xsd:dateTime)
      }
    } ORDER BY DESC(?date) LIMIT 1
  }
  GRAPH ?graph {
    person:Gert foaf:mbox ?mbox .
  }
}
```

**Implementation in Jena**: Jena already supports implicit graphs defined as unions, differences, and intersections of existing graphs. This could be used to provide implicit implementations of the closure graphs.
(NOTE: these have been deprecated because they have poor test coverage and are not used internally by Jena, may need some work: [#JENA-59](https://issues.apache.org/jira/browse/JENA-59), [mailing list thread](http://mail-archives.apache.org/mod_mbox/jena-users/201401.mbox/%3CCAHwiV1E_A7Cf1zom-2JTVg0N1m+ZJa11LNkh=4ih=1B8d5PsAw@mail.gmail.com%3E)).

### Merging topics ###

Now presume that another record has been created describing me, e.g. because I logged in using my work account:

```turtle
@prefix foaf: <http://xmlns.com/foaf/0.1/>.
@prefix person: <http://test.drugis.org/person/> .

person:ValkenhoefGHMvan a foaf:Person ;
  foaf:name "Valkenhoef, G.H.M. van" ;
  foaf:mbox <mailto:g.h.m.van.valkenhoef@umcg.nl> .
```

I might assert that these identifiers refer to the same person:

```turtle
@prefix person: <http://test.drugis.org/person/> .

person:ValkenhoefGHMvan owl:sameAs person:Gert .
```

What should happen at this point?
Inference (see [Jena inference support](http://jena.apache.org/documentation/inference/))?
It gets a little confusing because here the topic and the creator are both being merged...

I think this is what should happen:

 - Graphs by both persons describing the same topic should be merged, resulting in a new closure. This might be a ```stmt:MergeCreators```.
 - Graphs about both persons should be merged, resulting in a new closure. This might be a ```stmt:MergeTopics```.

Now, what happens if the statement resulting in the merge is retracted? Is there a simple way to disentangle? Yes - but this may result in multiple closures per ```stmt:Delta```. This means there needs to be a way of identifying closures that does not depend solely on the identity of the deltas.

The solution is simple: the closures are generated under a certain version of the relevant meta-graph; each closure can simply be identified by the chain of statements it is constructed from. The following query calculates all deltas under a specific version of the persons graph:

```sparql
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX owl:  <http://www.w3.org/2002/07/owl#>

PREFIX stmt: <http://test.drugis.org/ontology/statements#>
PREFIX claim: <http://test.drugis.org/claim/>
PREFIX retract: <http://test.drugis.org/retract/>
PREFIX person: <http://test.drugis.org/person/>

SELECT *
WHERE {
  GRAPH <http://test.drugis.org/meta> {
    BIND(claim:004 AS ?personsGraph)
    ?delta dc:creator person:Gert . # NOTE: not merging the creator, this would be a different operation!
    ?delta stmt:about ?topic .
    GRAPH ?personsGraph {
      ?topic (owl:sameAs | ^owl:sameAs)* person:Gert .
    }
    ?delta dc:date ?date .
    OPTIONAL { ?delta stmt:claims ?claims }
    OPTIONAL { ?delta stmt:retractions ?retractions }
  }
} ORDER BY ASC(?date)
```

### Merging accounts ###

### Relevant technologies ###

 - [Apache Jena](https://jena.apache.org/)
 - [Apache Jena Fuseki](http://jena.apache.org/documentation/serving_data/)

 - [Turtle](http://www.w3.org/TR/turtle/)
 - [SPARQL 1.1](http://www.w3.org/TR/sparql11-overview/)

 - [RIF](http://www.w3.org/TR/rif-overview/)

 - [SPIN](http://spinrdf.org/)
 - [SPIN W3C member submission](http://www.w3.org/Submission/2011/SUBM-spin-overview-20110222/)
 - [SPARQLMotion](http://sparqlmotion.org/)
 - [SWP](http://uispin.org/)

 - [Views over RDF datasets](http://arxiv.org/abs/1211.0224)
 - [Selecting materialized views for RDF data](http://dl.acm.org/citation.cfm?id=1927244)
 - [Networked Graphs](http://www.uni-koblenz-landau.de/campus-koblenz/fb4/west/Research/systems/NetworkedGraphs)
    - [paper](http://dl.acm.org/citation.cfm?id=1367577)
 - [Temporal views over RDF data](http://wwwconference.org/www2008/papers/pdf/p1131-manjunath.pdf)
 - [Trusty URIs](http://arxiv.org/pdf/1401.5775v3.pdf)

 - [Flake](http://boundary.com/blog/2012/01/12/flake-a-decentralized-k-ordered-unique-id-generator-in-erlang/) [Erlang implementation](https://github.com/boundary/flake) [Java implementation](https://github.com/mumrah/flake-java)
 - [Snowflake](https://blog.twitter.com/2010/announcing-snowflake)
 - [k-ordering](http://ci.nii.ac.jp/naid/110002673489/)
