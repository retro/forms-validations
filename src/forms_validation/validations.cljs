(ns forms-validation.validations
  (:require [forms.validator :as v]))

(def email-regex #"^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*$")

(def validations
  {:not-empty {:message "Value can't be empty"
               :validator (fn [v _ _] (not (empty? v)))}
   :email {:message "Value is not a valid email"
           :validator (fn [v _ _] 
                        (not (nil? (re-matches email-regex (str v)))))}
   :long-enough {:message "Value is too short (min 6 characters)"
                 :validator (fn [v _ _]
                              (> (count v) 5))}
   :email-confirmation {:message "Email doesn't match email confirmation"
                        :validator (fn [_ data _]
                                     (let [email (:email data)
                                           email-confirmation (:email-confirmation data)]
                                       (if (some nil? [email email-confirmation])
                                         true
                                         (= email email-confirmation))))}})

(defn to-validator
  "Helper function that extracts the validator definitions."
  [validations config]
  (reduce-kv (fn [m attr v]
               (assoc m attr
                      (map (fn [k] [k (get-in validations [k :validator])]) v))) {} config))

(def validator
  (v/validator
   (to-validator validations
                 {:username [:not-empty]
                  :email [:email :email-confirmation]
                  :email-confirmation [:email :email-confirmation]
                  :password [:long-enough]})))
