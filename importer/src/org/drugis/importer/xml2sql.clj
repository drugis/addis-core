(ns org.drugis.importer.xml2sql
  (:require [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc :as jdbc]
            [riveted.core :as vtd]))

(defn attr-value [node] (vtd/attr node (vtd/tag node)))

(defn attrs [node] (into {} (map (fn [attr] {(vtd/tag attr) (attr-value attr)}) (vtd/search node "./@*"))))

(defn parent-ref
  []
  ["." (fn [_] nil) :parent])

(defn sibling-ref
  [table function]
  ["." function :sibling table])

(defn value
  [val-or-fn]
  (if (fn? val-or-fn)
    ["." val-or-fn]
    ["." (fn [_] val-or-fn)]))

(defn xpath-text
  ([xpath]
   (xpath-text xpath identity))
  ([xpath transform]
   (value #(transform (vtd/text (vtd/at % xpath))))))

(defn xpath-attr
  ([xpath attr]
   (xpath-attr xpath attr identity))
  ([xpath attr transform]
   (value #(transform (vtd/attr (vtd/at % xpath) attr)))))

(defn apply-context
  ([row context] (apply-context row nil context))
  ([row parent context]
  (into {}
         (map (fn [[col-name val-fn]] {col-name (val-fn parent context)}) row))))

(defn get-xml-value
  [xml value-def]
  (let [[xpath transform] value-def
        node (if (= "." xpath) xml (vtd/at xml xpath))]
    (transform node)))

(defn get-column-value
  [xml col-name col-def]
  (let [value (get-xml-value xml col-def)
        ref-type (nth col-def 2 nil)
        ref-key (nth col-def 3 nil)]
  {col-name (fn [parent-id context]
              (if (nil? ref-type) value (if (= :sibling ref-type) (first (get-in context [ref-key value])) parent-id)))}))

(defn get-column-values
  [xml defs]
  (into {}
         (map (fn [[col-name col-def]]
                (get-column-value xml col-name col-def)) defs)))

(declare get-table)

(defn- get-collapsed
  [xml table xml-id columns] 
  (if (nil? table) nil
    (let [collapsed (:rows (get-table xml table))]
    (into {} (map 
               (fn [[nested-xml-id nested-row]]
                 {[xml-id nested-xml-id] (update-in nested-row [:columns] merge columns)}) collapsed)))))

(defn get-table-row
  [xml table]
  (if (every? table [:dependent-tables :collapse])
    (throw (IllegalArgumentException.
             (str "Error in definition of table "
                  (:table table)
                  " -- :dependent-tables and :collapse can not be mixed"))))
  (let [xml-id (if-let [id (:xml-id table)]
                 (get-xml-value xml id)
                 (java.util.UUID/randomUUID))
        columns (get-column-values xml (:columns table))
        rev-deps (map #(get-table xml %) (:dependent-tables table))]
    (if (:collapse table)
      (into {} (map #(get-collapsed xml % xml-id columns) (:collapse table)))
      {xml-id {:columns columns :dependent-tables rev-deps}})))

(defn get-table
  [xml table]
  (let [elements (vtd/search xml (:each table))
        rows (into {} (map #(get-table-row % table) elements))]
    {:table (:table table) :sql-id (:sql-id table) :rows rows}))

(defn jdbc-inserter
  [db]
  (fn [table columns]
    (try 
    (first (jdbc/insert! db table columns :entities (sql/quoted \")))
      (catch Exception e (do (println "JDBC ERROR" columns) (throw e))))))

(defn insert-row
  [inserter table-name sql-id-fn row-xml-id columns]
  [row-xml-id (sql-id-fn (inserter table-name columns))])

(defn insert-table
  ([inserter table-data] (insert-table inserter table-data nil {}))
  ([inserter {:keys [sql-id rows table]} parent-id context]
   {table (into {}(map (fn [[k v]]
                   (let [inserted (insert-row inserter table sql-id k (apply-context (:columns v) parent-id context))
                         xml-id (first inserted)
                         sql-id (second inserted)]
                     (loop [dt (:dependent-tables v) acc {}]
                       (if (seq dt)
                         (recur (rest dt) (merge acc (insert-table inserter (first dt) sql-id acc)))
                         {xml-id [sql-id acc]}))
                     )) rows))}))
