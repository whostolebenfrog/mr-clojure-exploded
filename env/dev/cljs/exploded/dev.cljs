(ns ^:figwheel-no-load exploded.dev
  (:require [exploded.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:8080/figwheel-ws"
  :jsload-callback core/mount-root)

(core/init!)
