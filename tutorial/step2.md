# Step 2

Let's render the cards.  We'll need some images--the ideal way would be to have .svg files for each shape/fill combination, which could have a color applied.  I couldn't find any on the internet, and I'm a terrible graphic artist, so I grabbed some .png files and put them in `src/static/assets/img` in this repo.  It's just the red ones, so we'll use CSS color filtering to get the other colors.  It's ugly... but this tutorial isn't about manipulating images.

We'll write a `render-card` function taking a card.  Since we want to get at all of the fields, we'll destructure it right in the argument list:

```clojure
(defn render-card [{:keys [number color fill shape]}]
  ...)
```

We'll need an outer div.  It will have a border with rounded edges, and will lay out its 1-3 children horizontally and centered.  It should also be a fixed width.  I like using flex for linear layouts like this.

```clojure
(defn render-card [{:keys [number color fill shape]}]
  [:div {:style {:border "1px solid black" :borderRadius 10
                 :padding 12 :width 86 ;; <- I just fished around until I found something that looked good
                 :display "inline-flex" :justifyContent "center"}}])
```

We need `number` cards.  There's a handy `repeat` function for that.  We can easily generate the image source url, and I fished around with hue rotation until I found decent values.  Here's the whole thing:

```clojure
(defn render-card [{:keys [number color fill shape]}]
  [:div {:style {:border "1px solid black" :borderRadius 10
                 :padding 12 :width 86
                 :display "inline-flex" :justifyContent "center"}}
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
```

Note the alternate capitalization/punctuation for the Chrome-specific CSS.  Now let's get this rendering in the application.  We'll pick a random card out of the deck, print out its properties for a sanity check, and render the card.

```clojure
(defn render-application [& [hot-reload?]]
  (react/render
   (react/create-element
     (let [{:keys [number color fill shape] :as card} (rand-nth utils/cards)]
       [:div {}
        [:div {} (str number " " (name color) " " (name fill) " " (name shape) (when (> number 1) "s") ":")]
        (render-card card)]))
   (.. js/document (getElementById "app"))
   nil
   hot-reload?))
```

This should now be rendering a random card in the browser.  Refresh the page to see a new card.  It would be nicer of course to have a "refresh" button but we haven't gotten into React components yet.

I think we have enough internals to start building our game.

[Step 3: layout out the game](https://github.com/MattPutnam/cljs-tutorial/blob/master/tutorial/step3.md)
