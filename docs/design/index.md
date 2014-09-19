---
layout: default
title: Introduction 
---

### Status ###

This is a DRAFT of the data management design document for the ADDIS 2 project.
It covers the design of the data management components of ADDIS 2, and is NOT a complete design of the ADDIS 2 system.

This draft is intended to lead to an initial specification to be delivered September 2014.
This initial specification will be revised based on stakeholder feedback, and expanded to include a number of topics that are considered out of scope for the initial specification (e.g. where further input from partners is needed first).

### Context ###

With ADDIS, we want to enable a much more efficient process for evidence-based treatment selection.
This includes evidence-based treatment guidelines, market authorization, and health technology assessment.
To achieve this, three main challenges need to be met:

  1. data acquisition: collecting the relevant evidence
  2. evidence synthesis: statistically combining the evidence (through meta-analysis and modeling)
  3. decision aiding: giving insight in the data and identifying trade-offs

The ADDIS 1.x system focussed on the latter two challenges, but did not address the data acquisition challenge to the extent desired.
In particular, we identify these shortcomings:

  1. ADDIS 1.x does not facilitate identifying relevant trials from the literature to extract data from.
  2. Extracting data is time consuming (unless they can be imported from ClinicalTrials.gov).
  3. ADDIS 1.x is a single-user system, which makes it difficult for teams to collaborate on creating data sets.
  4. ADDIS 1.x lacks a system for managing different versions of data sets, and has no audit trail.
  5. ADDIS 1.x does not allow multiple datasets to be combined for an overall synthesis.


### Purpose ###

The aim of this document is to design a new data management framework for the ADDIS 2 system that remedies the shortcomings of ADDIS 1.x for multi-user work.
In addition, the design will make data acquisition more efficient by allowing for more flexible re-use of previously extracted data, and for post-hoc harmonization of studies extracted by different users.

The IMI GetReal description of work (DoW) anticipates a number of design decisions and in particular identifies a component, called "ConceptMapper" to address the post-hoc harmonization issue.
Specifically, deliverable D4.12 calls for specification of the ConceptMapper component, which should allow for the flexible harmonization of meta-data describing data used in GetReal case studies (task T4.12.2).
This is addressed by the present document.
The ConceptMapper component is further referenced in tasks T4.13.2 and T4.14.2, which cover the development of this component to different stages of maturity; it is also part of milestone M4.5, delivery of the final software platform.

### Limitations ###

Because the GetReal project will explore the interface between randomized controlled trials and real world data (i.e. observational data and pragmatic trials), ADDIS will need to handle observational data to some extent.
Moreover, many case studies within GetReal will work with individual patient data from RCTs to fit models e.g. to predict real world effectiveness.
However, this is not covered by this design document, for the following reasons:

 - Through our work on ADDIS 1, we have significant experience with modeling RCT data, but not with observational data.
 - There is a wide variety of observational study designs, and it is unclear at this time what types of study we will need to deal with, and how the interactions will be structured. This requires further clarification before the design can proceed.
 - While storing published aggregate-level data from RCTs is both useful and uncontroversial, storing either individual participant data from RCTs or from observational studies raises issues of privacy and data ownership.
   Therefore, it seems more likely that such data would not be stored directly by (the public version of) ADDIS.
   Other solutions may need to be explored.

Future versions may address these limitations.
In addition, the components will be structured so that generic structures (e.g. outcomes, interventions, etc.) can be re-used in a future component for real world data.

### Outline ###

This document first covers the overall vision and architectural design of the ADDIS 2 system.
On this foundation, we develop the more detailed requirements of its data management components.
We then propose an initial design based on these requirements and a number of prototypes that have been constructed.

## The ADDIS 2 project ##

This section explains the aims, requirements, and architecture of the ADDIS 2 project insofar as is necessary for an informed discussion of the data management components.

### Vision and values ###

*Note*: this section reflects the views of the ADDIS development team, and not necessarily those of the consortium as a whole.

**TODO**

 - Transparency
    * Scientific software should be open source
    * Data should be open
    * Decisions should be made explicity / accountable
    * ...
 - ...

### Aims and objectives within GetReal ###

**TODO**

 - Viable data management solution / collaborative database building
 - GetReal analysis capabilities
 - Work with industry on interfacing in-house systems? Is not in the DoW but could be implied due to IPD.

### Rationale for re-development ###

ADDIS 1 is a cross-platform desktop application implemented in Java, designed for use by a single user.
This introduces two classes of inherent limitations.

First, to maintain cross-platform compatibility, all components needed to run on the JVM (Java Virtual Machine).
Java is not always the best tool for the job at hand, e.g. it is often easier to implement statistical analyses in R.
The constraints of bundling a desktop application make it difficult to choose the best available technology, leading to re-implementation of existing software.
Moreover, with a desktop application we have no control over the execution environment, which means that the application has to be aimed at the lowest common denominator.
For example this means supporting PCs with slow processors, little RAM, or very out-dated versions of the JVM.

Second, because ADDIS is a single-user application in which data are stored in files on the user's computer, sharing and collaborating on data sets is difficult.
ADDIS 1 does not support any way of merging changes made by different users to the same data file.
Although this would be possible in principle, sharing data would still require conscious effort on the part of users.
Rather, we would like sharing to be the default option.

### Non-functional requirements ###

We identify the main goals and non-functional requirements of the ADDIS 2 re-development effort according to five key concerns: valorization, development, research, ecosystem, and learning.

#### Valorization ####

The system should generate sufficient revenue to support its continued development and operation.
Since the business model has not determined, we focus on concerns that are likely to be common to potential approaches.

  - V1 Custom variants for different audiences, such as regulators, reimbursement authorities and industry (product line)
  - V2 `In-house' deployments of the system for when the analyses are confidential / sensitive
  - V3 Access to internal organization data, possibly in conjunction with public data
  - V4 Ease of use: the system should visually attractive to 'sell' it, and be easy to use to limit frustrations and the need for training

#### Development ####

System development should be efficient, agile, and sustainable.

  - D1 Analyses are often computationally intensive; they should run quickly and the system should scale to support many such computations in parallel if needed 
  - D2 There should be flexibility in choosing the right technologies, platforms, and frameworks to implement specific functionality (e.g. statistical computing using R and user interfaces using HTML + JavaScript)
  - D3 The system should be maintainable and hence divided up in loosely coupled components with well-defined APIs
  - D4 To ensure our public APIs are functioning in an optimal manner, our own software should use those APIs 
  - D5 The integrity of data should be closely guarded and all data should have clear provenance and versioning information

#### Research ####

The system should be an enabler for our own research and contribute to the success of grant proposals.

  - R1 Rapid prototyping of new analyses and user interfaces to support new research questions
  - R2 A large database to do research and meta-research
  - R4 Transparency of implementation (open source)
  - R5 Publishable (high quality) graphics and tables 

#### Ecosystem ####

For the ADDIS concept to work, it is critical that structured clinical trials data are available. It is unlikely that any one organization will be able to achieve this. Therefore, we should aim to 'bootstrap' an open ecosystem in which structured clinical trials data can be shared.

  - E1 Create a single collaborative portal for data extraction and sharing (open access / source)
  - E2 Enhance the efficiency of systematic review by 'closing the information chain' (i.e. capturing the entire process from literature screening to analysis)
  - E3 Enable the flexible re-use of data by enabling the definition, re-definition and mapping of meta-data (it should be possible to integrate data extracted by independent teams by mapping the meta-data)
  - E4 Enable automated (third party) data extraction systems (bots) to contribute
  - E5 The system(s) should be easy to use and hassle-free to use (e.g. single sign-on)
  - E6 All data and analyses can be traced back to their ultimate source
  - E7 Third parties should be able to develop new analysis tools and decision support systems based on the available data

#### Learning ####

The system should promote the use of structured, transparent, and quantitative methods in health care policy decisions.

  - L1 Enable access to 'complex' methods and tools through a usable interface for non-experts
  - L2 Clear in-system documentation and links to further reading (e.g. methodology papers)
  - L3 Raise awareness of newer statistical and decision making methods by implementing and applying them

### Sketch of functional requirements ###

Initially, ADDIS 2 is a web-based re-development of ADDIS 1, and therefore the functional requirements are derived from the current ADDIS 1 functionality:

 - A structured database of clinical trials
    * Manual input or semi-automated import from ClinicalTrials.gov
 - Evidence synthesis using network meta-analysis
 - Multiple criteria decision analysis (MCDA) methods to assess trade-offs between multiple outcomes
 - The system guides the creation of analyses, and allows flexibility in how interventions and outcomes are defined

In addition, as part of the IMI GetReal project, functional requirements related to the prediction of relative effectiveness and the use of real world data will be defined.

### Architecture ###

Given that many of the requirements imply loosely coupled and flexibly reusable components with well-defined public APIs (R1, V1, V2, V3, D1, D2, D3, E4, E7), we will assume that a service-based (and web-based) architecture is most appropriate. We distinguish the following components:

  - Web services that implement the different analyses (R1, D1, D2)
  - ADDIS-core, a 'business intelligence' system for drug benefit-risk analysis (V1, D3, L1) (basically a 'workflow engine' in which different ways of going from a database of trials to analyses can be implemented)
  - TrialVerse, a portal/database where researchers share structured RCT data (V3, D3, D4, E1)
  - TrialMine, a system for literature screening (E2)
  - ConceptMapper, a shared component where definitions (concepts) can be deposited, refined, and mapped (R2, E1, E2, E3)
  - A user management component underlying all systems (V4, D3, E5)

![ADDIS 2 architecture](architecture.svg)

The user-facing systems have a back-end that exposes an API, and an HTML + JavaScript GUI that calls that API (D2, D4). It is intended that this will enable third parties to create additional 'business intelligence' systems based on TrialVerse (E7). Moreover, the API can be used to build, test, and use automated systems for the extraction of data (E4). While the two user-facing systems have clearly separated concerns, they will be tightly integrated (E5), for example using:

  - Single sign-on authentication
  - Shared user and organization profiles
  - Consistent visual identity

We also want to support corporate deployments of the ADDIS system (V2). This is possible because TrialVerse exposes a well-defined API. We can then do the following:

  1. Deploy an internal system exposing company data through the TrialVerse interface (V3)
  2. Deploy an internal version of ADDIS that has access to both TrialVerse and the internal database
(V2)
  3. (Optional) internal deployment of the analysis web services (V2)

To better support this scenario there should be clearly separated read-only (to be used by ADDIS) and read-write (to be used by the TrialVerse GUI) interfaces for TrialVerse.

## Data management ##

**TODO**

In the ADDIS 2 architecture, the responsibility for data management is shared by the TrialVerse and ConceptMapper components:

  - TrialVerse ...
  - ConceptMapper ...

We now proceed to design these two components in tandem ...
However, in doing so we keep in mind that the ConceptMapper component is expected to collaborate with other components besides TrialVerse, e.g. components dealing with real world data.

### Requirements ###

**TODO**

This section elaborates on the requirements for the data management component by presenting a number of use cases.
Some use cases will be from the perspective of an end-user, while others may be from the perspective of another software component interacting with the system.

In general, we make a distinction between several types of record:

 - Datasets: user-created collections of studies and meta-data about those studies.
 - Extractions: user-provided information on existing objects, e.g. studies and systematic reviews.
 - ...

Note that not all functional requirements need to be satisfied by direct implementation in ADDIS.
Some could also be achieved by interoperation with other systems.
Moreover, all features are subject to prioritization and therefore it is likely that not all of the requirements will be satisfied by the end of the project.
The main aim of listing them here is to provide a basis for the overall design of the system and for the prioritization of features.

#### Working with a dataset ####

These requirements cover how we expect users to interact with a completed dataset, i.e. a single version of a dataset as it is used in an analysis project in ADDIS.
These are intentionally broad, as the specific analysis functionality to be built is out of scope for this document.

**F1.1** Any authenticated user must be able to create a project based on an available dataset. A title and description of the project must be provided by the user.

**F1.2** When viewing a project, any user must be presented with information on the dataset on which it is based. This information must link to a detailed view of this dataset (F1.5). (See [Mockup 001](#mockup001))

**F1.3** Any project participant must be able to create logical matching rules that define an intervention or an outcome. Such matching rules should be able to make use of terms from structured terminologies (e.g. ATC codes), as well as set restrictions on other properties such as dose. Templates should be provided for common scenarios (e.g. drug treatments). (See [Mockup 011](#mockup011))

**F1.4** ADDIS must be able to automatically apply defined matching rules (F1.3) to the project dataset to create analysis datasets for e.g. network meta-analysis. Matching should be intelligent, and e.g. make use of mappings between terminologies. For example, listing both the ATC code and the SNOMED CT code as in [Mockup 011](#mockup011) could be redundant if it is already known that these concepts are equivalent.

**F1.5** When viewing a dataset, the user is shown information on how study inclusion was decided upon, as well as how data were extracted. (See [Mockup 002](#mockup002))

**F1.6** When viewing a dataset, the user is show a table of basic information on each of the studies included in that dataset. Each row links to a detailed record on that study (F1.7). (See [Mockup 002](#mockup002))

**F1.7** When viewing a study, the user is first presented with a summary view of the key characteristics. This summary view links to a details view. (See [Mockup 005](#mockup005))

**F1.8** The detailed view shows the full semantic detail of the study, including basic study characteristics, population and eligibility information, the arms and epochs ([Mockup 006](#mockup006)), the activities performed ([Mockup 007](#mockup007)), the predefined outcome measures, the actual participant flow, study results, and the mapping of study concepts to structured vocabularies and ontologies ([Mockup 008](#mockup008)).

#### Provenance and versioning ####

Because the analyses being done in ADDIS potentially have a large impact, it is important to keep track of how datasets originated, and who made particular changes.

**F2.1** Users must be able to log in to the system. The system must enforce that a user is logged in before they can make changes to any extraction or dataset.

**F2.2** When a dataset or extraction is updated, ADDIS must be able to access the older versions of that record, so that an analysis that is based on an older version will continue to work and will continue to yield the same results ([Mockup 001](#mockup001)).

**F2.3** When viewing a dataset or extraction, the user is shown information on who created it, whether this is the most recent version, and when it has last been updated ([Mockup 002](#mockup002), [003](#mockup003), [015](#mockup015)).

**F2.4** The user must be able to verify the entire edit history of a dataset or extraction, including who made the changes and what were the precise changes made ([Mockup 013](#mockup013), [009](#mockup009)).

**F2.5** A logged in user can edit any dataset or extraction. When doing so, these changes are continuously saved to their working copy on their account. When the user is satisfied with the changes, they are prompted to check them before saving, and to provide a description of the reasons for their changes ([Mockup 009](#mockup009)). Only changes that have been so saved will be available outside of the edit view for that dataset or extraction. If the dataset or extraction was previously created by another user, saving an edited version will create a full copy of the record, including the full history.

**F2.6** Users who have created datasets or extractions are notified when other users have made changes to their records, and are prompted to accept or reject these changes ([Mockup 016](#mockup016)).

**F2.7** Multiple alternative extractions of a single object may exist. When viewing an extraction, this is clearly displayed to users ([Mockup003](#mockup003)).

**F2.8** The system should encourage contributions by clearly assigning credit for extractions and datasets. For example, a user profile page can showcase contributions made by the user ([Mockup 012](#mockup012)).

**F2.9** Extractions or partial extractions of publications or registry entries can also be made by software agents.
Credit for such extractions should be assigned to both the software agent and the entity that initiated the running of the software agent ([Mockup 016](#mockup016)).

**F2.10** Extractions can be performed using predefined extraction forms, where the system provides a mapping from the extraction forms to the appropriate data structures.

#### Creating datasets ####

**F3.1** Any user must be able to create a new dataset. The user will be prompted to provide a title and a description, as well as a method for study selection (F3.2-F3.5) and for data extraction (F3.6; F4.*).

**F3.2** Studies can be selected based on the inclusions of an existing systematic review, by referring to a specific extraction of that review ([Mockup 002](#mockup002), [003](#mockup003)).

**F3.3** Studies can be selected based on the inclusions of several existing systematic reviews, by referring to specific extractions of those reviews (umbrella reviews).

**F3.4** Studies can be selected ad hoc from the full database.

**F3.5** Studies can be selected based on a literature screening project (F6.*).

**F3.6** The dataset can refer to pre-existing extractions of the selected studies.

#### Data entry and import ####

**F4.1** The study edit view allows the user to edit the full semantic detail of the study, including basic study characteristics, population and eligibility information, the arms and epochs ([Mockup 006](#mockup006)), the activities performed ([Mockup 007](#mockup007)), the predefined outcome measures, the actual participant flow, study results, and the mapping of study concepts to structured vocabularies and ontologies ([Mockup 008](#mockup008)).
These are represented at a sufficient level of detail to allow automated matching and reasoning by ADDIS, but the user interface is specialized to presenting RCT information to users so that entering data correctly is not an overly complex task.

**F4.2** Extractions can be created by annotating the abstract of an article (e.g. identifying interventions and outcomes). TO BE SPECIFIED FURTHER.

**F4.3** Extractions can be created by annotating the full text of an article (e.g. identifying interventions and outcomes). TO BE SPECIFIED FURTHER.

**F4.4** The system will continuously extract and update data from ClinicalTrials.gov records (and potentially other structured sources). As per F4.1 and F2.5, users can further complete and refine such extractions ([Mockup 016](#mockup016)).

**F4.5** Third parties are able to deploy their own software agents to contribute automated extractions similar to F4.4.

**TODO** Defining mappings between concepts.

#### Working together ####

**F5.1** Projects with multiple participants will be able to set rules for study selection and data extraction to ensure a certain level of quality (F5.2-F5.4).

**F5.2** Group data entry without any restrictions: participants can select existing extractions for use in the project, or perform data extractions ad hoc.

**F5.3** Group data entry without use of existing extractions.

**F5.4** Double- or triple-blind data extraction with resolution of conflicts. The system detects and rates agreement between extractors.

#### In-house deployments ####

For industry and HTA stakeholders in-house deployments of the software will be important. This will allow them to keep sensitive data and analyses private.

**F6.1** It must be possible do deploy ADDIS in-house.

**F6.2** It must be possible to build front-ends for in-house databases so that the in-house ADDIS can interface with them.

**F6.3** It must be possible for ADDIS to interface with multiple data sources, for example multiple in-house systems or an in-house data source and the public version of ADDIS.

**F6.4** There must be an interchange format and functionality to selectively transfer data and analyses without allowing third parties access to those data. For example to transfer data from industry to HTA systems.

#### Literature screening ####

**TODO** (This is out of scope for GetReal, but needs to be considered from a "future proofing" perspective) How will users identify studies from the literature? How can we speed this up using our structured data?

#### Embargo ####

**TODO** Can we let users / teams place their reviews under embargo for a certain time or until publication? Is this a potential business model?

#### Curation and discussion ####

**TODO** There needs to be some source of authority on what extractions are of high quality and relevant to certain areas. Many approaches are possible, e.g. a user reputation system, an algorithm for computing a "concensus", or an approach where certain community groups create large curated datasets. In addition there needs to be a forum for discussion on difficult extractions, and this should be embedded with the extractions thembselves.

### Prototypes ###

To explore the design space for the TrialVerse and ConceptMapper components, several prototypes were constructed.

#### Vertical prototype ####

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

#### Triple stores, existing terminologies ####

A second prototype explored the capabilities of a number of existing triple stores, as well as their ability to handle key medical terminologies such as SNOMED CT, the ATC classification, ICD-10, MedDRA, and LOINC.
In addition, a prototype web frontend allowed full-text search within these terminologies, as well as browsing their hierarchy.
Mappings to ADDIS datasets could also be retrieved.
Through this process, we identified Apache Jena (with Fuseki) as the triplestore of choice.
It could easily handle the relatively large terminologies, and can support the much larger sets of triples that we expect to store in the future.
It also has good support for the SPARQL 1.1 specification and full text indexing (using a Lucene plugin), requirements we also discovered thanks to this prototype.
Finally, Apache Jena is an open source project with a very extensible architecture.

#### Event sourcing with RDF ####

[Event sourcing](http://martinfowler.com/eaaDev/EventSourcing.html) is a design pattern for the design of applications in which every change to the state of the application needs to be captured.
This allows for example reconstructing the state of the application at any past point in time.
An implementation of this design pattern using RDF named graphs was designed and tested, first using a static dataset and a number of SPARQL queries.
Subsequently, an extension to the Fuseki web service for Jena was developed.
It enabled fully transparent SPARQL querying of the store at any point in time, as well as updating the store using a REST interface.

#### Pure RDF data model ####

The initial vertical prototype used a relational database for the more well-defined parts of the data model (i.e. those describing the study design and measurements) and a triple store for the more open-ended parts (i.e. higher level concepts).
However, this generates some tension at the interface between these components.
In addition, an event sourcing implementation would have to work across this barrier, so would need to be implemented consistently for these two very different technology stacks.
Therefore, another prototype was constructed to evaluate the possibility of using a triple store for both TrialVerse and ConceptMapper.
This consists of a converter from ADDIS datafiles to RDF, and a set of SPARQL queries implementing the key used cases from the ADDIS point of view (i.e. matching studies to concept definitions and constructing datasets).
This was successful and should allow for a full implementation of the "read-only API" envisaged in the ADDIS 2 architecture.

#### ADDIS 2 R1 - R3 ####

The relational database schema, triplestore, and importer were deemed to be of sufficient quality to serve as the basis of the initial releases of ADDIS 2, pending the further design of the TrialVerse and ConceptMapper components.
Several design decisions of the prototype were also carried over, such as implementing the backend using Java and the Spring framework and the frontend using AngularJS.
Although the Core component developed during Releases 1-3 is not considered a prototype, the TrialVerse and ConceptMapper components are, and are expected to undergo a full rewrite.
The first two releases were based on the relational database model, and a transition to the RDF data model was made in Release 3.

### Preliminary design ###

**TODO**

 * Semantic web / RDF for knowledge representation
    - Flexible data modeling; relevant for "fuzzy" concepts like outcomes
    - Use existing technology for matching and reasoning
    - Use existing terminologies and ontologies to provide background knowledge
 * OAuth + ORCiD for authentication
 * Event sourcing
 * Git-like model for collaboration
 * Specialized UI for data entry and basic mapping of concepts
 * Generalized knowledge modelling UI for experts to create / upload higher level ontologies and complex mappings.

### Mockups ###

The mockups are unfortunately crude hand drawings for now.
At the top of each mockup is a mock address bar with an example URL and a mockup number.
Generally titles are given in all capital letters and hyperlinks are indicated by an underline.
Often hyperlinks refer to other mockups, which is indicated with an arrow pointing to the number of that mockup.
Links to outside of the system are inidicated by a box with an arrow pointing out of it (this element would also be shown in the final system).
A pencil pointing into a box is used as a general "edit" icon / button.

<a name="mockup001">
![Mockup 001](/images/mockup001.png)

**Mockup 001** Project overview.
</a>

<a name="mockup002">
![Mockup 002](/images/mockup002.png)

**Mockup 002** Dataset overview when a more recent version is available.
</a>

<a name="mockup003">
![Mockup 003](/images/mockup003.png)

**Mockup 003** Dataset overview when it is the most recent version.
</a>

<a name="mockup004">
![Mockup 004](/images/mockup004.png)

**Mockup 004** Overview of a partially extracted systematic review.
</a>

<a name="mockup005">
![Mockup 005](/images/mockup005.png)

**Mockup 005** Summary view of an extracted RCT.
</a>

<a name="mockup006">
![Mockup 006](/images/mockup006.png)

**Mockup 006** Details view of an extracted RCT: arms and epochs.
</a>

<a name="mockup007">
![Mockup 007](/images/mockup007.png)

**Mockup 007** Details view of an extracted RCT: activities.
</a>

<a name="mockup008">
![Mockup 008](/images/mockup008.png)

**Mockup 008** Details view of an extracted RCT: mapping entities to higher-level concepts.
The user has made changes to this extraction (indicated bottom left).
</a>

<a name="mockup009">
![Mockup 009](/images/mockup009.png)

**Mockup 009** Checking and saving changes made to a data extraction.
</a>

<a name="mockup010">
![Mockup 010](/images/mockup010.png)

**Mockup 010** Starting a new extraction based on a PubMed abstract.
</a>

<a name="mockup011">
![Mockup 011](/images/mockup011.png)

**Mockup 011** Defining an intervention matching rule in ADDIS.
</a>

<a name="mockup012">
![Mockup 012](/images/mockup012.png)

**Mockup 012** Defining a concept mapping based on a predefined template in TrialVerse.
</a>

<a name="mockup013">
![Mockup 013](/images/mockup013.png)

**Mockup 013** A user profile page in TrialVerse.
</a>

<a name="mockup014">
![Mockup 014](/images/mockup014.png)

**Mockup 014** A dataset history overview in TrialVerse (showing only the latest entry).
</a>

<a name="mockup015">
![Mockup 015](/images/mockup015.png)

**Mockup 015** Provenance information of a trial that was partially extracted using an automated tool.
</a>

<a name="mockup016">
![Mockup 016](/images/mockup016.png)

**Mockup 016** The user is notified that another user has made changes to their extraction and given the opportunity to incorporate those changes.
</a>

## (OLD) Contents ##

- [Use cases](usecases.html)
- [Prototypes](prototypes.html)
- [Meta-data model](metadata.html)
- [RCT data model](rct.html)
- [Interaction model](interaction.html)

NOTE: to compile this document, install [n3pygments](https://github.com/gertvv/n3pygments).
