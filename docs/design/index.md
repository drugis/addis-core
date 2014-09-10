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

#### Valorization
The system should generate sufficient revenue to support its continued development and operation.
Since the business model has not determined, we focus on concerns that are likely to be common to potential approaches.

  - V1 Custom variants for different audiences, such as regulators, reimbursement authorities and industry (product line)
  - V2 `In-house' deployments of the system for when the analyses are confidential / sensitive
  - V3 Access to internal organization data, possibly in conjunction with public data
  - V4 Ease of use: the system should visually attractive to 'sell' it, and be easy to use to limit frustrations and the need for training

#### Development

System development should be efficient, agile, and sustainable.

  - D1 Analyses are often computationally intensive; they should run quickly and the system should scale to support many such computations in parallel if needed 
  - D2 There should be flexibility in choosing the right technologies, platforms, and frameworks to implement specific functionality (e.g. statistical computing using R and user interfaces using HTML + JavaScript)
  - D3 The system should be maintainable and hence divided up in loosely coupled components with well-defined APIs
  - D4 To ensure our public APIs are functioning in an optimal manner, our own software should use those APIs 
  - D5 The integrity of data should be closely guarded and all data should have clear provenance and versioning information

#### Research

The system should be an enabler for our own research and contribute to the success of grant proposals.

  - R1 Rapid prototyping of new analyses and user interfaces to support new research questions
  - R2 A large database to do research and meta-research
  - R4 Transparency of implementation (open source)
  - R5 Publishable (high quality) graphics and tables 

#### Ecosystem

For the ADDIS concept to work, it is critical that structured clinical trials data are available. It is unlikely that any one organization will be able to achieve this. Therefore, we should aim to 'bootstrap' an open ecosystem in which structured clinical trials data can be shared.

  - E1 Create a single collaborative portal for data extraction and sharing (open access / source)
  - E2 Enhance the efficiency of systematic review by 'closing the information chain' (i.e. capturing the entire process from literature screening to analysis)
  - E3 Enable the flexible re-use of data by enabling the definition, re-definition and mapping of meta-data (it should be possible to integrate data extracted by independent teams by mapping the meta-data)
  - E4 Enable automated (third party) data extraction systems (bots) to contribute
  - E5 The system(s) should be easy to use and hassle-free to use (e.g. single sign-on)
  - E6 All data and analyses can be traced back to their ultimate source
  - E7 Third parties should be able to develop new analysis tools and decision support systems based on the available data

#### Learning

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

### Requirements ###

**TODO**

### Prototypes ###

**TODO**

### Preliminary design ###

**TODO**

## (OLD) Contents ##

- [Use cases](usecases.html)
- [Prototypes](prototypes.html)
- [Meta-data model](metadata.html)
- [RCT data model](rct.html)
- [Interaction model](interaction.html)

NOTE: to compile this document, install [n3pygments](https://github.com/gertvv/n3pygments).
