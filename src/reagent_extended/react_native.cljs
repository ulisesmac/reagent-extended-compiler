(ns reagent-extended.react-native
  (:require
   ["react-native" :refer [Dimensions NativeModules Platform StyleSheet useColorScheme useWindowDimensions]]
   [applied-science.js-interop :as j]
   [clojure.string :as string]))

(def platform Platform)
(def platform-os (keyword "platform" (j/get platform :OS)))
(def android? (= platform-os :platform/android))
(def ios? (= platform-os :platform/ios))
(def style-sheet-absolute-fill (j/get StyleSheet :absoluteFill))

(defn use-color-scheme []
  (let [color-scheme (or (useColorScheme) "light")]
    (keyword "theme" color-scheme)))

(defn use-window-dimensions []
  (let [dimensions (useWindowDimensions)]
    {:height (j/get dimensions :height)
     :scale  (j/get dimensions :scale)
     :width  (j/get dimensions :width)}))

(defn add-dimensions-listener [on-change]
  (let [subscription (j/call Dimensions :addEventListener
                             "change"
                             (fn [event]
                               (on-change (js->clj event :keywordize-keys true))))]
    #(j/call subscription :remove)))

(def device-language
  (let [locale (if ios?
                 (or (j/get-in NativeModules [:SettingsManager :settings :AppleLocale])
                     (j/get-in NativeModules [:SettingsManager :settings :AppleLanguages 0])) ;; TODO: test on iOS
                 (-> NativeModules
                     (j/call-in [:I18nManager :getConstants])
                     (j/get :localeIdentifier)))
        [lang region] (string/split locale #"_")]
    {:lang   (keyword :lang lang)
     :region region}))
