(ns org.drugis.importer.xml2sql_test2
  (:require [clojure.test :refer :all]
            [org.drugis.importer.xml2sql_mock :refer [mockInserter]]
            [org.drugis.importer.xml2sql :as x2s]
            [riveted.core :as vtd]))

(deftest integration-test
  (testing "insert of one table with one row and one column"
    (let [table-def {:xml-id (x2s/value vtd/text)
                     :sql-id :id
                     :table :root
                     :each "/root"
                     :columns {:text (x2s/value vtd/text)}}
          xml (vtd/navigator "<root>foo</root>")
          expected {[:root {:text "foo"}] {:id 8}}
          [inserter verify] (mockInserter expected)]
      (= {:root {"foo" [8 {}]}} (x2s/xml->sql xml table-def inserter))
      (verify)))
  (testing "xml-id is randomly generated when missing"
    (let [table-def {:sql-id :id
                     :table :root
                     :each "/root"
                     :columns {:text (x2s/value vtd/text)}}
          xml (vtd/navigator "<root>foo</root>")
          expected {[:root {:text "foo"}] {:id 8}}
          [inserter1 verify1] (mockInserter expected)
          [k1 v1] (first (:root (x2s/xml->sql xml table-def inserter1)))
          [inserter2 verify2] (mockInserter expected) 
          [k2 v2] (first (:root (x2s/xml->sql xml table-def inserter2)))]
      (verify1)
      (verify2)
      (is (= v1 [8 {}]))
      (is (= v2 [8 {}]))
      (is (not (= k1 k2)))))
  (testing "insert of one table with multiple rows and columns"
    (let [table-def {:xml-id (x2s/value vtd/text)
                     :sql-id :id
                     :table :root
                     :each "/root/el"
                     :columns {:text (x2s/value vtd/text)
                               :attr (x2s/value #(vtd/attr % :attr))}}
          xml (vtd/navigator "<root><el attr=\"bar\">foo</el><el attr=\"qox\">qux</el></root>")
          expected {[:root {:text "foo" :attr "bar"}] {:id 7}
                    [:root {:text "qux" :attr "qox"}] {:id 3}}
          [inserter verify] (mockInserter expected)]
      (= {:root {"foo" [7 {}] "qux" [3 {}]}} (x2s/xml->sql xml table-def inserter))
      (verify)))
  (testing "insert of dependent-tables"
    (let [nested-def {:xml-id (x2s/value #(vtd/attr % :name))
                      :sql-id :id
                      :table :nested
                      :each "./nested"
                      :columns {:name (x2s/value #(vtd/attr % :name))}}
          container-def {:xml-id (x2s/value #(vtd/attr % :name))
                         :sql-id :id
                         :table :container
                         :each "/root/container"
                         :columns {:name (x2s/value #(vtd/attr % :name))}
                         :dependent-tables [nested-def]}
          xml (vtd/navigator "<root><container name=\"x\"><nested name=\"foo\"/><nested name=\"bar\"/></container><container name=\"y\"/></root>")
          expected {[:container {:name "x"}] {:id 1}
                    [:container {:name "y"}] {:id 2}
                    [:nested {:name "foo"}] {:id 8}
                    [:nested {:name "bar"}] {:id 9}}
          [inserter verify] (mockInserter expected)]
      (= {:container {"x" [1 {:nested {"foo" [8 {}] "bar" [9 {}]}}]
                      "y" [2 {:nested {}}]}} (x2s/xml->sql xml container-def inserter))
      (verify)))
  (testing "insert of nested dependent-tables"
    (let [deep-def {:xml-id (x2s/value #(vtd/attr % :name))
                    :sql-id :id
                    :table :deep
                    :each "./deep"
                    :columns {:name (x2s/value #(vtd/attr % :name))}}
          nested-def {:xml-id (x2s/value #(vtd/attr % :name))
                      :sql-id :id
                      :table :nested
                      :each "./nested"
                      :columns {:name (x2s/value #(vtd/attr % :name))}
                      :dependent-tables [deep-def]}
          container-def {:xml-id (x2s/value #(vtd/attr % :name))
                         :sql-id :id
                         :table :container
                         :each "/root/container"
                         :columns {:name (x2s/value #(vtd/attr % :name))}
                         :dependent-tables [nested-def]}
          xml (vtd/navigator "<root><container name=\"x\"><nested name=\"foo\"><deep name=\"qux\"/></nested></container></root>")
          expected {[:container {:name "x"}] {:id 1}
                    [:nested {:name "foo"}] {:id 8}
                    [:deep {:name "qux"}] {:id 5}}
          [inserter verify] (mockInserter expected)]
      (= {:container {"x" [1 {:nested {"foo" [8 {:deep {"qux" [5 {}]}}]}}]}}
         (x2s/xml->sql xml container-def inserter))
      (verify)))
  (testing "insert of dependent-tables resolves parent-ref"
    (let [deep-def {:xml-id (x2s/value #(vtd/attr % :name))
                    :sql-id :id
                    :table :deep
                    :each "./deep"
                    :columns {:grand-parent (x2s/parent-ref :container)
                              :name (x2s/value #(vtd/attr % :name))}}
          nested-def {:xml-id (x2s/value #(vtd/attr % :name))
                      :sql-id :id
                      :table :nested
                      :each "./nested"
                      :columns {:parent (x2s/parent-ref)
                                :name (x2s/value #(vtd/attr % :name))}
                      :dependent-tables [deep-def]}
          container-def {:xml-id (x2s/value #(vtd/attr % :name))
                         :sql-id :id
                         :table :container
                         :each "/root/container"
                         :columns {:name (x2s/value #(vtd/attr % :name))}
                         :dependent-tables [nested-def]}
          xml (vtd/navigator "<root><container name=\"x\"><nested name=\"foo\"><deep name=\"qux\"/></nested></container><container name=\"y\"><nested name=\"bar\"/></container></root>")
          expected {[:container {:name "x"}] {:id 1}
                    [:container {:name "y"}] {:id 2}
                    [:nested {:parent 1 :name "foo"}] {:id 8}
                    [:nested {:parent 2 :name "bar"}] {:id 9}
                    [:deep {:grand-parent 1 :name "qux"}] {:id 3}}
          [inserter verify] (mockInserter expected)]
      (= {:container {"x" [1 {:nested {"foo" [8 {:deep {"qux" [3 {}]}}]}}]
                      "y" [2 {:nested {"bar" [9 {}]}}]}}
         (x2s/xml->sql xml container-def inserter))
      (verify)))
(testing "insert of dependent-tables resolves sibling-ref"
  (let [oldest-def {:xml-id (x2s/value #(vtd/attr % :name))
                    :sql-id :id
                    :table :oldest
                    :each "./oldest"
                    :columns {:parent (x2s/parent-ref)
                              :name (x2s/value #(vtd/attr % :name))}}
        older-def {:xml-id (x2s/value #(vtd/attr % :name))
                   :sql-id :id
                   :table :older
                   :each "./older"
                   :columns {:name (x2s/value #(vtd/attr % :name))}}
        young-def {:xml-id (x2s/value #(vtd/attr % :name))
                   :sql-id :id
                   :table :young
                   :each "./young"
                   :columns {:name (x2s/value #(vtd/attr % :name))
                             :oldest (x2s/sibling-ref :oldest #(vtd/attr % :oldest))}}
        container-def {:xml-id (x2s/value #(vtd/attr % :name))
                       :sql-id :id
                       :table :container
                       :each "/root/container"
                       :columns {:name (x2s/value #(vtd/attr % :name))}
                       :dependent-tables [oldest-def older-def young-def]}
        xml (vtd/navigator "<root>
                           <container name=\"x\">
                           <oldest name=\"foo\"/>
                           <oldest name=\"bar\"/>
                           <older name=\"baz\" />
                           <young name=\"qux\" oldest=\"bar\" />
                           </container>
                           <container name=\"y\">
                           <oldest name=\"bar\"/>
                           <young name=\"qox\" oldest=\"bar\" />
                           </container>
                           </root>")
        expected {[:container {:name "x"}] {:id 1}
                  [:oldest {:parent 1 :name "foo"}] {:id 3}
                  [:oldest {:parent 1 :name "bar"}] {:id 4}
                  [:older {:name "baz"}] {:id 5}
                  [:young {:name "qux" :oldest 4}] {:id 6}
                  [:container {:name "y"}] {:id 2}
                  [:oldest {:parent 2 :name "bar"}] {:id 7}
                  [:young {:name "qox" :oldest 7}] {:id 8}}
        [inserter verify] (mockInserter expected)]
    (= {:container {"x" [1 {:oldest {"foo" [3 {}] "bar" [4 {}]}
                            :older {"baz" [5 {}]}
                            :young {"qux" [6 {}]}}]
                    "y" [2 {:oldest {"bar" [7 {}]}
                            :older {}
                            :young {"qox" [8 {}]}}]}}
       (x2s/xml->sql xml container-def inserter))
    (verify)))
(testing "sibling-ref is resolved further up the tree"
  (let [entity-def {:xml-id (x2s/value #(vtd/attr % :name))
                    :sql-id :id
                    :table :entity
                    :each "./entity"
                    :columns {:name (x2s/value #(vtd/attr % :name))}}
        ref-def {:xml-id (x2s/value #(vtd/attr % :name))
                 :sql-id :id
                 :table :ref
                 :each "./ref"
                 :columns {:name (x2s/value #(vtd/attr % :name))
                           :entity (x2s/sibling-ref :entity #(vtd/attr % :entity))}}
        container-def {:xml-id (x2s/value #(vtd/attr % :name))
                       :sql-id :id
                       :table :container
                       :each "./container"
                       :columns {:name (x2s/value #(vtd/attr % :name))}
                       :dependent-tables [ref-def]}
        root-def {:xml-id (x2s/value :root-node)
                  :sql-id :id
                  :table :root
                  :each "/root"
                  :columns {:name (x2s/value #(vtd/attr % :name))}
                  :dependent-tables [entity-def container-def]}
        xml (vtd/navigator "<root name=\"root\">
                           <entity name=\"foo\"/>
                           <entity name=\"bar\"/>
                           <container name=\"x\">
                           <ref name=\"qux\" entity=\"bar\" />
                           </container>
                           </root>")
        expected {[:root {:name "root"}] {:id 0}
                  [:container {:name "x"}] {:id 1}
                  [:entity {:name "foo"}] {:id 2}
                  [:entity {:name "bar"}] {:id 3}
                  [:ref {:name "qux" :entity 3}] {:id 4}}
        [inserter verify] (mockInserter expected)]
    (= {:root {:root-node [0 {:entity {"foo" [2 {}] "bar" [3 {}]}
                              :container {"x" [1 {:ref {"qux" [4 {}]}}]}}]}}
       (x2s/xml->sql xml root-def inserter))
    (verify)))
(testing ":collapse with :dependent-tables throws an exception"
  (let [[inserter verify] (mockInserter {})]
    (is (thrown? IllegalArgumentException
                 (x2s/xml->sql (vtd/navigator "<root />")
                               {:xml-id (x2s/value vtd/tag)
                                :sql-id :id
                                :each "/root"
                                :table :root
                                :columns {}
                                :collapse [{}]
                                :dependent-tables [{}]}
                               inserter)))))
(testing "a single :collapse definition"
  (let [table-def {:xml-id (x2s/value vtd/tag)
                   :sql-id :id
                   :each "/root/*"
                   :table :root
                   :columns {:tag (x2s/value vtd/tag)}
                   :collapse [{:xml-id (x2s/value vtd/tag)
                               :each "@*"
                               :columns {:attr (x2s/value vtd/tag)
                                         :value (x2s/value vtd/text)}}]}
        xml (vtd/navigator "<root><foobar foo=\"baz\" bar=\"qux\"/><foobaz bar=\"qox\"/></root>")
        expected {[:root {:tag "foobar" :attr "foo" :value "baz"}] {:id 3}
                  [:root {:tag "foobar" :attr "bar" :value "qux"}] {:id 4}
                  [:root {:tag "foobaz" :attr "bar" :value "qox"}] {:id 5}}
        [inserter verify] (mockInserter expected)]
    (is (= {:root {["foobar" "foo"] [3 {}]
                   ["foobar" "bar"] [4 {}]
                   ["foobaz" "bar"] [5 {}]}}
           (x2s/xml->sql xml table-def inserter)))
    (verify)))
(testing "multiple :collapse definitions"
  (let [table-def {:xml-id (x2s/value vtd/tag)
                   :sql-id :id
                   :each "/root/*"
                   :table :root
                   :columns {:tag (x2s/value vtd/tag)}
                   :collapse [{:xml-id (x2s/value vtd/tag)
                               :each "@*"
                               :columns {:attr (x2s/value vtd/tag)
                                         :value (x2s/value vtd/text)}}
                              {:xml-id (x2s/value vtd/tag)
                               :each "./*"
                               :columns {:attr (x2s/value vtd/tag)
                                         :value (x2s/value vtd/text)}}
                              ]}
        xml (vtd/navigator "<root><foobar foo=\"baz\" bar=\"qux\"><x>3</x></foobar></root>")
        expected {[:root {:tag "foobar" :attr "foo" :value "baz"}] {:id 3}
                  [:root {:tag "foobar" :attr "bar" :value "qux"}] {:id 4}
                  [:root {:tag "foobar" :attr "x" :value "3"}] {:id 6}}
        [inserter verify] (mockInserter expected)]
    (is (= {:root {["foobar" "foo"] [3 {}]
                   ["foobar" "bar"] [4 {}]
                   ["foobar" "x"] [6 {}]}}
           (x2s/xml->sql xml table-def inserter)))
    (verify)))
)
