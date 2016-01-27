(ns org.drugis.addis.rdf.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [blank? lower-case]]
            [riveted.core :as vtd]
            [org.drugis.addis.rdf.trig :as trig]))

(defn spo-each [subj pred obj*]
  (reduce (fn [subj obj] (trig/spo subj [pred obj])) subj obj*))

(defn map-vals [f m] (into {} (for [[k v] m] [k (f v)])))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn unit-rdf [xml uri]
  (let [unit-name (vtd/attr xml :name)
        unit-symbol (vtd/attr xml :symbol)
        subj (cond
               (= unit-name "gram") (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :qudt "Gram")])
               (= unit-name "liter") (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :qudt "Liter")])
               :else uri)]
    (trig/spo subj 
              [(trig/iri :rdf "type") (trig/iri :owl "Class")]
              [(trig/iri :rdfs "label") (trig/lit unit-name)]
              [(trig/iri :qudt "symbol") (trig/lit unit-symbol)])))

(defn drug-rdf [xml uri]
  (let [atcCode (vtd/attr xml :atcCode)
        subj (if (or (nil? atcCode) (blank? atcCode))
               uri
               (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :atc atcCode)]))]
    (trig/spo subj
         [(trig/iri :rdf "type") (trig/iri :ontology "Drug")]
         [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))])))

(defn indication-rdf [xml uri]
  (let [snomedCode (vtd/attr xml :code)
        subj (if (or (nil? snomedCode) (blank? snomedCode))
               uri
               (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :snomed snomedCode)]))]
    (trig/spo subj 
         [(trig/iri :rdf "type") (trig/iri :ontology "Indication")]
         [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))])))

(defn variable-rdf [xml uri]
  (let [m-type-map { "rate" "dichotomous"
                     "continuous" "continuous"
                     "categorical" "categorical" }
        m-type (vtd/first-child xml)
        subj (trig/spo uri 
                       [(trig/iri :rdf "type") (trig/iri :ontology "Variable")]
                       [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
                       [(trig/iri :rdfs "comment") (trig/lit (vtd/attr xml :description))]
                       [(trig/iri :ontology "measurementType") (trig/iri :ontology (m-type-map (vtd/tag m-type)))])]
    (case (vtd/tag m-type)
      "rate" subj
      "continuous" (trig/spo subj [(trig/iri :rdfs "comment") (trig/lit (vtd/attr m-type :unitOfMeasurement))])
      "categorical" (trig/spo subj [(trig/iri :ontology "categoryList") (trig/coll (map #(trig/lit (vtd/text %)) (vtd/search m-type "./category")))]))))

(defn import-entity [xml rdf-fn]
  (let [uri (trig/iri :entity (uuid))]
    {:id (vtd/attr xml :name)
     :uri uri
     :rdf (rdf-fn xml uri)}))

(defn entities-uri-map [entities]
  (reduce #(assoc %1 (:id %2) (:uri %2)) {} entities))

(defn entities-rdf [entities]
  (map #(:rdf %) entities))

(defn import-entities [xml xpath entity-rdf-fn]
  (let [entities (map #(import-entity % entity-rdf-fn) (vtd/search xml xpath))]
    [(entities-uri-map entities) (entities-rdf entities)]))

(defn studyCharVal
  [xml charName]
  (vtd/text (vtd/at xml (str "./characteristics/" charName "/value"))))

(def allocationTypeUri
  {"RANDOMIZED" (trig/iri :ontology "AllocationRandomized")
   "NONRANDOMIZED" (trig/iri :ontology "AllocationNonRandomized")})

(defn allocation-rdf [subj xml]
  (let [allocation (allocationTypeUri (studyCharVal xml "allocation"))]
    (if allocation
      (trig/spo subj [(trig/iri :ontology "has_allocation") allocation])
      subj)))

(def blindingTypeUri
  {"OPEN" (trig/iri :ontology "OpenLabel")
   "SINGLE_BLIND" (trig/iri :ontology "SingleBlind")
   "DOUBLE_BLIND" (trig/iri :ontology "DoubleBlind")
   "TRIPLE_BLIND" (trig/iri :ontology "TripleBlind") })

(defn blinding-rdf [subj xml]
  (let [blinding (blindingTypeUri (studyCharVal xml "blinding"))]
    (if blinding
      (trig/spo subj [(trig/iri :ontology "has_blinding") blinding])
      subj)))

(def statusTypeUri
  {"NOT_YET_RECRUITING" (trig/iri :ontology "StatusNotYetRecruiting")
   "RECRUITING" (trig/iri :ontology "StatusRecruiting")
   "ENROLLING" (trig/iri :ontology "StatusEnrolling")
   "ACTIVE" (trig/iri :ontology "StatusActive")
   "COMPLETED" (trig/iri :ontology "StatusCompleted")
   "SUSPENDED" (trig/iri :ontogogy "StatusSuspended")
   "TERMINATED" (trig/iri :ontology "StatusTerminated")
   "WITHDRAWN" (trig/iri :ontology "StatusWithdrawn")
   "UNKNOWN" (trig/iri :ontology "StatusUnknown")})

(defn status-rdf [subj xml]
  (let [status (statusTypeUri (studyCharVal xml "status"))]
    (if status
      (trig/spo subj [(trig/iri :ontology "status") status])
      subj)))

(defn as-int [string] (if (nil? string) nil (Integer. string)))

(defn centers-rdf [subj xml]
  (let [centers (as-int (studyCharVal xml "centers"))]
    (if centers
      (trig/spo subj [(trig/iri :ontology "has_number_of_centers") (trig/lit centers)])
      subj)))

(defn date-start-rdf [subj xml]
  (let [start-date (studyCharVal xml "study_start")]
    (if start-date 
      (trig/spo subj [(trig/iri :ontology "has_start_date") (trig/lit start-date (trig/iri :xsd "date"))])
      subj)))

(defn date-end-rdf [subj xml]
  (let [end-date (studyCharVal xml "study_end")]
    (if end-date 
      (trig/spo subj [(trig/iri :ontology "has_end_date") (trig/lit end-date (trig/iri :xsd "date"))])
      subj)))

(defn objective-rdf [subj xml]
  (let [objective (studyCharVal xml "objective")]
    (trig/spo subj [(trig/iri :ontology "has_objective")
                    (trig/_po [(trig/iri :rdfs "comment") (trig/lit objective)])])))

(defn eligibility-rdf [subj xml]
  (let [inclusion (studyCharVal xml "inclusion")
        exclusion (studyCharVal xml "exclusion")
        eligibility (str "Inclusion criteria:\n\n" inclusion "\n\nExclusion criteria:\n\n" exclusion)]
    (trig/spo subj [(trig/iri :ontology "has_eligibility_criteria")
                    (trig/_po [(trig/iri :rdfs "comment") (trig/lit eligibility)])])))

(defn publications-rdf [subj xml]
  (let [pmids (map #(str "http://pubmed.com/" (vtd/text %)) (vtd/search xml "./characteristics/references/pubMedId"))
        pairs (map (fn [x] [(trig/iri :ontology "has_publication") (trig/_po [(trig/iri :ontology "has_id") (trig/iri x)])]) pmids)]
    (apply trig/spo subj pairs)))

(defn study-indication-rdf [xml instance-uri entity-uris]
  (let [entity-name (vtd/attr (vtd/at xml "./indication") :name)
        entity-uri ((:indication entity-uris) entity-name)]
    (trig/spo instance-uri
              [(trig/iri :rdf "type") (trig/iri :ontology "Indication")]
              [(trig/iri :owl "sameAs") entity-uri]
              [(trig/iri :rdfs "label") entity-name])))

(defn when-taken-key [xml]
  {:howLong (vtd/attr xml :howLong)
   :relativeTo (vtd/attr xml :relativeTo)
   :epochName (vtd/attr (vtd/at xml "./epoch") :name)})

(defn result-properties [xml]
  (case (vtd/tag (vtd/first-child xml))
    "rate" [(trig/iri :ontology "count") (trig/iri :ontology "sample_size")]
    "continuous" [(trig/iri :ontology "mean") (trig/iri :ontology "standard_deviation") (trig/iri :ontology "sample_size")]
    "categorical" []))

; clojure.string.capitalize lowercases the remainder of the string, hence workaround
(defn capitalize [s]
  (if (> (count s) 0)
    (str (Character/toUpperCase (.charAt s 0))
         (subs s 1))
    s))

(defn study-outcome-rdf [xml instance-uri entity-uris measurement-moment-uris]
  (let [entity-name (vtd/attr (vtd/first-child xml) :name)
        entity-type (vtd/tag (vtd/first-child xml))
        entity-uri ((entity-uris (keyword entity-type)) entity-name)
        entity-xml (vtd/at xml (str "//addis-data/" entity-type "s/*[@name='" entity-name "']"))
        mms (map #(measurement-moment-uris (when-taken-key %)) (vtd/search xml "./whenTaken"))]
    (-> 
      (trig/spo instance-uri
                [(trig/iri :rdf "type") (trig/iri :ontology (capitalize entity-type))]
                [(trig/iri :rdfs "label") entity-name]
                [(trig/iri :rdfs "comment") (trig/lit (vtd/attr entity-xml :description))]
                [(trig/iri :ontology "of_variable") (variable-rdf entity-xml (trig/_po [(trig/iri :owl "sameAs") entity-uri]))])
      (spo-each (trig/iri :ontology "has_result_property") (result-properties entity-xml))
      (spo-each (trig/iri :ontology "is_measured_at") mms))))

(defn study-drug-rdf [xml instance-uri entity-uris]
  (trig/spo instance-uri
            [(trig/iri :rdf "type") (trig/iri :ontology "Drug")]
            [(trig/iri :owl "sameAs") ((entity-uris :drug) (vtd/attr xml :name))]
            [(trig/iri :rdfs "label") (vtd/attr xml :name)]))

(defn dose-unit-key [xml]
  {:prefix (vtd/attr xml :scaleModifier)
   :baseUnit (vtd/attr (vtd/at xml "./unit") :name)})

(def unitPrefix
  {"MEGA"  1e6
   "KILO"  1e3
   "UNIT"  1e0
   "MILLI" 1e-3
   "MICRO" 1e-6
   "NANO"  1e-9})

(defn study-unit-rdf [xml instance-uri entity-uris]
  (let [unit-key (dose-unit-key xml)]
    (trig/spo instance-uri
              [(trig/iri :rdf "type") ((entity-uris :unit) (:baseUnit unit-key))]
              [(trig/iri :rdfs "label") (str (lower-case (:prefix unit-key)) (:baseUnit unit-key))]
              [(trig/iri :qudt "conversionMultiplier") (unitPrefix (:prefix unit-key))])))

(defn study-arm-rdf [xml instance-uri]
  (trig/spo instance-uri
            [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
            [(trig/iri :rdf "type") (trig/iri :ontology "Arm")]))

(defn fix-duration
  [duration]
  (if (= "P0D" duration) "PT0S" duration))

(defn study-epoch-rdf [xml instance-uri]
  (let [epoch-name (vtd/attr xml :name)
        duration (fix-duration (vtd/text (vtd/at xml "./duration")))
        subj (if duration
               (trig/spo instance-uri [(trig/iri :ontology "duration") (trig/lit duration (trig/iri :xsd "duration"))])
               instance-uri)]
  (trig/spo subj 
            [(trig/iri :rdfs "label") (trig/lit epoch-name)]
            [(trig/iri :rdf "type") (trig/iri :ontology "Epoch")])))

(defn activity-other-rdf [subj xml study-drug-uris unit-uris]
  (trig/spo subj
       [(trig/iri :rdf "type") (trig/iri :ontology "StudyActivity")]
       [(trig/iri :rdfs "comment") (trig/lit (vtd/text xml))]))

(defn activity-predefined-rdf [subj xml study-drug-uris unit-uris]
  (let [activity-predefined {"RANDOMIZATION" "RandomizationActivity"
                             "SCREENING" "ScreeningActivity"
                             "WASH_OUT" "WashOutActivity"
                             "FOLLOW_UP" "FollowUpActivity"}]
    (trig/spo subj [(trig/iri :rdf "type") (trig/iri :ontology (activity-predefined (vtd/text xml)))])))

(defn dose-rdf [xml attr unit-uris]
  (trig/_po [(trig/iri :rdf "value") (trig/lit (Double. (vtd/attr xml attr)))]
            [(trig/iri :ontology "unit") (unit-uris (dose-unit-key (vtd/at xml "./doseUnit")))]
            [(trig/iri :ontology "dosingPeriodicity") (trig/lit (vtd/attr (vtd/at xml "./doseUnit") :perTime) (trig/iri :xsd "duration"))]))

(defn treatment-rdf [xml study-drug-uris unit-uris]
  (let [subj (trig/_po [(trig/iri :ontology "treatment_has_drug") (study-drug-uris (vtd/attr (vtd/at xml "./drug") :name))])
        fixedDose (vtd/at xml "./fixedDose")
        flexibleDose (vtd/at xml "./flexibleDose")]
    (cond
      fixedDose (trig/spo subj
                          [(trig/iri :rdf "type") (trig/iri :ontology "FixedDoseDrugTreatment")]
                          [(trig/iri :ontology "treatment_dose") (dose-rdf fixedDose :quantity unit-uris)])
      flexibleDose (trig/spo subj
                             [(trig/iri :rdf "type") (trig/iri :ontology "TitratedDoseDrugTreatment")]
                             [(trig/iri :ontology "treatment_min_dose") (dose-rdf flexibleDose :minDose unit-uris)]
                             [(trig/iri :ontology "treatment_max_dose") (dose-rdf flexibleDose :maxDose unit-uris)])
      :else subj)))

(defn activity-treatment-rdf [subj xml study-drug-uris unit-uris]
  (let [drug-treatments (map #(treatment-rdf % study-drug-uris unit-uris) (vtd/search xml "./drugTreatment"))]
    (spo-each (trig/spo subj [(trig/iri :rdf "type") (trig/iri :ontology "TreatmentActivity")])
              (trig/iri :ontology "has_drug_treatment") drug-treatments)))

(def activity-rdf
  {"predefined" activity-predefined-rdf
   "other" activity-other-rdf
   "treatment" activity-treatment-rdf})

(defn activity-used-by-rdf
  [subj xml arm-uris epoch-uris]
  (let [used-by (trig/_po [(trig/iri :ontology "applied_to_arm") (arm-uris (vtd/attr xml "arm"))]
                          [(trig/iri :ontology "applied_in_epoch") (epoch-uris (vtd/attr xml "epoch"))])]
    (trig/spo subj [(trig/iri :ontology "has_activity_application") used-by])))

(defn study-activity-rdf [xml activity-uri entity-uris arm-uris epoch-uris study-drugs unit-uris]
  (let [activity (vtd/first-child (vtd/at xml "./activity"))
        activity-type (vtd/tag activity)
        used-by (fn [subj xml] (activity-used-by-rdf subj xml arm-uris epoch-uris))
        name (vtd/attr xml :name)
        activity-rdf ((activity-rdf activity-type) activity-uri activity study-drugs unit-uris)
        subj (trig/spo activity-rdf [(trig/iri :rdfs "label") (trig/lit name)])]
    (reduce used-by subj (vtd/search xml "./usedBy"))))

(defn arm-receives-treatment
  [studyNode epochName armName]
  (let [usedBys (vtd/search studyNode "./activities/studyActivity/usedBy")
        usedBy (first (filter #(and (= armName (vtd/attr % :arm)) (= epochName (vtd/attr % :epoch))) usedBys))]
    (and (not (nil? usedBy)) (not (nil? (vtd/at usedBy "../activity/treatment"))))))

(defn is-treatment-epoch
  [studyNode epochName]
  (let [arms (map #(vtd/attr % :name) (vtd/search studyNode "./arms/arm"))]
    (every? #(arm-receives-treatment studyNode epochName %) arms)))

(defn find-first-treatment-epoch
  [studyNode]
  (let [epochNames (map  #(vtd/attr % :name) (vtd/search studyNode "./epochs/epoch"))]
    (first (filter #(is-treatment-epoch studyNode %) epochNames))))

(defn primary-epoch-rdf
  [subj uri]
  (if uri
    (trig/spo subj [(trig/iri :ontology "has_primary_epoch") uri])
    subj))

(def anchorUri
  {"FROM_EPOCH_START" (trig/iri :ontology "anchorEpochStart")
   "BEFORE_EPOCH_END" (trig/iri :ontology "anchorEpochEnd")})

(defn study-measurement-moment-rdf
  [xml instance-uri epoch-uris]
  (let [mm (when-taken-key xml)]
    (trig/spo instance-uri 
              [(trig/iri :rdf "type") (trig/iri :ontology "MeasurementMoment")]
              [(trig/iri :rdfs "label") (str (:howLong mm) " " (:relativeTo mm) " " (:epochName mm))]
              [(trig/iri :ontology "relative_to_epoch") (epoch-uris (:epochName mm))]
              [(trig/iri :ontology "relative_to_anchor") (anchorUri (:relativeTo mm))]
              [(trig/iri :ontology "time_offset")
               (trig/lit (fix-duration (:howLong mm)) (trig/iri :xsd "duration"))])))

(defn study-measurement-rdf
  [xml subj study-outcomes arm-uris mm-uris]
  (let [som-id (vtd/attr (vtd/at xml "./studyOutcomeMeasure") :id)
        som-uri (:uri (study-outcomes som-id))
        arm-name (vtd/attr (vtd/at xml "./arm") :name)
        arm-uri (arm-uris arm-name)
        when-taken-key (when-taken-key (vtd/at xml "./whenTaken"))
        mm-uri (mm-uris when-taken-key)
        measurement (trig/spo subj
                              [(trig/iri :ontology "of_outcome") som-uri]
                              [(trig/iri :ontology "of_arm") (if arm-uri arm-uri (trig/lit "OVERALL"))]
                              [(trig/iri :ontology "of_moment") mm-uri])
        cont (vtd/at xml "./continuousMeasurement")
        rate (vtd/at xml "./rateMeasurement")
        catg (vtd/at xml "./categoricalMeasurement")]
    (cond
      cont (trig/spo measurement 
                     [(trig/iri :ontology "mean") (trig/lit (Double. (vtd/attr cont :mean)))]
                     [(trig/iri :ontology "standard_deviation") (trig/lit (Double. (vtd/attr cont :stdDev)))]
                     [(trig/iri :ontology "sample_size") (trig/lit (Integer. (vtd/attr cont :sampleSize)))])
      rate (trig/spo measurement 
                     [(trig/iri :ontology "count") (trig/lit (Integer. (vtd/attr rate :rate)))]
                     [(trig/iri :ontology "sample_size") (trig/lit (Integer. (vtd/attr rate :sampleSize)))])
      catg (reduce (fn [subj cat] (trig/spo subj [(trig/iri :ontology "category_count")
                                                  (trig/_po [(trig/iri :ontology "category") (trig/lit (vtd/attr cat :name))]
                                                            [(trig/iri :ontology "count") (trig/lit (Integer. (vtd/attr cat :rate)))])]))
                   measurement (vtd/search catg "./category"))
     :else measurement)
))

(defn participant-flow-rdf
  [instance-uri arm-uri epoch-uri nr-starting]
  (if nr-starting
    (trig/spo instance-uri
              [(trig/iri :rdf "type") (trig/iri :ontology "ParticipantFlow")]
              [(trig/iri :ontology "participants_starting") (trig/lit nr-starting)]
              [(trig/iri :ontology "of_arm") arm-uri]
              [(trig/iri :ontology "in_epoch") epoch-uri])))

(defn arm-size
  [arm-xml]
  (let [size (vtd/attr arm-xml :size)]
    (if size (Integer. size) nil)))

(defn println* [x] (println x) x)

(defn instances-by-key
  ([xmls key-fn] (instances-by-key xmls key-fn (fn [_] (trig/iri :instance (uuid)))))
  ([xmls key-fn uri-fn]
   (into {} (map (fn [el] [(key-fn el) { :xml el :uri (uri-fn el) }]) xmls))))


(defn study-rdf [xml uri entity-uris]
  (let [indication-uri (trig/iri :instance (uuid))
        study-outcomes (instances-by-key
                         (vtd/search xml "./studyOutcomeMeasures/studyOutcomeMeasure")
                         #(vtd/attr % :id))
        arms (instances-by-key
               (vtd/search xml "./arms/arm")
               #(vtd/attr % :name))
        epochs-order (map #(vtd/attr % :name) (vtd/search xml "./epochs/epoch"))
        epochs (instances-by-key
                 (vtd/search xml "./epochs/epoch")
                 #(vtd/attr % :name))
        primary-epoch (find-first-treatment-epoch xml)
        study-drugs (instances-by-key
                      (vtd/search xml "./activities/studyActivity/activity/treatment/drugTreatment/drug")
                      #(vtd/attr % :name))
        measurement-moments (instances-by-key
                              (vtd/search xml "./studyOutcomeMeasures/studyOutcomeMeasure/whenTaken")
                              when-taken-key)
        study-units (instances-by-key
                      (vtd/search xml "./activities/studyActivity/activity/treatment/drugTreatment/*[self::flexibleDose|self::fixedDose]/doseUnit")
                      dose-unit-key)
        study-activities (instances-by-key
                           (vtd/search xml "./activities/studyActivity")
                           #(vtd/attr % :name))]
    (concat
      [(-> uri
           (trig/spo [(trig/iri :rdf "type") (trig/iri :ontology "Study")]
                     [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
                     [(trig/iri :rdfs "comment") (trig/lit (vtd/text (vtd/at xml "./characteristics/title/value")))])
           (allocation-rdf xml)
           (blinding-rdf xml)
           (centers-rdf xml)
           (objective-rdf xml)
           (eligibility-rdf xml)
           (publications-rdf xml)
           (date-start-rdf xml)
           (date-end-rdf xml)
           (status-rdf xml)
           (trig/spo [(trig/iri :ontology "has_indication") indication-uri])
           (spo-each (trig/iri :ontology "has_outcome") (map :uri (vals study-outcomes)))
           (spo-each (trig/iri :ontology "has_arm") (map :uri (vals arms)))
           (spo-each (trig/iri :ontology "has_activity") (map :uri (vals study-activities)))
           (trig/spo [(trig/iri :ontology "has_epochs") (trig/coll (map #(:uri (epochs %)) epochs-order))])
           (primary-epoch-rdf (:uri (epochs primary-epoch))))]
      [(study-indication-rdf xml indication-uri entity-uris)]
      (map #(study-arm-rdf (:xml %) (:uri %)) (vals arms))
      (map #(study-epoch-rdf (:xml %) (:uri %)) (vals epochs))
      (filter (comp not nil?) (map #(participant-flow-rdf (trig/iri :instance (uuid)) (:uri %) (:uri (epochs primary-epoch)) (arm-size (:xml %))) (vals arms)))
      (map #(study-outcome-rdf (:xml %) (:uri %) entity-uris (map-vals :uri measurement-moments)) (vals study-outcomes))
      (map #(study-drug-rdf (:xml %) (:uri %) entity-uris) (vals study-drugs))
      (map #(study-unit-rdf (:xml %) (:uri %) entity-uris) (vals study-units))
      (map #(study-activity-rdf (:xml %) (:uri %) entity-uris (map-vals :uri arms) (map-vals :uri epochs) (map-vals :uri study-drugs) (map-vals :uri study-units)) (vals study-activities))
      (map #(study-measurement-moment-rdf (:xml %) (:uri %) (map-vals :uri epochs)) (vals measurement-moments))
      (map #(study-measurement-rdf % (trig/iri :instance (uuid)) study-outcomes (map-vals :uri arms) (map-vals :uri measurement-moments)) (vtd/search xml "./measurements/measurement")))))


(defn import-study [xml entity-uris]
  (let [graph-uri (trig/iri :graph (uuid))
        study-uri (trig/iri :study (uuid))]
    {:id (vtd/attr xml :name)
     :uri study-uri
     :rdf (trig/graph graph-uri (study-rdf xml study-uri entity-uris))}))

(defn import-studies [xml xpath entity-uris]
  (let [studies (map #(import-study % entity-uris) (vtd/search xml xpath))]
    [(entities-uri-map studies) (entities-rdf studies)]))

(defn dataset-source-doc [subj uri]
  (if uri
    (trig/spo subj [(trig/iri :dc "source") (trig/iri uri)])
    subj))

(defn rdfimport [label description source-doc-uri xml]
  (let [dataset-id (uuid)
        prefixes {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  :rdfs "http://www.w3.org/2000/01/rdf-schema#"
                  :xsd "http://www.w3.org/2001/XMLSchema#"
                  :owl "http://www.w3.org/2002/07/owl#"
                  :qudt "http://qudt.org/schema/qudt#"
                  :ontology "http://trials.drugis.org/ontology#"
                  :dataset "http://trials.drugis.org/datasets/"
                  :study "http://trials.drugis.org/studies/"
                  :graph "http://trials.drugis.org/graphs/"
                  :instance "http://trials.drugis.org/instances/"
                  :entity "http://trials.drugis.org/concepts/"
                  :atc "http://www.whocc.no/ATC2011/"
                  :snomed "http://www.ihtsdo.org/SCT_"
                  :dc "http://purl.org/dc/elements/1.1/" 
                  :dcterms "http://purl.org/dc/terms/" }
        [unit-uri-map units-rdf] (import-entities xml "/addis-data/units/unit" unit-rdf)
        [indication-uri-map indications-rdf] (import-entities xml "/addis-data/indications/indication" indication-rdf)
        [drug-uri-map drugs-rdf] (import-entities xml "/addis-data/drugs/drug" drug-rdf)
        [endpoint-uri-map endpoints-rdf] (import-entities xml "/addis-data/endpoints/endpoint" variable-rdf)
        [adverseEvent-uri-map adverseEvents-rdf] (import-entities xml "/addis-data/adverseEvents/adverseEvent" variable-rdf)
        [populationCharacteristic-uri-map populationCharacteristics-rdf] (import-entities xml "/addis-data/populationCharacteristics/populationCharacteristic" variable-rdf)
        entity-uris {:indication indication-uri-map
                     :drug drug-uri-map
                     :endpoint endpoint-uri-map
                     :adverseEvent adverseEvent-uri-map
                     :populationCharacteristic populationCharacteristic-uri-map
                     :unit unit-uri-map}
        [studies-uri-map studies-graphs] (import-studies xml "/addis-data/studies/study" entity-uris)
        dataset-rdf [(-> (trig/iri :dataset dataset-id)
                         (trig/spo [(trig/iri :rdf "type") (trig/iri :ontology "Dataset")]
                                   [(trig/iri :rdfs "label") label]
                                   [(trig/iri :dcterms "title") label]
                                   [(trig/iri :rdfs "comment") description]
                                   [(trig/iri :dcterms "description") description])
                         (spo-each (trig/iri :ontology "contains_study") (vals studies-uri-map))
                         (dataset-source-doc source-doc-uri))]
	concepts-graph (trig/graph (trig/iri :graph "concepts")
			(concat 
			 units-rdf
			 indications-rdf
			 drugs-rdf
			 endpoints-rdf
			 adverseEvents-rdf
			 populationCharacteristics-rdf))
	default-graph (trig/graph (trig/default-graph) dataset-rdf)]
    (trig/write-trig prefixes (concat [default-graph concepts-graph] studies-graphs))))

(defn -main
  [& args]
  (let [[options args banner]
        (cli args
             ["-h" "--help" "Show Help" :default false :flag true]
             ["-f" "--file" "ADDIS 1.x file"]
             ["-n" "--name" "Dataset short name"]
             ["-t" "--title" "Dataset description" :default "ADDIS data import"]
             ["-r" "--rdf" "RDF (TriG) file" :default "out.trig"]
             ["-s" "--source" "URI describing the data source"])]
    (when (or (:help options) (some nil? ((juxt :file :name :title) options)))
      (println banner)
      (System/exit 0))
    (let
        [data (vtd/navigator (slurp (as-file (options :file))))
         rdf (as-file (:rdf options))]
        (spit rdf (rdfimport (:name options) (:title options) (:source options) data))
      )))
