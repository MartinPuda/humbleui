(ns humbleui.gui1
  (:require
    [io.github.humbleui.ui :as ui])
  (:gen-class))

(def ui
  (ui/default-theme
    {}
    (ui/center
      (ui/label "Hello from Humble UI! 👋"))))

(comment
  (ui/start-app!
    (ui/window
      {:title "Humble 🐝 UI"}
      #'ui)))

