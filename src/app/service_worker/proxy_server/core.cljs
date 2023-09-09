(ns service-worker.proxy-server.core
  (:require
   [clojure.string :as string]
   [reitit.core :as r]
   [service-worker.html.core :as html]
   [service-worker.proxy-server.response :as response-interface]
   [service-worker.proxy-server.impl :as impl]))

(def origin (str js/origin "/"))

(defn htmx-request? [request]
  (get-in request [:headers "hx-request"]))

(defn navbar [] 
 (into [:ul {:hx-boost "true" :hx-target "#app-shell" :hx-swap "outerHTML transition:true"}] 
  (for [href (list "home" "section_A" "section_B" "section_C")] 
   [:li 
    [:a {:href (str "/sw_server/APP/" href)} href]]))) 
   
(def routes
  [["/sw_server/"
    ["APP/"
     ["home"
      {:name :test
       :get {:handler
             (fn [request]
               (js/console.log "HEADERS" (:headers request))
               (let [body [:div#app-shell
                           [:h1 "Home"]
                           [:hr]
                           (navbar)
                           [:hr]
                           [:p "This is the home page"]]]
                 (response-interface/content-type
                  {:status 200
                   :body (if (htmx-request? request)
                           (html/render-html body)
                           (html/render-html (html/document body)))}
                  "text/html")))}}]
     ["section_A"
      {:name :a
       :get {:handler (fn [request]
                        (let [body [:div#app-shell
                                    [:h1 "Section A"]
                                    [:hr]
                                    (navbar)
                                    [:hr]
                                    [:p "This is section A"]]]
                          (response-interface/content-type
                           {:status 200
                            :body (if (htmx-request? request)
                                    (html/render-html body)
                                    (html/render-html (html/document body)))}
                           "text/html")))}}]
     ["section_B"
      {:name :b
       :get {:handler (fn [request]
                        (let [body [:div#app-shell
                                    [:h1 "Section B"]
                                    [:hr]
                                    (navbar)
                                    [:hr]
                                    [:p "This is section B"]]]
                          (response-interface/content-type
                           {:status 200
                            :body (if (htmx-request? request)
                                    (html/render-html body)
                                    (html/render-html (html/document body)))}
                           "text/html")))}}]
     ["section_C"
      {:name :c
       :get {:handler (fn [request]
                        (let [body [:div#app-shell
                                    [:h1 "Section C"]
                                    [:hr]
                                    (navbar)
                                    [:hr]
                                    [:p "This is section C"]]]
                          (response-interface/content-type
                           {:status 200
                            :body (if (htmx-request? request)
                                    (html/render-html body)
                                    (html/render-html (html/document body)))}
                           "text/html")))}}]]]])


(defn not-found []
  (response-interface/content-type
   {:status 404
    :body (html/render-html [:body 
                             [:h1 "error"]
                             [:p "you step in the steam but the water has moved on"]])}  
   "text/html"))

(defn handler [^js e]
  (let [url (.. e -request -url)]
    (js/console.debug "DEBUG URL" url e (.. e -clientId))
    (js/console.log "prod debug:" 
                    (clj->js {:url url 
                              :match (string/starts-with? url (str origin "sw_server/" "APP"))
                              :origin origin}))
     
    (cond
      (or 
       (string/ends-with? url "shared") 
       (string/ends-with? url "install.js"))
      (do (js/console.log "handling script")
          (.respondWith
           e
           (->
            (impl/make-request (.. e -request))
            (.then
             (fn [_request]
               (js/console.log "install req \n" _request)
               (response-interface/compile-response
                (response-interface/content-type
                 {:body (str "console.log('skip: " url "')")
                  :status 200}
                 "application/javascript")))))))

      (string/starts-with? url (str origin "sw_server/" "APP"))
      (do (js/console.log "HANDLING THIS" url)
       (.respondWith
        e
        (->
         (impl/make-request (.. e -request))
         (.then
          (fn [request]
            (js/console.debug "api request" (clj->js request))
            (let [match (r/match-by-path (r/router routes) (:uri request))
                  router-handler (get-in match [:data (:method request) :handler] not-found)]
              (response-interface/compile-response
               (router-handler request))))))))
      :else (js/console.debug "Service worker is not handling" url))))



