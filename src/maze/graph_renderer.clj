(ns maze.graph-renderer
  (:require maze.irenderer))

(deftype GraphRenderer [^javafx.scene.Node base
                        ^javafx.scene.shape.Shape packman-avatar
                        ^javafx.scene.Group wall-avatars-paceholder
                        cell-size]
  maze.irenderer/IRenderer

  (draw-candidate [this candidat]
    ())

  (draw-walls [this walls]

    ;; remove all
    (javafx.application.Platform/runLater
     (fn [] (->
            wall-avatars-paceholder
            .getChildren
            (.remove 0 (-> wall-avatars-paceholder .getChildren .size)))))

    (let [[cell-x cell-y] cell-size

          wall-avatars (map
                        (fn [wall]
                          (let [[base-space-coord-x base-space-coord-y] (maze.irenderer/model->view-transform wall cell-size)]
                            (doto (javafx.scene.shape.Rectangle. cell-x cell-y (javafx.scene.paint.Color/ORANGE))
                              (.setTranslateX base-space-coord-x)
                              (.setTranslateY base-space-coord-y))))
                        walls)]
      (javafx.application.Platform/runLater
       (fn [] (-> wall-avatars-paceholder .getChildren (.addAll wall-avatars))))))

  #_(draw-wall [this wall]
      (let [[new-x new-y :as new-pos] (maze.irenderer/model->view-transform wall cell-size)
            wall-avatar (walls-avatars-map wall)]
        (println "graph renderer draw-wall")
        (doto wall-avatar
          (.setVisible true)
          (.setTranslateX new-x)
          (.setTranslateY new-y))))

  (draw-packman [this {pos :pos :as packman}]
    (let [[new-x new-y :as new-pos] (maze.irenderer/model->view-transform pos cell-size)]
      (println "graph renderer draw-cackman")
      (.setVisible packman-avatar true)
      (.setTranslateX packman-avatar new-x)
      (.setTranslateY packman-avatar new-y)
      ))

  (clear-scene [this]
    (let [children (.getChildren base)]
      (doseq [child children]
        (.setVisible child false)))))


(defn create-graph-renderer
  [^javafx.scene.Node base cell-size]
  (let [[cell-x cell-y :as cell-size] cell-size
        radius (* 0.5 cell-x)
        packman-avatar (javafx.scene.shape.Circle. radius radius radius)
        wall-avatars-paceholder (javafx.scene.Group.)
        ]
    (javafx.application.Platform/runLater
     (fn [] (-> base .getChildren (.addAll
                                  [
                                   packman-avatar
                                   wall-avatars-paceholder]))))
    (.setVisible packman-avatar true)
    (GraphRenderer. base packman-avatar wall-avatars-paceholder cell-size)))
