---
layout: default
title: Prototypes
---

To explore the design space for the TrialVerse and ConceptMapper components, several prototypes were constructed.

### Vertical prototype ###

A vertical prototype was constructed as a proof-of-concept of how the ConceptMapper, TrialVerse, and Core components would interact.
It was constructed in short bursts of activity during the design phase of the ADDIS 2 architecture (Jan 2013), during the specification of the IMI GetReal DoW (Apr 2013), and during the run up to the GetReal project (Aug & Sep 2013). It consisted of:

 - A relational database schema for RCTs ("TrialVerse")
 - An RDF triplestore with semantic meta-data for between-studies information ("ConceptMapper")
 - An importer for .ADDIS files to TrialVerse/ConceptMapper format
 - A throw-away prototype that constructed network meta-analysis by querying TrialVerse/ConceptMapper ("Core")
 - A throw-away prototype network meta-analysis component based on the [gemtc](https://github.com/gertvv/gemtc) package for R and [Patavi](https://github.com/joelkuiper/patavi)

In particular, the prototype defined [RESTful](http://en.wikipedia.org/wiki/Representational_state_transfer) web services to expose the RCT data and meta-data.
The "Core" component used these interfaces to construct datasets compatible with the user's requests.
The client (running in a web browser, based on the [AngularJS](https://angularjs.org/) framework) in turn interacted with the "Core" component using RESTful interfaces.

The development of the prototype allowed us to verify that the import of .ADDIS files was sufficiently complete to allow constructing analysis datasets.
As a result of the prototyping work, we fine-tuned both the relational data model and the RDF representation of the meta-data.
However, significant tension remains: for example the definition of interventions is largely defined in the relational data model, which limits its flexibility to drug-based interventions, and limits semantic rules to matching the definitions of drugs (i.e. compounds) rather than the entire intervention regimen.
We also discovered that the REST interface we defined originally did not allow us to optimize query performance sufficiently, and therefore it required a redesign.

### Triple stores, existing terminologies ###

A second prototype explored the capabilities of a number of existing triple stores, as well as their ability to handle key medical terminologies such as SNOMED CT, the ATC classification, ICD-10, MedDRA, and LOINC.
In addition, a prototype web frontend allowed full-text search within these terminologies, as well as browsing their hierarchy.
Mappings to ADDIS datasets could also be retrieved.
Through this process, we identified Apache Jena (with Fuseki) as the triplestore of choice.
It could easily handle the relatively large terminologies, and can support the much larger sets of triples that we expect to store in the future.
It also has good support for the SPARQL 1.1 specification and full text indexing (using a Lucene plugin), requirements we also discovered thanks to this prototype.
Finally, Apache Jena is an open source project with a very extensible architecture.

### ADDIS 2 R1 & R2 ###

The relational database schema, triplestore, and importer were deemed to be of sufficient quality to serve as the basis of the initial releases of ADDIS 2, pending the further design of the TrialVerse and ConceptMapper components.
Several design decisions of the prototype were also carried over, such as implementing the backend using Java and the Spring framework and the frontend using AngularJS.
Although the Core component developed during Release 1 and Release 2 of ADDIS 2 is not considered a prototype, the TrialVerse and ConceptMapper components are, and are expected to undergo a full rewrite.
We are considering whether the design for TrialVerse/ConceptMapper has sufficiently matured for this to happen during Release 3.

### Event sourcing with RDF ###

[Event sourcing](http://martinfowler.com/eaaDev/EventSourcing.html) is a design pattern for the design of applications in which every change to the state of the application needs to be captured.
This allows for example reconstructing the state of the application at any past point in time.
Prototyping work is currently ongoing as to how the event sourcing pattern can be implemented using triple stores, and an initial design is in progress; see the [interaction model](interaction.html) for details.
A full implementation will likely require some extensions to Jena.

### Pure RDF data model ###

The initial vertical prototype used a relational database for the more well-defined parts of the data model (i.e. those describing the study design and measurements) and a triple store for the more open-ended parts (i.e. higher level concepts).
However, as was noted previously this generates some tension at the interface between these components.
In addition, an event sourcing implementation would have to work across this barrier, so would need to be implemented consistently for these two very different technology stacks.
Therefore, another prototype was constructed to evaluate the possibility of using a triple store for both TrialVerse and ConceptMapper.
This consists of a converter from ADDIS datafiles to RDF, and a set of SPARQL queries implementing the key used cases from the ADDIS point of view (i.e. matching studies to concept definitions and constructing datasets).
This was successful and should allow for a full implementation of the "read-only API" envisaged in the ADDIS 2 architecture.
