(ns org.drugis.importer.xml2sql_test
  (:require [clojure.test :refer :all]
            [org.drugis.importer.xml2sql :refer :all]
            [org.drugis.importer.xml2sql_mock :refer [mockInserter]]
            [riveted.core :as vtd]))

(deftest test-xpath-parent
  (testing "xpath can go back up the tree"
    (let [xml "<root attr=\"x\"><foobar/></root>"
          node (vtd/at (vtd/navigator xml) "/root/foobar")]
          (is (= "root" (vtd/tag (vtd/at node "..")))))))

(deftest test-xpath-attr
  (testing "xpath can select attributes"
    (let [xml (vtd/navigator "<root attr=\"x\"/>")
          node (vtd/at xml "/root/@attr")]
      (is (= "attr" (vtd/tag node)))
      (is (= "x" (vtd/attr node (vtd/tag node)))))))

(deftest test-attrs
  (testing "attrs returns attribute-value map"
    (let [xml (vtd/navigator "<root x=\"5\" y=\"7\"/>")
          node (vtd/at xml "/root")]
      (is (= {"x" "5" "y" "7"} (attrs node))))))

(deftest test-get-column-value
  (testing "get-column-value should apply xpath and transform"
    (is (= {:foo "bar"}
           (apply-context (get-column-value
             (vtd/navigator "<root><foobar>bar</foobar></root>")
             :foo
             (xpath-text "/root/foobar")) [])))
    (is (= {:foo "baz"}
           (apply-context (get-column-value
             (vtd/navigator "<root><foobar foo=\"baz\">bar</foobar></root>")
             :foo
             (xpath-attr "/root/foobar" :foo)) []))))
  (testing "get-column-value generates context closure where sql-id required"
    (let [rval (:foo (get-column-value
                       (vtd/navigator "<root><foobar>bar</foobar></root>")
                       :foo
                       (sibling-ref :pitty #(vtd/text (vtd/at % "/root/foobar")))))
          context {:pitty {"bar" [8 {}] "baz" [10 {}]}}]
      (is (= 8 (rval [[nil nil nil context]]))))))

(deftest test-get-column-values
  (testing "get-column-values maps all columns"
    (is (= {:foo "bar" :fu "baz"}
           (apply-context (get-column-values
             (vtd/navigator "<root><foobar foo=\"baz\">bar</foobar></root>")
             {:foo (xpath-text "/root/foobar")
              :fu  (xpath-attr "/root/foobar" :foo)}) [])))))

;(deftest test-value
;  (testing "xpath-text works"
;    (is (= "bar" (((xpath-text "/foobar") (vtd/navigator "<foobar foo=\"baz\">bar</foobar>")) nil nil)))))

(deftest test-get-table
  (let [foobar-def {:xml-id (value vtd/text)
                    :sql-id :foo
                    :each "./foobar"
                    :table :foobar
                    :columns {:foo (value vtd/text)
                              :fu (value #(vtd/attr % :foo))}
                    :dependent-tables []}
        ctx-map-row (fn [row-tpl parent context] (assoc row-tpl :columns (apply-context (:columns row-tpl) [[nil nil parent context]])))
        nil-map #(ctx-map-row % nil nil)
        ctx-map-rows (fn [rows-tpl parent context] (into {} (map (fn [[k v]] {k (ctx-map-row v parent context)}) rows-tpl)))
        nil-map-rows #(ctx-map-rows % nil nil)
        ctx-map-table (fn [table-tpl parent context] (assoc table-tpl :rows (ctx-map-rows (:rows table-tpl) parent context)))
        nil-map-table #(ctx-map-table % nil nil)
        ctx-map-tables (fn [tables-tpl parent context] (into [] (map #(ctx-map-table % parent context) tables-tpl)))
        nil-map-tables #(ctx-map-tables % nil nil)]
    (testing "get-table-row returns xml-id and columns"
      (let [table-row (nil-map-rows (get-table-row
                                     (vtd/at (vtd/navigator "<foobar foo=\"baz\">bar</foobar>") "/foobar")
                                     foobar-def))]
        (is (= {"bar" {:columns {:foo "bar" :fu "baz"} :dependent-tables []}} table-row))))
    (testing "get-table-row generates random xml-id when missing"
      (let [table-def (dissoc foobar-def :xml-id)
            table-row-fn (fn [] (nil-map-rows (get-table-row
                                     (vtd/at (vtd/navigator "<foobar foo=\"baz\">bar</foobar>") "/foobar")
                                     table-def)))
            table-row1 (table-row-fn)
            table-row2 (table-row-fn)
            xml-id1 (first (keys table-row1))
            xml-id2 (first (keys table-row2))]
        (is (not (nil? xml-id1)))
        (is (not (= xml-id1 xml-id2)))
        (is (= {:columns {:foo "bar" :fu "baz"} :dependent-tables []} (get table-row1 xml-id1)))))
    (testing "get-table returns xml-id and columns"
      (let [table (nil-map-table (get-table
                                   (vtd/at (vtd/navigator "<root><foobar foo=\"baz\">bar</foobar><foobar foo=\"qux\">qox</foobar></root>") "/root")
                                   foobar-def))]
        (is (= {"bar" {:columns {:foo "bar" :fu "baz"} :dependent-tables []}
                "qox" {:columns {:foo "qox" :fu "qux"} :dependent-tables []}} (:rows table)))))
    (testing "get-table-row recurses for dependent-tables"
      (let [table-def {:xml-id (value #(vtd/attr % :id))
                       :sql-id :id
                       :each "/root/container"
                       :table :container
                       :columns {:id (value #(vtd/attr % :id))}
                       :dependent-tables [foobar-def]}
            table-tpl (get-table-row
                        (vtd/at (vtd/navigator "<root><container id=\"3\"><foobar foo=\"baz\">bar</foobar></container></root>") "/root/container")
                        table-def)
            table (assoc-in (nil-map-rows table-tpl) ["3" :dependent-tables] (nil-map-tables (get-in table-tpl ["3" :dependent-tables]))) ]
        (is (= {"3" {:columns {:id "3"}
                     :dependent-tables [{:table :foobar
                                         :sql-id :foo
                                         :rows {"bar" {:columns {:foo "bar" :fu "baz"}
                                                       :dependent-tables []}}}]}}
               table))))
    (testing "get-table-row recurses with parent keys"
      (let [nested-def {:xml-id (value #(vtd/attr % :name))
                        :sql-id :id
                        :each "./nested"
                        :table :nested
                        :columns {:parent (parent-ref)
                                  :name (value #(vtd/attr % :name))}
                        :dependent-tables []}
            container-def {:xml-id (value #(vtd/attr % :name))
                           :sql-id :id
                           :each "/root/container"
                           :table :container
                           :columns {:name (value #(vtd/attr % :name))}
                           :dependent-tables [nested-def]}
            table-tpl (get-table-row
                        (vtd/at (vtd/navigator "<root><container name=\"foo\"><nested name=\"bar\" /></container></root>") "/root/container")
                        container-def)
            table (assoc-in (nil-map-rows table-tpl)
                            ["foo" :dependent-tables]
                            (ctx-map-tables (get-in table-tpl ["foo" :dependent-tables]) 12 {})) ]
        (is (= {"foo" {:columns {:name "foo"}
                       :dependent-tables [{:table :nested
                                           :sql-id :id
                                           :rows {"bar" {:columns {:parent 12 :name "bar"}
                                                         :dependent-tables []}}}]}}
               table))))
    (testing "get-table-row does not allow :collapse with :dependent-tables"
      (is (thrown? IllegalArgumentException (get-table-row "<root />" 
                                                           {:xml-id (value vtd/tag)
                                                            :sql-id :id
                                                            :each "/root"
                                                            :table :root
                                                            :columns {}
                                                            :collapse [{}]
                                                            :dependent-tables [{}]}))))
    (testing "get-table-row returns multiple rows for :collapse"
      (let [table-def {:xml-id (value vtd/tag)
                       :sql-id :id
                       :each "/root/*"
                       :table :root
                       :columns {:tag (value vtd/tag)}
                       :collapse [{:xml-id (value vtd/tag)
                                   :each "@*"
                                   :columns {:attr (value vtd/tag)
                                             :value (value vtd/text)}}]}
            node (vtd/at (vtd/navigator"<root><foobar foo=\"baz\" bar=\"qux\"/></root>") "/root/*")
            table (nil-map-rows (get-table-row node table-def))]
        (is (= {["foobar" "foo"] {:columns {:tag "foobar" :attr "foo" :value "baz"}
                                  :dependent-tables []}
                ["foobar" "bar"] {:columns {:tag "foobar" :attr "bar" :value "qux"}
                                  :dependent-tables []}}
               table))))
    (testing "get-table-row concatenates multiple :collapse definitions"
      (let [table-def {:xml-id (value vtd/tag)
                       :sql-id :id
                       :each "/root/*"
                       :table :root
                       :columns {:tag (value vtd/tag)}
                       :collapse [{:xml-id (value vtd/tag)
                                   :each "@*"
                                   :columns {:attr (value vtd/tag)
                                             :value (value vtd/text)}}
                                  {:xml-id (value vtd/tag)
                                   :each "./*"
                                   :columns {:attr (value vtd/tag)
                                             :value (value vtd/text)}}]}
            node (vtd/at (vtd/navigator "<root><foobar foo=\"baz\" bar=\"qux\"><x>3</x></foobar></root>") "/root/*")
            table (nil-map-rows (get-table-row node table-def))]
        (is (= {["foobar" "foo"] {:columns {:tag "foobar" :attr "foo" :value "baz"}
                                  :dependent-tables []}
                ["foobar" "bar"] {:columns {:tag "foobar" :attr "bar" :value "qux"}
                                  :dependent-tables []} 
                ["foobar" "x"] {:columns {:tag "foobar" :attr "x" :value "3"}
                                  :dependent-tables []}}
               table))))))

(deftest test-insert-table
    (testing "insert-row returns xml->sql id map"
      (let [columns {:name "foo"}
            expected {[:foobar {:name "foo"}] {:id 1}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-row inserter :foobar :id "foo" columns) ["foo" 1]))
        (verify)))
    (testing "A simple insert-table returns xml->sql id map"
      (let [table {:table :foobar
                   :sql-id :id
                   :rows {"foo" {:columns {:name (fn [_] "foo")} :dependent-tables []}
                          "bar" {:columns {:name (fn [_] "bar")} :dependent-tables []}}}
            expected {[:foobar {:name "foo"}] {:id 1}
                      [:foobar {:name "bar"}] {:id 2}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table) {:foobar {"foo" [1 {}] "bar" [2 {}]}}))
        (verify)))
    (testing "insert-table passes parent id to dependent-tables"
      (let [nested-foo {:table :baz
                        :sql-id :id
                        :rows {"baz" {:columns {:parent (parent-finder) :name (fn [_] "baz")}}}}
            nested-bar {:table :baz
                        :sql-id :id
                        :rows {"baz" {:columns {:parent (parent-finder) :name (fn [_] "baz")}}
                               "qux" {:columns {:parent (parent-finder) :name (fn [_] "qux")}}}}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foo" {:columns {:name (fn [_] "foo")} :dependent-tables [nested-foo]}
                          "bar" {:columns {:name (fn [_] "bar")} :dependent-tables [nested-bar]}}}
            expected {[:foobar {:name "foo"}] {:id 1}
                      [:foobar {:name "bar"}] {:id 2}
                      [:baz {:parent 1 :name "baz"}] {:id 3}
                      [:baz {:parent 2 :name "baz"}] {:id 4}
                      [:baz {:parent 2 :name "qux"}] {:id 5}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table)
               {:foobar {"foo" [1 {:baz {"baz" [3 {}]}}]
                         "bar" [2 {:baz {"baz" [4 {}]
                                         "qux" [5 {}]}}]}}))
        (verify)))
    (testing "insert-table passes sibling ids to dependent-tables"
      (let [nested-foo {:table :foo
                        :sql-id :id
                        :rows {"foo" {:columns {:parent (parent-finder) :name (fn [_] "foo")}}}}
            nested-bar {:table :bar
                        :sql-id :id
                        :rows {"bar" {:columns {:parent (parent-finder)
                                                :foo (sibling-finder :foo "foo")
                                                :name (fn [_] "bar")}}
                               "qux" {:columns {:parent (parent-finder)
                                                :foo (sibling-finder :foo "foo")
                                                :name (fn [_] "qux")}}}}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foobar" {:columns {:name (fn [_] "foobar")} :dependent-tables [nested-foo nested-bar]}}}
            expected {[:foobar {:name "foobar"}] {:id 1}
                      [:foo {:parent 1 :name "foo"}] {:id 2}
                      [:bar {:parent 1 :foo 2 :name "bar" }] {:id 3}
                      [:bar {:parent 1 :foo 2 :name "qux" }] {:id 4}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table)
               {:foobar {"foobar" [1 {:foo {"foo" [2 {}]}
                                      :bar {"bar" [3 {}]
                                            "qux" [4 {}]}}]}}))
        (verify)))
    (testing "insert-table keeps context from upwards on the stack"
      (let [nested-qux {:table :qux
                        :sql-id :id
                        :rows {"qux" {:columns {:name (fn [_] "qux")}}
                               "qox" {:columns {:name (fn [_] "qox")}}} }
            nested-foo {:table :foo
                        :sql-id :id
                        :rows {"foo" {:columns {:grand-parent (parent-finder :foobar)
                                                :parent (parent-finder)
                                                :qux (fn [contexts] (first (get (:qux (nth (second contexts) 3)) "qox")))
                                                :name (fn [_] "foo")}}}}
            nested-bar {:table :bar
                        :sql-id :id
                        :rows {"bar" {:columns {:parent (parent-finder)
                                                :name (fn [_] "bar")}
                                      :dependent-tables [nested-foo]}}}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foobar" {:columns {:name (fn [_] "foobar")}
                                    :dependent-tables [nested-qux nested-bar]}}}
            expected {[:foobar {:name "foobar"}] {:id 8}
                      [:qux {:name "qux"}] {:id 5}
                      [:qux {:name "qox"}] {:id 6}
                      [:bar {:parent 8 :name "bar"}] {:id 2}
                      [:foo {:grand-parent 8 :parent 2 :qux 6 :name "foo"}] {:id 3}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table)
               {:foobar
                {"foobar"
                 [8 {:bar {"bar" [2 {:foo {"foo" [3 {}]}}]}
                     :qux {"qux" [5 {}] "qox" [6 {}]}}]}}))
        (verify)))
    (testing "insert-table calls post-insert"
      (let [called (atom false)
            table {:table :foobar
                   :sql-id :id
                   :rows {"foo" {:columns {:name (fn [_] "foo")} :dependent-tables []}}
                   :post-insert (fn [_ inserted _]  (reset! called true) (is (= inserted {"foo" [1 {}]})))}
            expected {[:foobar {:name "foo"}] {:id 1}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table) {:foobar {"foo" [1 {}]}}))
        (is @called)))
    (testing "insert-table passes the context to post-insert"
      (let [nested-foo {:table :foo
                        :sql-id :id
                        :rows {"foo" {:columns {:name (fn [_] "foo")}}}
                        :post-insert (fn [_ inserted contexts] 
                                       (is (= inserted {"foo" [2 {}]}))
                                       (is (= contexts [[:foobar "foobar" 1 {}]])))}
            nested-bar {:table :bar
                        :sql-id :id
                        :rows {"bar" {:columns {:name (fn [_] "bar")}}}
                        :post-insert (fn [_ inserted contexts] 
                                       (is (= inserted {"bar" [3 {}]}))
                                       (is (= contexts [[:foobar "foobar" 1 {:foo {"foo" [2 {}]}}]])))}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foobar" {:columns {:name (fn [_] "foobar")} :dependent-tables [nested-foo nested-bar]}}
                   :post-insert (fn [row-data inserted contexts] 
                                  (is (= row-data {:name "foobar"}))
                                  (is (= inserted {"foobar" [1 {:foo {"foo" [2 {}]}
                                                                :bar {"bar" [3 {}]}}]}))
                                  (is (= contexts [])))}
            expected {[:foobar {:name "foobar"}] {:id 1}
                      [:foo {:name "foo"}] {:id 2}
                      [:bar {:name "bar" }] {:id 3}}
            [inserter verify] (mockInserter expected)]
        (is (= (insert-table inserter table)
               {:foobar {"foobar" [1 {:foo {"foo" [2 {}]}
                                      :bar {"bar" [3 {}]}}]}}))
        (verify))))
