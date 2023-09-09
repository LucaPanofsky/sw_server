(ns service-worker.proxy-server.response
  "Ring utils for responses from 
   https://github.com/ring-clojure/ring/blob/1.9.0/ring-core/src/ring/util/response.clj#L47")

(defn ring-response
  "Returns a skeletal Ring response with the given body, status of 200, and no
  headers."
  [body]
  {:status  200
   :headers {}
   :body    body})

(defn status
  "Returns an updated Ring response with the given status."
  ([status]
   {:status  status
    :headers {}
    :body    nil})
  ([resp status]
   (assoc resp :status status)))

(defn header
  "Returns an updated Ring response with the specified header added."
  [resp name value]
  (assoc-in resp [:headers name] (str value)))

(defn content-type
  "Returns an updated Ring response with the a Content-Type header corresponding
  to the given content-type."
  [resp content-type]
  (header resp "Content-Type" content-type))

(defn compile-headers [ring-response] 
  (when-let [ring-headers (seq (:headers ring-response))] 
    (let [Headers (new js/self.Headers)] 
      (doseq [[k v] ring-headers] (.append Headers k v)) 
      Headers)))

(defn compile-response [ring-response] 
 (new js/Response
      (clj->js (:body ring-response))
      (clj->js
       {:headers (compile-headers ring-response)
        :status (:status ring-response)})))

 
 
 

