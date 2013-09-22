(ns maze.irenderer)

(defprotocol IRenderer
  "all methods for rendering game's state"
  (draw-game [this packman walls])
  (draw-packman [this packman])
  (draw-wall [this wall])
  (draw-walls [this walls])
  (draw-candidate [this candidat])
  (clear-scene [this])
  (init [this & args]))


;;---- this is helper functions for rendering -----------
;;---- TODO: make this via matrix application -----------
; Im definitly dont know for now where this method should be.
; I place it here or convenience, because all implementers of this protocol whould require this ns,
; and access this function
; TODO: consider to bring it in something sharable by implementations but separated

(defn model->view-transform
  [[p-x p-y :as p] [cell-x cell-y :as cell-size]]
  (map #(+ (* %1 %2) (* 0.5 %2)) p cell-size ))
