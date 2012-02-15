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

(ns immutant.cache
  (:use [clojure.core.cache :only (defcache)])
  (:require [clojure.core.cache]
            [immutant.registry :as lookup])
  (:import [clojure.core.cache CacheProtocol]
           [org.infinispan.config Configuration$CacheMode]
           [org.infinispan.manager DefaultCacheManager]))

(def local-manager (DefaultCacheManager.))
(def clustered-manager (lookup/fetch "jboss.infinispan.web"))

(defcache InfinispanCache [cache]
  CacheProtocol
  (lookup [_ key]
    (.get cache key))
  ;; (lookup [_ key not-found]
  ;;   (if (.containsKey cache key)
  ;;     (.get cache key)
  ;;     not-found))
  (has? [_ key]
    (.containsKey cache key))
  (hit [this key] this)
  (miss [this key value]
    (.put cache key value)
    this)
  (evict [this key]
    (and key (.remove cache key))
    this)
  (seed [this base]
    (and base (.putAll cache base))
    this)

  Object
  (toString [_] (str cache)))

(defn cache-mode
  [kw sync]
  (cond
   (= :invalidated kw) (if sync Configuration$CacheMode/INVALIDATION_SYNC Configuration$CacheMode/INVALIDATION_ASYNC)
   (= :distributed kw) (if sync Configuration$CacheMode/DIST_SYNC Configuration$CacheMode/DIST_ASYNC)
   (= :replicated kw) (if sync Configuration$CacheMode/REPL_SYNC Configuration$CacheMode/REPL_ASYNC)
   (= :local kw) Configuration$CacheMode/LOCAL
   :default (throw (IllegalArgumentException. "Must be one of :distributed, :replicated, :invalidated, or :local"))))

(defn reconfigure
  [name mode]
  (let [cache (.getCache clustered-manager name)
        config (.getConfiguration cache)
        current (.getCacheMode config)]
    (when-not (= mode current)
      (println "Reconfiguring cache" name "from" (str current) "to" (str mode))
      (.stop cache)
      (.setCacheMode config mode)
      (.defineConfiguration clustered-manager name config)
      (.start cache))
    cache))

(defn configure
  [name mode]
  (println "Configuring cache" (str name) "as" (str mode))
  (let [config (.clone (.getDefaultConfiguration clustered-manager))]
    (.setClassLoader config (.getContextClassLoader (Thread/currentThread)))
    (.setCacheMode config mode)
    (.defineConfiguration clustered-manager name config)
    (doto (.getCache clustered-manager name)
      (.start))))

(defn clustered-cache
  [name & {:keys [mode base sync] :or {mode :invalidated}}]
  (let [result (InfinispanCache. (if (.isRunning clustered-manager name)
                                   (reconfigure name (cache-mode mode sync))
                                   (configure name (cache-mode mode sync))))]
    (clojure.core.cache/seed result base)))

(defn local-cache
  ([] (InfinispanCache. (.getCache local-manager)))
  ([name] (InfinispanCache. (.getCache local-manager name)))
  ([name base] (clojure.core.cache/seed (local-cache name) base)))

(defn cache
  "The entry point to determine whether clustered or local"
  ([name] (cache name nil nil))
  ([name v] (if (keyword? v) (cache name v nil) (cache name nil v)))
  ([name mode seed]
     (if clustered-manager
       (clustered-cache name :mode (or mode :invalidated) :base seed)
       (local-cache name seed))))
     