---
---

Trialverse API specification
============================

Prefix
------

```
export PREFIX=https://trialverse.com
```

Personal workspace
------------------

TBD: versioning of personal workspaces.

TBD: supported vocabularies / information for profiles, and the updatable subset.

```
$PREFIX/:agent-id
```

A personal workspace for each agent.
Created when the agent is created in the system, for example after sign-up.
An agent can be a person, but may also be a group or organization (e.g. `drugis.org`).

```
GET $PREFIX/:agent-id
```

Retrieves basic profile information for this agent, in an RDF serialization (other content types TBD).
This also includes a list of datasets owned by this agent.

```
POST $PREFIX/:agent-id
```

May be used by the agent (or a representative) to update the basic profile information.


Datasets
--------

```
GET $PREFIX/:agent-id/:dataset-id
```

Returns a dataset description using the [VoID](http://www.w3.org/TR/2011/NOTE-void-20110303/) and [SPARQL Service Description] vocabularies. TODO: example.

```
$PREFIX/:agent-id/:dataset-id/query
```

SPARQL protocol query endpoint (read only) for the latest version of this dataset. May `302 Found` to a specific version.

```
$PREFIX/:agent-id/:dataset-id/update
```

SPARQL protocol update endpoint for the latest version of this dataset.
Expects a request header `X-Trialverse-Commit-Message` containing a human-readable description of the change.
The server returns a response header `X-Trialverse-Revision` containing the URI of the created version (TODO: could also be a `Link:`).

```
$PREFIX/:agent-id/:dataset-id/graph/:graph-id
```

SPARQL graph store API for the latest version of this dataset.
POST, PUT, and DELETE requests must specify the `X-Trialverse-Commit-Message` header.

Versions of datasets
--------------------

```
$PREFIX/:agent-id/:dataset-id
$PREFIX/:agent-id/:dataset-id/query
$PREFIX/:agent-id/:dataset-id/graph/:graph-id
```

These resources act as their own timegate (and thus `Vary: Accept-Datetime`).

```
$PREFIX/:agent-id/:dataset-id/rev
```

This is a timemap for the original resource at `$PREFIX/:agent-id/:dataset-id`.

```
$PREFIX/:agent-id/:dataset-id/rev/:version-id
```

This is a memento for the original resource at `$PREFIX/:agent-id/:dataset-id`.

```
$PREFIX/:agent-id/:dataset-id/rev/:version-id/query
```

SPARQL protocol query endpoint (read only) for a specific version of this dataset.
This is a memento for the original resource at `$PREFIX/:agent-id/:dataset-id/query` (with identical query parameters).
There is no timemap for this resource.

```
$PREFIX/:agent-id/:dataset-id/rev/:version-id/graph/:graph-id
```

SPARQL graph store API, supporting only GET for a specific version of this dataset.
This is a memento for the original resource at `$PREFIX/:agent-id/:dataset-id/graph/:graph-id`.

```
$PREFIX/:agent-id/:dataset-id/graph/:graph-id/rev
```

TODO: should this be a timemap for `$PREFIX/:agent-id/:dataset-id/graph/:graph-id`?
The mementos should just be the ones at `$PREFIX/:agent-id/:dataset-id/rev/:version-id/graph/:graph-id`.

Copying graphs from other datasets
----------------------------------

```
$PREFIX/:agent-id/:dataset-id/graph/:new-graph-id?copyFrom=$URI
```

TBD: where is meta-data about graphs stored? How is it added to the dataset? For example when a graph describing a study is copied from an existing dataset, how is it described in the new dataset? Is meta-data copied along with it?
