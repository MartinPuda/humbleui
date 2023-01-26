(ns humbleui.gui4
  (:require
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.paint :as paint]
    [humbleui.utils :as utils]
    [clojure.string :as str])
  (:import (java.time LocalDateTime YearMonth)
           (javax.swing JOptionPane JFrame JColorChooser)
           (java.awt Color)
           (java.time.format DateTimeFormatter)
           (java.util Locale))
  (:gen-class))

(def *app-state (atom nil))

(def *ui (atom nil))

(declare edit-todo
         todos)

(defn box-with-border [{:keys [border-color stroke-width color]
                        :or   {border-color 0xFF000000
                               stroke-width 1
                               color        0xFFFFFFFF}}
                       child]
  (ui/rect (paint/stroke border-color stroke-width)
           (ui/rect (paint/fill color)
                    child)))

(defn confirm []
  (JOptionPane/showConfirmDialog
    (JFrame. "")
    "Do you really want to delete this todo?" "Delete todo"
    JOptionPane/YES_NO_OPTION
    JOptionPane/WARNING_MESSAGE))

(defn datetime-column [jtime]
  (let [[date_ time_] (utils/format-datetime jtime)]
    (ui/column (ui/padding 5 (ui/label date_))
               (ui/padding 5 (ui/center (ui/label time_))))))

(defn day-rect [day]
  (ui/center
    (ui/rect (paint/fill 0xff00cccc)
             (ui/padding 25 10 25 10 (ui/label day)))))

(defn days-row []
  (->> utils/weekdays
       (mapv day-rect)))

(defn nav-row [left mid right]
  (ui/center
    (ui/row
      (ui/column)
      (ui/column left)
      (ui/column)
      (ui/column mid)
      (ui/column)
      (ui/column right)
      (ui/column))))

(defn text-button [f s]
  (ui/button f (ui/label s)))

(defn left-button [f]
  (text-button f "⫷"))

(defn right-button [f]
  (text-button f "⫸"))

(defn control-row [lf mid rf]
  (nav-row
    (left-button lf)
    mid
    (right-button rf)))

(defn year-row [*state]
  (control-row
    (fn [] (swap! *state #(.minusYears % 1)))
    (ui/dynamic
      _ctx [year (.getYear @*state)]
      (ui/center (ui/padding 10 (ui/label (.getYear @*state)))))
    (fn [] (swap! *state #(.plusYears % 1)))))

(defn month-row [*state]
  (control-row
    (fn [] (swap! *state #(.minusMonths % 1)))
    (ui/dynamic
      _ctx [month (.getMonth @*state)]
      (ui/center (ui/padding 10 (ui/label (str/capitalize (.getMonth @*state))))))
    (fn [] (swap! *state #(.plusMonths % 1)))))

(defn weekday-tile [day]
  (ui/center (ui/rect (paint/fill 0xff00cccc)
                      (ui/padding 25 10 25 10 (ui/label day)))))

(defn weekdays-tiles [weekdays]
  (->> weekdays
       (mapv weekday-tile)))

(defn calendar-tiles [*state]
  (ui/dynamic
    _ctx [month (YearMonth/from @*state)]
    (mapv #(ui/label %)
          (concat (repeat (dec (.getValue (.getDayOfWeek (.atDay month 1)))) "")
                  (range 1 (inc (.lengthOfMonth month)))))))

(defn gap-column [& args]
  (->> (interleave (repeatedly #(ui/gap 10 10)) args)
       (apply ui/column)))

(defn calendar [*state]
  (gap-column
    (year-row *state)
    (month-row *state)
    (ui/dynamic
      _ctx [st (YearMonth/from @*state)]
      (ui/grid
        (partition
          7
          (concat
            (weekdays-tiles utils/weekdays)
            (repeatedly (dec (.getValue (.getDayOfWeek (.atDay st 1))))
                        #(ui/label ""))
            (mapv (fn [n] (ui/button (fn [] (swap! *state #(.withDayOfMonth % n)))
                                     (ui/label n)))
                  (range 1 (inc (.lengthOfMonth st))))
            (repeatedly 7 #(ui/label ""))))))))

(let [datetime_ (atom nil)]
  (defn datetime [& {:keys [jtime]}]
    (cond
      jtime (reset! datetime_ jtime)
      (nil? datetime_) (reset! datetime_ (LocalDateTime/now)))
    (ui/default-theme
      {}
      (ui/focus-controller
        (ui/valign
          0
          (ui/center
            (gap-column
              (ui/center
               ; (ui/row
                  (text-button (fn [] (reset! *ui (edit-todo {})))
                               "Go back"))
              (ui/dynamic
                _ctx [dt @datetime_]
                (ui/center (datetime-column dt)))
              (ui/center
                (calendar datetime_))
              (ui/center
                (ui/width
                  50
                  (text-button (fn [] (reset! *ui (edit-todo {:time (str @datetime_)})))
                               "Save"))))))))))

(defn todo [[id {:keys [text time color] :as todo}]]
  (map #(ui/column %)
       [(box-with-border {}
                         (datetime-column (LocalDateTime/parse time)))
        (ui/width 250 (box-with-border {:color color}
                                       (ui/padding 15
                                                   (ui/label text))))
        (ui/button (fn [] (reset! *ui (edit-todo (conj todo [:id id]))))
                   (ui/padding 5 (ui/label "⚙")))
        (ui/button (fn []
                     (when (zero? (confirm))
                       (swap! *app-state dissoc id)
                       (spit "resources/todos.txt" (str @*app-state))
                       (reset! *ui (todos))))
                   (ui/padding 5 (ui/label "❌")))]))

(defn change-color [*state]
  (when-let [new-color (JColorChooser/showDialog (JFrame. "Choose a color")
                                                 "Color" (Color/BLACK))]
    (let [hex (format "%02x%02x%02x%02x"
                      (.getAlpha new-color)
                      (.getRed new-color)
                      (.getGreen new-color)
                      (.getBlue new-color))]
      (swap! *state assoc :text (str (Long/parseLong hex 16)))
      (str (Long/parseLong hex 16)))))

(let [*state (atom {})]
  (defn edit-todo [opts]
    (swap! *state merge opts)
    (let [todo @*state
          *todo-text (atom {:text (or (:text todo) "")})
          *todo-time (atom {:text (.format (DateTimeFormatter/ofPattern "dd. MMMM yyyy HH:mm"
                                                                        (Locale. "EN"))
                                           (or (some-> (:time todo)
                                                       (LocalDateTime/parse))
                                               (LocalDateTime/now)))})
          *todo-color (atom {:text (str (or (:color todo) 4289391081))})]
      (add-watch *todo-text :watcher
                 (fn [key atom old-state new-state]
                   (swap! *state assoc :text (:text new-state))))
      (ui/default-theme
        {}
        (ui/focus-controller
          (ui/valign
            0
            (ui/center
              (ui/padding
                10
                (gap-column
                  (ui/center (ui/row (text-button (fn []
                                                    (reset! *state {})
                                                    (reset! *ui (todos)))
                                                  "Go back")))
                  (ui/center
                    (ui/label (if (:id todo) "Edit todo:" "Add new todo:")))
                  (ui/center (ui/rect (paint/stroke 0xFF000000 5)
                                      (ui/grid [[(box-with-border {} (ui/padding 10 (ui/label "Text")))
                                                 (ui/text-field *todo-text)
                                                 (ui/gap 10 10)]
                                                [(box-with-border {} (ui/padding 10 (ui/label "Time")))
                                                 (ui/text-field *todo-time)
                                                 (text-button (fn [] (let [jtime (LocalDateTime/parse (:text @*todo-time)
                                                                                                      (DateTimeFormatter/ofPattern "dd. MMMM yyyy HH:mm"
                                                                                                                                   (Locale. "EN")))]
                                                                       (reset! *ui (datetime :jtime jtime))))
                                                              "⚙")]
                                                [(box-with-border {} (ui/padding 10 (ui/label "Todo color")))
                                                 (ui/dynamic
                                                   _ctx [col @*todo-color]
                                                   (ui/rect (paint/fill (parse-long (:text col)))
                                                            (ui/label "")))
                                                 (text-button (fn [] (when-let [color (change-color *todo-color)]
                                                                       (swap! *state assoc :color color)))
                                                              "⚙")]])))
                  (ui/center (ui/row (text-button (fn []
                                                    (let [new-todo {:text  (:text @*todo-text)
                                                                    :time  (str (LocalDateTime/parse (:text @*todo-time)
                                                                                                     (DateTimeFormatter/ofPattern "dd. MMMM yyyy HH:mm"
                                                                                                                                  (Locale. "EN"))))


                                                                    :color (parse-long (:text @*todo-color))}]
                                                      (if (:id todo)
                                                        (swap! *app-state assoc (:id todo) new-todo)
                                                        (let [new-id (if (seq @*app-state)
                                                                       (inc (key (apply max-key key @*app-state)))
                                                                       0)]
                                                          (swap! *app-state assoc new-id new-todo)))
                                                      (spit "resources/todos.txt" (str @*app-state))
                                                      (reset! *state {})
                                                      (reset! *ui (todos))))
                                                  "Save"))))))))))))

(defn button-new []
  (ui/center
    (ui/width
      50
      (ui/row
        (ui/button (fn [] (reset! *ui (edit-todo {})))
                   (ui/label "➕"))))))

(defn todos-list []
  (ui/center
    (ui/rect
      (paint/stroke 0xFF000000 5)
      (ui/grid
        (->> @*app-state
             (sort-by (comp :time val))
             (map todo))))))

(defn todos []
  (ui/default-theme
    {}
    (ui/vscrollbar
      (ui/valign
        0
        (ui/center
          (ui/column
            (ui/gap 10 10)
            (todos-list)
            (ui/gap 10 25)
            (button-new)))))))

(defn main [& _]
  (reset! *app-state (utils/resource->data "todos.txt"))
  (reset! *ui (todos))
  (ui/start-app! (ui/window {:title  "Todos"
                             :width  700
                             :height 500}
                            *ui)))