(ns app
  (:require [cljfx.api :as fx])
  (:require [clojure.java.io :as jio])
  (:import [javafx.scene.text Font])
  (:import [javafx.scene.input TransferMode]))

(def *state
  (atom {:display :main
         :uploading-file nil}))

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
  (swap! *state (fn [s]
                  (-> s
                      (assoc :display :upload)
                      (assoc :uploading-file file)))))

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
                             :on-action (fn [_]
                                          (set-upload-with-file! nil))
                             :on-drag-over (fn [e]
                                             (do
                                               (if (.hasFiles (.getDragboard e))
                                                 (.acceptTransferModes e TransferMode/ANY))
                                               (.consume e)))
                             :on-drag-dropped (fn [e]
                                                (let [files (.getFiles (.getDragboard e))
                                                      file-to-upload (first files)]
                                                  (set-upload-with-file! file-to-upload)))}]}
                {:fx/type :v-box
                 :alignment :center
                 :children [{:fx/type heading-label
                             :text "Receive"}
                            {:fx/type :button
                             :text "\uF019"
                             :font font-awesome-big
                             :on-action (fn [_]
                                          (swap! *state assoc :display :download))}]}]}))

(defn- get-name
  "Gets the name of a file"
  [file]
  (if (nil? file)
    "nil"
    (.getName file)))

(defn upload-pane
  "Draws the Upload pane"
  [{:keys [uploading-file]}]
  {:fx/type :v-box
   :alignment :center
   :children [{:fx/type heading-label
               :text "Sending file..."}
              {:fx/type :label
               :text (get-name uploading-file)}
              {:fx/type :text-field
               :editable false
               :text "7-crossover-clockwork"}
              {:fx/type :h-box
               :alignment :center
               :children [{:fx/type :progress-bar}
                          {:fx/type :button
                           :text "Cancel"
                           :on-action (fn [_]
                                        (swap! *state assoc :display :main))}]}]})

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
                           :on-action (fn [_]
                                        (swap! *state assoc :display :main))}]}]})

(defn root [{:keys [display uploading-file]}]
  (let [pane (cond
               (= display :main) {:fx/type main-pane}
               (= display :upload) {:fx/type upload-pane
                                    :uploading-file uploading-file}
               (= display :download) {:fx/type download-pane}
               :else {:fx/type :label
                      :text "Unknown display!"})]
    {:fx/type :stage
     :showing true
     :width 400
     :height 200
     :title "Magic Wormhole"
     :scene {:fx/type :scene
             :root pane}}))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)))

(fx/mount-renderer *state renderer)

