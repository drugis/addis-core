(ns org.drugis.addis.rdf.trig-test
  (:use clojure.test)
  (:use org.drugis.addis.rdf.trig))

; TODO: actually blank nodes are also allowed in the subject position
(deftest test-ttl-str 
  (is (= (ttl-str (iri "http://example.com/8")) "<http://example.com/8>") "Handles plain URIs")
  (is (= (ttl-str (iri :rdfs "comment")) "rdfs:comment") "Handles QName URIs")
  (is (thrown? IllegalArgumentException (ttl-str (lit "testing..."))) "Literals not allowed")
  (is (thrown? IllegalArgumentException (ttl-str (lit 3))) "Literals not allowed")
  (is (thrown? IllegalArgumentException (ttl-str (coll [(iri "http://example.com/8")]))) "Collections not allowed")
  (is (thrown? IllegalArgumentException (ttl-str (_po [(iri :rdfs "comment") "no comment"]))) "Blank node property lists not allowed"))

(deftest test-ttl-object-str
  (let [prefixes {:xsd "http://www.w3.org/2001/XMLSchema#" :rdfs "http://www.w3.org/2000/01/rdf-schema#" :ex "http://example.com/"}]
    (testing "References and literals"
      (is (= (ttl-object-str prefixes (iri "http://example.com/8")) "<http://example.com/8>") "Handles plain URIs")
      (is (= (ttl-object-str prefixes (iri :rdfs "comment")) "rdfs:comment") "Handles QName URIs")
      (is (= (ttl-object-str prefixes (lit "testing...")) "\"testing...\"") "Handles string literals")
      (is (= (ttl-object-str prefixes (lit "\"testing\"")) "\"\\\"testing\\\"\"") "Escapes quotes in string literals")
      (is (= (ttl-object-str prefixes (lit "đ")) "\"\\u0111\"") "Escapes unicode in string literals")
      (is (= (ttl-object-str prefixes (lit 3)) "3") "Handles integer literals")
      (is (= (ttl-object-str prefixes (lit 3.0)) (format "%e" 3.0)) "Handles double literals")
      (is (= (ttl-object-str prefixes (lit false)) "false") "Handles boolean literals"))
    (testing "Collections"
      (is (= (ttl-object-str prefixes (coll [(iri "http://example.com/8")]))
             "(\n  <http://example.com/8>\n)")
          "Collection with single member (unindented)")
      (is (= (ttl-object-str prefixes (coll [(iri :ex 8) (iri :ex 9)]))
             "(\n  ex:8\n  ex:9\n)")
          "Collection with multiple members (unindented)")
      (is (= (ttl-object-str prefixes (coll [(iri "http://example.com/8")]) "  ")
             "(\n    <http://example.com/8>\n  )")
          "Collection with single member (indented)")
      (is (= (ttl-object-str prefixes (coll [(iri :ex 8) (iri :ex 9)]) "  ")
             "(\n    ex:8\n    ex:9\n  )")
          "Collection with multiple members (indented)"))
    (testing "Blank node property list"
      (is (= (ttl-object-str prefixes (_po [(iri :rdfs "comment") "no comment"]))
             "[\n  rdfs:comment \"no comment\"\n]")
          "Blank node with single property (unindentend)")
      (is (= (ttl-object-str prefixes (_po [(iri :rdfs "comment") "no comment"] [(iri :rdfs "label") "label"]))
             "[\n  rdfs:comment \"no comment\" ;\n  rdfs:label \"label\"\n]")
          "Blank node with multiple properties (unindented)")
      (is (= (ttl-object-str prefixes (_po [(iri :rdfs "comment") "no comment"]) "  ")
             "[\n    rdfs:comment \"no comment\"\n  ]")
          "Blank node with single property (indentend)")
      (is (= (ttl-object-str prefixes (_po [(iri :rdfs "comment") "no comment"] [(iri :rdfs "label") "label"]) "  ")
             "[\n    rdfs:comment \"no comment\" ;\n    rdfs:label \"label\"\n  ]")
          "Blank node with multiple properties (indented)"))
    (testing "Collections of blank nodes"
      (is (= (ttl-object-str prefixes (coll [(_po [(iri :rdfs "comment") "no comment"])]))
             "(\n  [\n    rdfs:comment \"no comment\"\n  ]\n)")))
    (testing "Nested blank nodes"
      (is (= (ttl-object-str prefixes (_po [(iri :ex "onceSaid") (_po [(iri :rdfs "comment") "no comment"])]))
             "[\n  ex:onceSaid [\n    rdfs:comment \"no comment\"\n  ]\n]")))
    ; TODO: test more cases - these are the ones used by "core"
    ))

(deftest test-write-triples
  (let [prefixes {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  :rdfs "http://www.w3.org/2000/01/rdf-schema#"
                  :ex "http://example.com/"}]
    (testing "Simple triples"
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])
             "ex:8\n  ex:lessThan ex:9 .")))
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)] [(iri :rdf "value") (lit 8)])
             "ex:8\n  ex:lessThan ex:9 ;\n  rdf:value 8 .")))
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)]) "  ")
             "  ex:8\n    ex:lessThan ex:9 ."))
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)] [(iri :rdf "value") (lit 8)]) "  ")
             "  ex:8\n    ex:lessThan ex:9 ;\n    rdf:value 8 .")))
    (testing "Triples with blank nodes as objects"
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (_po [(iri :ex "lessThan") (iri :ex 10)])]))
             "ex:8\n  ex:lessThan [\n    ex:lessThan ex:10\n  ] ."))
      (is (= (write-triples prefixes (spo (iri :ex 8) [(iri :ex "lessThan") (_po [(iri :ex "lessThan") (iri :ex 10)])]) "  ")
             "  ex:8\n    ex:lessThan [\n      ex:lessThan ex:10\n    ] .")))
    (testing "Triples generated through chaining"
      (is (= (write-triples prefixes (spo (spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)]) [(iri :rdf "value") (lit 8)])
             "ex:8\n  ex:lessThan ex:9 ;\n  rdf:value 8 ."))))))

(deftest test-write-triples-list
  (let [prefixes {:ex "http://example.com/"}]
    (is (= (write-triples-list prefixes [(spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])])
           "ex:8\n  ex:lessThan ex:9 ."))
    (is (= (write-triples-list prefixes [(spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])
                                         (spo (iri :ex 7) [(iri :ex "lessThan") (iri :ex 8)])])
           "ex:8\n  ex:lessThan ex:9 .\n\nex:7\n  ex:lessThan ex:8 ."))
    (is (= (write-triples-list prefixes [(spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])
                                         (spo (iri :ex 7) [(iri :ex "lessThan") (iri :ex 8)])] "  ")
           "  ex:8\n    ex:lessThan ex:9 .\n\n  ex:7\n    ex:lessThan ex:8 ."))))

(deftest test-write-prefixes
  (is (= (write-prefixes {:ex "http://example.com/"})
         "@prefix ex: <http://example.com/> ."))
  (is (= (write-prefixes (array-map :ex "http://example.com/"
                                    :rdfs "http://www.w3.org/2000/01/rdf-schema#" ))
         "@prefix ex: <http://example.com/> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .")))

(deftest test-write-ttl
  (let [prefixes {:ex "http://example.com/"}]
    (is (= (write-ttl prefixes [(spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])
                                (spo (iri :ex 7) [(iri :ex "lessThan") (iri :ex 8)])])
           "@prefix ex: <http://example.com/> .\n\nex:8\n  ex:lessThan ex:9 .\n\nex:7\n  ex:lessThan ex:8 ."))))

(deftest test-write-graph
  (let [prefixes {:ex "http://example.com/"}]
    (is (= (write-graph prefixes (graph (iri :ex "n")
                                        [(spo (iri :ex 8) [(iri :ex "lessThan") (iri :ex 9)])
                                        (spo (iri :ex 7) [(iri :ex "lessThan") (iri :ex 8)])]))
           "ex:n {\n\n  ex:8\n    ex:lessThan ex:9 .\n\n  ex:7\n    ex:lessThan ex:8 .\n\n}"))))
