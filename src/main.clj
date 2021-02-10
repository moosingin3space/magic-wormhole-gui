(ns main
  (:require [app])
  (:require [cljfx.api :as fx])
  (:import [javafx.application Platform]))

(defn -main [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer app/*state app/renderer))
