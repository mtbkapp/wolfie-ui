(ns wolfie.main
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as string]
            [wolfie.recorder :as recorder]
            [wolfie.drawing :as drawing]))


; The most complicated part of this app will be a way for users to draw on a
; canvas of 240x240 pixels (24 bit color). It needs to have undo/redo.
;
; color picker
; pallete
; undo/redo
; canvas
; apple pencil support

(def black "#000000")
(def width 24)
(def height 24)

; state
; pallete [color-1 color-2 color-3 color-4 color-5]
; selected color
; pic vector
; pic index - undo redo moves this index
;
; making a change to the pic deletes everything in the pic vector from just
; past its current position and adds the new state onto that


(def blank-pic
  (let [row (vec (repeat width black))]
    (vec (repeat height row))))


(defn vec->hex
  [[a b c]]
  (-> (bit-or (bit-shift-left a 16) (bit-shift-left b 8) c)
      (.toString 16)
      (.padStart 6 "0")))



(def indexed-pic 
  (into []
        (map (fn [row]
               (into []
                     (map (fn [col]
                            (str "#" (vec->hex [(* 10 row)
                                                (* 10 col)
                                                (* 10 (bit-or row col))]))))
                     (range height))))
        (range height)))


(defonce the-pic (r/atom indexed-pic))


(defn on-pixel-click
  [x y & _]
  (swap! the-pic assoc-in [y x] black))


(defn pic
  [{:keys [pic]}]
  (into [:div {:id "pic"}]
        (map-indexed 
          (fn [y row]
            (into [:div {:class "row"}]
                  (map-indexed
                    (fn [x color]
                      [:div {:class "pixel"
                             :style {:background-color color}
                             :on-mouse-enter (fn [e]
                                               (if (not= 0 (.-buttons e))
                                                 (on-pixel-click x y)))
                             :on-click (partial on-pixel-click x y)}]))
                  row)))
        pic)

  #_[:table {:id "pic"}
   (into [:tbody]
         (map (fn [row]
                (into [:tr]
                      (map (fn [col]
                             [:td]
                             ))
                      row)))
         pic)])


(defn app
  [props]
  [:div
   [recorder/ui]
   #_[:button {:type "button"} "Undo"]
   #_[:button {:type "button"} "Redo"]
   #_[:input {:type "color" :default-value "#0000ff"}]
   #_[:input {:type "color" :default-value "#0000ff"}]
   #_[:input {:type "color" :default-value "#0000ff"}]
   #_[:input {:type "color" :default-value "#0000ff"}]
   #_[:input {:type "color" :default-value "#0000ff"}]
   #_[pic {:pic @the-pic}]
   ]
  )

(defn draw-line
  [ctx x1 y1 x2 y2]
  (.beginPath ctx)
  (set! (.-strokeStyle ctx) "blue")
  (set! (.-lineWidth ctx) 5)
  (.moveTo ctx x1 y1)
  (.lineTo ctx x2 y2)
  (.stroke ctx)
  (.closePath ctx))

(def state-init
  {:drawing? false :x 0 :y 0})

(defn draw
  []
  ; https://developer.mozilla.org/en-US/docs/Web/API/Element/mousemove_event
  (let [canvas (js/document.getElementById "canvas")
        ctx (.getContext canvas "2d")
        state (atom state-init)]
    (set! (.-lineCap ctx) "round")
    (set! (.-lineJoin ctx) "round")
    (.addEventListener canvas
                       "mousedown"
                       (fn [e]
                         (swap! state
                                assoc
                                :x (.-offsetX ctx)
                                :y (.-offsetY ctx)
                                :drawing? true)))
    (.addEventListener canvas
                       "mousemove"
                       (fn [e]
                         (let [{:keys [drawing? x y]} @state
                               nx (.-offsetX e)
                               ny (.-offsetY e)]
                           (when drawing? 
                             (draw-line ctx x y nx ny)
                             (swap! state assoc :x nx :y ny)))))
    (.addEventListener canvas
                       "mouseup"
                       (fn [e]
                         (let [{:keys [drawing? x y]} @state
                               nx (.-offsetX e)
                               ny (.-offsetY e)]
                           (when drawing? 
                             (draw-line ctx x y nx ny)
                             (reset! state state-init)))))
    (.addEventListener (js/document.getElementById "save-btn")
                       "click"
                       (fn [e]
                         (js/console.log (.toDataURL canvas))))))


(defn init
  []
  (rdom/render [drawing/drawing] (js/document.getElementById "app"))
  #_(draw))

