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



(defn btn-config
  [btn-num]
  [:div {:class (str "btn btn" btn-num)}
   [:table
    [:tr [:td [:span {:class "btn-title"} (str "Button " btn-num)]]]
    [:tr [:td
          [:table
           [:tr 
            [:td [:img {:class "btn-drawing"}]]
            [:td [:div {:class "btn-list"}
                  [:button {:type "button"} "Set Sound"]
                  [:button {:type "button"} "Set Drawing"]
                  [:button {:type "button"} "Activate"]]]]]]]]])


(defn home
  []
  [:table
   [:tr
    [:td [btn-config 1]]
    [:td [btn-config 2]]]
   [:tr
    [:td [btn-config 3]]
    [:td [btn-config 4]]]])


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

