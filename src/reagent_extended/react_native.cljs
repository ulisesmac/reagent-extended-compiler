(ns reagent-extended.react-native
  (:require
   ["react-native" :refer [Dimensions Keyboard NativeModules Platform StyleSheet useColorScheme useWindowDimensions]]
   [applied-science.js-interop :as j]
   [cljs-bean.core :refer [->clj]]
   [clojure.string :as string]))

(def platform Platform)
(def platform-os (keyword "platform" (j/get platform :OS)))
(def android? (= platform-os :platform/android))
(def ios? (= platform-os :platform/ios))

(def keyboard Keyboard)
(defn keyboard-metrics []
  (->clj (j/call keyboard :metrics)))
(defn add-keyboard-listener! [evt f]
  (j/call keyboard :addListener (name evt) f))
(def keyboard-dismiss! (.-dismiss Keyboard))
(defn keyboard-visible? []
  (j/call keyboard :isVisible))

(def style-sheet-absolute-fill (j/get StyleSheet :absoluteFill))

(defn use-color-scheme []
  (let [color-scheme (or (useColorScheme) "light")]
    (keyword "theme" color-scheme)))

(defn use-window-dimensions
  "Returns the current React Native window dimensions as a map with `:width`,
  `:height`, and `:scale`.

  On Android, these window metrics are not guaranteed to be safe-area
  normalized. Depending on the device, OEM, and navigation mode, `:height` may
  or may not include areas covered by system bars."
  []
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
