(ns synthesis.examples_tables
  (:require [clojure.contrib.sql :as sql]
            [synthesis.db :as db]
            [local-file]))

;;;;;;;;;;
;; Create the examples.

; Query to evolve
; SELECT *
; FROM adult
; WHERE (age < 28 OR age > 48) AND (hours_per_week < 35 OR hours_per_week > 46)

(def pos-ex
  (vec (take 50 (db/run-db-function db/synthesis-db
                                    db/db-query
                                    "SELECT *
                                     FROM adult
                                     WHERE (age < 28 OR age > 48) AND (hours_per_week < 35 OR hours_per_week > 46)"))))

#_(def neg-ex
  (vec (take 50 (db/run-db-function db/synthesis-db
                                    db/db-query
                                    "SELECT *
                                     FROM adult
                                     WHERE NOT ((age < 28 OR age > 48) AND (hours_per_week < 35 OR hours_per_week > 46))"))))


(def neg-ex (vec (set (concat (take 30 (db/run-db-function db/synthesis-db
                                    db/db-query
                                    "SELECT *
                                     FROM adult
                                     WHERE NOT ((age < 28 OR age > 48) AND (hours_per_week < 35 OR hours_per_week > 46))"))
                    (take 10 (db/run-db-function db/synthesis-db
                                    db/db-query
                                    "SELECT *
                                     FROM adult
                                     WHERE (NOT (age < 28 OR age > 48)) AND (hours_per_week < 35)"))
                    (take 16 (db/run-db-function db/synthesis-db
                                    db/db-query
                                    "SELECT *
                                     FROM adult
                                     WHERE (NOT (age < 28 OR age > 48)) AND (hours_per_week > 46)"))))))


;;;;;;;;;;
;; Create small table for positive and negative examples.

(defn create-and-populate-examples-table
  "Creates table for positive and negative examples, and populates them with pos-ex and neg-ex."
  [pos-ex neg-ex]
  (db/run-db-function db/synthesis-db db/create-table
                      :adult_examples
                      db/synthesis-db-columns)
  (db/run-db-function db/synthesis-db db/insert-records
                      :adult_examples
                      pos-ex)
  (db/run-db-function db/synthesis-db db/insert-records
                      :adult_examples
                      neg-ex))

(defn drop-examples-table
  "Drops examples table."
  []
  (db/run-db-function db/synthesis-db db/drop-table :adult_examples))

;;;;;;;;;;
;; Example uses

;(create-and-populate-examples-table pos-ex neg-ex)

;(drop-examples-table)

; Displays the example table nicely
#_(sort #(compare (get %1 :education_num) (get %2 :education_num))
      (map #(select-keys % '(:education_num :hours_per_week :occupation :age))
           neg-ex))

#_(count (filter #(and (> (:age %) 48)
                     (< (:hours_per_week %) 35))
        pos-ex))

#_(do
  (println "Negative,Examples")
  (println "Age,Hours_Per_Week")
  (println (apply str (interpose "\n" (map #(str (:age %) "," (:hours_per_week %)) neg-ex))))
  )

; Displays the count of each occupation
#_(db/run-db-function db/synthesis-db
                           db/db-query
                           "SELECT occupation, count(*)
                            FROM adult
                            GROUP BY occupation"
                           )

#_(count (db/run-db-function db/synthesis-db
                           db/db-query
                           "SELECT *
                            FROM adult
                            WHERE (age < 28 OR age > 48) AND (hours_per_week < 35 OR hours_per_week > 46)"
                           ))
