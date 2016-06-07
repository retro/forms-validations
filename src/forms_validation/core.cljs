(ns forms-validation.core
  (:require [reagent.core :as reagent]
            [forms.core :as f]
            [forms-validation.validations :refer [validator validations]]))

(enable-console-print!)

(defn setter
  "Set the value of the key path in the data atom"
  [path data-atom]
  (fn [e]
    (swap! data-atom assoc-in path (.. e -target -value))))

(defn render-errors
  "Renders the errors for the given key-path."
  [form path]
  (let [errors @(f/errors-for-path form path)]
    (when errors
      [:div.text-danger.errors-wrap 
       [:ul.list-unstyled
        (map (fn [error]
               [:li {:key error} (get-in validations [error :message])]) (:failed errors))]])))

(defn render-input
  "Renders an input field and it's errors.
  Validation behaves differently based on the key path error state:
  - If the key path is in the `valid` state, validation will be triggered on blur
  - If the key path is in the `invalid` state, validation will be triggered on change
  "
  ([form path label] (render-input form path label :text))
  ([form path label type]
   (fn [] 
     (let [form-data-atom (f/data form)
           form-data @form-data-atom
           is-valid? @(f/is-valid-path? form path)
           input-setter (setter path form-data-atom)
           on-change-handler (fn [e]
                               (input-setter e)
                               (when (not is-valid?)
                                 (f/validate! form true)))
           errors @(f/errors-for-path form path)]
       [:div.form-group {:class (when (not is-valid?) "has-error")}
        [:label.control-label label]
        [:input.form-control {:type type
                              :value (get-in form-data path)
                              :on-change on-change-handler
                              :on-blur #(f/validate! form true)}]
        [render-errors form path]]))))



(def registration-form (f/constructor validator {}))

(defn form-renderer [form]
  (let [on-submit (fn [e]
                    (.preventDefault e)
                    (f/validate! form))]
    [:form {:on-submit on-submit}
     [:h1 "User Registration"]
     [render-input form [:username] "Username"]
     [render-input form [:email] "Email"]
     [render-input form [:email-confirmation] "Email Confirmation"]
     [render-input form [:password] "Password" :password]
     [:hr]
     [:button.btn.btn-primary "Register"]]))



(defn main []
  (reagent/render [form-renderer registration-form]
                  (.getElementById js/document "app")))

(main)

(defn on-js-reload []
  (main))
