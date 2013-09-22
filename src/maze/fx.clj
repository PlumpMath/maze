(ns maze.fx
  (:gen-class)
  (:import
    (java.util Timer TimerTask)
    (javax.swing JFrame SwingUtilities)
    (javafx.embed.swing JFXPanel)
    (javafx.application Platform)
    (javafx.scene Group Scene Parent GroupBuilder SceneBuilder)
    (javafx.scene.paint Color)
    (javafx.scene.canvas Canvas CanvasBuilder)
    (javafx.scene.shape Rectangle)
    (javafx.scene.layout VBox VBoxBuilder HBox HBoxBuilder Priority)
    (javafx.scene.control Button ButtonBuilder)
    (javafx.fxml FXMLLoader)
    (javafx.geometry Insets)
    (java.awt.event ActionListener)
    (javafx.animation AnimationTimer TimelineBuilder Timeline)
    (java.io FileInputStream)
    ))


(defn create-jframe-jfxpanel
  [& frameName ]
  (let [jframe (JFrame. (or (first frameName) "JFrame with JFXPanel" ))
        jfxpanel (JFXPanel.)]
    (SwingUtilities/invokeLater
      #(do
         (.setSize jframe 500 400 )
         (.setDefaultCloseOperation jframe JFrame/DISPOSE_ON_CLOSE)
         (.add jframe jfxpanel)
         (.setVisible jframe true))
      )
    [jframe jfxpanel]))


;-----------------------------------------------------------
(defn create-timeline [interval handler & cycle-count]
  (.. (TimelineBuilder/create)
      (keyFrames
       [(javafx.animation.KeyFrame.
         (javafx.util.Duration. interval)
         (reify javafx.event.EventHandler
           (handle [this e] (handler)))
         nil)])
      (cycleCount (or (first cycle-count) Timeline/INDEFINITE)
                                        ;100
                                        ;javafx.animation.Timeline/INDEFINITE
                  )
      (build)))

(defn add-timeline-hanller
  [timeline handler interval]
  (-> timeline .getKeyFrames (.add
    (javafx.animation.KeyFrame.
      (javafx.util.Duration. interval)
      (reify javafx.event.EventHandler
        (handle [this e] (handler)))
      nil))))

;--------------- swing timer --------------------------
(def swing-timer (javax.swing.Timer. 50
          (reify java.awt.event.ActionListener
            #_(actionPerformed [this e] (println "aga")))))
; потом можно:
#_(.addActionListener swing-timer
     (reify java.awt.event.ActionListener
               (actionPerformed [this e] (println "ugu"))))


;------------------- "src/test_ui.fxml" -------------------
(defn loaded-mxml
  [file-name]
  (delay
    (with-open
      [in (FileInputStream. file-name )]
      (.load (FXMLLoader.) in))))


;;----------------------------------------------------------
(defn add-event-listener
  [eventSource ^javafx.event.EventType event-type foo]
  (let [handler (reify javafx.event.EventHandler
                  (handle
                    [this e]
                    (foo e)))]
    (.addEventHandler eventSource event-type handler)
    [eventSource event-type handler]))

(defn add-event-filter
  "same as a event listener but for capturing phase"
  [eventSource ^javafx.event.EventType event-type  foo]
  (let [filter (reify javafx.event.EventHandler
                 (handle
                   [this e]
                   (foo e)))]
    (.addEventFilter eventSource event-type filter)
    [eventSource event-type filter]))

(defn add-key-listener-util
  [eventSource foo]
  ;; may be with addEventHandler will be better
  (.setOnKeyPressed eventSource
                    (reify javafx.event.EventHandler
                      (handle
                        [this e]
                        (foo e)))))
