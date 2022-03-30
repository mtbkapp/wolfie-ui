(ns wolfie.drawing-list
  (:require [reagent.core :as r]
            [wolfie.utils :as utils]
            [wolfie.test-data :as test-data]))
             


(def state (r/atom {}))


(defn recv-drawings!
  [drawings]
  (swap! state
         (fn [{:keys [status]}]
           (when (= :waiting status)
             {:status :loaded
              :drawings (into [{:id :take-photo :src "images/camera_icon.svg"}
                               {:id :create-drawing :src "images/pencil_icon.png"}]
                              drawings)}))))



(defn load-fake-drawings!
  []
  (js/setTimeout (fn []
                   (recv-drawings!
                     (into []
                           (comp cat
                                 (map-indexed (fn [i d]
                                                (assoc d :id i))))
                           (repeat 3 test-data/drawings))))
                 100))


(defn load-drawings!
  []
  (reset! state {:status :waiting})
  (load-fake-drawings!))


(defn cancel! 
  []
  (reset! state {:status :canceled})
  (utils/goto!))


(defn select-drawing!
  [id]
  (swap! state #(assoc % :selected id)))


(defn set-drawing!
  [btn-id selected]
  (case selected
    :take-photo (utils/goto! "buttons" btn-id "set-drawing" "camera") 
    :create-drawing (utils/goto! "buttons" btn-id "set-drawing" "new")
    (prn "set" btn-id selected)))


(defn select-drawing 
  [{:keys [btn-id]}]
  (load-drawings!)
  (fn []
    (let [{:keys [status drawings selected]} @state]
      [:div {:class "select-drawing"}
       [:h1 (str "Select Drawing for Button " btn-id)]
       [:div
        (into [:div]
              (map (fn [{:keys [id data src]}]
                     [:img {:class (if (= id selected) "selected" "")
                            :src (if (some? data) 
                                   (str "data:image/png;base64, " data)
                                   src)
                            :on-click #(select-drawing! id)}]))
              drawings)
        [:button {:type "button" :on-click #(cancel!)} "Cancel"]
        [:button {:type "button"
                  :on-click #(set-drawing! btn-id selected)
                  :disabled (or (not= status :loaded)
                                (nil? selected))}
         "Set Drawing"]]])))
