(ns service-worker.html.core
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt]))

(defn document [body]
  [:html [:head
          [:title "Service worker | htmx-app"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "service-worker" :content "service-worker.js"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:rel "stylesheet" :href "/static/css/style.css"}]]
   [:body body]
   [:script {:src "https://unpkg.com/htmx.org@1.9.5"}]])

(defn render-html [hiccup-form]
  (str (html hiccup-form)))