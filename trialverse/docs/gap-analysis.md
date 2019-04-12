EUdraCT XML - ADDIS data model Gap Analysis
===========================================

2019-04-12

Daan Reid - d.reid@umcg.nl

Introduction
------------

This document describes what currently goes well when transforming from EUdraCT XML to the ADDIS data model, and what information still needs to be added after transformation. The ADDIS data model is based on the [CDISC Bridg model](https://www.cdisc.org/standards/domain-information-module/bridg), allowing for flexible yet structured representation of clinical trial study designs and their measurement results.

ADDIS importers
---------------

ADDIS 2 uses an [RDF data store](https://github.drugis.org/jena-es). There is software available to transform study data into ADDIS RDF for the following file formats: clinicaltrials.gov XML by NCT ID, EUdraCT XML files, [ADDIS structured Excel](http://drugis.org/files/addisExcelSpec.pdf) and [ADDIS XML](http://drugis.org/files/Addis_DM_1.0.zip). After importing, actions may still need to be taken to make the imported study analysis-ready.

Requirements
------------

To automatically extract measurement data from studies, ADDIS requires those studies to be appropriately structured. This means that the following needs to be defined:

- Treatment arms (subgroups of the study population).
- Epochs (ordered partitions the progression and partitioning in time of the study). *Examples: Screening, Randomization, Treatment, Follow-up.*
- Activities. *Examples: randomization, washout, 30 milligram aspirin per day.*
- The study design: which activity took place in each arm, during which epoch.
- Measurement moments (when variables were measure). These need to be linked to an epoch. *Examples: "At the end of the treatment phase", "12 weeks after the start of the treatment phase."*
- Variables (Baseline characteristics, endpoints, adverse events), plus which type of measurements they measure, plus at which measurement moment(s) they were measured. *Examples: Headache measures the number of subjects with headache and the sample size, at the measurement moment .*
- Concepts and their mappings to dataset-level concepts.

The table below shows how ADDIS study data can be taken from the clinicaltrials.gov and EUdraCT XML formats, and which actions are required to complete the ADDIS study. The actions are explained in more detail below.

| ADDIS study data   | Linked to | clinicaltrials.gov data | EUdraCT data        | required action |
|--------------------|-----------|-------------------------|---------------------|-----------------|
| Study information  | N/A       | present                 | present             | none |
| Population information | N/A   | present (flat text)     | screening present (flat text) | none |
| Arms                | N/A       | missing                 | Present for baselines and endpoints | ctgov: merge groups that are the same, classify groups as arms. EUdraCT: classify adverse events groups as arms, merge those groups into correct arms |
| Epochs              | N/A       | missing (flat text)     | missing (flat text) | Generate based on study design description       |
| Activities         | N/A       | flat text               | flat text           | Generate based on study design and treatment descriptions |
| Study design       | arms, epochs, activities | flat text | flat text          | Generate based on study design description       |
| Measurement moments | Epoch     |     Flat text           | Flat text           | link to epoch; merge redundant measurement moments |
| Baseline characteristics | measurement moments | present  | present             | none |
| Endpoints          | measurement moments | present  | present             | In case of misused categories: add measurement moments/variables |
| Adverse events          | measurement moments | present  | present             | none |
| Concept mappings   | variables, treatment drugs (defined in activities), units | N/A | N/A | Map study-level concepts to dataset-level concepts |

Import sections and required actions
------------------------------------

### Study information

Study information is a collective term for basic descriptives of the study: the group allocation, blinding, study status, number of centers and study objective. This information is automatically extracted. EUdraCT todo: import number of centers.

### Population information

Population information is a description of how participants were enrolled in the study, including the health condition of interest and the eligibility (inclusion and exclusion) criteria. Screening text is automatically extracted. Indication is not (but could be for ct.gov).

### Arms and other groups

Arms are groups into which study participants are divided which receive different interventions. Any group that is not an arm,  including the total study population, is classified as an 'other group'. ADDIS analyses only use measurements that are linked to arms. Our importers only classify groups as arms when they are explicitly marked as such in the XML. Therefore frequently after importing a study a user with knowledge of the trial should indicate which of the imported groups are arms.

In clinicaltrials.gov XML, measurements are linked to groups but these groups are never explicitly indicated as being arms. Further, different group ids are used for different measurement types (baseline, endpoint etc.) causing duplicate groups.

In EudraCT XMl, baseline and endpoint measurements are linked to groups explicitly defined as arms. Adverse event measurements however are not linked to arms, but to their own groups.

The ADDIS study interface lets users reclassify groups as arms and vice versa, and allows the merging together of redundant groups, e.g. in an imported EUdraCT study often the 'other group's that measure adverse events are the same as the treatment arms, and as such can be safely reclassfied as arms and merged.

### Epochs

Epochs are an ordered segmentation of the running time of a study into segments in which a similar activity takes place for all arms, e.g. washout, randomization, treatment, follow-up. Epochs can have any duration, including instantaneous for activities such as randomization. Epochs let us represent more complex study designs such as crossover studies in a structured way. The main treatment phase should be indicated as the primary epoch.

Neither XML format has such a structured representation of time, and as such the epoch(s) need to be created after importing a study. ADDIS analyses expect at least a primary (treatment) epoch.

### Activities

An activity describes something that happens to participants during a specific epoch, e.g. during the "treatment" epoch, the "aspirin arm" might receive "20 mg/day of aspirin". In addition to treatments, activities include things such as wash-out and randomization. ADDIS' structured representation of drug treatments enables analyses to categorise data according to e.g. dosage or combination therapies.

Neither EUdraCT nor clinicaltrials.gov XML have structured representations of the performed activities. They must be created in the ADDIS interface after importing. The ADDIS manual explains this in more detail in the [guided study entry example](https://addis-test.drugis.org/manual.html#study-entry-example).

### Study design

The study design table captures which activities were applied to each arm during each of epochs in the study.

This table must be filled in after importing a study and creating the appropriate arms, epochs, and activities. Treatment activities that are not assigned to an arm/epoch combination in the study design will be ignored when retrieving data for analyses.

### Measurement moments

Measurement moments indicate at which point in time a measurement is taken. Frequently, variables will be measured multiple times during a study. For example, at 4-week intervals during a 12-week treatment, and then once more after a 4-week treatment-free follow-up period. In ADDIS, these moments are defined relative to either the end or beginning of an epoch. So in the example above, there might be several measurement moments defined as '4 weeks after start of treatment', '8 weeks after start of treatment', 'at end of treament' and '4 weeks after end of treatment.' ADDIS supports data entry for multiple moments for a variable.

Neither clinicaltrials.gov nor EUdraCT XML support a structured representation of measurement moments. Variables have a 'time frame' property, but this flat text, and frequently this text even differs between variables when clearly the same moment is intended (e.g. '12 months (The course of treatment for each participant in the trial)', 'Measured at Month 12' and '12 months' all occurring within the same study). Further, neither format supports a structured way for a single variable to have measurements for multiple measurement moments.

After importing a study, redundant measurement moments should be merged together, and then the remaining measurement moments should be anchored to an epoch.

Further, it may be necessary to create more measurement moments in the case that many measurements were entered for the same moment. 

### Baseline characteristics

These are variables that are typically measured at the start of a study, and describe the enrolled population and/or are used to assess whether randomization led to adequate balance between the arms of the study. 

Baseline characteristics and their measured data should be imported automatically.

### Endpoints

Endpoints are the quantities of interest in the study.

Baseline characteristics and their measured data should be imported automatically, **except** for cases where categories were abused to group similar variables into one, or enter data for multiple measurement moments. In these cases, only the data for the first 'category' are imported. A way to import these extra data and use the ADDIS interface to split them among multiple outcomes or measurement moments is in progress.

### Adverse events

Adverse events are unintended events observed during the study, which may or may not have been caused by the study interventions.

Adverse events and their measured data should be imported automatically.

### Concept mappings

A concept mapping harmonizes a term used at the study level (e.g. "50% or greater improvement on HAM-D"), to a concept defined at the dataset level (e.g. "HAM-D response"). Concept mappings enable greater flexibility in re-using study extractions, and enable automatic extraction of data from a set of studies. Study concepts are automatically generated when outcomes or drug treatments are created. Their mappings to dataset-level concepts are performed by hand.

Concept mappings are ADDIS-specific, and as such are not automatically generated when importing a study. In general we recommend first creating the concepts of interest at the dataset level (primary outcomes, drugs, units) and only then importing the studies and mapping the specific study concepts to these general dataset concepts.
