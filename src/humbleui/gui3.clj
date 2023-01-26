(ns humbleui.gui3
  (:require
    [io.github.humbleui.ui :as ui])
  (:gen-class))

(def ui
  (let [*toggle-state (atom true)
        *slider-state (atom {:step 5 :min 0 :max 100 :value 50})]
    (ui/default-theme
      {}
      (ui/center
        (ui/width
          150
          (ui/column
            (ui/label "Hello from Humble UI! 👋")
            (ui/gap 10 10)
            (ui/image "https://clojure.org/images/clojure-logo-120b.png")
            (ui/gap 10 100)
            (ui/label "Clojure is a great language!")
            (ui/gap 10 10)
            (ui/toggle *toggle-state)
            (ui/gap 10 10)
            (ui/dynamic
              _ctx [ts @*toggle-state]
              (ui/center
                (ui/label ts)))
            (ui/gap 10 10)
            (ui/center
              (ui/label "Rate Clojure:"))
            (ui/gap 10 10)
            (ui/slider *slider-state)
            (ui/gap 10 10)
            (ui/dynamic
              _ctx [ss (:value @*slider-state)]
              (ui/center
                (ui/label (str ss " %"))))))))))

(comment
  (ui/start-app! (ui/window
                   {:title "Humble 🐝 UI"}
                   #'ui)))
