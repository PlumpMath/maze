(ns maze.packman
  (:require (maze [fx] [graph-renderer] [canvas-renderer])))

(def ^:const UP [0 -1])
(def ^:const DOWN [0 1])
(def ^:const LEFT [-1 0])
(def ^:const RIGHT [1 0])

(def ^:const GAME-MODE 0)
(def ^:const EDITOR-MODE 1)

(def cell-size [15 15])

(def app-mode-atom (atom nil))


(defn create-packman
  "Naive a bit implementation. See TODO."
  ([] (create-packman [(rand-int 10) (rand-int 10)] ))
  ([pos] (create-packman pos [(dec (rand-int 3)) (dec (rand-int 3))])); TODO: dependant on level
  ([pos dir]
     {:pos pos
      :dir dir}))

(defn create-ingame-creature
  [[width height :as level-dim] walls]
  (let [free-cells (for [x (range width) y (range height)] [x y])
        free-cells (clojure.set/difference free-cells walls)
        new-pos (rand-nth (seq free-cells))]
    {:pos new-pos
     :dir [0 0]}))


;;--- move packman variants -------------------
(defn move-packman-v5
  [{[dir-x dir-y :as dir] :dir pos :pos :as packman}
   [dim-x dim-y :as dim]
   walls]
  (let [new-pos (map + dir pos)
        new-pos (map mod new-pos dim)]
    (if (contains? walls new-pos)
      packman
      (assoc packman :pos new-pos ))))

;(move-packman-v5 (create-packman) [10 10] #{})
;;--- mutable state -----------------------------------
(def packman-atom (atom (create-packman [10 10] UP)))
(def level-dimentions [30 30])
(def walls-atom (atom #{[1 3] [10 10] [20 45]}))
;(.draw-walls @graph-renderer @walls-atom)


;;--- main timeline aka timer. ----------------
(declare draw-packman-both-renderers canvas-renderer)
(defn tick
  []
  (println "this is a tick. Seems its works")
  (swap! packman-atom move-packman-v5 level-dimentions @walls-atom)
  ; canvas
  (.clear-scene @canvas-renderer)
  (.draw-walls @canvas-renderer @walls-atom)
  ; graph
  (draw-packman-both-renderers @packman-atom)
  )


;; and there, I actually prefere explicitely subscribe/unsubscribe to timeline events
;; consider this later. TODO
(def main-timeline (maze.fx/create-timeline 500 #'tick))

;;--- settings event listeners ------------------
(defn onStartBtListener
  [e]
  (println "Im so happy !!!!")
  (.play main-timeline))

(defn onStopBtListener
  [e]
  (println "onStopBtListener !!")
  (.stop main-timeline))

(defn onKeyListener
  [e]
  (println "onKeyListener")
  (println e)
  )

(defn onKeyFilter
  [e]
  (println "onKeyFilter")
  (println (.getCode e))
  (when-let [new-dir (
                      {javafx.scene.input.KeyCode/UP UP
                       javafx.scene.input.KeyCode/DOWN DOWN
                       javafx.scene.input.KeyCode/LEFT LEFT
                       javafx.scene.input.KeyCode/RIGHT RIGHT}
                      (.getCode e))]
    (swap! packman-atom assoc :dir new-dir)))

;----- util/setup ------------

(javafx.application.Platform/setImplicitExit false)

(let [[f p] (maze.fx/create-jframe-jfxpanel)]
     (def frame-1 f)
     (def panel-1 p))


(def scene-1
  (let [s (atom nil)]
    (javafx.application.Platform/runLater
      (fn []
        (reset! s (javafx.scene.Scene. (javafx.scene.Group.)))
        ))
    s))

;--- remoning listeners -----------------
; this ugly stuff with keeping referencies on handlers and filters is all
; because of lack of removeAllListeners or removeListenerByEventType in JavaFX
;

(def listeners-atom (atom []))
(def filters-atom (atom []))

(defn remove-all-listeners []
  "Im really dont know how to do this in more elegant way."
  (doseq [[client event-type handler] @listeners-atom] (.removeEventHandler client event-type handler))
  (doseq [[client event-type filter] @filters-atom] (.removeEventFilter client event-type filter))
  (reset! listeners-atom [])
  (reset! filters-atom [])
  )

(defn game-setup []
  (remove-all-listeners)
  (swap! listeners-atom conj (maze.fx/add-event-listener
                              (.lookup @scene-1 "#bt_start")
                              javafx.scene.input.MouseEvent/MOUSE_CLICKED
                              #'onStartBtListener))
  (swap! listeners-atom conj (maze.fx/add-event-listener
                              (.lookup @scene-1 "#bt_stop")
                              javafx.scene.input.MouseEvent/MOUSE_CLICKED
                              #'onStopBtListener))
  ;; For note: this not triggers if the focus is on the battons.
  ;; Solutions are 1) switch focus to something who dont cansumes allow keys events
  ;; or better 2) use event filters aka event handlers on capture phase
                                        ;(add-key-listener-util @scene-1 #'onKeyListener)
  (swap! filters-atom conj (maze.fx/add-event-filter @scene-1 javafx.scene.input.KeyEvent/KEY_PRESSED #'onKeyFilter))
  (doto (.lookup @scene-1 "#tile_pane")
    (.setVisible false)))


(defn onDragDetected-capture [^javafx.scene.input.MouseEvent e]
  (println "onDragDetected-capture")
  (println e)
  (let [w (.lookup @scene-1 "#wall_image_view")
        o (.lookup @scene-1 "#osminog_image_view")
        t (.getTarget e)]
    (cond
     (identical? t w) (do
                        (println "this is a wall image")
                        ;(.acceptTransferModes e javafx.scene.input.TransferMode/COPY)
                        (let [db (.startDragAndDrop t (into-array [javafx.scene.input.TransferMode/COPY]))
                              clip-board-content (javafx.scene.input.ClipboardContent.)]
                         (do
                            (.putString clip-board-content "Hello frim clipboard")
                            (.setContent db clip-board-content)))
                        (.consume e))
      (identical? t o) (println "this is osminoge image"))))


(declare graph-renderer canvas-renderer)

(defn onDragDetected-bubbling [e]
  (println "onDragDetected-bubbling")
  (println e)
  )

(defn onDragOver-capture [e]
  (println "onDragOver-capture")
  (println e)
  (let [mouse [(.getX e) (.getY e)]])
  (.draw-candidate graph-renderer )
  )

(defn onDragOver-bubbling [e]
  (println "onDragOver-bubbling")
  (println e)
  )

(defn editor-setup []
  (remove-all-listeners)
  (.stop main-timeline)
  (doto (.lookup @scene-1 "#tile_pane")
    (.setVisible true))
   ; Drag and Drop
  (swap! filters-atom conj (maze.fx/add-event-filter
                            (.lookup @scene-1 "#tile_pane")
                            javafx.scene.input.MouseEvent/DRAG_DETECTED
                            #'onDragDetected-capture))


  (swap! filters-atom conj (maze.fx/add-event-filter
                            (.lookup @scene-1 "#canvas_pane")
                            javafx.scene.input.DragEvent/DRAG_ENTERED
                            #'onDragOver-capture))

  (swap! filters-atom conj (maze.fx/add-event-filter
                            (.lookup @scene-1 "#graph_pane")
                            javafx.scene.input.DragEvent/DRAG_OVER
                            #'onDragOver-capture)))

(defn setup-scene []
  (.setScene panel-1 @scene-1)
  (.setRoot @scene-1 @(maze.fx/loaded-mxml "src/main_gui.fxml"))
  (.setVisible frame-1 true)
  (game-setup))


;;-- I still dont know which is better: delays or place it inside some "init" function
(def graph-renderer (delay (maze.graph-renderer/create-graph-renderer (.lookup @scene-1 "#pane") cell-size)))
(def canvas-renderer (delay (maze.canvas-renderer/create-canvas-renderer (.lookup @scene-1 "#canvas") cell-size)))
;;---------------------------------
;(.clear-scene @canvas-renderer)
;(.draw-walls @graph-renderer @walls-atom)
;(.draw-walls @canvas-renderer @walls-atom)
;;---------------------------------
(defn draw-packman-both-renderers
  [packman]
  (.draw-packman @graph-renderer packman)
  (.draw-packman @canvas-renderer packman))

;;----------------------------------------------





(defn -main
  [& args]
  (javafx.application.Platform/setImplicitExit true)
  (setup-scene))
