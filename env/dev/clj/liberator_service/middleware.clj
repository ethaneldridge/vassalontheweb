(ns liberator-service.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults api-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (-> handler
; ECE 2017.06.17      (wrap-defaults site-defaults)
      (wrap-defaults api-defaults)
      wrap-exceptions
      wrap-reload))
