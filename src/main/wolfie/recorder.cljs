(ns wolfie.recorder
  (:require [reagent.core :as r]
            [clojure.core.async :as async]))


(def state (r/atom {:recording? false
                    :chunks []
                    :stream nil
                    :recorder nil
                    :sounds []}))


(defn get-stream
  []
  (if-let [stream (:stream @state)]
    (async/go stream)
    (let [c (async/chan)]
      (-> (js/navigator.mediaDevices.getUserMedia #js {:audio true})
          (.then (fn [stream]
                   (async/go
                     (swap! state assoc :stream stream)
                     (async/>! c stream)
                     (async/close! c))))
          (.catch (fn [err]
                    (async/go
                      (js/console.log "Error getting user media" err)
                      (async/>! c nil)
                      (async/close! c)))))
      c)))


(defn chunks->blob
  [[c :as chunks]]
  (js/Blob. (clj->js chunks) #js {:type (.-type c)}))


(defn chunks->base64
  [chunks]
  (let [ch (async/chan)
        reader (js/FileReader.)]
    (set! (.-onloadend reader)
          (fn []
            (async/go
              (async/>! ch (.-result reader))
              (async/close! ch))))
    (.readAsDataURL reader (chunks->blob chunks))
    ch))


(defn on-recorder-stop
  [_]
  (async/go
    (js/console.log (async/<! (chunks->base64 (:chunks @state))))
    (swap! state
           update
           :sounds
           conj
           (js/window.URL.createObjectURL (chunks->blob (:chunks @state))))
    (swap! state
           assoc
           :recorder nil
           :chunks [])
    (prn (count (:sounds @state)))))


(defn add-chunk
  [data]
  (js/console.log "add chunk")
  (swap! state update :chunks conj data))


(defn on-record-click
  [e]
  (async/go
    (when-let [stream (async/<! (get-stream))]
      (let [recorder (js/MediaRecorder. stream)]
        (set! (.-onstop recorder) on-recorder-stop)
        (set! (.-ondataavailable recorder) #(add-chunk (.-data %)))
        (.start recorder)
        (swap! state
               assoc
               :recording? true
               :recorder recorder)))))


(defn on-stop-click
  [e]
  (.stop (:recorder @state))
  (swap! state assoc :recording? false))


(defn ui
  [props]
  [:div
   (if (:recording? @state)
     [:button {:type "button" :on-click on-stop-click} "Stop"]
     [:button {:type "button" :on-click on-record-click} "Record"])
   (into [:ul]
         (map (fn [url]
                [:li [:audio {:controls true :src url}]]))
         (:sounds @state)) ])
