(ns service-worker.proxy-server.impl
  (:require [clojure.string :as string]))

;; to do: fix this mess 

(defn get-search [url]
  (let [search (let [split (string/split url "?")]
                 (if (= (count split) 1) nil
                     (string/replace (second split) "?" "")))]
    (when search
      (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" search)]
                 [(keyword k) v])))))

;; useless record
(defrecord Request [method uri body headers])

(defn make-request [^js request]
  (let [get-body (-> (.json request)
                     (.then (fn [x] x))
                     (.catch (fn [_e] nil)))
        search (get-search (.. request -url))
        router-uri (let [full (string/replace (.. request -url) js/self.location.origin "")]
                     (if search 
                       (first (string/split full "?"))
                       full))]
    (-> get-body
        (.then (fn [body]
                 (->
                  (new Request
                       (keyword (string/lower-case (.. request -method)))
                       router-uri
                       (js->clj body :keywordize-keys true)
                       (when-let [headers (.. request -headers)]  (js->clj (.fromEntries js/Object headers))))
                  (assoc-in [:parameters :query-params] search))))
        (.catch (fn [error]
                  (js/console.error error)
                  false)))))
