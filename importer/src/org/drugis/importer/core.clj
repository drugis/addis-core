(ns org.drugis.importer.core
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.java.io :refer [as-file]]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            [org.drugis.importer.xml2sql :as x2s]
            [org.drugis.importer.definitions :refer [namespaces-table entities-to-rdf]]
            [riveted.core :as vtd]))

(defn addis-import
  [datadef db ttl]
  (let [table-def (namespaces-table (:name datadef) (:description datadef))
        table (x2s/get-table (:data datadef) table-def)
        [namespace] (:namespace (:namespaces (x2s/insert-table (x2s/jdbc-inserter db) table)))]
    (entities-to-rdf (:data datadef) ttl namespace)
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
    (when (or (:help options) (some nil? ((juxt :file :database :name :title) options)))
      (println banner)
      (System/exit 0))
    (let
        [data (vtd/navigator (slurp (as-file (options :file))))
         db {:connection-uri (str "jdbc:" (options :database))}
         ttl (as-file "out.ttl")]
      (jdbc/db-transaction* db
                            (fn [db]
                              (let [namespace
                                    (addis-import {:data data
                                                   :name (options :name)
                                                   :description (options :title)} db ttl)]
                                (println "namespace_id" namespace)
                                (println (jdbc/query db (sql/select "COUNT(*) AS entity_count" :namespace_concepts (sql/where {:namespace namespace}))))
                                (println (jdbc/query db
                                                     (sql/select [:id :name] :studies
                                                                 (sql/join :namespace_studies {:studies.id :namespace_studies.study})
                                                                 (sql/where {:namespace_studies.namespace namespace})
                                                                 (sql/order-by :studies.name))))))))))
