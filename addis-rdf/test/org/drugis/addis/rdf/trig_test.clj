(ns org.drugis.addis.rdf.trig-test
  (:use clojure.test)
  (:use org.drugis.addis.rdf.trig))

(deftest test-ttl-str 
  (is (= (ttl-str (rdf-uri "http://example.com/8")) "<http://example.com/8>") "Handles plain URIs")
  (is (= (ttl-str (rdf-uri :rdfs "comment")) "rdfs:comment") "Handles QName URIs")
  (is (thrown? IllegalArgumentException (ttl-str "testing...")) "Literals not allowed")
  (is (thrown? IllegalArgumentException (ttl-str 3)) "Literals not allowed")
  (is (thrown? IllegalArgumentException (ttl-str (rdf-coll [(rdf-uri "http://example.com/8")]))) "Collections not allowed")
  (is (thrown? IllegalArgumentException (ttl-str (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"]]))) "Blank node property lists not allowed"))

(deftest test-ttl-object-str
  (let [prefixes {:xsd "http://www.w3.org/2001/XMLSchema#" :rdfs "http://www.w3.org/2000/01/rdf-schema#" :ex "http://example.com/"}]
    (testing "References and literals"
      (is (= (ttl-object-str prefixes (rdf-uri "http://example.com/8")) "<http://example.com/8>") "Handles plain URIs")
      (is (= (ttl-object-str prefixes (rdf-uri :rdfs "comment")) "rdfs:comment") "Handles QName URIs")
      (is (= (ttl-object-str prefixes "testing...") "\"testing...\"") "Handles string literals")
      (is (= (ttl-object-str prefixes "\"testing\"") "\"\\\"testing\\\"\"") "Escapes quotes in string literals")
      (is (= (ttl-object-str prefixes "đ") "\"\\u0111\"") "Escapes unicode in string literals")
      (is (= (ttl-object-str prefixes 3) "3") "Handles integer literals")
      (is (= (ttl-object-str prefixes 3.0) (format "%e" 3.0)) "Handles double literals")
      (is (= (ttl-object-str prefixes false) "false") "Handles boolean literals"))
    (testing "Collections"
      (is (= (ttl-object-str prefixes (rdf-coll [(rdf-uri "http://example.com/8")]))
             "(\n  <http://example.com/8>\n)")
          "Collection with single member (unindented)")
      (is (= (ttl-object-str prefixes (rdf-coll [(rdf-uri :ex 8) (rdf-uri :ex 9)]))
             "(\n  ex:8\n  ex:9\n)")
          "Collection with multiple members (unindented)")
      (is (= (ttl-object-str prefixes (rdf-coll [(rdf-uri "http://example.com/8")]) "  ")
             "(\n    <http://example.com/8>\n  )")
          "Collection with single member (indented)")
      (is (= (ttl-object-str prefixes (rdf-coll [(rdf-uri :ex 8) (rdf-uri :ex 9)]) "  ")
             "(\n    ex:8\n    ex:9\n  )")
          "Collection with multiple members (indented)"))
    (testing "Blank node property list"
      (is (= (ttl-object-str prefixes (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"]]))
             "[\n  rdfs:comment \"no comment\"\n]")
          "Blank node with single property (unindentend)")
      (is (= (ttl-object-str prefixes (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"] [(rdf-uri :rdfs "label") "label"]]))
             "[\n  rdfs:comment \"no comment\" ;\n  rdfs:label \"label\"\n]")
          "Blank node with multiple properties (unindented)")
      (is (= (ttl-object-str prefixes (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"]]) "  ")
             "[\n    rdfs:comment \"no comment\"\n  ]")
          "Blank node with single property (indentend)")
      (is (= (ttl-object-str prefixes (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"] [(rdf-uri :rdfs "label") "label"]]) "  ")
             "[\n    rdfs:comment \"no comment\" ;\n    rdfs:label \"label\"\n  ]")
          "Blank node with multiple properties (indented)"))
    (testing "Collections of blank nodes"
      (is (= (ttl-object-str prefixes (rdf-coll [(rdf-blank [[(rdf-uri :rdfs "comment") "no comment"]])]))
             "(\n  [\n    rdfs:comment \"no comment\"\n  ]\n)")))
    (testing "Nested blank nodes"
      (is (= (ttl-object-str prefixes (rdf-blank [[(rdf-uri :ex "onceSaid") (rdf-blank [[(rdf-uri :rdfs "comment") "no comment"]])]]))
             "[\n  ex:onceSaid [\n    rdfs:comment \"no comment\"\n  ]\n]")))
    ; TODO: test more cases - these are the ones used by "core"
    ))

(deftest test-write-triples
  (let [prefixes {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                  :rdfs "http://www.w3.org/2000/01/rdf-schema#"
                  :ex "http://example.com/"}]
    (testing "Simple triples"
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan") (rdf-uri :ex 9)]]])
             "ex:8\n  ex:lessThan ex:9 ."))
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan") (rdf-uri :ex 9)] [(rdf-uri :rdf "value") 8]]])
             "ex:8\n  ex:lessThan ex:9 ;\n  rdf:value 8 ."))
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan") (rdf-uri :ex 9)]]] "  ")
             "  ex:8\n    ex:lessThan ex:9 ."))
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan") (rdf-uri :ex 9)] [(rdf-uri :rdf "value") 8]]] "  ")
             "  ex:8\n    ex:lessThan ex:9 ;\n    rdf:value 8 .")))
    (testing "Triples with blank nodes as objects"
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan")
                                                        (rdf-blank [[(rdf-uri :ex "lessThan") (rdf-uri :ex 10)]])]]])
             "ex:8\n  ex:lessThan [\n    ex:lessThan ex:10\n  ] ."))
      (is (= (write-triples prefixes [(rdf-uri :ex 8) [[(rdf-uri :ex "lessThan")
                                                        (rdf-blank [[(rdf-uri :ex "lessThan") (rdf-uri :ex 10)]])]]] "  ")
             "  ex:8\n    ex:lessThan [\n      ex:lessThan ex:10\n    ] .")))
  ))
