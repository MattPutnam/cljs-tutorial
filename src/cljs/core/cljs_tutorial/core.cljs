(ns cljs-tutorial.core
  (:require
    [dmohs.react :as react]
    [cljs-tutorial.utils :as utils]
    ))


(defn render-card [{:keys [number color fill shape]} selected? on-click]
  [:div {:style {:border "1px solid black" :borderRadius 10
                 :padding 12 :width 86 :margin 8 :cursor "pointer"
                 :backgroundColor (when selected? "lightblue")
                 :display "inline-flex" :justifyContent "center"}
         :onClick on-click}
   (repeat number
           [:img {:src (str "assets/img/red_"
                            (name fill)
                            "_"
                            (name shape)
                            ".png")
                  :style (let [color-rotate (case color
                                              :red nil
                                              :green "hue-rotate(90deg)"
                                              :purple "hue-rotate(260deg)")]
                           {:height 55
                            :filter color-rotate
                            :WebkitFilter color-rotate})}])])

(react/defc Button
  {:render
   (fn [{:keys [props]}]
     [:div {:style {:display "inline-block"
                    :fontSize "initial" :fontWeight "bold"
                    :padding "0.7em 1em" :borderRadius 8
                    :color "white" :backgroundColor "blue"
                    :cursor "pointer"}
            :onClick (:on-click props)}
      (:text props)])})


(react/defc SetGame
  {:get-initial-state
   (fn []
     {:game-state :not-started})
   :render
   (fn [{:keys [state this]}]
     [:div {:style {:width 500 :border "1px solid lightgray" :padding "0.5em"}}
      [:div {:style {:textAlign "center" :fontSize "200%"}}
       "Set Game"]
      [:div {:style {:margin "1em 0"}}
       (case (:game-state @state)
         :not-started (react/call :render-not-started this)
         :playing (react/call :render-playing this)
         :finished (react/call :render-finished this))]
      [:div {}
       [Button {:text (if (= :not-started (:game-state @state)) "Start" "Restart")
                :on-click #(react/call :restart this)}]]])
   :component-did-update
   (fn [{:keys [prev-state state]}]
     ;; (:error prev-state) means this is not the update that showed the error,
     ;; so get rid of it if it's still there
     (when (and (:error @state) (:error prev-state))
       (swap! state dissoc :error))
     ;; When there are no more cards in the deck or sets on the board, end the game:
     (when (and (not= :finished (:game-state @state))
                (empty? (:deck @state))
                (empty? (utils/find-sets (:cards @state))))
       (swap! state assoc :game-state :finished)))
   :render-not-started
   (fn []
     [:div {} "Click 'Start' to begin!"])
   :render-playing
   (fn [{:keys [state this]}]
     [:div {}
      (map (fn [cards]
             [:div {:style {:display "flex" :alignItems "center" :justifyContent "center"}}
              (map (fn [[index card]]
                     (let [selected? (contains? (:selected-indexes @state) index)
                           on-click #(swap! state update :selected-indexes (if selected? disj conj) index)]
                       (render-card card selected? on-click)))
                   cards)])
           (partition 3 (utils/with-index (:cards @state))))
      (when (= 3 (count (:selected-indexes @state)))
        [:div {:style {:textAlign "center"}}
         [Button {:text "Confirm" :on-click #(react/call :check-set this)}]])
      (when-let [error (:error @state)]
        [:div {:style {:textAlign "center" :color "red" :padding "0.7em 1em"}} error])
      (let [sets (:sets-found @state)]
        [:div {} (str sets " set" (when-not (= sets 1) "s") " found")])
      [:div {} (str (count (:deck @state)) " cards left in deck")]
      (when-not (empty? (:deck @state))
        [:div {} [Button {:text "Deal 3 cards"
                          :on-click #(swap! state update :cards into (react/call :deal this 3))}]])])
   :render-finished
   (fn []
     [:div {} "Game over!  Click 'Restart' to play again!"])
   :check-set
   (fn [{:keys [state this]}]
     (if (apply utils/is-set? (replace (:cards @state) (:selected-indexes @state)))
       ;; If we have 12 cards in play and cards left in the deck...
       (if (and (= 12 (count (:cards @state)))
                (not (empty? (:deck @state))))
         ;; ... then deal out 3 cards to replace them, otherwise...
         (let [dealt (react/call :deal this 3)
               new-cards (apply assoc (:cards @state) (interleave (:selected-indexes @state) dealt))
               new-count (inc (:sets-found @state))]
           (swap! state assoc :cards new-cards :sets-found new-count :selected-indexes #{}))
         ;; ... just remove those cards.
         (let [new-cards (vec (keep-indexed (fn [index card]
                                              (when-not (contains? (:selected-indexes @state) index) card))
                                            (:cards @state)))
               new-count (inc (:sets-found @state))]
           (swap! state assoc :cards new-cards :sets-found new-count :selected-indexes #{})))
       (swap! state assoc :error "Not a set" :selected-indexes #{})))
   :deal
   (fn [{:keys [state]} num-cards]
     (let [dealt (take num-cards (:deck @state))]
       (swap! state update :deck (partial drop num-cards))
       dealt))
   :restart
   (fn [{:keys [state]}]
     (let [deck (shuffle utils/cards)]
       (swap! state assoc
              :deck (drop 12 deck)
              :cards (vec (take 12 deck))
              :game-state :playing
              :sets-found 0
              :selected-indexes #{})))})


(defn render-application [& [hot-reload?]]
  (react/render
   (react/create-element [SetGame])
   (.. js/document (getElementById "app"))
   nil
   hot-reload?))
