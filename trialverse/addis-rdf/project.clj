(defproject org.drugis.addis/rdfexport "1.1.2"
  :description "Convert ADDIS 1.x XML to ADDIS 2 RDF"
  :url "https://drugis.org/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [riveted "0.0.9"]
                 [org.clojure/tools.cli "0.3.1"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.7" "-source" "1.7"]
  :target-path "target/%s"
  :plugins [[org.apache.maven.wagon/wagon-ssh-external "2.6"]]
  :repositories [["drugis" { :url "scp://drugis.org/srv/mvn/" :username "maven" :sign-releases false } ]]
  :profiles {:uberjar {:aot :all}}
  :main org.drugis.addis.rdf.core
  :license GPL-3)

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))
