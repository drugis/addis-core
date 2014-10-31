(ns org.drugis.addis.rdf.pubmed-test
  (:require [riveted.core :as vtd]
            [org.drugis.addis.rdf.trig :as trig]
            [clojure.java.io :refer [as-file]])
  (:use clojure.test)
  (:use org.drugis.addis.rdf.pubmed))

(def hansen
  {:xml "16172440.xml"
   :pmid 16172440
   :title "Efficacy and safety of second-generation antidepressants in the treatment of major depressive disorder."
   :authors ["Hansen RA" "Gartlehner G" "Lohr KN" "Gaynes BN" "Carey TS"]
   :journal "Annals of internal medicine"
   :issn "1539-3704"
   :volume "143"
   :number "6"
   :pages "415-26"
   :year "2005"})

(deftest test-basics
  (is (= (pmid-uri "16172440") (trig/iri "info:pmid/16172440")))
  (is (= (pmid-uri 16172440) (trig/iri "info:pmid/16172440")))
  (is (= (pmid-uri "0016172440") (trig/iri "info:pmid/16172440")))
  (is (thrown? IllegalArgumentException (pmid-uri "abcdefg")))
  (let [xml (vtd/first-child (vtd/navigator (slurp (as-file "16172440.xml"))))]
    (is (= 16172440 (pmid xml)))
    (is (= (:title hansen) (title xml)))
    (is (= (:authors hansen) (authors xml)))
    (is (= (:journal hansen) (journal-title xml)))
    (is (= (:issn hansen) (journal-issn xml)))
    (is (= (:volume hansen) (journal-volume xml)))
    (is (= (:number hansen) (journal-number xml)))
    (is (= (:pages hansen) (journal-pages xml)))
    (is (= (:year hansen) (journal-year xml)))))

(defn println* [x]
  (println x)
  x)

(deftest test-rdf
  (let [prefixes {:dc "http://purl.org/dc/elements/1.1/" :prism "http://prismstandard.org/namespaces/1.2/basic/" :foaf "http://xmlns.com/foaf/0.1/" :rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"}
        rdf (pubmed-rdf (vtd/first-child (vtd/navigator (slurp (as-file "16172440.xml")))))]
    (println rdf)
    (println (trig/write-ttl prefixes [rdf]))
    (is (=
         (trig/spo (pmid-uri (:pmid hansen))
                   [(trig/iri :dc "title") (:title hansen)]
                   [(trig/iri :dc "creator") (trig/coll (map #(trig/_po [(trig/iri :rdf "type") (trig/iri :foaf "Person")] [(trig/iri :foaf "name") %]) (:authors hansen)))]
                   [(trig/iri :dc "date") (:year hansen)]
                   [(trig/iri :prism "publicationName") (:journal hansen)])
         rdf))))
