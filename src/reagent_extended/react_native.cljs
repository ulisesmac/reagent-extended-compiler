(ns reagent-extended.react-native
  (:require
   ["react-native" :refer [Alert AppState BackHandler Dimensions Keyboard NativeModules PermissionsAndroid Platform
                           Linking StyleSheet useColorScheme useWindowDimensions]]
   [applied-science.js-interop :as j]
   [cljs-bean.core :refer [->clj]]
   [clojure.string :as string]))

(def platform Platform)
(def platform-os (keyword "platform" (j/get platform :OS)))
(def android? (= platform-os :platform/android))
(def ios? (= platform-os :platform/ios))

(def keyboard Keyboard)
(def back-handler BackHandler)
(defn keyboard-metrics []
  (->clj (j/call keyboard :metrics)))
(defn add-keyboard-listener! [evt f]
  (j/call keyboard :addListener (name evt) f))
(defn add-back-handler-listener! [f]
  (j/call back-handler :addEventListener "hardwareBackPress" f))
(def keyboard-dismiss! (.-dismiss Keyboard))
(defn keyboard-visible? []
  (j/call keyboard :isVisible))

(def permissions-android PermissionsAndroid)

(def app-state AppState)

(defn add-app-state-listener! [f]
  (j/call app-state :addEventListener "change" f))

(defn open-url [link]
  (j/call Linking :openURL link))

(def android-version (j/get platform :Version))

(def android-permission
  (j/lookup (j/get permissions-android :PERMISSIONS)))

(def android-permission-result
  (j/lookup (j/get permissions-android :RESULTS)))

(defn permissions-android-check [permission]
  (j/call permissions-android :check permission))

(defn permissions-android-request [permission]
  (j/call permissions-android :request permission))

(def hairline-width (j/get StyleSheet :hairlineWidth))
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

(defn- button->js [{:keys [is-preferred on-press style text]}]
  (cond-> #js {:text text}
    on-press (j/assoc! :onPress on-press)
    style (j/assoc! :style style)
    (some? is-preferred) (j/assoc! :isPreferred is-preferred)))

(defn- theme->ui-style [theme]
  (case theme
    :theme/dark "dark"
    :theme/light "light"
    nil))

(defn- options->js [{:keys [cancelable on-dismiss theme user-interface-style]}]
  (let [user-interface-style (or user-interface-style
                                 (theme->ui-style theme))]
    (cond-> #js {}
      (some? cancelable) (j/assoc! :cancelable cancelable)
      on-dismiss (j/assoc! :onDismiss on-dismiss)
      user-interface-style (j/assoc! :userInterfaceStyle user-interface-style))))

(defn alert
  "Show a native `Alert.alert` dialog from a props map.

  Props:
  - `:title` string
  - `:message` optional string
  - `:theme` optional app theme keyword such as `:theme/light` or `:theme/dark`
  - `:buttons` optional vector of button maps with:
    - `:text` string
    - `:on-press` callback
    - `:style` one of \"default\", \"cancel\", or \"destructive\"
    - `:is-preferred` iOS-only boolean
  - `:options` optional map with:
    - `:cancelable` Android-only boolean
    - `:on-dismiss` Android-only callback
    - `:user-interface-style` iOS-only string"
  [{:keys [buttons message options theme title]}]
  (j/call Alert
          :alert
          title
          message
          (when buttons
            (into-array (map button->js buttons)))
          (when options
            (options->js (assoc options :theme theme)))))
