(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [clj-xpath.core :refer [$x $x:tag $x:text $x:text* $x:attrs $x:attrs* $x:node $x:tag*]]
            [clojure.core :refer [slurp]]
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

(defn import-study
  [data db ttl namespace study]
  (let [xpath #(format "/addis-data/studies/study[@name=\"%s\"]/characteristics/%s/value" (get-in study [:attrs :name]) (name %))
        characteristic (fn [name] (first ($x:text* (println* (xpath name)) data)))
        characteristics [:title :objective :allocation :blinding :centers :created_at :source :exclusion :inclusion :study_status
                     :study_start :study_end]
        row (doall (merge {:name (get-in study [:attrs :name])}
                   (zipmap characteristics (map characteristic characteristics))))]
    (:id (first (jdbc/insert! db :studies row)))))

(defn import-studies
  [data db ttl namespace]
  (let [studies ($x "/addis-data/studies/study" data)]
    (doall (map (partial import-study data db ttl namespace) studies))))

(defn addis-import
  [datadef db ttl]
  (let [data (datadef :data)
        namespace (init-namespace db (datadef :name) (datadef :description))]
    (import-entities data db ttl namespace)
    (import-studies data db ttl namespace)
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
    (when (:help options)
      (println banner)
      (System/exit 0))
    (let
      [data (slurp (as-file (options :file)))
       db {:connection-uri (str "jdbc:" (options :database))}
       ttl (as-file "out.ttl")]
      ;(println (map (fn [tag] (:attrs tag)) ($x "/addis-data/indications/indication" data)))
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
                                (throw (InterruptedException.))
                                ))
        (catch InterruptedException e)))))
