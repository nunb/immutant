;; Copyright 2008-2012 Red Hat, Inc, and individual contributors.
;; 
;; This is free software; you can redistribute it and/or modify it
;; under the terms of the GNU Lesser General Public License as
;; published by the Free Software Foundation; either version 2.1 of
;; the License, or (at your option) any later version.
;; 
;; This software is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
;; Lesser General Public License for more details.
;; 
;; You should have received a copy of the GNU Lesser General Public
;; License along with this software; if not, write to the Free
;; Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
;; 02110-1301 USA, or see the FSF site: http://www.fsf.org.

(ns immutant.integs.jobs
  (:use [fntest.core])
  (:use clojure.test)
  (:require [clj-http.client :as client]))

(use-fixtures :once (with-deployment *file*
                      {
                       :root "target/apps/jobs/"
                       }))

(defn get-values
  ([]
     (get-values ""))
  ([query]
     (let [result (client/get (str "http://localhost:8080/jobs?" query))]
       ;;(println "RESPONSE" result)
       (read-string (:body result)))))

(deftest simple "it should work"
  (let [initial-value (:a-value (get-values))]
    (Thread/sleep 1000)
    (let [next-value (:a-value (get-values))]
      (is (> next-value initial-value))
      (Thread/sleep 1000)
      (is (> (:a-value (get-values)) next-value)))))

(deftest rescheduling
  (is (> (:another-value (get-values)) 0))
  (get-values "reschedule")
  (Thread/sleep 2000)
  (is (= (:another-value (get-values)) "rescheduled")))

