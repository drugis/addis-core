(ns org.drugis.importer.core_test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-xpath.core :refer [xml->doc $x $x:node?]]
            [org.drugis.importer.core :refer :all]))

(deftest test-get-column-value
  (testing "get-column-value should apply xpath and transform"
    (is (= {:foo "bar"}
           (apply-context (get-column-value
             "<root><foobar>bar</foobar></root>"
             :foo
             ["./root/foobar" :text]) nil)))
    (is (= {:foo "baz"}
           (apply-context (get-column-value
             "<root><foobar foo=\"baz\">bar</foobar></root>"
             :foo
             ["./root/foobar" #(get-in % [:attrs :foo])]) nil))))
  (testing "get-column-value generates context closure where sql-id required"
    (let [rval (:foo (get-column-value
                       "<root><foobar>bar</foobar></root>"
                       :foo
                       ["./root/foobar" :text :pitty]))
          context {:pitty {"bar" 8 "baz" 10}}]
      (is (= 8 (rval context)))))
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
        nil-map (fn [row-tpl] (assoc row-tpl :columns (apply-context (:columns row-tpl) nil)))
        nil-map-all (fn [rows-tpl] (into {} (map (fn [[k v]] {k (nil-map v)}) rows-tpl)))
        nil-map-table (fn [table-tpl] (assoc table-tpl :rows (nil-map-all (:rows table-tpl))))
        nil-map-tables (fn [tables-tpl] (into [] (map nil-map-table tables-tpl))) ]
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
               table))))))

(deftest test-xpath-parent
  (testing "xpath can go back up the tree"
    (let [xml "<root attr=\"x\"><foobar/></root>"
          node ($x? "/root/foobar" xml)]
          (is (= :root (:tag ($x? ".." node)))))))

