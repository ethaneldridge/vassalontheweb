(ns liberator-service.handler
  (:require [compojure.core :refer [GET ANY defroutes]]
            [compojure.route :refer [not-found resources files]]
            [hiccup.page :refer [include-js include-css html5]]
            [liberator-service.middleware :refer [wrap-middleware]]
            [liberator.core :refer [defresource resource]]
            [config.core :refer [env]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]))

(import '[java.io StringWriter]
        '[java.net Socket URL])

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn home-page []
  (html5
   (head)
    [:body
;     (anti-forgery-field)
     [:p (str (anti-forgery-field))]
     [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]]
     (include-js "js/app.js")]))


(defn send-request
  "Sends an HTTP GET request to the specified host, port, and path"
  [host port path]
  (with-open [sock (Socket. host port)
              writer (io/writer sock)
              reader (io/reader sock)
              response (StringWriter.)]
    (.append writer ( str path "\n"))
    (.flush writer)
    (io/copy reader response)
    (str response)))

(defresource mainmap
  :allowed-methods [:get]
  :handle-ok (fn [_] (send-request "localhost" 3030  "{\"GET_GAMESTATE\": {}}"))
  :available-media-types ["application/json"])

;; a helper to create a absolute url for the entry with the given id
(defn build-mainmap-url [request id]
  (URL. (format "%s://%s:%s/%s"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
;                (:uri request)
                (str id))))

(defresource gamepiece
  :allowed-methods [:post]
  :post!
  (fn [context]
    (dosync
     (let [body (slurp (get-in context [:request :body]))
           response (send-request "localhost" 3030 (str body))]
       {::data  response})))
  :post-redirect? true
  :location #(build-mainmap-url (get % :request) "mainmap")
  :available-media-types ["application/json"])



(defresource home
  :allowed-methods [:get]
  :headers {"Content-Type" "text/html"}
  :handle-ok home-page
  :etag "fixed-etag"
;  :available-media-types ["application/json"]
  :available-media-types ["text/html"])

(defroutes routes
  (ANY "/mainmap" request mainmap)
  (ANY "/gamepiece" request gamepiece)
  (resources "/")
;  (files "/")
  )

(defroutes routes-orig
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
