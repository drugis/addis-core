(ns org.drugis.addis.rdf.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [blank?]]
            [riveted.core :as vtd]
            [org.drugis.addis.rdf.trig :as trig]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn drug-rdf [xml uri]
  (let [atcCode (vtd/attr xml :atcCode)
        subj (if (or (nil? atcCode) (blank? atcCode))
               uri
               (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :atc atcCode)]))]
    (trig/spo subj
         [(trig/iri :rdf "type") (trig/iri :owl "Class")]
         [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
         [(trig/iri :rdfs "subClassOf") (trig/iri :ontology "Drug")])))

(defn indication-rdf [xml uri]
  (let [snomedCode (vtd/attr xml :code)
        subj (if (or (nil? snomedCode) (blank? snomedCode))
               uri
               (trig/spo uri [(trig/iri :owl "sameAs") (trig/iri :snomed snomedCode)]))]
    (trig/spo subj 
         [(trig/iri :rdf "type") (trig/iri :owl "Class")]
         [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
         [(trig/iri :rdfs "subClassOf") (trig/iri :ontology "Indication")])))

; TODO: rate/continuous/etc, unitOfMeasurement, direction?
(defn variable-rdf [xml uri superClass]
  (trig/spo uri 
       [(trig/iri :rdf "type") (trig/iri :owl "Class")]
       [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
       [(trig/iri :rdfs "comment") (trig/lit (vtd/attr xml :description))]
       [(trig/iri :rdfs "subClassOf") (trig/iri :ontology superClass)]))

(defn endpoint-rdf [xml uri]
  (variable-rdf xml uri "Endpoint"))

(defn adverseEvent-rdf [xml uri]
  (variable-rdf xml uri "AdverseEvent"))

(defn populationCharacteristic-rdf [xml uri]
  (variable-rdf xml uri "PopulationCharacteristic"))

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
  {"RANDOMIZED" (trig/iri :ontology "allocationRandomized")
   "NONRANDOMIZED" (trig/iri :ontology "allocationNonRandomized")})

(defn allocation-rdf [subj xml]
  (let [allocation (allocationTypeUri (studyCharVal xml "allocation"))]
    (if allocation
      (trig/spo subj [(trig/iri :ontology "has_allocation") allocation])
      subj)))

(def blindingTypeUri
  {"OPEN" (trig/iri :ontology "blindingNone")
   "SINGLE_BLIND" (trig/iri :ontology "blindingSingle")
   "DOUBLE_BLIND" (trig/iri :ontology "blindingDouble")
   "TRIPLE_BLIND" (trig/iri :ontology "blindingTriple") })

(defn blinding-rdf [subj xml]
  (let [blinding (blindingTypeUri (studyCharVal xml "blinding"))]
    (if blinding
      (trig/spo subj [(trig/iri :ontology "has_blinding") blinding])
      subj)))

(defn as-int [string] (if (nil? string) nil (Integer. string)))

(defn centers-rdf [subj xml]
  (let [centers (as-int (studyCharVal xml "centers"))]
    (if centers
      (trig/spo subj [(trig/iri :ontology "has_number_of_centers") (trig/lit centers)])
      subj)))

(defn study-indication-rdf [xml entity-uris instance-uri]
  (let [entity-name (vtd/attr (vtd/at xml "./indication") :name)
        entity-uri ((:indication entity-uris) entity-name)]
    (trig/spo instance-uri
              [(trig/iri :rdf "type") entity-uri]
              [(trig/iri :rdfs "label") entity-name])))

(defn study-outcome-rdf [xml entity-uris instance-uri]
  (let [entity-name (vtd/attr (vtd/first-child xml) :name)
        entity-type (vtd/tag (vtd/first-child xml))
        entity-uri ((entity-uris (keyword entity-type)) entity-name)
        description (vtd/attr (vtd/at xml (str "//addis-data/" entity-type "s/*[@name='" entity-name "']")) :description)]
    (trig/spo instance-uri
              [(trig/iri :rdf "type") entity-uri]
              [(trig/iri :rdfs "label") entity-name]
              [(trig/iri :rdfs "comment") (trig/lit description)])))

(defn study-drug-rdf [drug-name entity-uris instance-uri]
  (trig/spo instance-uri
            [(trig/iri :rdf "type") ((entity-uris :drug) drug-name)]
            [(trig/iri :rdfs "label") drug-name]))

(defn study-arm-rdf [arm-name instance-uri]
  (trig/spo instance-uri
            [(trig/iri :rdfs "label") arm-name]
            [(trig/iri :rdf "type") (trig/iri :ontology "Arm")]))

; TODO: duration, etc.
(defn study-epoch-rdf [epoch-name instance-uri]
  (trig/spo instance-uri
            [(trig/iri :rdfs "label") epoch-name]
            [(trig/iri :rdf "type") (trig/iri :ontology "Epoch")]))

(defn activity-other-rdf [subj xml study-drug-uris]
  (trig/spo subj
       [(trig/iri :rdf "type") (trig/iri :ontology "StudyActivity")]
       [(trig/iri :rdfs "comment") (trig/lit (vtd/text xml))]))

(defn activity-predefined-rdf [subj xml study-drug-uris]
  (let [activity-predefined {"RANDOMIZATION" "RandomizationActivity"
                             "SCREENING" "ScreeningActivity"
                             "WASH_OUT" "WashOutActivity"
                             "FOLLOW_UP" "FollowUpActivity"}]
    (trig/spo subj [(trig/iri :rdf "type") (trig/iri :ontology (activity-predefined (vtd/text xml)))])))

; TODO: dosing, units
(defn treatment-rdf [xml study-drug-uris]
  (trig/_po [(trig/iri :ontology "treatment_has_drug") (study-drug-uris (vtd/attr (vtd/at xml "./drug") :name))]))

(defn activity-treatment-rdf [subj xml study-drug-uris]
  (let [drugs-coll (trig/coll (map #(treatment-rdf % study-drug-uris) (vtd/search xml "./drugTreatment")))]
    (trig/spo subj 
              [(trig/iri :rdf "type") (trig/iri :ontology "TreatmentActivity")]
              [(trig/iri :ontology "administered_drugs") drugs-coll])))

(def activity-rdf
  {"predefined" activity-predefined-rdf
   "other" activity-other-rdf
   "treatment" activity-treatment-rdf})

(defn activity-used-by-rdf
  [subj xml arm-uris epoch-uris]
  (let [used-by (trig/_po [(trig/iri :ontology "applied_to_arm") (arm-uris (vtd/attr xml "arm"))]
                          [(trig/iri :ontology "applied_in_epoch") (epoch-uris (vtd/attr xml "epoch"))])]
    (trig/spo subj [(trig/iri :ontology "activity_application") used-by])))

(defn study-activity-rdf [xml activity-uri entity-uris arm-uris epoch-uris study-drug-uris]
  (let [activity (vtd/first-child (vtd/at xml "./activity"))
        activity-type (vtd/tag activity)
        used-by (fn [subj xml] (activity-used-by-rdf subj xml arm-uris epoch-uris))
        subj ((activity-rdf activity-type) activity-uri activity study-drug-uris)]
    (reduce used-by subj (vtd/search xml "./usedBy"))))

(defn spo-each [subj pred obj*]
  (reduce (fn [subj obj] (trig/spo subj [pred obj])) subj obj*))

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

; TODO: import the interesting stuff
(defn study-rdf [xml uri entity-uris]
  (let [indication-uri (trig/iri :instance (uuid))
        study-outcome-uris (apply merge (map (fn [el] {(vtd/attr el :id) (trig/iri :instance (uuid))}) (vtd/search xml "./studyOutcomeMeasures/studyOutcomeMeasure")))
        arm-uris (apply merge (map (fn [el] {(vtd/attr el :name) (trig/iri :instance (uuid))}) (vtd/search xml "./arms/arm")))
        epochs (map #(vtd/attr % :name) (vtd/search xml "./epochs/epoch"))
        epoch-uris (apply merge (map (fn [epoch] {epoch (trig/iri :instance (uuid))}) epochs))
        primary-epoch (find-first-treatment-epoch xml)
        study-drug-uris (apply merge (map (fn [el] {(vtd/attr el :name) (trig/iri :instance (uuid))}) (vtd/search xml "./activities/studyActivity/activity/treatment/drugTreatment/drug")))]
    (concat
      [(-> uri
           (trig/spo [(trig/iri :rdf "type") (trig/iri :ontology "Study")]
                     [(trig/iri :rdfs "label") (trig/lit (vtd/attr xml :name))]
                     [(trig/iri :rdfs "comment") (trig/lit (vtd/text (vtd/at xml "./characteristics/title/value")))])
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
           (trig/spo [(trig/iri :ontology "has_indication") indication-uri])
           (spo-each (trig/iri :ontology "has_outcome") (vals study-outcome-uris))
           (spo-each (trig/iri :ontology "has_arm") (vals arm-uris)) ; TODO: arm sizes
           (trig/spo [(trig/iri :ontology "has_epochs") (trig/coll (map epoch-uris epochs))]) ; TODO: duration
           (primary-epoch-rdf (epoch-uris primary-epoch))
           ; measurements
           )]
      [(study-indication-rdf xml entity-uris indication-uri)]
      (map #(study-arm-rdf % (arm-uris %)) (keys arm-uris))
      (map #(study-epoch-rdf % (epoch-uris %)) epochs)
      (map #(study-outcome-rdf (vtd/at xml (str "./studyOutcomeMeasures/studyOutcomeMeasure[@id='" % "']")) entity-uris (study-outcome-uris %)) (keys study-outcome-uris))
      (map #(study-drug-rdf % entity-uris (study-drug-uris %)) (keys study-drug-uris))
      (map #(study-activity-rdf % (trig/iri :instance (uuid)) entity-uris arm-uris epoch-uris study-drug-uris) (vtd/search xml "./activities/studyActivity")))))

(defn import-study [xml entity-uris]
  (let [uri (trig/iri :study (uuid))]
    {:id (vtd/attr xml :name)
     :uri uri
     :rdf (trig/graph uri (study-rdf xml uri entity-uris))}))

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
        dataset-rdf [(-> (trig/iri :dataset dataset-id)
                         (trig/spo [(trig/iri :rdf "type") (trig/iri :ontology "Dataset")]
                                   [(trig/iri :rdfs "label") label]
                                   [(trig/iri :rdfs "comment") description])
                         (spo-each (trig/iri :ontology "contains_study") (vals studies-uri-map)))]
        meta-graph (concat 
                     indications-rdf
                     drugs-rdf
                     endpoints-rdf
                     adverseEvents-rdf
                     populationCharacteristics-rdf
                     dataset-rdf)]
    (str
      (trig/write-trig prefixes (cons (trig/graph (trig/iri :dataset dataset-id) meta-graph) studies-graphs)))))

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
