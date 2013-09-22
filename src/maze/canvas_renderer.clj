(ns maze.canvas-renderer
  (:require maze.irenderer))


;;---
(deftype CanvasRenderer [^javafx.scene.canvas.Canvas canvas
                         cell-size]
  maze.irenderer/IRenderer

  (draw-candidate [this candidat]
    ())

  (draw-packman [this packman]
    (let [gc (.getGraphicsContext2D canvas)
          [new-x new-y] (maze.irenderer/model->view-transform (:pos packman) cell-size)
          ;left-upper-corner (map - new-pos cell-size)
          [cell-width cell-height] cell-size
          ]
      (println "cavas renderer draw-cackman" (.getWidth canvas))
                                        ; cleaning context
      (.setFill gc javafx.scene.paint.Color/RED)
      (.fillOval gc new-x new-y cell-width cell-height)))

  (draw-walls [this walls]
    (doseq [wall walls]
      (let [[cell-width cell-height] cell-size
            [base-space-coord-x base-space-coord-y] (maze.irenderer/model->view-transform wall cell-size) ]
        (doto (.getGraphicsContext2D canvas)
          (.setFill javafx.scene.paint.Color/ORANGE)
          (.fillRect base-space-coord-x base-space-coord-y cell-width cell-height)))))

  (clear-scene [this]
                 (doto (.getGraphicsContext2D canvas)
                   (.setFill javafx.scene.paint.Color/LIGHTGREY)
                   (.fillRect 0 0 (.getWidth canvas) (.getHeight canvas)))))

(defn create-canvas-renderer
  [^javafx.scene.canvas.Canvas canvas cell-size]
  (let [[cell-x cell-y :as cell-size] cell-size]
    (CanvasRenderer. canvas cell-size)))
