(ns me.panzoo.goo.dialog.test
  (:require
    [me.panzoo.goo.util :as u]
    [me.panzoo.goo.dialog :as dialog]
    [goog.dom :as dom]
    [goog.style :as style]
    [goog.events :as events]
    [goog.events.EventType :as event-type]))

(defn ^:export run []
  (style/installStyles dialog/style)
  (let [d1-ok (u/node "div" nil "Ok")
        d1 (dialog/construct "Hide on close"
                            [(u/node "p" nil "Scintillating content.")
                             d1-ok])
        d1-show (u/node "div" nil "Show d1")
        d2 (dialog/construct "Delete on close"
                             [(u/node "p" nil "This space left blank.")]
                             :onclose dialog/delete)
        d2-show (u/node "div" nil "Show d2")]

    (dialog/add-listener-keys
      d1
      (events/listen
        d1-ok event-type/CLICK
        (fn [_] (dialog/delete d1))))

    (events/listen
      d1-show event-type/CLICK
      (fn [_]
        (if @(:deleted? d1)
          (dom/setTextContent d1-show "D1 deleted")
          (dialog/show d1))))

    (events/listen
      d2-show event-type/CLICK
      (fn [_]
        (if @(:deleted? d2)
          (dom/setTextContent d2-show "D2 deleted")
          (dialog/show d2))))

    (dom/append (. js/document body) d1-show d2-show)))
