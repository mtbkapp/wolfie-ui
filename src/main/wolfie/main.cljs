(ns wolfie.main
  (:require [clojure.string :as string]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]] 
            [reagent.core :as r] 
            [reagent.dom :as rdom] 
            [wolfie.recorder :as recorder]
            [wolfie.drawing :as drawing])
  (:import [goog History]
           [goog.history EventType]))


(def page (r/atom :home))


(secretary/set-config! :prefix "#")

(defn home
  []
  [:div {:class "home-btn-grid"}
   [:div {:class "btn btn1"} "Button 1"]
   [:div {:class "btn btn2"} "Button 2"]
   [:div {:class "btn btn3"} "Button 3"]
   [:div {:class "btn btn4"} "Button 4"]])


(defroute recorder-route "/recorder" []
  (reset! page :recorder))

(defroute drawing-route "/drawing" []
  (reset! page :drawing))

(defroute home-route "/" []
  (reset! page :home))


(defn root
  []
  (case @page
    :drawing [drawing/drawing]
    :recorder [recorder/ui]
    :home [home]))


(defn start-routing!
  []
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   #(secretary/dispatch! (.-token ^object %)))
    (.setEnabled true)))


(defn render-app!
  []
  (rdom/render [root]
               (js/document.getElementById "app")))


(defn init
  []
  (start-routing!)
  (render-app!))

