(ns app
  (:require [cljfx.api :as fx])
  (:require [clojure.java.io :as jio])
  (:import [javafx.scene Node])
  (:import [javafx.scene.text Font])
  (:import [javafx.scene.input TransferMode])
  (:import [javafx.stage FileChooser]))

(def *state
  (atom {:display :main
         :upload-state {:file nil
                        :is-uploading false
                        :progress 0}}))

(defn- font-from-resource
  "Loads a font from a resource"
  [font-name font-size]
  (let [url (jio/resource font-name)
        fnt (Font/loadFont (.toExternalForm url) font-size)]
    fnt))

(defn heading-label
  "Component that displays a Heading-formatted label"
  [{:keys [text]}]
  {:fx/type :label
   :text text
   :padding {:top 10 :bottom 5 :left 0 :right 0}
   :style {:-fx-font [:bold 24 :sans-serif]}})

(defn set-upload-with-file!
  "Sets the state to Upload with a file chosen"
  [file]
  (if (not (nil? file))
    (swap! *state (fn [s]
                    (-> s
                        (assoc :display :upload)
                        (assoc-in [:upload-state :file] file))))))

(defn- select-file
  "Opens a file dialog for picking a file to upload"
  [window]
  (let [chooser (doto (FileChooser.)
                  (.setTitle "Select a file to upload"))
        file (.showOpenDialog chooser window)]
    file))

(defn main-pane
  "Draws the Main pane"
  [& args]
  (let [font-awesome-big
        (font-from-resource "fontawesome-webfont.ttf" 64.0)]
    {:fx/type :h-box
     :alignment :center
     :spacing 10
     :children [{:fx/type :v-box
                 :alignment :center
                 :children [{:fx/type heading-label
                             :text "Send"}
                            {:fx/type :button
                             :text "\uF093"
                             :font font-awesome-big
                             :on-action {:event/type ::select-file-for-upload}
                             :on-drag-over {:event/type ::upload-drag-over}
                             :on-drag-dropped {:event/type ::upload-file-dropped}}]}
                {:fx/type :v-box
                 :alignment :center
                 :children [{:fx/type heading-label
                             :text "Receive"}
                            {:fx/type :button
                             :text "\uF019"
                             :font font-awesome-big
                             :on-action {:event/type ::download}}]}]}))

(defn- get-name
  "Gets the name of a file"
  [file]
  (if (nil? file)
    "nil"
    (.getName file)))

(defn upload-pane
  "Draws the Upload pane"
  [props]
  (let [font-awesome-small
        (font-from-resource "fontawesome-webfont.ttf" 14.0)
        uploading-file (get-in props [:upload-state :file])
        progress-component (if (get-in props [:upload-state :is-uploading])
                             {:fx/type :progress-bar
                              :progress (get-in props [:upload-state :progress])}
                             {:fx/type :h-box
                              :alignment :center
                              :spacing 10
                              :children [{:fx/type :progress-indicator}
                                         {:fx/type :label
                                          :text "Waiting for friend..."}]})]
    {:fx/type :v-box
     :alignment :center
     :spacing 5
     :children [{:fx/type heading-label
                 :text "Sending file..."}
                {:fx/type :label
                 :text (get-name uploading-file)}
                {:fx/type :h-box
                 :v-box/margin {:top 0 :bottom 0 :left 10 :right 10}
                 :children [{:fx/type :text-field
                             :editable false
                             :text "7-crossover-clockwork"
                             :h-box/hgrow :always}
                            {:fx/type :button
                             :text "\uF0EA"
                             :font font-awesome-small}]}
                {:fx/type :label
                 :text "Copy the code above and send it to your friend!"
                 :style {:-fx-font-style :italic}}
                {:fx/type :h-box
                 :alignment :center
                 :spacing 20
                 :children [progress-component
                            {:fx/type :button
                             :text "Cancel"
                             :on-action {:event/type ::cancel}}]}]}))

(defn download-pane
  "Draws the Download pane"
  [& args]
  {:fx/type :v-box
   :alignment :center
   :children [{:fx/type heading-label
               :text "Enter the code:"}
              {:fx/type :text-field
               :prompt-text "i.e. 7-crossover-clockwork"}
              {:fx/type :h-box
               :children
                 [{:fx/type :text-field
                   :prompt-text "Downloads path"}
                  {:fx/type :button
                   :text "Download"}]}
              {:fx/type :h-box
               :alignment :center
               :children [{:fx/type :progress-bar}
                          {:fx/type :button
                           :text "Cancel"
                           :on-action {:event/type ::cancel}}]}]})

(defn root [{:keys [display upload-state]}]
  (let [pane (cond
               (= display :main) {:fx/type main-pane}
               (= display :upload) {:fx/type upload-pane
                                    :upload-state upload-state}
               (= display :download) {:fx/type download-pane}
               :else {:fx/type :label
                      :text "Unknown display!"})]
    {:fx/type :stage
     :showing true
     :width 400
     :height 250
     :title "Magic Wormhole"
     :scene {:fx/type :scene
             :root pane}}))

(defmulti event-handler :event/type)

(defmethod event-handler ::download [_]
  (swap! *state assoc :display :download))

(defmethod event-handler ::cancel [_]
  (swap! *state assoc :display :main))

(defmethod event-handler ::select-file-for-upload [e]
  (let [window (.getWindow (.getScene ^Node (.getTarget (:fx/event e))))
        file (select-file window)]
    (set-upload-with-file! file)))

(defmethod event-handler ::upload-drag-over [e]
  (do
    (if (.hasFiles (.getDragboard (:fx/event e)))
      (.acceptTransferModes (:fx/event e) TransferMode/ANY))
    (.consume (:fx/event e))))

(defmethod event-handler ::upload-file-dropped [e]
  (let [files (.getFiles (.getDragboard (:fx/event e)))
        file-to-upload (first files)]
    (set-upload-with-file! file-to-upload)))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)

