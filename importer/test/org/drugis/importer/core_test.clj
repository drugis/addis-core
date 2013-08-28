(ns org.drugis.importer.core_test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-xpath.core :refer [xml->doc $x $x:node?]]
            [org.drugis.importer.core :refer :all]))

(deftest test-xpath-parent
  (testing "xpath can go back up the tree"
    (let [xml "<root attr=\"x\"><foobar/></root>"
          node ($x? "/root/foobar" xml)]
          (is (= :root (:tag ($x? ".." node)))))))

(deftest test-get-column-value
  (testing "get-column-value should apply xpath and transform"
    (is (= {:foo "bar"}
           (apply-context (get-column-value
             "<root><foobar>bar</foobar></root>"
             :foo
             ["./root/foobar" :text]) nil nil)))
    (is (= {:foo "baz"}
           (apply-context (get-column-value
             "<root><foobar foo=\"baz\">bar</foobar></root>"
             :foo
             ["./root/foobar" #(get-in % [:attrs :foo])]) nil nil))))
  (testing "get-column-value generates context closure where sql-id required"
    (let [rval (:foo (get-column-value
                       "<root><foobar>bar</foobar></root>"
                       :foo
                       ["./root/foobar" :text :sibling :pitty]))
          context {:pitty {"bar" 8 "baz" 10}}]
      (is (= 8 (rval nil context)))))
  )

(deftest test-get-column-values
  (testing "get-column-values maps all columns"
    (is (= {:foo "bar" :fu "baz"}
           (apply-context (get-column-values
             "<root><foobar foo=\"baz\">bar</foobar></root>"
             {:foo["./root/foobar" :text]
              :fu ["./root/foobar"
                   #(get-in % [:attrs :foo])]}) nil)))))

(deftest test-get-xml-value
  (testing "get-xml-value works"
    (is (= "bar" (get-xml-value "<foobar foo=\"baz\">bar</foobar>" ["/foobar" :text])))))

(deftest test-get-table
  (let [foobar-def {:xml-id ["." :text]
                    :sql-id :foo
                    :each "./foobar"
                    :table :foobar
                    :columns {:foo ["." :text]
                              :fu ["." #(get-in % [:attrs :foo])]}
                    :dependent-tables []}
        ctx-map-row (fn [row-tpl parent context] (assoc row-tpl :columns (apply-context (:columns row-tpl) parent context)))
        nil-map #(ctx-map-row % nil nil)
        ctx-map-rows (fn [rows-tpl parent context] (into {} (map (fn [[k v]] {k (ctx-map-row v parent context)}) rows-tpl)))
        nil-map-all #(ctx-map-rows % nil nil)
        ctx-map-table (fn [table-tpl parent context] (assoc table-tpl :rows (ctx-map-rows (:rows table-tpl) parent context)))
        nil-map-table #(ctx-map-table % nil nil)
        ctx-map-tables (fn [tables-tpl parent context] (into [] (map #(ctx-map-table % parent context) tables-tpl)))
        nil-map-tables #(ctx-map-tables % nil nil)]
    (testing "get-table-row returns xml-id and columns"
      (let [table-row (nil-map-all (get-table-row
                                     ($x? "/foobar" "<foobar foo=\"baz\">bar</foobar>")
                                     foobar-def))]
        (is (= {"bar" {:columns {:foo "bar" :fu "baz"} :dependent-tables []}} table-row))))
    (testing "get-table returns xml-id and columns"
      (let [table (nil-map-table (get-table
                                   ($x? "/root" "<root><foobar foo=\"baz\">bar</foobar><foobar foo=\"qux\">qox</foobar></root>")
                                   foobar-def))]
        (is (= {"bar" {:columns {:foo "bar" :fu "baz"} :dependent-tables []}
                "qox" {:columns {:foo "qox" :fu "qux"} :dependent-tables []}} (:rows table)))))
    (testing "get-table-row recurses for dependent-tables"
      (let [table-def {:xml-id ["." #(get-in % [:attrs :id])]
                       :sql-id :id
                       :each "/root/container"
                       :table :container
                       :columns {:id ["." #(get-in % [:attrs :id])]}
                       :dependent-tables [foobar-def]}
            table-tpl (get-table-row
                        ($x? "/root/container" "<root><container id=\"3\"><foobar foo=\"baz\">bar</foobar></container></root>")
                        table-def)
            table (assoc-in (nil-map-all table-tpl) ["3" :dependent-tables] (nil-map-tables (get-in table-tpl ["3" :dependent-tables]))) ]
        (is (= {"3" {:columns {:id "3"}
                     :dependent-tables [{:table :foobar
                                         :sql-id :foo
                                         :rows {"bar" {:columns {:foo "bar" :fu "baz"}
                                                       :dependent-tables []}}}]}}
               table))))
    (testing "get-table-row recurses with parent keys"
      (let [nested-def {:xml-id ["." #(get-in % [:attrs :name])]
                        :sql-id :id
                        :each "./nested"
                        :table :nested
                        :columns {:parent [".." #(get-in % [:attrs :name]) :parent]
                                  :name ["." #(get-in % [:attrs :name])]}
                        :dependent-tables []}
            container-def {:xml-id ["." #(get-in % [:attrs :name])]
                           :sql-id :id
                           :each "/root/container"
                           :table :container
                           :columns {:name ["." #(get-in % [:attrs :name])]}
                           :dependent-tables [nested-def]}
            table-tpl (get-table-row
                        ($x? "/root/container" "<root><container name=\"foo\"><nested name=\"bar\" /></container></root>")
                        container-def)
            table (assoc-in (nil-map-all table-tpl)
                            ["foo" :dependent-tables]
                            (ctx-map-tables (get-in table-tpl ["foo" :dependent-tables]) 12 {})) ]
        (is (= {"foo" {:columns {:name "foo"}
                       :dependent-tables [{:table :nested
                                           :sql-id :id
                                           :rows {"bar" {:columns {:parent 12 :name "bar"}
                                                         :dependent-tables []}}}]}}
               table))))))

(deftest test-insert-table
  (let [inserter-fn (fn [expected]
                      (let [remaining (atom expected)]
                        (fn [table-name columns]
                          (if (nil? table-name)
                            (is (empty? @remaining))
                            (let [rval (get @remaining [table-name columns])]
                              (is (not (nil? rval)))
                              (swap! remaining dissoc [table-name columns])
                              rval)))))]
    (testing "insert-row returns xml->sql id map"
      (let [columns {:name "foo"}
            expected {[:foobar {:name "foo"}] {:id 1}}
            inserter (inserter-fn expected)]
        (is (= (insert-row inserter :foobar :id "foo" columns) ["foo" 1]))))
    (testing "A simple insert-table returns xml->sql id map"
      (let [table {:table :foobar
                   :sql-id :id
                   :rows {"foo" {:columns {:name (fn [_ _] "foo")} :dependent-tables []}
                          "bar" {:columns {:name (fn [_ _] "bar")} :dependent-tables []}}}
            expected {[:foobar {:name "foo"}] {:id 1}
                      [:foobar {:name "bar"}] {:id 2}}
            inserter (inserter-fn expected)]
        (is (= (insert-table inserter table) {:foobar {"foo" [1 {}] "bar" [2 {}]}}))))
    (testing "insert-table passes parent id to dependent-tables"
      (let [nested-foo {:table :baz
                        :sql-id :id
                        :rows {"baz" {:columns {:parent (fn [p _] p) :name (fn [_ _] "baz")}}}}
            nested-bar {:table :baz
                        :sql-id :id
                        :rows {"baz" {:columns {:parent (fn [p _] p) :name (fn [_ _] "baz")}}
                               "qux" {:columns {:parent (fn [p _] p) :name (fn [_ _] "qux")}}}}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foo" {:columns {:name (fn [_ _] "foo")} :dependent-tables [nested-foo]}
                          "bar" {:columns {:name (fn [_ _] "bar")} :dependent-tables [nested-bar]}}}
            expected {[:foobar {:name "foo"}] {:id 1}
                      [:foobar {:name "bar"}] {:id 2}
                      [:baz {:parent 1 :name "baz"}] {:id 3}
                      [:baz {:parent 2 :name "baz"}] {:id 4}
                      [:baz {:parent 2 :name "qux"}] {:id 5}}
            inserter (inserter-fn expected)]
        (is (= (insert-table inserter table)
               {:foobar {"foo" [1 {:baz {"baz" [3 {}]}}]
                         "bar" [2 {:baz {"baz" [4 {}]
                                         "qux" [5 {}]}}]}}))
        (inserter nil nil)
        ))
    (testing "insert-table passes sibling ids to dependent-tables"
      (let [nested-foo {:table :foo
                        :sql-id :id
                        :rows {"foo" {:columns {:parent (fn [p _] p) :name (fn [_ _] "foo")}}}}
            nested-bar {:table :bar
                        :sql-id :id
                        :rows {"bar" {:columns {:parent (fn [p _] p)
                                                :foo (fn [_ ctx] (first (get-in ctx [:foo "foo"])))
                                                :name (fn [_ _] "bar")}}
                               "qux" {:columns {:parent (fn [p _] p)
                                                :foo (fn [_ ctx] (first (get-in ctx [:foo "foo"])))
                                                :name (fn [_ _] "qux")}}}}
            table {:table :foobar
                   :sql-id :id
                   :rows {"foobar" {:columns {:name (fn [_ _] "foobar")} :dependent-tables [nested-foo nested-bar]}}}
            expected {[:foobar {:name "foobar"}] {:id 1}
                      [:foo {:parent 1 :name "foo"}] {:id 2}
                      [:bar {:parent 1 :foo 2 :name "bar" }] {:id 3}
                      [:bar {:parent 1 :foo 2 :name "qux" }] {:id 4}}
            inserter (inserter-fn expected)]
        (is (= (insert-table inserter table)
               {:foobar {"foobar" [1 {:foo {"foo" [2 {}]}
                                      :bar {"bar" [3 {}]
                                            "qux" [4 {}]}}]}}))
        (inserter nil nil)
        )))
  )

