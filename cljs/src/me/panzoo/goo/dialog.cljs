(ns me.panzoo.goo.dialog
  (:require
    [me.panzoo.goo.util :as u]
    [goog.events :as events]
    [goog.events.EventType :as event-type]
    [goog.events.KeyCodes :as key-code]
    [goog.events.EventTarget :as event-target]
    [goog.style :as style]
    [goog.dom :as dom]
    [goog.dom.classes :as class]))

(def close-evt-type "me.panzoo.goo.dialog-close")

(defn hide
  "Hide the passed dialog."
  [{:keys [node]}]
  (style/showElement node false))

(defn show
  "Show the passed dialog."
  [{:keys [node]}]
  (dom/appendChild (. js/document body) node)
  (style/showElement node true))

(defn delete
  "Delete the passed dialog by removing it from the DOM tree and deleting all
  listeners in listener-keys. A deleted dialog cannot be re-shown."
  [{:keys [node listener-keys deleted?]}]
  (dom/removeNode node)
  (doseq [k @listener-keys]
    (events/unlistenByKey k))
  (reset! deleted? true))

(defn deleted?
  "Returns true if dialog has been deleted."
  [dialog]
  @(:deleted? dialog))

(defn- resize [{:keys [node]}]
  (style/setWidth node 0)
  (style/setHeight node 0)
  (style/setWidth node (. js/document width))
  (style/setHeight node (. js/document height)))

(defn add-listener-keys
  "Add keys to dialog so when dialog is deleted the corresponding listeners
  will be too."
  [dialog & keys]
  (swap! (:listener-keys dialog)
         #(concat % keys)))

(defn construct
  "Create a new dialog with the supplied title and sequence of DOM nodes. Also
  takes a key-value map of options (opts):

  :onclose on-argument function

  When the dialog is closed, this function is called with the dialog as its
  only argument. :onclose defaults to hide.
  
  The DOM hierarchy:
  div.modal-bg
    div.dialog
      div.dialog-closer
      h1.dialog-title
        title
      div.dialog-body
        nodes"
  [title nodes & {:as opts}]
  (let [closefn (or (:onclose opts) hide)
        closer (u/node "div" {:class "dialog-closer"})
        box (u/node "div" {:class "dialog"}
                    closer
                    (u/node "h1" {:class "dialog-title"}
                            (u/text title))
                    (apply u/node "div" {:class "dialog-body"}
                           nodes))
        bg (u/node "div" {:class "modal-bg"} box)
        target (events/EventTarget.)
        d {:node bg
           :closer closer
           :event-target target
           :listener-keys (atom nil)
           :deleted? (atom false)}]
    (reset!
      (:listener-keys d)
      [(events/listen
         js/window event-type/RESIZE
         (fn [_] (resize d)))
       (events/listen
         closer event-type/CLICK
         (fn [_] (.dispatchEvent target close-evt-type)))
       (events/listen
         bg event-type/CLICK
         (fn [evt]
           (when (= bg (. evt target))
             (.dispatchEvent target close-evt-type))))
       (events/listen
         js/window event-type/KEYDOWN
         (fn [evt]
           (when (= key-code/ESC (. evt keyCode))
             (. evt (preventDefault))
             (.dispatchEvent target close-evt-type))))
       (events/listen
         target close-evt-type
         (fn [_] (closefn d)))])
    (resize d)
    d))

(def style
  "Minimal CSS string for dialog."
  (u/css
    [:div.modal-bg
     :position "absolute"
     :top "0"
     :left "0"
     :background-color "black"
     :opacity "0.5"]
    [:div.dialog
     :position "absolute"
     :display "inline-block"
     :border "solid 1 black"
     :background "white"
     :top "10em"
     :left "10em"]
    [:div.dialog-closer:before
     :content "'✕'"]))
