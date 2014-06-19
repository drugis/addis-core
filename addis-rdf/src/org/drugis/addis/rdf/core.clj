(ns org.drugis.addis.rdf.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [blank?]]
            [riveted.core :as vtd]
            [org.drugis.addis.rdf.trig :refer [write-trig rdf-uri rdf-blank rdf-coll]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn drug-rdf [xml uri]
  [uri
   (concat 
     [[(rdf-uri :rdf "type") (rdf-uri :owl "Class")]
      [(rdf-uri :rdfs "label") (vtd/attr xml :name)]
      [(rdf-uri :rdfs "subClassOf") (rdf-uri :ontology "Drug")]]
     (let [atcCode (vtd/attr xml :atcCode)]
       (if (not (or (nil? atcCode) (blank? atcCode)))
         [[(rdf-uri :owl "sameAs") (rdf-uri :atc atcCode)]]
         [])))])

(defn indication-rdf [xml uri]
  [uri
   (concat
     [[(rdf-uri :rdf "type") (rdf-uri :owl "Class")]
      [(rdf-uri :rdfs "label") (vtd/attr xml :name)]
      [(rdf-uri :rdfs "subClassOf") (rdf-uri :ontology "Indication")]]
     (let [snomedCode (vtd/attr xml :code)]
       (if (not (or (nil? snomedCode) (blank? snomedCode)))
         [[(rdf-uri :owl "sameAs") (rdf-uri :snomed snomedCode)]]
         [])))])

; TODO: rate/continuous/etc, unitOfMeasurement, direction?
(defn variable-rdf [xml uri superClass]
  [uri
   [[(rdf-uri :rdf "type") (rdf-uri :owl "Class")]
    [(rdf-uri :rdfs "label") (vtd/attr xml :name)]
    [(rdf-uri :rdfs "comment") (vtd/attr xml :description)]
    [(rdf-uri :rdfs "subClassOf") (rdf-uri :ontology superClass)]]
   ])

(defn endpoint-rdf [xml uri]
  (variable-rdf xml uri "Endpoint"))

(defn adverseEvent-rdf [xml uri]
  (variable-rdf xml uri "AdverseEvent"))

(defn populationCharacteristic-rdf [xml uri]
  (variable-rdf xml uri "PopulationCharacteristic"))

(defn import-entity [xml rdf-fn]
  (let [uri (rdf-uri :entity (uuid))]
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
  {"RANDOMIZED" (rdf-uri :ontology "allocationRandomized")
   "NONRANDOMIZED" (rdf-uri :ontology "allocationNonRandomized")})

(defn allocation-rdf [xml]
  (let [allocation (allocationTypeUri (studyCharVal xml "allocation"))]
    (if (nil? allocation) [] [[(rdf-uri :ontology "has_allocation") allocation]])))

(def blindingTypeUri
  {"OPEN" (rdf-uri :ontology "blindingNone")
   "SINGLE_BLIND" (rdf-uri :ontology "blindingSingle")
   "DOUBLE_BLIND" (rdf-uri :ontology "blindingDouble")
   "TRIPLE_BLIND" (rdf-uri :ontology "blindingTriple") })

(defn blinding-rdf [xml]
  (let [blinding (blindingTypeUri (studyCharVal xml "blinding"))]
    (if (nil? blinding) [] [[(rdf-uri :ontology "has_blinding") blinding]])))

(defn as-int [string] (if (nil? string) nil (Integer. string)))

(defn centers-rdf [xml]
  (let [centers (as-int (studyCharVal xml "centers"))]
    (if (nil? centers) [] [[(rdf-uri :ontology "has_number_of_centers") centers]])))

; TODO: add rdfs:label and rdfs:comment from the source entity
(defn study-indication-rdf [xml entity-uris instance-uri]
  [[instance-uri
  [[(rdf-uri :rdf "type") ((:indication entity-uris) (vtd/attr (vtd/at xml "./indication") :name))]]]])

; TODO: add rdfs:label and rdfs:comment from the source entity. more info?
(defn study-outcome-rdf [xml entity-uris instance-uri]
  (let [entity-name (vtd/attr (vtd/first-child xml) :name)
        entity-type (vtd/tag (vtd/first-child xml))]
  [instance-uri
  [[(rdf-uri :rdf "type") ((entity-uris (keyword entity-type)) entity-name)]]]))

; TODO: add rdfs:label and rdfs:comment from the source entity. more info?
(defn study-drug-rdf [drug-name entity-uris instance-uri]
  [instance-uri
   [[(rdf-uri :rdf "type") ((entity-uris :drug) drug-name)]]])

(defn activity-other-rdf [xml study-drug-uris]
  [[(rdf-uri :rdf "type") (rdf-uri :ontology "StudyActivity")]
   [(rdf-uri :rdfs "comment") (vtd/text xml)]])

(defn activity-predefined-rdf [xml study-drug-uris]
  ({"RANDOMIZATION" [[(rdf-uri :rdf "type") (rdf-uri :ontology "RandomizationActivity")]]
   "SCREENING" [[(rdf-uri :rdf "type") (rdf-uri :ontology "ScreeningActivity")]]
   "WASH_OUT" [[(rdf-uri :rdf "type") (rdf-uri :ontology "WashOutActivity")]]
   "FOLLOW_UP" [[(rdf-uri :rdf "type") (rdf-uri :ontology "FollowUpActivity")]]}
   (vtd/text xml)))

; TODO: dosing, units
(defn treatment-rdf [xml study-drug-uris]
  (rdf-blank [[(rdf-uri :ontology "treatment_has_drug") (study-drug-uris (vtd/attr (vtd/at xml "./drug") :name))]]))

(defn activity-treatment-rdf [xml study-drug-uris]
  [[(rdf-uri :rdf "type") (rdf-uri :ontology "TreatmentActivity")]
   [(rdf-uri :ontology "administeredDrugs") (rdf-coll (map #(treatment-rdf % study-drug-uris) (vtd/search xml "./drugTreatment")))]])

(def activity-rdf
  {"predefined" activity-predefined-rdf
   "other" activity-other-rdf
   "treatment" activity-treatment-rdf})

(defn study-activity-rdf [xml activity-uri entity-uris arm-uris epoch-uris study-drug-uris]
  (let [activity (vtd/first-child (vtd/at xml "./activity"))
        activity-type (vtd/tag activity)]
  [activity-uri
   (apply concat
          ((activity-rdf activity-type) activity study-drug-uris)
          (map (fn [xml] [
                          [(rdf-uri :ontology "applied_to_arm") (arm-uris (vtd/attr xml "arm"))]
                          [(rdf-uri :ontology "applied_in_epoch") (epoch-uris (vtd/attr xml "epoch"))]])
               (vtd/search xml "./usedBy")))]))

; TODO: import the interesting stuff
(defn study-rdf [xml uri entity-uris]
  (let [indication-uri (rdf-uri :instance (uuid))
        study-outcome-uris (apply merge (map (fn [el] {(vtd/attr el :id) (rdf-uri :instance (uuid))}) (vtd/search xml "./studyOutcomeMeasures/studyOutcomeMeasure")))
        arm-uris (apply merge (map (fn [el] {(vtd/attr el :name) (rdf-uri :arm (uuid))}) (vtd/search xml "./arms/arm")))
        epoch-uris (apply merge (map (fn [el] {(vtd/attr el :name) (rdf-uri :epoch (uuid))}) (vtd/search xml "./epochs/epoch")))
        study-drug-uris (apply merge (map (fn [el] {(vtd/attr el :name) (rdf-uri :drug (uuid))}) (vtd/search xml "./activities/studyActivity/activity/treatment/drugTreatment/drug")))]
    (concat
      [[uri
        (concat
          [[(rdf-uri :rdf "type") (rdf-uri :ontology "Study")]
           [(rdf-uri :rdfs "label") (vtd/attr xml :name)]
           [(rdf-uri :rdfs "comment") (vtd/text (vtd/at xml "./characteristics/title/value"))]]
          ; # characteristics
          (allocation-rdf xml)
          (blinding-rdf xml)
          (centers-rdf xml)
          ; objective: new node with rdfs:comment the text
          ; inclusion: new node with rdfs:comment the text
          ; exclusion: new node with rdfs:comment the text
          ; references: not sure?
          ; source: omit?
          ; study_start
          ; study_end
          ; status
          ; 
          ; ## actual stuff
          [[(rdf-uri :ontology "has_indication") indication-uri]]
          (map (fn [el] [(rdf-uri :ontology "has_outcome") el]) (vals study-outcome-uris))
          (map (fn [el] [(rdf-uri :ontology "has_arm") el]) (vals arm-uris)) ; TODO: arm sizes
          (map (fn [el] [(rdf-uri :ontology "has_epoch") el]) (vals epoch-uris)) ; TODO: duration
          ; measurements
          )]]
      (study-indication-rdf xml entity-uris indication-uri)
      (map #(study-outcome-rdf (vtd/at xml (str "./studyOutcomeMeasures/studyOutcomeMeasure[@id='" % "']")) entity-uris (study-outcome-uris %)) (keys study-outcome-uris))
      (map #(study-drug-rdf % entity-uris (study-drug-uris %)) (keys study-drug-uris))
      (map #(study-activity-rdf % (rdf-uri :activity (uuid)) entity-uris arm-uris epoch-uris study-drug-uris) (vtd/search xml "./activities/studyActivity")))))

(defn import-study [xml entity-uris]
  (let [uri (rdf-uri :study (uuid))]
    {:id (vtd/attr xml :name)
     :uri uri
     :rdf [uri (study-rdf xml uri entity-uris)]}))

(defn import-studies [xml xpath entity-uris]
  (let [studies (map #(import-study % entity-uris) (vtd/search xml xpath))]
    [(entities-uri-map studies) (entities-rdf studies)]))

(defn rdfimport [label description xml]
  (let [dataset-id (uuid)
        prefixes {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  :rdfs "http://www.w3.org/2000/01/rdf-schema#"
                  :owl "http://www.w3.org/2002/07/owl#"
                  :ontology "http://trials.drugis.org/ontology#"
                  :dataset "http://trials.drugis.org/datasets/"
                  :study "http://trials.drugis.org/studies/"
                  :arm "http://trials.drugis.org/arms/"
                  :epoch "http://trials.drugis.org/epochs/"
                  :activity "http://trials.drugis.org/activities/"
                  :drug "http://trials.drugis.org/drugs/"
                  :instance "http://trials.drugis.org/instances/"
                  :entity "http://trials.drugis.org/entities/"
                  :atc "http://www.whocc.no/ATC2011/"
                  :snomed "http://www.ihtsdo.org/SCT_"}
        [indication-uri-map indications-rdf] (import-entities xml "/addis-data/indications/indication" indication-rdf)
        [drug-uri-map drugs-rdf] (import-entities xml "/addis-data/drugs/drug" drug-rdf)
        [endpoint-uri-map endpoints-rdf] (import-entities xml "/addis-data/endpoints/endpoint" endpoint-rdf)
        [adverseEvent-uri-map adverseEvents-rdf] (import-entities xml "/addis-data/adverseEvents/adverseEvent" adverseEvent-rdf)
        [populationCharacteristic-uri-map populationCharacteristics-rdf] (import-entities xml "/addis-data/populationCharacteristics/populationCharacteristic" populationCharacteristic-rdf)
        entity-uris {:indication indication-uri-map
                     :drug drug-uri-map
                     :endpoint endpoint-uri-map
                     :adverseEvent adverseEvent-uri-map
                     :populationCharacteristic populationCharacteristic-uri-map}
        [studies-uri-map studies-graphs] (import-studies xml "/addis-data/studies/study" entity-uris)
        contains-studies (map (fn [study-uri] [(rdf-uri :ontology "contains_study") study-uri]) (vals studies-uri-map))
        dataset-rdf [[(rdf-uri :dataset dataset-id)
                      (concat
                        [[(rdf-uri :rdf "type") (rdf-uri :ontology "Dataset")]
                         [(rdf-uri :rdfs "label") label]
                         [(rdf-uri :rdfs "comment") description]]
                        contains-studies)]]
        meta-graph (concat 
                     indications-rdf
                     drugs-rdf
                     endpoints-rdf
                     adverseEvents-rdf
                     populationCharacteristics-rdf
                     dataset-rdf)]
    (str
      (write-trig prefixes (cons [(rdf-uri :dataset dataset-id) meta-graph] studies-graphs)))))

(defn -main
  [& args]
  (let [[options args banner]
        (cli args
             ["-h" "--help" "Show Help" :default false :flag true]
             ["-f" "--file" "ADDIS 1.x file"]
             ["-n" "--name" "Dataset short name"]
             ["-t" "--title" "Dataset description" :default "ADDIS data import"]
             ["-r" "--rdf" "RDF (TriG) file" :default "out.trig"]
             )]
    (when (or (:help options) (some nil? ((juxt :file :name :title) options)))
      (println banner)
      (System/exit 0))
    (let
        [data (vtd/navigator (slurp (as-file (options :file))))
         rdf (as-file (:rdf options))]
        (spit rdf (rdfimport (:name options) (:title options) data))
      )))
