(ns org.drugis.importer.definitions
  (:require [clojure.string :refer [join]]
            [clojure.set :refer [map-invert]]
            [org.drugis.importer.xml2sql :as x2s]
            [riveted.core :as vtd]))

(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
        (doto (java.security.MessageDigest/getInstance "MD5")
          (.reset)
          (.update (.getBytes token)))]
    (.toString
     (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
     16)))

(defn entity-path
  [entity]
  (format "%s/%s"(:type entity) (md5 (get entity "name"))))

(defn entity-uri
  [namespace entity]
  (format "http://trials.drugis.org/namespaces/%d/%s" namespace (entity-path entity)))

(defn study-uri
  [study-id]
  (format "http://trials.drugis.org/study/%d", study-id))

(defn entity-ref-uri
  [study-id entity-type entity-id]
  (format "http://trials.drugis.org/study/%d/%s/%d" study-id entity-type entity-id))

(defn tag-to-entity
  [tag]
  (merge (x2s/attrs tag) {:type (vtd/tag tag)}))

(def variable-types ["endpoints" "adverseEvents" "populationCharacteristics"])
(def entity-types (concat ["units" "indications" "drugs"] variable-types))

(defn xpath-tag-or
  [tag-names]
  (join "|" (map (fn [tag-name] (str "self::" tag-name)) tag-names)))

(defn snomed-uri
  [snomed-id]
  (format "http://www.ihtsdo.org/SCT_%s" snomed-id))

(defn atc-uri
  [atc-id]
  (format "http://www.whocc.no/ATC2011/%s" atc-id))

(defn write-ttl
  [prefixes statements]
  (let [ttl-str (fn [resource] (if (sequential? resource) (second resource) (str "\"" resource "\"")))
        write-triples
        (fn [triples]
          (let [intermediate (map (fn [[k v]] (str "  " (ttl-str k) " " (ttl-str v))) (second triples))
                triple-str (join " ;\n" intermediate)]
            (str (ttl-str (first triples)) "\n" triple-str " .\n")))
        write-prefix (fn [[prefix uri]] (format "@prefix %s: <%s> ." (name prefix)  uri))]
      (join "\n" (concat (map write-prefix prefixes) [""] (map write-triples statements)))))

(def entity-type-map
  {"indication" (fn [entity] (snomed-uri (get entity "code")))
   "drug" (fn [entity] (atc-uri (get entity "atcCode")))})

(defn rdf-uri
  ([uri] [:uri (str "<" uri ">")])
  ([prefix resource] [:uri (str (name prefix) ":" resource)]))

(defn entity-mapping
  [entity]
  (if  (contains? entity-type-map (:type entity))
    [[(rdf-uri :owl "sameAs") (rdf-uri ((entity-type-map (:type entity)) entity))]]
    []))

(defn entity-rdf
  "@TODO: resolve additional entity properties"
  [namespace entity]
  [(rdf-uri (entity-uri namespace entity))
   (concat [[(rdf-uri :rdf "type") (rdf-uri :owl "Class")]
            [(rdf-uri :rdfs "label") (get entity "name")]
            [(rdf-uri :rdfs "comment") (get entity "description")]
            [(rdf-uri :rdfs "subClassOf") (rdf-uri (str "http://trials.drugis.org/" (:type entity)))]]
           (entity-mapping entity))])

(def ttl-buffer (atom []))

(defn append-ttl
  [statement]
  (swap! ttl-buffer conj statement))

(defn entity-ref-rdf
  [entity-ref entity-ref-uri entity-cls-uri study-uri]
  [(rdf-uri entity-ref-uri)
   [[(rdf-uri :rdf "type") (rdf-uri entity-cls-uri)]
    [(rdf-uri :rdfs "label") (:name entity-ref)]
    [(rdf-uri :rdfs "comment") (:description entity-ref)]
    [(rdf-uri "http://trials.drugis.org/partOfStudy") (rdf-uri study-uri)]]])

(defn entity-ref-rdf-callback
  [entity-type-or-fn]
  (let [find-namespace (x2s/parent-finder :namespaces)
        find-study (x2s/parent-finder :studies)]
    (fn [entity-ref inserted contexts]
      (let [namespace (find-namespace contexts)
            [xml-id [sql-id _]] (first inserted)
            name (:name entity-ref)
            study (find-study contexts)
            entity-type (if (fn? entity-type-or-fn) (entity-type-or-fn entity-ref) entity-type-or-fn)
            entity-ref-uri (entity-ref-uri study entity-type sql-id)
            entity-cls-uri (entity-uri namespace {"name" name :type entity-type})
            study-uri (study-uri study)]
        (append-ttl (entity-ref-rdf entity-ref entity-ref-uri entity-cls-uri study-uri))))))

(defn entities-to-rdf
  [data ttl namespace]
  (let [xpath-expr (xpath-tag-or entity-types)
        nodes (vtd/search data (str "/addis-data/*[" xpath-expr "]/*"))
        entities (map tag-to-entity nodes)
        prefixes {:rdfs "http://www.w3.org/2000/01/rdf-schema#"
                  :rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  :owl "http://www.w3.org/2002/07/owl#"}]
    (spit ttl (write-ttl prefixes (concat @ttl-buffer (map #(entity-rdf namespace %) entities))))))

(defn as-int [x] (when-not (nil? x) (Integer. x)))

(defn as-double [x] (when-not (nil? x) (Double. x)))

(defn as-boolean [x] (when-not (nil? x) (Boolean. x)))

(defn parse-xml-datetime
  [x]
  (.getTimeInMillis (javax.xml.bind.DatatypeConverter/parseDateTime x)))

(defn as-date
  [x]
  (when-not (nil? x) (java.sql.Date. (parse-xml-datetime x))))

(defn as-timestamp
  [x]
  (when-not (nil? x) (java.sql.Timestamp. (parse-xml-datetime x))))

(def ^:private dtf (javax.xml.datatype.DatatypeFactory/newInstance))

(defn as-duration
  [x]
  (if (nil? x)
    nil
    (let [d (.newDuration dtf x)]
      (org.postgresql.util.PGInterval. (.getYears d) (.getMonths d) (.getDays d)
                                       (.getHours d) (.getMinutes d) (.getSeconds d)))))

(defn as-enum
  [type value]
  (let [object (org.postgresql.util.PGobject.)]
    (.setValue object (clojure.string/upper-case value))
    (.setType object type)
    object))

(def as-allocation-enum (partial as-enum "allocation_type"))
(def as-blinding-enum (partial as-enum "blinding_type"))
(def as-source-enum (partial as-enum "study_source"))
(def as-status-enum (partial as-enum "study_status"))

(def variable-type-xml-to-sql {:populationCharacteristic "POPULATION_CHARACTERISTIC"
                               :endpoint "ENDPOINT"
                               :adverseEvent "ADVERSE_EVENT"})
(def variable-type-sql-to-xml (map-invert variable-type-xml-to-sql))

(def as-measurement-type (partial as-enum "measurement_type"))

(def as-variable-type #(as-enum "variable_type"
                                (variable-type-xml-to-sql (keyword %))))

(def as-epoch-offset-enum (partial as-enum "epoch_offset"))

(defn as-activity-enum
  [tag]
  (let [tag-name (vtd/tag tag)]
    (if (= tag-name "predefined")
      (as-enum "activity_type" (vtd/text tag))
      (as-enum "activity_type" tag-name))))

(def references-table
  {:xml-id (x2s/value vtd/text)
   :sql-id :id
   :each "./characteristics/references/pubMedId"
   :table :references
   :columns {:study (x2s/parent-ref)
             :id (x2s/value vtd/text)
             :repository (x2s/value "PubMed")}})

(def arms-table
  {:xml-id (x2s/value nil)
   :sql-id :id
   :each "."
   :table :arms
   :columns {:study (x2s/parent-ref) }
   :collapse [{:xml-id (x2s/value #(vtd/attr % :name))
               :each "./arms/arm"
               :columns {:name (x2s/value #(vtd/attr % :name))
                         :arm_size (x2s/value #(as-int (vtd/attr % :size)))}}
              {:xml-id (x2s/value nil)
               :each "."
               :columns {:name (x2s/value "")
                         :arm_size (x2s/value nil)}}]})

(def epochs-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./epochs/epoch"
   :table :epochs
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))
             :duration (x2s/xpath-text "./duration" as-duration)
             }})

(def drugs-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./activities/studyActivity/activity/treatment/drugTreatment/drug"
   :table :drugs
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))}
   :post-insert (entity-ref-rdf-callback "drug")})

(def units-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./activities/studyActivity/activity/treatment/drugTreatment/*/doseUnit/unit"
   :table :units
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))}
   :post-insert (entity-ref-rdf-callback "unit")})

(def treatment-dosings-table
  {:sql-id (juxt :treatment :planned_time)
   :each "."
   :table :treatment_dosings
   :columns {:treatment (x2s/parent-ref)
             :planned_time (x2s/value (as-duration "P0D"))
             :scale_modifier (x2s/xpath-attr "./*/doseUnit" :scaleModifier)
             :unit (x2s/sibling-ref :units #(vtd/attr (vtd/at % "./*/doseUnit/unit") :name))}
   :collapse [{:each "./flexibleDose"
               :columns {:min_dose (x2s/value #(as-double (vtd/attr % :minDose)))
                         :max_dose (x2s/value #(as-double (vtd/attr % :maxDose)))}}
              {:each "./fixedDose"
               :columns {:min_dose (x2s/value #(as-double (vtd/attr % :quantity)))
                         :max_dose (x2s/value #(as-double (vtd/attr % :quantity)))}}]})

(def treatments-table
  {:sql-id :id
   :each "./activity/treatment/drugTreatment"
   :table :treatments
   :columns {:activity (x2s/parent-ref)
             :drug (x2s/sibling-ref :drugs #(vtd/attr (vtd/at % "./drug") :name))
             :periodicity (x2s/xpath-attr "./*/doseUnit" :perTime as-duration)}
   :dependent-tables [treatment-dosings-table]})

(def designs-table
  {:xml-id (x2s/value (fn [node] [(vtd/attr node :arm) (vtd/attr node :epoch)]))
   :sql-id (juxt :arm :epoch)
   :each "./usedBy"
   :table :designs
   :columns {:activity (x2s/parent-ref)
             :arm (x2s/sibling-ref :arms (fn [node] [nil (vtd/attr node :arm)]))
             :epoch (x2s/sibling-ref :epochs #(vtd/attr % :epoch))}})

(def activities-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./activities/studyActivity"
   :table :activities
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))
             :type (x2s/value #(as-activity-enum (vtd/at % "./activity/*")))}
   :dependent-tables [treatments-table designs-table]})

(defn when-taken-name
  [node]
  (let [howLong (vtd/attr node :howLong)
        relativeTo (vtd/attr node :relativeTo)
        epochName (vtd/attr (vtd/at node "./epoch") :name)]
    (str howLong " " relativeTo " " epochName)))

(defn arm-receives-treatment
  [studyNode epochName armName]
  (let [usedBys (vtd/search studyNode "./activities/studyActivity/usedBy")
        usedBy (first (filter #(and (= armName (vtd/attr % :arm)) (= epochName (vtd/attr % :epoch))) usedBys))]
    (and (not (nil? usedBy)) (not (nil? (vtd/at usedBy "../activity/treatment"))))))

(defn is-treatment-epoch
  [studyNode epochName]
  (let [arms (map #(vtd/attr % :name) (vtd/search studyNode "./arms/arm"))]
    (every? #(arm-receives-treatment studyNode epochName %) arms)))

(defn is-first-treatment-epoch
  [studyNode epochName]
  (let [epochNames (map  #(vtd/attr % :name) (vtd/search studyNode "./epochs/epoch"))
        firstTrtEpoch (first (filter #(is-treatment-epoch studyNode %) epochNames))]
    (= firstTrtEpoch epochName)))

(defn is-measurement-moment-primary
  [node]
  (and (= (vtd/attr node :howLong) "P0D")
       (= (vtd/attr node :relativeTo) "BEFORE_EPOCH_END")
       (is-first-treatment-epoch (vtd/at node "../../..") (vtd/attr (vtd/at node "./epoch") :name))))

(def measurement-moments-table
 {:xml-id (x2s/value when-taken-name)
   :sql-id :id
   :each "./studyOutcomeMeasures/studyOutcomeMeasure/whenTaken"
   :table :measurement_moments
   :columns {:study (x2s/parent-ref)
             :name (x2s/value when-taken-name)
             :epoch (x2s/sibling-ref :epochs (fn [node] (vtd/attr (vtd/at node "./epoch") :name)))
             :offset_from_epoch (x2s/value #(as-duration (vtd/attr % :howLong)))
             :relative_to (x2s/value #(as-epoch-offset-enum (vtd/attr % :relativeTo)))
             :is_primary (x2s/value is-measurement-moment-primary)
             }})

(defn resolve-var-ref
  [var-ref]
  (vtd/at var-ref
          (format "/addis-data/*[%s]/%s[@name=\"%s\"]"
                  (xpath-tag-or variable-types)
                  (vtd/tag var-ref)
                  (vtd/attr var-ref :name))))

(def variable-categories-table
  {:xml-id (x2s/value #(vtd/text %))
   :sql-id (juxt :variable :category_name)
   :each #(vtd/search (resolve-var-ref %) "./categorical/category")
   :table :variable_categories
   :columns {:variable (x2s/parent-ref)
             :category_name (x2s/value #(vtd/text %))}})

(def variables-table
  {:xml-id (x2s/value #(vtd/attr (vtd/at % "..") :id))
   :sql-id :id
   :each (str "./studyOutcomeMeasures/studyOutcomeMeasure/*[" (xpath-tag-or ["adverseEvent" "endpoint" "populationCharacteristic"]) "]")
   :table :variables
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))
             :description (x2s/value #(vtd/attr (resolve-var-ref %) :description))
             :unit_description (x2s/value #(vtd/attr (vtd/at (resolve-var-ref %) "./continuous") :unitOfMeasurement))
             :is_primary (x2s/xpath-attr ".." :primary as-boolean)
             :variable_type (x2s/value #(as-variable-type (vtd/tag %)))
             :measurement_type (x2s/value #(as-measurement-type
                                             (vtd/tag (vtd/at (resolve-var-ref %)
                                                              (str "./*[" (xpath-tag-or ["rate" "continuous" "categorical"]) "]")))))
             }
   :dependent-tables [variable-categories-table]
   :post-insert (entity-ref-rdf-callback (fn [entity-ref]
                                           (name (get variable-type-sql-to-xml (.getValue (:variable_type entity-ref))))))})

(def measurement-attrs
  {"mean" "mean"
   "stdDev" "standard deviation"
   "sampleSize" "sample size"
   "rate" "rate"})

(def integer-attrs ["sampleSize" "rate"])
(def real-attrs ["mean" "stdDev"])

(defn in? [coll x] (some #(= x %) coll))

(def measurements-table
  {:xml-id (x2s/value (fn [tag] [(vtd/attr (vtd/at tag "./studyOutcomeMeasure") :id)
                                 (vtd/attr (vtd/at tag "./arm") :name)
                                 (when-taken-name (vtd/at tag "./whenTaken"))]))
   :sql-id :id
   :each "./measurements/measurement"
   :table :measurements
   :columns {:study (x2s/parent-ref)
             :variable (x2s/sibling-ref :variables #(vtd/attr (vtd/at % "./studyOutcomeMeasure") :id))
             :arm (x2s/sibling-ref :arms (fn [node] [nil (vtd/attr (vtd/at node "./arm") :name)]))
             :measurement_moment (x2s/sibling-ref :measurement_moments #(when-taken-name (vtd/at % "./whenTaken")))}
   :collapse [{:xml-id (x2s/value #(vtd/attr % :name))
               :each "./categoricalMeasurement/category"
               :columns {:attribute (x2s/value #(vtd/attr % :name))
                         :integer_value (x2s/value #(as-int (vtd/attr % :rate)))
                         :real_value (x2s/value nil)}}
              {:xml-id (x2s/value vtd/tag)
               :each (str "./*[" (xpath-tag-or ["continuousMeasurement" "rateMeasurement"]) "]/@*")
               :columns {:attribute (x2s/value (fn [tag] (get measurement-attrs (vtd/tag tag))))
                         :integer_value (x2s/value (fn [tag] (if (in? integer-attrs (vtd/tag tag)) (as-int (vtd/text tag)) nil)))
                         :real_value (x2s/value (fn [tag] (if (in? real-attrs (vtd/tag tag)) (as-double (vtd/text tag)) nil)))
                         }}]})

(def indications-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "indication"
   :table :indications
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))}
   :post-insert (entity-ref-rdf-callback "indication")})

(def studies-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./studies/study"
   :table :studies
   :columns {:name (x2s/value #(vtd/attr % :name))
             :title (x2s/xpath-text "./characteristics/title/value")
             :objective (x2s/xpath-text "./characteristics/objective/value")
             :allocation (x2s/xpath-text "./characteristics/allocation/value" as-allocation-enum)
             :blinding (x2s/xpath-text "./characteristics/blinding/value" as-blinding-enum)
             :number_of_centers (x2s/xpath-text "./characteristics/centers/value" as-int)
             :created_at (x2s/xpath-text "./characteristics/created_at/value" as-timestamp)
             :source (x2s/xpath-text "./characteristics/source/value" as-source-enum)
             :exclusion (x2s/xpath-text  "./characteristics/exclusion/value")
             :inclusion (x2s/xpath-text "./characteristics/inclusion/value")
             :status (x2s/xpath-text "./characteristics/status/value" as-status-enum)
             :start_date (x2s/xpath-text "./characteristics/study_start/value" as-date)
             :end_date (x2s/xpath-text "./characteristics/study_end/value" as-date)}
   :dependent-tables [indications-table drugs-table units-table references-table
                      arms-table epochs-table activities-table
                      measurement-moments-table variables-table measurements-table]})

(def namespace-studies-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :study
   :each "./studies/study"
   :table :namespace_studies
   :columns {:namespace (x2s/parent-ref)
             :study (x2s/sibling-ref :studies #(vtd/attr % :name))}})

(def namespace-concepts-table
  {:xml-id (x2s/value #(entity-path (tag-to-entity %)))
   :sql-id (juxt :namespace :concept_path)
   :each (str "./*[" (xpath-tag-or entity-types) "]/*")
   :table :namespace_concepts
   :columns {:namespace (x2s/parent-ref)
             :concept_path (x2s/value #(entity-path (tag-to-entity %)))}})

(defn namespaces-table
  [name description]
  {:xml-id (x2s/value :namespace)
   :sql-id :id
   :each "/addis-data"
   :table :namespaces
   :columns {:name (x2s/value name)
             :description (x2s/value description)}
   :dependent-tables [namespace-concepts-table studies-table namespace-studies-table]})
