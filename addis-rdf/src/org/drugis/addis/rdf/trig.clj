(ns org.drugis.addis.rdf.trig
  (:require [clojure.string :refer [join]]))

; TODO: support for the xsd: datatypes

(defn ttl-str [resource]
  (if (sequential? resource) (second resource) (str "\"" resource "\"")))

; write triples regarding a single subject
(defn write-triples 
  ([prefixes triples] (write-triples prefixes triples ""))
  ([prefixes triples indent]
   (let [intermediate (map (fn [[k v]] (str indent "  " (ttl-str k) " " (ttl-str v))) (second triples))
         triple-str (join " ;\n" intermediate)]
     (str indent (ttl-str (first triples)) "\n" triple-str " .\n"))))

; write a list of prefixes
(defn write-prefixes [prefixes]
  (let [write-prefix (fn [[prefix uri]] (format "@prefix %s: <%s> ." (name prefix)  uri))]
    (join "\n" (map write-prefix prefixes))))

(defn write-ttl
  [prefixes statements]
      (join "\n" (concat [(write-prefixes prefixes) ""] (map #(write-triples prefixes %) statements))))

(defn write-graph
  [prefixes graph]
    (join "\n" (concat [(str (ttl-str (first graph)) " {") ""] (map #(write-triples prefixes % "  ") (second graph)) ["}"])))

(defn write-trig
  [prefixes graphs]
    (join "\n\n" (concat [(write-prefixes prefixes)] (map #(write-graph prefixes %) graphs))))

(defn rdf-uri
  ([uri] [:uri (str "<" uri ">")])
  ([prefix resource] [:uri (str (name prefix) ":" resource)]))
