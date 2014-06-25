(ns org.drugis.addis.rdf.trig
  (:require [clojure.string :refer [join]]))

(defn iri? [x]
  "True if x is an IRI."
  (and (sequential? x) (or (= :uri (first x)) (= :qname (first x)))))

(declare write-pairs)

(def _indent_ "  ")

(defn- encode-char [c]
  (let [cp (int c)
        throw-cp (fn [] (throw (IllegalArgumentException. (str "Invalid code point. " (format "0x%x" cp)))))]
    (cond
      ;; String escape sequences
      (= c \\) "\\\\"
      (= c \") "\\\""
      (= c \') "\\'"
      (= c \tab) "\\t"
      (= c \backspace) "\\b"
      (= c \newline) "\\n"
      (= c \return) "\\r"
      (= c \formfeed) "\\f"
      (< cp 32) (throw-cp)
      (< cp 128) c
      (< cp 0xfff) (format "\\u%04x" cp)
      (< cp 0x10fff) (format "\\U%08x" cp)
      :else (throw-cp))))

(defn write-str [string]
  (str "\"" (join (map encode-char string)) "\""))

(defn iri-str [prefixes resource]
  (case (first resource)
    :uri (str "<" (second resource) ">")
    :qname 
    (let [prefix (second resource)
          suffix (nth resource 2)]
      (if (contains? prefixes prefix)
        (str (name prefix) ":" suffix)
        (throw (IllegalArgumentException. (str "Undefined prefix " prefix)))))))
    

(defn ttl-object-str
  "Resource to Turtle string for object position."
  ([prefixes resource] (ttl-object-str prefixes resource ""))
  ([prefixes resource indent]
     (let [indent+ (str indent _indent_)]
       (if (sequential? resource)
         (case (first resource)
           :uri (iri-str prefixes resource)
           :qname (iri-str prefixes resource)
           :blank (str "[\n" (write-pairs prefixes (second resource) indent) "\n" indent "]")
           :coll (let [collection (second resource)
                       members (map #(ttl-object-str prefixes % indent+) collection)
                       content (str indent+ (join (str "\n" indent+) members))]
                   (str "(\n" content "\n" indent ")"))
           :lit (let [x (second resource)] (cond
                 (instance? Boolean x) (str x)
                 (integer? x) (str x)
                 (float? x) (format "%e" (double x))
                 (string? x) (write-str x))))))))

(defn ttl-str
  "Resource to Turtle string for subject and predicate positions"
  [prefixes resource]
  (if (iri? resource)
    (iri-str prefixes resource)
    (throw (IllegalArgumentException. "Can only have URIs for graph, subject, and predicate positions"))))

; write pairs (predicate-object pairs)
(defn write-pairs [prefixes pairs indent]
  (let [indent+ (str indent _indent_)
        intermediate (map (fn [[k v]] (str indent+ (ttl-str prefixes k) " " (ttl-object-str prefixes v indent+))) pairs)]
    (join " ;\n" intermediate)))

; write triples regarding a single subject
(defn write-triples 
  ([prefixes triples] (write-triples prefixes triples ""))
  ([prefixes triples indent]
   (let [triple-str (write-pairs prefixes (second triples) indent)]
     (str indent (ttl-str prefixes (first triples)) "\n" triple-str " ."))))

(defn write-triples-list
  ([prefixes triples-list] (write-triples-list prefixes triples-list ""))
  ([prefixes triples-list indent]
   (join "\n\n" (map #(write-triples prefixes % indent) triples-list))))

; write a list of prefixes
(defn write-prefixes [prefixes]
  (let [write-prefix (fn [[prefix uri]] (format "@prefix %s: <%s> ." (name prefix)  uri))]
    (join "\n" (map write-prefix prefixes))))

(defn write-ttl
  [prefixes statements]
  (join "\n\n" [(write-prefixes prefixes) (write-triples-list prefixes statements)]))

(defn write-graph
  [prefixes graph]
    (join "\n\n" [(str (ttl-str prefixes (first graph)) " {") (write-triples-list prefixes (second graph) "  ") "}"]))

(defn write-trig
  [prefixes graphs]
    (join "\n\n" (concat [(write-prefixes prefixes)] (map #(write-graph prefixes %) graphs))))

(defn iri
  "Generate an RDF IRI."
  ([uri] [:uri uri])
  ([prefix resource] [:qname prefix resource]))

(defn lit
  "Generate an RDF literal of the given value."
  [value]
  [:lit value])

(defn coll
  "Generate an RDF collection from the given sequence of resources."
  [resources] [:coll resources])

(defn spo 
  "Generate a named node with a number of predicate-object pairs.
  s may be (i) an IRI or (ii) the result of a previous call to spo.
  In the latter case, the predicate-object pairs are appended to the predicate-object pair list of s."
  [s & po*]
  (if (and (sequential? s) (iri? (first s)))
    [(first s) (concat (second s) po*)]
    (cond
     (or (iri? s) (= :blank s)) [s po*]
     :else (throw (IllegalArgumentException. "Not a valid subject")))))

(defn _po
  "Generate a blank node with a number of predicate-object pairs."
  [& po*]
  (apply spo :blank po*))

(defn graph
  "Generate a graph with the given IRI and triples"
  [g triples]
  [g triples])
