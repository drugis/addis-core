(ns org.drugis.addis.rdf.pubmed
  (:require [clojure.java.io :refer [as-file]]
            [riveted.core :as vtd]
            [org.drugis.addis.rdf.trig :as trig]))

(defn pmid [xml]
  "Get the PubMed ID from a PubMed XML record"
  (Integer. (vtd/text (vtd/at xml "./MedlineCitation/PMID"))))

(defn title [xml]
  "Get the article title from a PubMed XML record"
  (vtd/text (vtd/at xml "./MedlineCitation/Article/ArticleTitle")))

(defn journal-title [xml]
  "Get the journal title from a PubMed XML record"
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Journal/Title")))

(defn journal-volume [xml]
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Journal/JournalIssue/Volume")))

(defn journal-number [xml]
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Journal/JournalIssue/Issue")))

; Also see http://xissn.worldcat.org/xissnadmin/doc/api.htm
(defn journal-issn [xml]
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Journal/ISSN")))

(defn journal-pages [xml]
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Pagination/MedlinePgn")))

(defn journal-year [xml]
  (vtd/text (vtd/at xml "./MedlineCitation/Article/Journal/JournalIssue/PubDate/Year")))

(defn author-name [xml]
  (str (vtd/text (vtd/at xml "./LastName")) " " (vtd/text (vtd/at xml "./Initials"))))

(defn authors [xml]
  (map author-name (vtd/search xml "./MedlineCitation/Article/AuthorList/Author")))

(defn pmid-uri [pmid]
  (trig/iri (str "info:pmid/" (Integer. pmid))))

; FIXME: consider BIBO instead of PRISM
(defn pubmed-rdf [xml]
  (trig/spo (pmid-uri (pmid xml)) ; FIXME: how to set the identifier?
            [(trig/iri :dc "identifier") (pmid-uri (pmid xml))]
            [(trig/iri :dc "title") (title xml)]
            [(trig/iri :dc "creator") (trig/coll (map #(trig/_po [(trig/iri :rdf "type") (trig/iri :foaf "Person")] [(trig/iri :foaf "name") %]) (authors xml)))]
            [(trig/iri :dc "date") (journal-year xml)]
            [(trig/iri :prism "publicationName") (journal-title xml)]
            [(trig/iri :prism "issn") (journal-issn xml)]
            [(trig/iri :prism "volume") (journal-volume xml)]
            [(trig/iri :prism "number") (journal-number xml)]
            [(trig/iri :prism "startingPage") (journal-pages xml)] ; FIXME: split up starting and ending page
            ))
