(ns wolfie.camera
  (:require [clojure.core.async :as async]
            [reagent.core :as r]))

(def WIDTH 240)
(def HEIGHT 240)


(def state (r/atom {:init? false}))


; code from adapted from https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Taking_still_photos#demo
; not sure why this doesn't work on ios but it does on the demo


(defn take-photo!
  []
  (let [{:keys [video]} @state
        canvas (js/document.createElement "canvas")
        ctx (.getContext canvas "2d")]
    (set! (.-width canvas) WIDTH)
    (set! (.-height canvas) HEIGHT)
    (.drawImage ctx video 0 0 WIDTH HEIGHT)
    (swap! state assoc :photo-src (.toDataURL canvas "image/png"))))


(defn camera
  [{:keys [btn-id]}]
  (r/create-class
    {:component-did-mount
     (fn [this]
       (let [refs (.-refs this)
             video (.-video ^object refs)
             
             photo (.-photo ^object refs)]
         (-> (js/navigator.mediaDevices.getUserMedia #js {:video true :audio false})
             (.then (fn [stream]
                      (set! (.-srcObject video) stream)
                      (.play video)
                      (swap! state
                             assoc
                             :init? true
                             :video video)
                      (js/console.log (.-height (.getSettings (aget (.getVideoTracks stream) 0))))
                      (js/console.log (.-width (.getSettings (aget (.getVideoTracks stream) 0))))
                      ))
             (.catch (fn [error]
                       (swap! state assoc :error error))))))
     :render 
     (fn [this]
       (let [{:keys [init? photo-src error]} @state]
         [:div {:ref "div" :class "camera"}
          [:video {:ref "video" :width WIDTH :height HEIGHT}]
          [:img {:ref "photo" :src photo-src}]
          [:button {:type "button"
                    :on-click #(take-photo!)
                    :disabled (not init?)}
           "Take Photo"]]))}))
