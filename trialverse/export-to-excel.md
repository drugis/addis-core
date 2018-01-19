ADDIS excel/table study data export
===================================

This describes an export format for ADDIS study data to an excel format with minimal loss of structure. The format places a complete machine-readable data table in the first worksheet, intended for consumption by analytical tools or non-addis users. The study structure, concept mappings, etc. are placed on separate worksheets to allow the study to be re-imported into ADDIS without having to rebuild the structural aspects by hand.

Notes/considerations:

- cell references may be better to just be data copies instead since it's an export anyway
- most ID/uri fields may be redundant if we choose to regenerate uris on import anyway
- a decision needs to be made about dataset export, see the relevant section.
- the import to addis may be best to make as a clojure module since we have already a complete addis import toolset there.

ADDIS study export
==================

Sheet 1: data
-------------

- **headers**
  - 1 row for subcategories, marked in bold below
  - 1 row for variable names; each variable column should have the name of the variable in this row (use a reference (e.g. `=Concepts!B2`))
  - 1 row containing column names 

- **Per arm 1 row**
- 1 extra 'overall population' row

- columns: 
  - **Study Information: (only fill out on 1st row of study)**
    - id (short title)
    - addis url of study
    - full title
    - group allocation (randomised, etc)
    - blinding (double blind, etc)
    - status (completed, etc)
    - number of centers
    - objective (free text)
  - **Population information (only fill out on 1st row of study)**
    - indication (free text)
    - Eligibility (free text; inclusion/exclusion criteria)
  - **arm information**
    - title
    - description
    - treatment (reference to activities worksheet, title of treatmentactivity of primary epoch for this arm)
  - **measurement data (repeat below for each variable)**
    - id (URI) (required for concept mapping)
    - variable type (baseline characteristic, endpoint, adverse event)
    - measurement type (count, continuous, categorical, survival)
    - **repeat below once for each measurement moment**
      - measurement moment (reference to title of measurement moment on their worksheet (e.g. `=Sheet1!B2`))
      - **one column per category (in case of categorical variable) or per measured result property, e.g. mean, std.err**

Activities
----------

The activities are represented on a third worksheet.

- columns:
  - id (URI)
  - title
  - type
  - description
  - **if drug treatment** repeat following columns once per treatment drug
  - drug label (reference to concepts worksheet (e.g. `=Sheet2!B2`))
  - dose type (fixed/titrated)
  - dose amount
  - dose unit
  - dose periodicity (duration string)

Epochs
------

Epochs are represented on a separate worksheet.

- columns:
  - id (URI)
  - name
  - description
  - duration
  - isPrimary?

Study design
------------

The study design is represented on a separate worksheet.

- **Design** (as in addis interface)
  - 1 row per arm
  - 1 column for the arm name
  - 1 column per epoch
  - 1 activity per cell

- epoch column names are filled with references to the title cells of the epochs table on their worksheet (e.g. `=Sheet2!B2`)
- row names are filled with references to the arm title cells of the study data worksheet (e.g. `=Sheet1!B2`)
- Activity cells are filled with references to the title cells of the activities table on their worksheet (e.g. `=Sheet2!B2`)


Measurement moments
-------------------

Measurement moments are represented on a separate worksheet

- columns: 
  - id (URI)
  - name
  - epoch (reference to epoch name (e.g. `=Sheet1!B2`))
  - relativeToAnchor (start or end)
  - offset (duration string)

Concepts
--------

Concepts are represented on a separate worksheet.

**1 row per variable, drug and unit**

- columns:
  - id (URI) (in case of outcome or drug, reference to e.g. `=Sheet2!B2`)
  - label
  - type
  - dataset concept URI (only relevant for dataset export)
  - mapping multiplier (only relevant for units, and dataset export)

Dataset export
==============

The dataset export has the same structure as the study export, but there are data for multiple studies per worksheet, and there are two extra worksheets with dataset information, metadata and concepts.
**Required decision: how to handle build-up of columns if different studies measure different variables. Concept selection before export step, to harmonise/unify?**

Dataset metadata
----------------

- columns: 
  - title
  - addis url
  - creation date
  - ??

Concepts
--------

- columns:
  - id (URI)
  - label
  - type

