(ns wolfie.drawing
  (:require [reagent.core :as r]
            [wolfie.utils :as utils]))


(def HEIGHT 240)
(def WIDTH 240)
(def CANVAS-ID "new-drawing-canvas")


(defn target-value
  [e]
  (-> e (.-target) (.-value)))


(defn palette
  [{:keys [selected-color line-width colors on-change] :as s}]
  [:div
   [:div [:input {:type "range"
                  :min 1
                  :max 20
                  :value line-width
                  :on-change #(on-change [:line-width] (js/parseInt (target-value %)))}]]
   (into [:div]
         (map-indexed 
           (fn [i c]
             [:span
              [:input {:type "color"
                       :value c
                       :on-change #(on-change [:colors i] (target-value %))}]
              [:input {:type "radio"
                       :name "selected-color"
                       :checked (= i selected-color)
                       :on-change #(on-change [:selected-color] i)}]]))
         colors)])

(defn selected-color-from-palette
  [{:keys [colors selected-color]}]
  (nth colors selected-color))

(defn draw-line
  [ctx palette x1 y1 x2 y2]
  (let [r (quot (:line-width palette) 2)
        two-pi (* 2 js/Math.PI)]
    ; draw circle at the start 
    (.beginPath ctx)
    (set! (.-fillStyle ctx) (selected-color-from-palette palette))
    (.arc ctx x1 y1 r 0 two-pi)
    (.closePath ctx)
    (.fill ctx)
    ; draw circle at the end 
    (.beginPath ctx)
    (set! (.-fillStyle ctx) (selected-color-from-palette palette))
    (.arc ctx x2 y2 r 0 two-pi)
    (.closePath ctx)
    (.fill ctx)
    ; draw line between two points
    (.beginPath ctx)
    (set! (.-strokeStyle ctx) (selected-color-from-palette palette))
    (set! (.-lineWidth ctx) (:line-width palette))
    (.moveTo ctx x1 y1)
    (.lineTo ctx x2 y2)
    (.closePath ctx)
    (.stroke ctx)))


(defn palette-from-component
  [c]
  (let [[_ palette] (.-argv (.-props c))]
    palette))


(defn start-drawing
  [state ctx e]
  (swap! state assoc :x (.-offsetX e) :y (.-offsetY e) :drawing? true))

(defn move-brush
  [state ctx e]
  (let [{:keys [drawing? x y palette]} @state
        nx (.-offsetX e)
        ny (.-offsetY e)]
    (when drawing? 
      (draw-line ctx palette x y nx ny)
      (swap! state assoc :x nx :y ny))))

(defn finish-drawing 
  [state ctx e]
  (let [{:keys [drawing? x y palette]} @state
        nx (.-offsetX e)
        ny (.-offsetY e)]
    (when drawing? 
      (draw-line ctx palette x y nx ny)
      (swap! state assoc :drawing? false))))

(defn prevent-default
  [f]
  (fn [e]
    (.preventDefault e)
    (f e)))

(defn set-drawing!
  [btn-id]
  (let [canvas (js/document.getElementById CANVAS-ID)
        drawing (.toDataURL canvas "image/png")]
    ; post drawing to server, get new drawing id 
    ; config the button
    ; go back to home
    ))

; undo with canvas stack?
(def canvas
  (let [state (atom {:drawing? false :x 0 :y 0 :palette nil})]
    (r/create-class
      {:component-did-mount 
       (fn [this]
         (swap! state assoc :palette (palette-from-component this))
         (prn @state)
         (let [refs (.-refs this)
               div (.-div ^object refs)
               canvas (js/document.createElement "canvas") 
               ctx (.getContext canvas "2d")]
           (.setAttribute canvas "class" "drawing-canvas")
           (.setAttribute canvas "width" (str WIDTH))
           (.setAttribute canvas "height" (str HEIGHT))
           (.setAttribute canvas "id" CANVAS-ID)
           (.addEventListener canvas "mousedown" #(start-drawing state ctx %))
           (.addEventListener canvas "mousemove" #(move-brush state ctx %))
           (.addEventListener canvas "mouseup" #(finish-drawing state ctx %))
           (.addEventListener canvas "mouseleave" #(finish-drawing state ctx %))
           (.addEventListener canvas "touchstart" (prevent-default #(js/console.log "touchstart" %)))
           (.addEventListener canvas "touchmove" (prevent-default #(js/console.log "touchmove" %)))
           (.addEventListener canvas "touchend" (prevent-default #(js/console.log "touchend" %)))
           (.addEventListener canvas "touchcancel" (prevent-default #(js/console.log "touchcancel" %)))
           (.appendChild div canvas)))
       :component-did-update
       (fn [this old-argv old-state snapshot]
         (swap! state assoc :palette (palette-from-component this)))
       ;TODO component-will-unmount ?
       :render (fn [this]
                 [:div {:ref "div"}])})))


(defn drawing
  [{:keys [btn-id]}]
  (let [state (r/atom {:line-width 8
                       :colors ["#000000" "#ff0000" "#00ff00"]
                       :selected-color 0})]
    (fn []
      [:div "Drawing"
       [palette (assoc @state
                       :on-change (fn [ks v]
                                    (swap! state assoc-in ks v)))]
       [canvas @state]
       [:div
        [:button {:type "button" :on-click #(utils/goto!)} "Cancel"]
        [:button {:type "button" :on-click #(set-drawing! btn-id)} "Set"]]])))

