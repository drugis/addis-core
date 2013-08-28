(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [clj-xpath.core :refer [$x $x:tag $x:text $x:text* $x:attrs $x:attrs* $x:node $x:tag* xml->doc]]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-file]]))

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
  (merge (:attrs tag) {:type (name (:tag tag))}))

(def entity-types ["units" "indications" "drugs" "endpoints" "adverseEvents" "populationCharacteristics"])

(defn import-entities
  [data db ttl namespace]
  (let [xpath-expr (join "|" (map (fn [type] (str "self::" type)) entity-types))
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

(defn as-date
  [x]
  (if (nil? x) nil (java.sql.Date. (.getTimeInMillis (javax.xml.bind.DatatypeConverter/parseDateTime x)))) )

{:studies {"Aberg-Wisted sdljdfs" 74}}

(def references-table
  {:xml-id ["." :text]
   :sql-id :id
   :each "./characteristics/references/pubMedId"
   :table :references
   :columns {:study ["." (fn [_] nil) :parent]
             :id ["." :text]
             :repository ["." (fn [_] "PubMed")]}})

(def studies-table
  {:xml-id ["." #(get-in % [:attrs :name])]
   :sql-id :id
   :each "/addis-data/studies/study"
   :table :studies
   :columns {:name ["." #(get-in % [:attrs :name])]
             :title ["./characteristics/title/value" :text]
             :objective ["./characteristics/objective/value" :text]
             :allocation ["./characteristics/allocation/value" :text]
             :blinding ["./characteristics/objective/value" :text]
             :number_of_centers ["./characteristics/centers/value" #(as-int (:text %))]
             :created_at ["./characteristics/created_at/value" #(as-date (:text %))]
             :source ["./characteristics/source/value" :text]
             :exclusion ["./characteristics/exclusion/value" :text]
             :inclusion ["./characteristics/inclusion/value" :text]
             :status ["./characteristics/study_status/value" :text]
             :start_date ["./characteristics/study_start/value" #(as-date (:text %))]
             :end_date ["./characteristics/study_end/value" #(as-date (:text %))]}
   :dependent-tables [references-table]})

(defn $x?
  [xpath xml]
  (first ($x xpath xml)))

(defn apply-context
  ([row context] (apply-context row nil context))
  ([row parent context]
  (into {}
         (map (fn [[col-name val-fn]] {col-name (val-fn parent context)}) row))))

(defn get-xml-value
  [xml value-def]
  (let [node ($x? (first value-def) xml)] ((second value-def) node)))

(defn get-column-value
  [xml col-name col-def]
  (let [value (get-xml-value xml col-def)
        ref-type (nth col-def 2 nil)
        ref-key (nth col-def 3 nil)]
  {col-name (fn [parent-id context]
              (if (nil? ref-type) value (if (= :sibling ref-type) (get-in context [ref-key value]) parent-id)))}))

(defn get-column-values
  [xml defs]
  (into {}
         (map (fn [[col-name col-def]]
                (get-column-value xml col-name col-def)) defs)))

(declare get-table)

(defn get-table-row
  [xml table]
  (let [xml-id (get-xml-value xml (:xml-id table))
        columns (get-column-values xml (:columns table))
        rev-deps (map #(get-table xml %) (:dependent-tables table))]
  {xml-id {:columns columns :dependent-tables rev-deps}}))

(defn get-table
  [xml table]
  (let [elements ($x (:each table) xml)
        rows (into {} (map #(get-table-row % table) elements))]
    {:table (:table table) :sql-id (:sql-id table) :rows rows}))

(defn jdbc-inserter
  [db]
  (fn [table columns]
    (first (jdbc/insert! db table columns :entities (sql/quoted \")))))

(defn insert-row
  [inserter table-name sql-id-fn row-xml-id columns]
  [row-xml-id (sql-id-fn (inserter table-name columns))])

(defn insert-table
  ([inserter table-data] (insert-table inserter table-data nil {}))
  ([inserter {:keys [sql-id rows table]} parent-id context]
   {table (into {}(map (fn [[k v]]
                   (let [inserted (insert-row inserter table sql-id k (apply-context (:columns v) parent-id context))
                         xml-id (first inserted)
                         sql-id (second inserted)]
                     (loop [dt (:dependent-tables v) acc {}]
                       (if (seq dt)
                         (recur (rest dt) (merge acc (insert-table inserter (first dt) sql-id acc)))
                         {xml-id [sql-id acc]}))
                     )) rows))}))

(defn addis-import
  [datadef db ttl]
  (let [data (datadef :data)
        namespace (init-namespace db (datadef :name) (datadef :description))]
    (import-entities data db ttl namespace)
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
