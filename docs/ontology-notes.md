ADDIS ontology design notes
===========================

Outcomes and Measurements
-------------------------

ADDIS 1.x supports three types of things that may be measured in a study:

 - Endpoints: measures intended to capture the effects of treatment (also called "outcomes")
 - Population characteristics: measures intended to capture the characteristics of the population at baseline (also called "baseline characteristics")
 - Adverse events: unintended health effects recorded during a trial

This categorization is problematic in a number of ways. First, because from a data modeling standpoint the categories are highly similar, but there is no clear label to attach to all three. ADDIS 1.x sometimes uses "outcome" or "outcome measure", but these are typically understood to refer only to predefined measures of treatment effect, and not to baseline characteristics. The BRIDG model uses "observation", but this seems appropriate at the level of the individual, but not the aggregate. An appropriate label might be "observation aggregation". Second, the categories are not always clearly delineated. For example, if blood pressure is measured at baseline as well as at later times during the study, it is both a baseline characteristic and an outcome. Having to define it twice seems undesirable. For adverse events, once a safety issue is known it may be studies as an outcome in newer trials. Such a reclassification should not result in the newer study not being found when looking for studies that investigate the adverse event in question. The following hierarchy may work:

 - Observation aggregation
    - Outcome: an observation aggregation that was predefined, and is measured at least once after baseline
    - Baseline characteristic: an observation aggregation that was predefined, and is measured at baseline
    - Adverse event: an observation aggregation that was not predefined, and counts the number of individuals experiencing a clinical event

This implies that there are also observation aggregations that fall in none of these three categories, though they are probably exceedingly rare. The "outcome" and "baseline characteristic" classes are not mutually exclusive. From the perspective of the user interface, the term "observation aggregation" is not desirable. A user interface might combine the outcome and baseline characteristics sections and automatically "tag" which ones are outcomes and which ones are baseline characteristics (or both). A separate adverse events section seems unavoidable.

Another source of confusion arises when defining the measurement type of an observation aggregation. For example, the occurrence (or non-occurrence) of an adverse event is a dichotomous variable at the individual level, but does not take a yes/no value at the aggregate level. Therefore calling it a dichotomous variable at the aggregate level seems inappropriate. This could be solved by distinguishing the statistical analysis from the individual level variable:

    <outcomeA> a ontology:ObservationAggregation ;
      rdfs:label "HAM-D Responders" ;
      ontology:is_aggregation_of [
        a ontology:DichotomousObservation ;
        rdfs:label "HAM-D Response" ;
        rdfs:comment "Treatment response defined as an improvement of >= 40% on the HAM-D rating scale." ] ;
      ontology:is_prespecified true ;
      ontology:has_analysis <analysisX> .

The DichotomousObservation becomes the logical anchor for between-study harmonization, and would also apply in matching individual patient data between studies. It is especially important to capture the above kind of observation accurately, as it is probably the most prevalent type of observation (where each patient is defined as a "success" if they experience a specific event at least once).

A third problem occurs in recording the results of the statistical analysis; it may result in a mean, standard deviation, median, 2.5% quantile, etc. How do we allow flexibility as to which properties are reported? We currently do this as follows:

    <x> has_result_property ontology:mean .
    <measurement> ontology:mean 12 .

However it is difficult to capture this in an OWL model. A solution is needed but has not been found.

Subgroups and categorical variables
-----------------------------------

Reporting by subgroup is important in some trials. Subgroups are defined by some criterion that allows each individual to be assigned to one of a finite number of mutually exclusive and exhaustive categories. Aggregation then happens over a subgroup, or over the intersection of a subgroup with another subgroup. There are two notable types of subgroups:

 - Arms are subgroups created through randomization.
 - Strata are subgroups created prior to randomization, and which modify the randomization procedure to ensure balance of subgroups over arms (see "stratified randomization").

The following hierarchy applies:

 - Group
    - StudyPopulation
    - Subgroup
       - Arm
       - Stratum

Each Subgroup is a subdivision of some StudyPopulation. To capture how the StudyPopulation is subdivided by subgroups, we must also capture which combinations of subgroups are mutually exclusive and exhaustive, e.g. that "Male" and "Female" together subdivide the StudyPopulation by gender. These are exactly categorical variables:

    <gender> a ontology:CategoricalObservation ;
      ontology:has_category
        [ a ontology:Category ;
          rdfs:label "Male" ] ,
        [ a ontology:Category ;
          rdfs:label "Female" ] .

Note that according to RDF semantics, the above collection of categories is not necessarily exhaustive (and in the case of gender that may be appropriate). This may be desirable as in practice the categories may be exhaustive within the study, but not in a broader context. Subgroups are then created by making such a CategoricalObservation at a specific time.

This could also apply to the reporting of categorical variables themselves, as these are just a head count over each category at a certain time. The difference is that the time of measurement is also the time at which the subgroup is created, whereas in reporting by subgroups the subgroup is typically created at some time prior to the measurement.

Subgroups of arms and subgroups of subgroups of arms can be defined through an IntersectionGroup, with appropriate OWL axioms.
