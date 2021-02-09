(ns app
  (:require [cljfx.api :as fx])
  (:import [javafx.application Platform]))

(def *state
  (atom {:showing true}))

(defn header [{:keys [text]}]
  {:fx/type :label
   :text text
   :padding {:top 10 :bottom 15 :left 0 :right 0}
   :style {:-fx-font [:bold 24 :sans-serif]}})

(defn upload-pane [& args]
  {:fx/type :v-box
   :children [{:fx/type header
               :text "Upload"}
              {:fx/type :h-box
               :children [{:fx/type :text-field
                           :prompt-text "File path"}
                          {:fx/type :button
                           :text "Browse"}]}
              {:fx/type :button
               :text "Upload!"}
              {:fx/type :h-box
               :children [{:fx/type :text-field
                           :editable false
                           :prompt-text "Generated code goes here"}
                          {:fx/type :button
                           :text "Copy"}]}]})

(defn download-pane [& args]
  {:fx/type :v-box
   :children [{:fx/type header
               :text "Download"}
              {:fx/type :h-box
               :children [{:fx/type :text-field
                           :prompt-text "Wormhole code"}
                          {:fx/type :button
                           :text "Download"}]}
              {:fx/type :h-box
               :children [{:fx/type :text-field
                           :prompt-text "Destination"}
                          {:fx/type :button
                           :text "Browse"}]}
              {:fx/type :progress-bar}]})

(defn root [& args]
  {:fx/type :stage
   :showing true
   :title "Magic Wormhole"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :spacing 5
                  :children [{:fx/type :label
                              :text "Magic Wormhole"
                              :padding {:top 5 :bottom 0 :left 5 :right 0}
                              :style {:-fx-font [:bold 30 :sans-serif]}}
                             {:fx/type :h-box
                                    :alignment :center
                                    :spacing 20
                                    :padding {:top 10 :bottom 20 :left 20 :right 20}
                                    :children [{:fx/type upload-pane}
                                               {:fx/type download-pane}]}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))
