(ns me.panzoo.goo.dialog.test
  (:use
    clojure.test
    clj-webdriver.core
    [cljs.closure :only (build)])
  (:import
    [java.io File]))

(deftest dialog
  (.mkdir (File. "out"))
  (build "cljs" {:output-dir "out" :output-to "out/deps.js"})
  (let [b (start :firefox
                 (str "file://"
                      (.getAbsolutePath (File. ""))
                      "/test/me/panzoo/goo/dialog/test.html"))]
    (try
      (is
        (find-it b {:text "Show d1"})
        "Test implementation broken.")

      (-> b
        (find-it {:text "Show d1"})
        click)

      (is
        (exists? (find-it b {:class "dialog-title" :text "Hide on close"}))
        "Dialog not showing.")

      (-> b
        (find-it {:class "dialog-closer"})
        click)

      (is
        (and (exists? (find-it b {:class "dialog-title"}))
             (not (visible? (find-it b {:class "dialog-title"}))))
        "Dialog not hidden on dialog-closer click.")

      (-> b
        (find-it {:text "Show d1"})
        click)

      (-> b
        (find-it {:class "modal-bg"})
        click)

      (is
        (and (exists? (find-it b {:class "dialog-title"}))
             (not (visible? (find-it b {:class "dialog-title"}))))
        "Dialog not hidden on modal-bg click.")

      (-> b
        (find-it {:text "Show d1"})
        click)

      (-> b
        (find-it {:text "Ok"})
        click)

      (is
        (not (exists? (find-it b {:class "dialog-title"})))
        "Extra listener not fired.")

      (-> b
        (find-it {:text "Show d1"})
        click)

      (is
        (exists? (find-it b {:text "D1 deleted"}))
        ":deleted? not set or read correctly.")

      (-> b
        (find-it {:text "Show d2"})
        click)

      (is
        (exists? (find-it b {:class "dialog-title" :text "Delete on close"}))
        "Dialog not showing.")

      (-> b
        (find-it {:class "dialog-closer"})
        click)

      (is
        (not (exists? (find-it b {:class "dialog-title"})))
        "Dialog not deleted.")

      (-> b
        (find-it {:text "Show d2"})
        click)

      (is
        (exists? (find-it b {:text "D2 deleted"}))
        ":deleted? not set or read correctly.")

      (finally
        (close b)))))
