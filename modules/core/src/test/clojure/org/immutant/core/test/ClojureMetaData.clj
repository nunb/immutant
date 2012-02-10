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

(ns org.immutant.core.test.ClojureMetaData
  (:use clojure.test)
  (:import [org.immutant.core ClojureMetaData])
  (:require [clojure.java.io    :as io]
            [immutant.bootstrap :as bootstrap]))

(let [descriptor (ClojureMetaData/parse (io/file (io/resource "simple-descriptor.clj")))
      md (ClojureMetaData. "app-name" descriptor)]
  (deftest parse "it should parse the descriptor and return a map"
    (is (= "my.namespace/init" (.get descriptor "init"))))

  (deftest getString "it should return the proper value as a String"
    (let [value (.getString md "init")]
      (is (= "my.namespace/init" value))
      (is (instance? String value))))

  (deftest getInitFunction "it should return the proper value"
    (is (= "my.namespace/init" (.getInitFunction md))))

  (deftest it-should-allow-access-to-any-metadata-value
    (is (= "biscuit" (.getString md "ham"))))

  (let [merged-md (doto (ClojureMetaData. "app-name" descriptor)
                     (.setLeinProject (bootstrap/read-and-stringify-project (io/file (io/resource "project-root")))))]
    (deftest the-immutant-map-from-project-clj-should-be-included
      (is (= "gravy" (.getString merged-md "biscuit"))))

    (deftest the-immutant-map-from-project-clj-should-take-a-backseat-to-existing-config
      (is (= "biscuit" (.getString merged-md "ham"))))))

(deftest getHash "it should return the proper value as a Hash"
  (let [md (ClojureMetaData. "app-name"
                              (ClojureMetaData/parse (io/file (io/resource "hashy-descriptor.clj"))))
        value (.getHash md "ham")]
    (is (= {"biscuit" "gravy"} value))))



