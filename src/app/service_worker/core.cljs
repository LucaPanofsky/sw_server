(ns service-worker.core
  (:require [service-worker.proxy-server.core :as proxy-server]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init
  "Register service worker listeners"
  []
  (js/console.debug "Init sw")

  (js/self.addEventListener
   "install"
   (fn []
     (js/console.debug "Calling skipWaiting()")
     (.skipWaiting js/self)))

  (js/self.addEventListener
   "activate"
   (fn []
     (js/console.debug "Sw active, claiming clients")
     (.claim js/self.clients))

   ;; PROXY SERVER HANDLER
   (js/self.addEventListener "fetch" proxy-server/handler)))