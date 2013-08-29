(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-file]]
            [org.drugis.importer.xml2sql :refer :all]
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

(defn init-namespace
  [db name description]
  (:id (first (jdbc/insert! db :namespaces {:name name :description description}))))

(defn entity-path
  [entity]
  (format "%s/%s"(:type entity) (md5 (:name entity))))

(defn entity-uri
  [namespace entity]
  (format "http://trials.drugis.org/namespace/%d/%s" namespace (entity-path entity)))

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

(defn tag-to-entity
  [tag]
  (merge (:attrs tag) {:type (name (vtd/tag tag))}))

(def entity-types ["units" "indications" "drugs" "endpoints" "adverseEvents" "populationCharacteristics"])

(defn xpath-tag-or
  [tag-names]
  (join "|" (map (fn [tag-name] (str "self::" tag-name)) tag-names)))

(defn import-entities
  [data db ttl namespace]
  (let [xpath-expr (xpath-tag-or entity-types)
        entities (map tag-to-entity ($x (str "/addis-data/*[" xpath-expr "]/*") data))]
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
  {:xml-id ["." vtd/text]
   :sql-id :id
   :each "./characteristics/references/pubMedId"
   :table :references
   :columns {:study ["." (fn [_] nil) :parent]
             :id ["." vtd/text]
             :repository ["." (fn [_] "PubMed")]}})

(def arms-table
  {:xml-id ["." #(vtd/attr % :name)]
   :sql-id :id
   :each "./arms/arm"
   :table :arms
   :columns {:study ["." (fn [_] nil) :parent]
             :name ["." #(vtd/attr % :name)]
             :arm_size ["." #(vtd/attr % :size)]}})

(def epochs-table
  {:xml-id ["." #(vtd/attr % :name)]
   :sql-id :id
   :each "./epochs/epoch"
   :table :epochs
   :columns {:study ["." (fn [_] nil) :parent]
             :name ["." #(vtd/attr % :name)]
             :duration ["." #(vtd/attr % :duration)]}})

(defn when-taken-name
  [node]
  (let [howLong (vtd/attr node :howLong)
        relativeTo (vtd/attr node :relativeTo)
        epochName (vtd/attr ($x? "./epoch" node) :name)]
    (str howLong " " relativeTo " " epochName)))

(def measurement-moments-table
 {:xml-id ["." when-taken-name]
   :sql-id :id
   :each "./studyOutcomeMeasures/studyOutcomeMeasure/whenTaken"
   :table :measurement_moments
   :columns {:study ["." (fn [_] nil) :parent]
             :name ["." when-taken-name]
             :epoch ["./epoch" #(vtd/attr % :name) :sibling :epochs]
             }})

(def variables-table
  {:xml-id ["." #(vtd/attr % :id)]
   :sql-id :id
   :each "./studyOutcomeMeasures/studyOutcomeMeasure"
   :table :variables
   :columns {:study ["." (fn [_] nil) :parent]
             :name [(str "./*[" (xpath-tag-or ["adverseEvent" "endpoint" "populationCharacteristic"]) "]") #(vtd/attr % :name)]}
   })

(def measurement-attrs
  {:mean "mean"
   :stdDev "standard deviation"
   :sampleSize "sample size"
   :rate "rate"})

(def integer-attrs [:sampleSize :rate])
(def real-attrs [:mean :stdDev])

(defn in? [coll x] (some #(= x %) coll))

(def measurements-table
  {:xml-id ["." (fn [tag] [(vtd-value ($x? "./studyOutcomeMeasure/@id" tag))
                           (vtd-value ($x? "./arm/@name" tag))
                           (when-taken-name ($x? "./whenTaken" tag))])]
   :sql-id :id
   :each "./measurements/measurement"
   :table :measurements
   :columns {:study ["." (fn [_] nil) :parent]
             :variable ["./studyOutcomeMeasure" #(vtd/attr % :id) :sibling :variables]
             :arm ["./arm" #(vtd/attr % :name) :sibling :arms]
             :measurement_moment ["./whenTaken" when-taken-name :sibling :measurement_moments]
             :attribute ["." (fn [_] "x")]}
   :collapse [{:xml-id ["." #(vtd/attr % :name)]
               :each "./categoricalMeasurement/category"
               :columns {:attribute ["." #(vtd/attr % :name)]
                         :integer_value ["." (fn [tag] (as-int (vtd/attr tag :rate)))]
                         :real_value ["." (fn [_] nil)]}}
              {:xml-id ["." vtd/tag]
               :each (str "./*[" (xpath-tag-or ["continuousMeasurement" "rateMeasurement"]) "]/@*")
               :columns {:attribute ["." (fn [tag] ((vtd/tag tag) measurement-attrs))]
                         :integer_value ["." (fn [tag] (if (in? integer-attrs (vtd/tag tag)) (as-int (vtd-value tag)) nil))]
                         :real_value ["." (fn [tag] (if (in? real-attrs (vtd/tag tag)) (as-double (vtd-value tag)) nil))]
                         }}]
   })

(def studies-table
  {:xml-id ["." #(vtd/attr % :name)]
   :sql-id :id
   :each "/addis-data/studies/study"
   :table :studies
   :columns {:name ["." #(vtd/attr % :name)]
             :title ["./characteristics/title/value" vtd/text]
             :objective ["./characteristics/objective/value" vtd/text]
             :allocation ["./characteristics/allocation/value" vtd/text]
             :blinding ["./characteristics/objective/value" vtd/text]
             :number_of_centers ["./characteristics/centers/value" #(as-int (vtd/text %))]
             :created_at ["./characteristics/created_at/value" #(as-date (vtd/text %))]
             :source ["./characteristics/source/value" vtd/text]
             :exclusion ["./characteristics/exclusion/value" vtd/text]
             :inclusion ["./characteristics/inclusion/value" vtd/text]
             :status ["./characteristics/study_status/value" vtd/text]
             :start_date ["./characteristics/study_start/value" #(as-date (vtd/text %))]
             :end_date ["./characteristics/study_end/value" #(as-date (vtd/text %))]}
   :dependent-tables [references-table arms-table epochs-table measurement-moments-table variables-table measurements-table]})

(defn addis-import
  [datadef db ttl]
  (let [data (datadef :data)
        namespace (init-namespace db (datadef :name) (datadef :description))]
    ;(import-entities data db ttl namespace)
    (insert-table (jdbc-inserter db) (get-table data studies-table))
    namespace))

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
    (let
      [data (xml->doc (slurp (as-file (options :file))))
       db {:connection-uri (str "jdbc:" (options :database))}
       ttl (as-file "out.ttl")]
      (try
        (jdbc/db-transaction* db
                              (fn [db]
                                (let [namespace
                                      (addis-import {:data data
                                                     :name (options :name)
                                                     :description (options :title)} db ttl)]
                                  (println (jdbc/query db (sql/select "COUNT(*)" :namespace_concepts (sql/where {:namespace namespace}))))
                                  (println (jdbc/query db (sql/select [:id :name] :studies)))
                                  )
                                ;(throw (InterruptedException.))
                                ))
        (catch InterruptedException e)))))
