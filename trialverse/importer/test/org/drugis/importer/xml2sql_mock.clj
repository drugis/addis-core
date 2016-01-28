(ns org.drugis.importer.xml2sql_mock
  (:require [clojure.test :as test]))

(defprotocol IMockInserter
  (insert-record [this table columns])
  (insert [this table rows])
  (verify [this]))

(defrecord MockInserter
  [remaining]
  IMockInserter
  (insert-record
    [this table-name columns]
    (let [rval (get @(get this :remaining) [table-name columns])]
      ;(println "INSERTING" columns "IN" table-name "=>" rval)
      (test/is (not (nil? rval)))
      (swap! (get this :remaining) dissoc [table-name columns])
      rval))
  (insert
    [this table-name rows]
    (map (partial insert-record this table-name) rows))
  (verify [this] 
    (test/is (empty? @(get this :remaining)))))

(defn mockInserter
  [expected]
  (let [mock (MockInserter. (atom expected))]
    [(fn [table rows] (insert mock table rows)) (fn [] (verify mock))]))
