(ns org.drugis.addis.rdf.trig
  (:require [clojure.string :refer [join]]))

; TODO: support for the xsd: datatypes

(declare write-pairs)

(defn ttl-object-str [prefixes resource indent]
  (if (sequential? resource)
    (({:uri (fn [x] x)
       :anon (fn [x] (str "\n" indent "[\n" (write-pairs prefixes x indent) "\n" indent "]")) ; anonymous node
       :coll (fn [x] (str "\n" indent "(" (join "" (map #(ttl-object-str prefixes % (str indent "  ")) x)) "\n" indent ")"))} ; RDF collection
      (first resource))
     (second resource))
    (str "\"" resource "\"")))

(defn ttl-str [resource]
  (if (and (sequential? resource) (= :uri (first resource)))
    (second resource)
    (throw (Throwable. "Can only have URIs for graph, subject, and predicate positions"))))

; write pairs (predicate-object pairs)
(defn write-pairs [prefixes pairs indent]
  (let [intermediate (map (fn [[k v]] (str indent "  " (ttl-str k) " " (ttl-object-str prefixes v (str indent "  " "  ")))) pairs)]
    (join " ;\n" intermediate)))

; write triples regarding a single subject
(defn write-triples 
  ([prefixes triples] (write-triples prefixes triples ""))
  ([prefixes triples indent]
   (let [triple-str (write-pairs prefixes (second triples) indent)]
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

(defn rdf-anon
  [pairs] [:anon pairs])

(defn rdf-coll
  [resources] [:coll resources])
