(ns humbleui.gui2
  (:require
    [io.github.humbleui.ui :as ui])
  (:gen-class))

(def ui
  (let [*state (atom 0)]
    (ui/default-theme
      {}
      (ui/center
        (ui/column
          (ui/label "Hello from Humble UI! 👋")
          (ui/gap 10 10)
          (ui/dynamic
            _ctx [count @*state]
            (ui/center
              (ui/label count)))
          (ui/gap 10 10)
          (ui/button (fn [] (swap! *state inc))
                    (ui/label "Click me!")))))))

(comment
  (ui/start-app! (ui/window
                   {:title "Humble 🐝 UI"}
                   #'ui)))