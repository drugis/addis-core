(defproject addis-rdf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [riveted "0.0.9"]
                 [org.clojure/tools.cli "0.3.1"]]
  :plugins [[codox "0.8.9"]]
  :codox {:defaults {:doc/format :markdown}}
  :main ^:skip-aot org.drugis.addis.rdf.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
