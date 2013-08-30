(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-file]]
            [org.drugis.importer.xml2sql :as x2s]
            [riveted.core :as vtd]  
            ))

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
  (format "http://trials.drugis.org/namespace/%d/%s" namespace (entity-path entity)))

(defn tag-to-entity
  [tag]
  (merge (x2s/attrs tag) {:type (vtd/tag tag)}))

(def variable-types ["endpoints" "adverseEvents" "populationCharacteristics"])
(def entity-types (concat ["units" "indications" "drugs"] variable-types))

(defn xpath-tag-or
  [tag-names]
  (join "|" (map (fn [tag-name] (str "self::" tag-name)) tag-names)))

(defn init-namespace
  [db name description]
  (:id (first (jdbc/insert! db :namespaces {:name name :description description}))))

(defn snomed-uri
  [snomed-id]
  (format "http://www.ihtsdo.org/SCT_%s" snomed-id))

(defn atc-uri
  [atc-id]
  (format "http://www.whocc.no/ATC2011/%s" atc-id))

(defn write-ttl
  [statements]
  (let [ttl-str (fn [resource] (if (sequential? resource) (second resource) (str "\"" resource "\"")))
        write-triples
        (fn [triples]
          (let [intermediate (map (fn [[k v]] (str "  " (ttl-str k) " " (ttl-str v))) (second triples))
                triple-str (reduce (fn [itm acc] (str acc " ;\n" itm)) intermediate)]
            (str (ttl-str (first triples)) "\n" triple-str " .\n")))]
    (reduce (fn [itm acc] (str acc "\n" itm)) (map write-triples statements))))

(def entity-type-map
  {"indication" (fn [entity] (snomed-uri (:code entity)))
   "drug" (fn [entity] (atc-uri (:atcCode entity)))})

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
            [(rdf-uri :rdfs "label") (:name entity)]
            [(rdf-uri :rdfs "comment") (:description entity)]]
           (entity-mapping entity))])

(defn import-entities
  [data db ttl namespace]
  (let [xpath-expr (xpath-tag-or entity-types)
        entities (map tag-to-entity (vtd/search (str "/addis-data/*[" xpath-expr "]/*") data))]
    (spit ttl (write-ttl (map #(entity-rdf namespace %) entities)))
    (apply (partial jdbc/insert! db :namespace_concepts [:namespace :concept_path])
           (map (fn [entity] [namespace (entity-path entity)]) entities))))

(defn println*
  [arg]
  (println arg)
  arg)

(defn as-int
  [x]
  (if (nil? x) nil (Integer. x)))

(defn as-double
  [x]
  (if (nil? x) nil (Double. x)))

(defn as-date
  [x]
  (if (nil? x) nil (java.sql.Date. (.getTimeInMillis (javax.xml.bind.DatatypeConverter/parseDateTime x)))) )

(def references-table
  {:xml-id (x2s/value vtd/text)
   :sql-id :id
   :each "./characteristics/references/pubMedId"
   :table :references
   :columns {:study (x2s/parent-ref)
             :id (x2s/value vtd/text)
             :repository (x2s/value "PubMed")}})

(def arms-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./arms/arm"
   :table :arms
   :columns {:study (x2s/parent-ref) 
             :name (x2s/value #(vtd/attr % :name))
             :arm_size (x2s/value #(vtd/attr % :size))}})

(def epochs-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./epochs/epoch"
   :table :epochs
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))
             :duration (x2s/value #(vtd/attr % :duration))}})

(defn when-taken-name
  [node]
  (let [howLong (vtd/attr node :howLong)
        relativeTo (vtd/attr node :relativeTo)
        epochName (vtd/attr (vtd/at node "./epoch") :name)]
    (str howLong " " relativeTo " " epochName)))

(def measurement-moments-table
 {:xml-id (x2s/value when-taken-name)
   :sql-id :id
   :each "./studyOutcomeMeasures/studyOutcomeMeasure/whenTaken"
   :table :measurement_moments
   :columns {:study (x2s/parent-ref)
             :name (x2s/value when-taken-name)
             :epoch (x2s/sibling-ref :epochs (fn [node] (vtd/attr (vtd/at node "./epoch") :name)))}
  })

(defn resolve-var-ref
  [var-ref]
  (vtd/at var-ref
          (format "/addis-data/*[%s]/%s[@name=\"%s\"]"
                  (xpath-tag-or variable-types)
                  (vtd/tag var-ref)
                  (vtd/attr var-ref :name))))

(def variables-table
  {:xml-id (x2s/value #(vtd/attr (vtd/at % "..") :id))
   :sql-id :id
   :each (str "./studyOutcomeMeasures/studyOutcomeMeasure/*[" (xpath-tag-or ["adverseEvent" "endpoint" "populationCharacteristic"]) "]")
   :table :variables
   :columns {:study (x2s/parent-ref)
             :name (x2s/value #(vtd/attr % :name))
             :description (x2s/value #(vtd/attr (resolve-var-ref %) :description))}})

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
             :arm (x2s/sibling-ref :arms #(vtd/attr (vtd/at % "./arm") :name))
             :measurement_moment (x2s/sibling-ref :measurement_moments #(when-taken-name (vtd/at % "./whenTaken")))}
   :collapse [{:xml-id (x2s/value #(vtd/attr % :name))
               :each "./categoricalMeasurement/category"
               :columns {:attribute (x2s/value #(vtd/attr % :name))
                         :integer_value (x2s/value #(as-int (vtd/attr % :rate)))
                         :real_value (x2s/value nil)}}
              {:xml-id (x2s/value vtd/tag)
               :each (str "./*[" (xpath-tag-or ["continuousMeasurement" "rateMeasurement"]) "]/@*")
               :columns {:attribute (x2s/value (fn [tag] (get measurement-attrs (vtd/tag tag))))
                         :integer_value (x2s/value (fn [tag] (if (in? integer-attrs (vtd/tag tag)) (as-int (x2s/attr-value tag)) nil)))
                         :real_value (x2s/value (fn [tag] (if (in? real-attrs (vtd/tag tag)) (as-double (x2s/attr-value tag)) nil)))
                         }}]})

(def studies-table
  {:xml-id (x2s/value #(vtd/attr % :name))
   :sql-id :id
   :each "./studies/study"
   :table :studies
   :columns {:name (x2s/value #(vtd/attr % :name))
             :title (x2s/xpath-text "./characteristics/title/value")
             :indication (x2s/sibling-ref :indications #(vtd/attr % :name))
             :objective (x2s/xpath-text "./characteristics/objective/value")
             :allocation (x2s/xpath-text "./characteristics/allocation/value")
             :blinding (x2s/xpath-text "./characteristics/blinding/value")
             :number_of_centers (x2s/xpath-text "./characteristics/centers/value" as-int)
             :created_at (x2s/xpath-text "./characteristics/created_at/value" as-date)
             :source (x2s/xpath-text "./characteristics/source/value")
             :exclusion (x2s/xpath-text  "./characteristics/exclusion/value")
             :inclusion (x2s/xpath-text "./characteristics/inclusion/value")
             :status (x2s/xpath-text "./characteristics/status/value")
             :start_date (x2s/xpath-text "./characteristics/start_date/value" as-date)
             :end_date (x2s/xpath-text "./characteristics/end_date/value" as-date)}
   :dependent-tables [references-table arms-table epochs-table measurement-moments-table variables-table measurements-table]})

(def indications-table
  {:xml-id (x2s/value #(vtd/attr (vtd/at % "..") :name))
   :sql-id :id
   :each "./studies/study/indication"
   :table :indications
   :columns {:name (x2s/value #(vtd/attr % :name))}})

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
   :dependent-tables [namespace-concepts-table indications-table studies-table namespace-studies-table]})

(defn addis-import
  [datadef db ttl]
  (let [table-def (namespaces-table (:name datadef) (:description datadef))
        table (x2s/get-table (:data datadef) table-def)]
    (first (:namespace (:namespaces (x2s/insert-table (x2s/jdbc-inserter db) table))))))

(defn -main
  [& args]
  (let [[options args banner]
        (cli args
             ["-h" "--help" "Show Help" :default false :flag true]
             ["-d" "--database" "JDBC connection URI for the db"]
             ["-f" "--file" "ADDIS 1.x file"]
             ["-n" "--name" "Dataset short name"]
             ["-t" "--title" "Dataset description" :default "ADDIS data import"])]
    (when (or (:help options) (not (:database options)) (not (:file options)) (not (:name options)))
      (println banner)
      (System/exit 0))
    ;(Thread/sleep 2000)
    (let
      [data (vtd/navigator (slurp (as-file (options :file))))
       db {:connection-uri (str "jdbc:" (options :database))}
       ttl (as-file "out.ttl")]
      (try
        (jdbc/db-transaction* db
                              (fn [db]
                                (let [namespace
                                      (addis-import {:data data
                                                     :name (options :name)
                                                     :description (options :title)} db ttl)]
                                  (println namespace)
                                  (println (jdbc/query db (sql/select "COUNT(*)" :namespace_concepts (sql/where {:namespace namespace}))))
                                  (println (jdbc/query db (sql/select [:id :name] :studies)))
                                  )
                                ;(throw (InterruptedException.))
                                ))
        (catch InterruptedException e)))))
