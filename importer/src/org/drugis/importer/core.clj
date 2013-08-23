(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [clj-xpath.core :refer [$x $x:tag $x:text $x:attrs $x:attrs* $x:node $x:tag*]]
            [clojure.core :refer [slurp]]
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

(defn entity-uri
  [namespace type name]
  (format "http://trials.drugis.org/namespace/%d/%s/%s" namespace type (md5 name))
  )

(defn snomed-uri
  [snomed-id]
  (format "http://www.ihtsdo.org/SCT_%s" snomed-id))

(defn entities
  [data type])

(defn owl-class [uri] (format "<%s> a owl:Class ." uri))
(defn owl-same [uri-a uri-b] (format "<%s> owl:sameAs <%s>" uri-a uri-b))

(defn addis-import
  [datadef db]
  (let [data (datadef :data)
        namespace (init-namespace db (datadef :name) (datadef :description))
        endpoints (map (fn [tag] (:attrs tag)) ($x "/addis-data/endpoints/endpoint" data))
        indications (map (fn [tag] (:attrs tag)) ($x "/addis-data/indications/indication" data))]
    (println (map owl-class (map #(entity-uri namespace "endpoint" (:name %)) endpoints)))
    (println (map #(entity-uri namespace "indication" (:name %)) indications))
    (println (map #(owl-same (entity-uri namespace "indication" (:name %)) (snomed-uri (:code %))) (filter :code indications)))
    (println (map #(snomed-uri (:code %)) indications))))

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
       db {:connection-uri (str "jdbc:" (options :database))}]
      ;(println (map (fn [tag] (:attrs tag)) ($x "/addis-data/indications/indication" data)))
      (jdbc/db-transaction* db (fn [db] (addis-import {:data data :name (options :name) :description (options :title)} db) (throw (Exception.))))
      (println (jdbc/query db (sql/select * :studies)))
      )))
