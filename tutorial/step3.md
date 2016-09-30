# Step 3

We'll create a SetGame React component.  We have two major pieces of mutable state: the cards in play, and the cards remaining in the deck.  We'll also track what stage of the game we're in (not yet started, playing, finished), the number of sets found as a really basic "score", and the selected card indexes.

I also want a *deal* method to deal out a given number of cards.  Here's how I've outlined it:
```clojure
(react/defc SetGame
  {:get-initial-state
   (fn []
     {:game-state :not-started})
   :render
   (fn []
     [:div {} "TODO"])
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
```

Get this rendering by modifying `render-application`:
```clojure
(defn render-application [& [hot-reload?]]
  (react/render
   (react/create-element [SetGame])
   (.. js/document (getElementById "app"))
   nil
   hot-reload?))
```

I've written `:deal` in a way that leaves `state` in a funky place, but different parts of the game will need to do different things with the cards that are dealt.  After finding a set, we want the 3 new cards to replace the selected ones, and when we are stuck and deal out 3 additional cards we want them to go at the end.  So it will be the caller's responsibility to put the dealt cards into `state` in the right place.

For rendering, we'll have 3 significantly different things depending on the stage of the game we're in, so let's set up an outer container and call out to helper methods.  We'll need a Button class too.

```clojure
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

;; other methods omitted for brevity
(react/defc SetGame
  {:render
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
   :render-not-started
   (fn []
     [:div {} "Click 'Start' to begin!"])
   :render-playing
   (fn [{:keys [state this]}]
     [:div {}
      (map (fn [cards]
             [:div {:style {:display "flex" :alignItems "center" :justifyContent "center"}}
              (map render-card cards)])
           (partition 3 (:cards @state)))
      (let [sets (:sets-found @state)]
        [:div {} (str sets " set" (when-not (= sets 1) "s") " found")])
      [:div {} (str (count (:deck @state)) " cards left in deck")]
      (when-not (empty? (:deck @state))
        [:div {} [Button {:text "Deal 3 cards"
                          :on-click #(swap! state update :cards into (react/call :deal this 3))}]])])
   :render-finished
   (fn []
     [:div {} "Game over!  Click 'Restart' to play again!"])})
```

In `:render-playing`, I partition out the cards in play into groups of 3 to create rows.  I used flex to lay out the rows.  Grid is another (possibly better) way of doing this but it's a little heavy for what we're doing.  I also added `:margin 8 :cursor "pointer"` to `render-card`'s outer div.  It would be a little better from an architectural standpoint to not add those, and deal with margins and cursors upon laying out the cards, but that's a small concern.

[Step 4: making the game playable](https://github.com/MattPutnam/cljs-tutorial/blob/master/tutorial/step4.md)
