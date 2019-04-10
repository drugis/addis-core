EUdraCT XML - ADDIS data model Gap Analysis
===========================================

Requirements
------------

To automatically extract measurement data from studies, ADDIS requires those studies to be appropriately structured. This means that the following needs to be defined:

- Treatment arms (subgroups of the study population).
- Epochs (the progression and partitioning in time of the study). *Examples: Screening, Randomization, Treatment, Follow-up.*
- Activities. *Examples: randomization, washout, 30 milligram aspirin per day.*
- The study design: which activity took place in each arm, during which epoch.
- Measurement moments (when variables were measure). These need to be linked to an epoch. *Examples: "At the end of the treatment phase", "12 weeks after the start of the treatment phase."*
- Variables (Baseline characteristics, endpoints, adverse events), plus which type of measurements they measure, plus at which measurement moment(s) they were measured. *Examples: Headache measures the number of subjects with headache and the sample size, at the measurement moment .*
- Concepts and their mappings to dataset-level concepts.

| ADDIS data         | Linked to | ClinicalTrials.gov data | EUdraCT data        | required action |
|--------------------|-----------|-------------------------|---------------------|-----------------|
| Study information  | N/A       | present                 | present             | none |
| Population information | N/A   | present (flat text)     | screening present (flat text) | none |
| Arms                | N/A       | missing                 | Present for baselines and endpoints | ctgov: merge groups that are the same, classify groups as arms. EUdraCT: classify adverse events groups as arms, merge those groups into correct arms |
| Epochs              | N/A       | missing (flat text)     | missing (flat text) | Generate based on study design description       |
| Activities         | N/A       | flat text               | flat text           | Generate based on study design and treatment descriptions |
| Study design       | arms, epochs, activities | flat text | flat text          | Generate based on study design description       |
| Measurement moments | Epoch     |     Flat text           | Flat text           | link to epoch; merge redundant measurement moments |
| Baseline characteristics | measurement moments | present  | present             | none |
| Endpoints          | measurement moments | present  | present             | none |
| Adverse events          | measurement moments | present  | present             | ctgov: none |
| Concept mappings   | variables, treatment drugs (defined in activities), units | N/A | N/A | Map study-level concepts to dataset-level concepts |

EUdraCT actions in detail
-------------------------

### Study information

- Group allocation, blinding, study status, number of centers, objective
\#centers, status needs  importing still

### Population information

- Indication missing, eligibility imported.

### Arms / groups

- Endpoint arms and baseline arms are linked by ID. AE groups are not explicitly linked to arms.
- Overall population does not have a custom tag/group

### Epochs

- Entirely missing.

### Activities

- Entirely missing

### Study design

- Entirely missing

### Measurement moments

- Textually indicated in `<timeframe>` tag. No structured representation, no support for multiple measurement moments.
- Often need both merging and splitting.

### Baseline characteristics

- All imported fine.

### Endpoints

- All imported OK, **except** cases where categories were abused to group similar variables into one, or enter data for multiple measurement moments.

### Adverse events

- All imported OK.

### Concept mappings

- ADDIS-specific, i.e. not present.

