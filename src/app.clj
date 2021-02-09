(ns app
  (:require [cljfx.api :as fx])
  (:import [javafx.application Platform]))

(def *state
  (atom {:label-val "Hello world"
         :showing true}))

(defn hello-world [{:keys [label-val]}]
  {:fx/type :label
   :text label-val})

(defn text-input [{:keys [label-val]}]
  {:fx/type :text-field
   :text label-val
   :on-text-changed #(swap! *state assoc :label-val %)})

(defn root [{:keys [showing label-val]}]
  {:fx/type :stage
   :showing showing
   :title "Magic Wormhole"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 25
                  :spacing 40
                  :children [{:fx/type hello-world
                              :label-val label-val}
                             {:fx/type text-input
                              :label-val label-val}
                             {:fx/type :button
                              :text "Close"
                              :on-action (fn [_]
                                           (swap! *state assoc :showing false))}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))
