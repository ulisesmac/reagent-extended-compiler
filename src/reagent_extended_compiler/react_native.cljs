(ns reagent-extended-compiler.react-native
  (:require
   ["react-native" :refer [LogBox TouchableOpacity StatusBar Platform AppState Dimensions
                           Keyboard Animated KeyboardAvoidingView Appearance StyleSheet
                           Linking PermissionsAndroid useColorScheme BackHandler]]))

(defn use-color-scheme []
  (keyword "theme" (or (useColorScheme) "light")))

(def hairline-width (.-hairlineWidth StyleSheet))
(def style-sheet-absolute-fill (.-absoluteFill StyleSheet))

(def platform-os (keyword (.-OS Platform)))
(def platform-android? (= platform-os :android))

(def status-bar-height (.. StatusBar -currentHeight))
(def screen-height (.. Dimensions (get "screen") -height))
(defn window-width []
  (.. Dimensions (get "window") -width))
(def window-height (.. Dimensions (get "window") -height))

(def log-box LogBox)

(def keyboard Keyboard)

(defn safe-area-view [children]
  [:view {:style {:flex 1}}
   children])

(def animated Animated)

(defn color-scheme []
  (keyword (useColorScheme)))

(defn get-color-scheme []
  (keyword (.getColorScheme Appearance)))

(def app-state AppState)

(defn open-url [link]
  (.openURL Linking link))

(def permissions-android PermissionsAndroid)

(defn check-fine-location-android-permission [on-response]
  (-> permissions-android
      (.check (.-ACCESS_FINE_LOCATION (.-PERMISSIONS permissions-android)))
      (.then (fn [granted?]
               (when (fn? on-response)
                 (on-response granted?))))
      (.catch (fn [e]
                (prn e)))))

(defn request-fine-location-android-permission [on-response]
  (-> permissions-android
      (.request (.-ACCESS_FINE_LOCATION (.-PERMISSIONS permissions-android)))
      (.then (fn [response]
               (when (fn? on-response)
                 (on-response response))))
      (.catch (fn [e]
                (prn e)))))

(def back-handler BackHandler)
