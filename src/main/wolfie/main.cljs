(ns wolfie.main
  (:require [clojure.string :as string]
            [goog.events :as events]
            [secretary.core :as secretary :refer-macros [defroute]] 
            [reagent.core :as r] 
            [reagent.dom :as rdom] 
            [wolfie.camera :as camera]
            [wolfie.data :as data]
            [wolfie.drawing :as drawing]
            [wolfie.drawing-list :as drawing-list]
            [wolfie.recorder :as recorder]
            [wolfie.utils :as utils])
  (:import [goog History]
           [goog.history EventType]))


(def page (r/atom :home))


(secretary/set-config! :prefix "#")




(defn btn-config
  [btn-num]
  [:table {:class "btn"}
   [:tbody
    [:tr [:td [:span {:class "btn-title"} (str "Button " btn-num)]]]
    [:tr [:td
          [:table
           [:tbody
            [:tr 
             [:td [:img {:class "btn-drawing"}]]
             [:td [:div {:class "btn-list"}
                   [:button {:type "button"} "Set Sound"]
                   [:button {:type "button"
                             :on-click #(utils/goto! "buttons" btn-num "set-drawing")}
                    "Set Drawing"]
                   [:button {:type "button"} "Activate"]]]]]]]]]])


(defn home
  []
  (data/get-button-config)
  (fn []
    [:table
     [:tbody
      [:tr
       [:td [btn-config 1]]
       [:td [btn-config 2]]]
      [:tr
       [:td [btn-config 3]]
       [:td [btn-config 4]]]]]))


(defroute home-route "/" []
  (reset! page [:home]))

(defroute set-drawing "/buttons/:button-id/set-drawing"
  [button-id]
  (reset! page [:set-drawing {:btn-id button-id}]))

(defroute set-drawing-camera "/buttons/:button-id/set-drawing/camera"
  [button-id]
  (reset! page [:set-drawing-camera {:btn-id button-id}]))

(defroute set-drawing-new "/buttons/:button-id/set-drawing/new"
  [button-id]
  (reset! page [:set-drawing-new {:btn-id button-id}]))


(defn root
  []
  (let [[page-name args] @page]
    (case page-name
      :home [home]
      :set-drawing [drawing-list/select-drawing args]
      :set-drawing-camera [camera/camera args]
      :set-drawing-new [drawing/drawing args])))


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

