ADDIS excel/table study data export
===================================

This describes an export format for ADDIS study data to an excel format with minimal loss of structure. The format places a complete machine-readable data table in the first worksheet, intended for consumption by analytical tools or non-addis users. The study structure, concept mappings, etc. are placed on separate worksheets to allow the study to be re-imported into ADDIS without having to rebuild the structural aspects by hand.

ADDIS study export
==================

Sheet 1: Study data
-------------

- **headers**
  - 1 row for subcategories, marked in bold below
  - 1 row for variable names; each variable column should have the name of the variable in this row (use a reference (e.g. `=Concepts!B2`))
  - 1 row containing column names 

- **Per arm 1 row**
- 1 extra 'overall population' row

- columns: 
  - **Study Information: (only fill out on 1st row of study)**
    - *Empty row*
      - id (short title)
      - addis url (of study)
      - title (full title)
      - group allocation (randomised, etc)
      - blinding (double blind, etc)
      - status (completed, etc)
      - number of centers
      - objective (free text)
  - **Population information (only fill out on 1st row of study)**
    - *Empty row*
      - indication (free text)
      - eligibility criteria (free text; inclusion/exclusion criteria)
  - **Arm information**
    - *Empty row*
      - title
      - description
  - **Measurement data (repeat below for each variable)**
    - *Variable name* (reference to concept, e.g. `=Concepts!B6`)
      - variable type (baseline characteristic, endpoint, adverse event)
      - measurement type (count, continuous, categorical, survival)
      - **repeat below once for each measurement moment**
        - measurement moment (reference to title of measurement moment on their worksheet (e.g. `='Measurement moments'!B3`))
        - **one column per category in case of categorical variable (e.g. 'male', 'female')) or per measured result property in all other cases, (e.g. mean, std.err)**

Activities
----------

The activities are represented on a seperate worksheet.

- columns:
  - id (URI)
  - title
  - type
  - description
  - **if drug treatment** repeat following columns once per treatment drug
    - drug label (reference to concepts worksheet (e.g. `=Sheet2!B2`))
    - dose type (fixed/titrated)
    - dose (amount)
    - max dose
    - unit (reference to concepts worksheet (e.g. `=Sheet2!B2`))
    - periodicity (duration string)

Epochs
------

Epochs are represented on a separate worksheet.

- columns:
  - id (URI)
  - name
  - description
  - duration
  - isPrimary

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
  - from (start or end)
  - offset (duration string)

Concepts
--------

Concepts are represented on a separate worksheet.

**1 row per variable, drug and unit**

- columns:
  - id (URI) (in case of outcome or drug, reference to e.g. `=Sheet2!B2`)
  - label
  - type
  - dataset concept uri (only relevant for dataset export)
  - multiplier (only relevant for units, and dataset export)

Dataset export
==============

The dataset export has the same structure as the study export, but there are data for multiple studies per worksheet, and there are two extra worksheets with dataset information, metadata and concepts.
**In order to handle studies that measure difference variable we let each study have its own variables. We currently do not harmonise across the entire dataset. In the future options such as concept selection might considered.**

Study data
----------
 - First row with headers is only done once, the rest is repeated for every study.

Activities / Epochs / Study design / Measurement moments / Concepts
-------------------------------------------------------------------
- First row with headers is only done once.
- **For every study**
  - first row is a reference to the study title (e.g. `='Study data'!A4`)
  - Then the same as a single study
  - Then an empty row to separate form the next study

Dataset metadata
----------------

- columns: 
  - title
  - ADDIS url
  - description

Concepts
--------

- columns:
  - id (URI)
  - label
  - type (Variable, Drug, Unit)
