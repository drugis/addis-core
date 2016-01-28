(defproject org.drugis.importer "0.1.0-SNAPSHOT"
  :description "Imports ADDIS 1.x files into Trialverse and ConceptMapper"
  :url "http://drugis.org"
  :main org.drugis.importer.core
  :dependencies [[riveted "0.0.9"]
                 [clj-http "0.7.6"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [postgresql "9.1-901-1.jdbc4"]
                 [org.clojure/clojure "1.5.1"]])
