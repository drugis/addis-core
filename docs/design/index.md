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

### Vision and values ###

*Note*: this section reflects the views of the ADDIS development team, and not necessarily those of the consortium as a whole.

### Aims and objectives within GetReal ###

### Rationale for re-development ###

### Non-functional requirements ###

### Sketch of functional requirements ###

### Architecture ###

## Data management ##

### Requirements ###

### Prototypes ###

### Preliminary design ###

## (OLD) Contents ##

- [Use cases](usecases.html)
- [Prototypes](prototypes.html)
- [Meta-data model](metadata.html)
- [RCT data model](rct.html)
- [Interaction model](interaction.html)

NOTE: to compile this document, install [n3pygments](https://github.com/gertvv/n3pygments).
