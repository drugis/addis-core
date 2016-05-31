(defproject addis-rdf "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [riveted "0.0.9"]
                 [org.clojure/tools.cli "0.3.1"]]
  :plugins [[codox "0.8.9"]]
  :codox {:defaults {:doc/format :markdown}}
  :main ^:skip-aot org.drugis.addis.rdf.core
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
