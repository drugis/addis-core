---
layout: default
title: Introduction 
---

With ADDIS, we want to enable a much more efficient process for evidence-based treatment selection.
This includes evidence-based treatment guidelines, market authorization, and health technology assessment.
To achieve this, three main challenges need to be met:

  1. data acquisition: collecting the relevant evidence
  2. evidence synthesis: statistically combining the evidence (through meta-analysis and modeling)
  3. decision aiding: giving insight in the data and identifying trade-offs

The ADDIS 1.x system focussed on the latter two challenges, providing automated network meta-analysis and multiple criteria decision aiding models.
However, we realized that a radical move would be needed to tackle the data acquisition challenge in a meaningful way.
Constructing a dataset for analysis in ADDIS 1.x typically entails conducting a systematic review (searching the literature and other sources, screening the search results for eligibility, and extracting data from identified relevant reports), or identifying a relevant existing systematic review and re-extracting data from the underlying trial reports.
This is a large investment, and for most decisions it is prohibitive.
Even if the costs of data acquisition are not prohibitive, the ADDIS 1.x system is strictly a single-user system, which makes it difficult for teams to collaborate on creating data sets and analyses.
Moreover, ADDIS 1.x lacks a system for versioning of data extractions, and once analyses have been based on a study, corrections to the extracted data can no longer be made.

The aim of this document is to design a new data management framework for the ADDIS 2 system that remedies the shortcomings of ADDIS 1.x for multi-user work.
In addition, the design will make data acquisition more efficient by allowing for more flexible re-use of previously extracted data, and for post-hoc harmonization of studies extracted by different users.
The [ADDIS 2 architecture](http://drugis.org/software/addis2/architecture) separates the data management framework into two components:

  1. TrialVerse, intended to store within-study data
  2. ConceptMapper, intended to store complex between-study information, i.e. for harmonization of concepts

The ConceptMapper should be applicable beyond just trials, and therefore should allow for very general-purpose concepts to be expressed.
On the other hand, the TrialVerse has a more limited domain, that of randomized controlled trials, and therefore a more well-defined data model.
In this design document, both components are designed in tandem to arrive at an optimal interface between the two.
In addition, they will probably share a number of aspects of design between them.

### Context ###

For the design of ADDIS 2, see the [ADDIS 1.x retrospective](http://drugis.org/software/addis1/retrospective), the [ADDIS 2 requirements](http://drugis.org/software/addis2/requirements), and the [ADDIS 2 architecture](http://drugis.org/software/addis2/architecture).
The IMI GetReal description of work (DoW), deliverable D4.12 calls for specification of the ConceptMapper component, which should allow for the flexible harmonization of meta-data describing data used in GetReal case studies.
The ConceptMapper component is further referenced in tasks T4.12.2, T4.13.2, and T4.14.2, which cover the development of this component to various stages of maturity; it is also part of milestone M4.5, delivery of the final software platform.

### A note on observational data ###

Because the GetReal project will explore the interface between randomized controlled trials and real world data (i.e. observational data and pragmatic trials), ADDIS will need to handle observational data to some extent.
However, this is not (yet) covered by this design document, for the following reasons:

 - Through our work on ADDIS 1, we have significant experience with modeling RCT data, but not with observational data.
 - There is a wide variety of observational study designs, and it is unclear at this time what types of study we will need to deal with, and how the interactions will be structured. This requires further clarification before the design can proceed.
 - TrialVerse and ConceptMapper have been defined as separate components precisely because we want to enable mapping of meta-data not only for RCTs entered in TrialVerse, but also for other data sources.
 - While storing published aggregate-level data from RCTs is both useful and uncontroversial, storing either individual participant data from RCTs or from observational studies raises issues of privacy and data ownership.
   Therefore, it seems more likely that such data would not be stored directly by ADDIS.
   Other solutions may need to be explored.

## Contents ##

 - [Use cases](usecases.html)
 - [Prototypes](prototypes.html)
 - [Meta-data model](metadata.html)
 - [RCT data model](rct.html)
 - [Interaction model](interaction.html)

NOTE: to compile this document, install [n3pygments](https://github.com/gertvv/n3pygments).
