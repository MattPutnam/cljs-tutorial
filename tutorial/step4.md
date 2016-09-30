# Step 4

Now we need to make the cards clickable.  We'll modify the rendering function to take "am I selected?" and an onClick as parameters.  We're going to track card selection by index, so we'll modify things to carry around the index.  Add this to `utils.cljs`:

```clojure
(defn with-index [coll]
  (map vector (range) coll))
```

Modify the signature of `render-card` and add some styles:
```clojure
(defn render-card [{:keys [number color fill shape]} selected? on-click]
  [:div {:style {:border "1px solid black" :borderRadius 10
                 :padding 12 :width 86 :margin 8 :cursor "pointer"
                 :backgroundColor (when selected? "lightblue")
                 :display "inline-flex" :justifyContent "center"}
         :onClick on-click}
   ...
```

And modify the big `map` in `:render-playing`:
```clojure
(map (fn [cards]
       [:div {:style {:display "flex" :alignItems "center" :justifyContent "center"}}
        (map (fn [[index card]]
               (let [selected? (contains? (:selected-indexes @state) index)
                     on-click #(swap! state update :selected-indexes (if selected? disj conj) index)]
                 (render-card card selected? on-click)))
             cards)])
     (partition 3 (utils/with-index (:cards @state))))
```

Now we need to see if 3 cards are selected, and if so, add a "confirm" button to check if it's a set and do things.  Let's put this button right below the cards, right above the stats:
```clojure
(when (= 3 (count (:selected-indexes @state)))
  [:div {:style {:textAlign "center"}}
   [Button {:text "Confirm" :on-click #(react/call :check-set this)}]])
```

And then add that method to `SetGame`:
```clojure
:check-set
(fn [{:keys [state this]}]
  (if (apply utils/is-set? (replace (:cards @state) (:selected-indexes @state)))
    (utils/log "It's a set!")
    (utils/log "It's not a set")))
```

We need to modify several parts of `state` on a correct set.  The easy things are incrementing the number of found sets and clearing the selection.  Dealing out new cards is a little trickier.

If there are only 12 cards in play and there are cards left in the deck, then we need to deal out 3 more cards and replace the selected ones.  To do that, we'll use `assoc`, but we need to generate the key/value pairs to feed to it.  That can be done with `interleave`.  Then, since `assoc` takes the arguments inline but `interleave` leaves us with a data structure, we'll `apply` it.

If there are more than 12 cards in play or there are no remaining cards in the deck, then we just remove those cards.  To do that, we'll `keep-indexed` over the cards and throw out the selected indexes.  That results in a lazy sequence, so we'll have to convert back to a vector.  This has gotten kinda hairy so let's add some comments too:

```clojure
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
  (utils/log "It's not a set")))
```

Aside: The snazzier way to increment `:sets-found` in state would be `(swap! state update :sets-found inc)` but there's no clean way to jam that into one `swap!` along with the other things.  You could do multiple swaps, but since each triggers a re-render there's benefit in doing all of your updates in one go like this.


If the selection is not a set, we need to inform the user.  Let's stick a banner right where that button goes.  Replace that last remaining `utils/log` statement with:
```clojure
(swap! state assoc :error "Not a set" :selected-indexes #{})
```

Then put this right after the `when` that shows the "confirm" button:
```clojure
(when-let [error (:error @state)]
  [:div {:style {:textAlign "center" :color "red" :padding "0.7em 1em"}} error])
```

Okay, now it shows up, but we need it to go away too.  It should go away whenever the player clicks a card, hits the "deal 3 cards" button, or restarts the game... basically, any change to `state` (except the one that adds the error message, of course).  Rather than stick this everywhere, we can use `component-did-update`.  Add this method:
```clojure
:component-did-update
(fn [{:keys [prev-state state]}]
  (when (and (:error @state) (:error prev-state))
    (swap! state dissoc :error)))
```

The only thing left is to detect the end of the game.  The game is over when there are no more cards in the deck, and there are no more sets among the cards.  Rather than make our `:check-set` method even bigger, let's also stick this in `:component-did-update`.  Let's add some comments too.

```clojure
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
```

[Step 5: future enhancements](https://github.com/MattPutnam/cljs-tutorial/blob/master/tutorial/step5.md)

