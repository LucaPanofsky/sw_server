(ns install.core
  "Install the service worker declared in index.html")

(defn service-worker-trigger []
  (js/console.debug "Dispatching swRready")
  (.dispatchEvent
   (js/document.getElementById "app-shell")
   (new js/Event "swReady")))

(defn init
  "Install the service worker"
  []
  (letfn [(get-service-worker-script []
            (.-content (js/document.head.querySelector "[name=service-worker][content]")))]
    (js/console.debug "Init: install service worker")
    (.addEventListener
     js/window.navigator.serviceWorker
     "controllerchange"
     (fn [_event]
       (let [registration-completed? (.getItem js/localStorage "registration")]
         (if-not registration-completed?
           (do
             (.setItem js/localStorage "registration" true)
             (.reload js/location))
           (service-worker-trigger)))))
    (.addEventListener
     js/window.navigator.serviceWorker
     "message"
     (fn [event]
       (js/console.log "MESSAGE FROM SW:" event)))
    (->
     (js/navigator.serviceWorker.register (get-service-worker-script))
     (.then (fn [response]
              (js/console.debug "reg result:" response)
              (.setItem js/localStorage "registration" true)
              (service-worker-trigger))))))
       
    
