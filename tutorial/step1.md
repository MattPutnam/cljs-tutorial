# Step 1

First, let's clear out the demo stuff that came with the project template.  Go into `src/cljs/core/[project-name]/core.cljs` and blast everything between the requires at the top and the `render-application` at the bottom.  And then replace the contents with a single "hello world" div just so things compile.  It should look like this:

```clojure
(defn render-application [& [hot-reload?]]
  (react/render
   (react/create-element [:div {} "Hello world!"])
   (.. js/document (getElementById "app"))
   nil
hot-reload?))
```

I like keeping my project clean of anything I don't actually need, so I'd get rid of `use-live-data?` and `ajax` in `utils.cljs` as well.  We won't be making any AJAX calls, this is going to be entirely client-side.

## Initial design and core utilities

I like to design a GUI application by thinking about what I want the user to see, and coming up with some core functionality that will be needed to support that.  It's clear that we'll need some representation of a card, and a function that determines whether or not 3 cards are a set.  We'll also want to be able to find a set among the cards in play--at the very least, we need to be able to tell when there are no more sets left and end the game, and we might also want a "hint" function.

There are several options for the card representation:

1. Create a Card class
 * Pros - A small amount of type safety.  At the very least, if I try to treat something else as a Card I'll get an ugly error.
 * Cons - Very heavy, and we don't get to use the built-in functions very easily.  In CLJS we usually only create a class when we want to implement a protocol, for example when creating our own data structure implementation.
2. Use a vector: [2 :red :solid :diamonds] for example
 * Pros - Very terse, automatically get a very readable string representation
 * Cons - Have to remember the property order.  Thanks to English adjective order rules the number clearly comes first and the shape goes last, but color and fill are ambiguous.  I might get confused and assume it's [3 :open :blue :squiggles] for example.
3. Use a map: {:number 2 :color :red :fill :solid :shape :diamond}
 * Pros - Don't have to remember property order
 * Cons - More verbose than the vector, representation is slightly less readable, will require a little more processing

To me, the cons of option 3 are very minor and the pro is substantial.  We'll go with that.

### Utility 1: Creating a deck

We can use a `for` comprehension to iterate over the combinations and generate each card:

```clojure
(def cards (for [number [1 2 3]
                 color [:red :green :purple]
                 fill [:open :striped :solid]
                 shape [:oval :diamond :squiggle]]
             {:number number :color color :fill fill :shape shape}))
```

### Utility 2: Determine if 3 cards are a set

We're going to use a pipeline of operations:

1. Boil the maps down to vectors (so it looks like option 2 above; this is the more processing I alluded to)
2. Group them by field, effectively a matrix transposition.  This will give a list of 3 numbers, a list of 3 colors, etc.
3. Count the distinct members of each field
4. Check that none of those counts is 2

Step 1 can be done with the [replace](http://clojuredocs.org/clojure.core/replace) function:
```clojure
(replace {:number 2 :fill :solid :color :green :shape :diamond} [:number :fill :color :shape])
=> [2 :solid :green :diamond]
```

Step 2 can be done with a moderately obscure usage of `map`:  When given multiple sequences, it applies the function across corresponding elements.  The standard implementation of matrix transformation, using vectors-of-vectors, is this:
```clojure
(defn transpose [matrix]
  (apply mapv vector matrix))
(transpose [[1 2 3] [4 5 6]])
=> [[1 4] [2 5] [3 6]]
```

We don't need the vectors, so we'll use `map` and `list` instead.

Step 3 is a straightforward usage of `count` and `distinct`, we can be clever and combine them in a single map operation with `comp`.

Step 4 is a straightfoward predicate application, in fact there's a built-in `not-any?` that seems amusingly specific but does exactly what we want.

```clojure
(defn is-set? [a b c]
  (->> [a b c]
       (map #(replace % [:number :fill :color :shape]))
       (apply map list)
       (map (comp count distinct))
       (not-any? (partial = 2))))
```

### Utility 3: Find a set among any number of cards

We'll iterate over all 3-card subsets and leverage `is-set?`.  There's not a built-in "all subsets of size k" so we'll have to create that too.

```clojure
(defn combinations [size coll]
  (if (= size 1)
    (map vector coll)
    (apply concat
           (map-indexed
             (fn [i elem]
               (map (fn [x] (conj x elem))
                    (combinations (dec size) (drop (inc i) coll))))
             coll))))

(defn find-sets [cards]
  (filter (partial apply is-set?)
          (combinations 3 cards)))
```

Try this all out:
```clojure
(find-sets (take 12 (shuffle cards)))
;; Will usually return 1 or 2 sets, will sometimes return an empty list
```

Add all of these to `utils.cljs`.

[Step 2: rendering cards](https://github.com/MattPutnam/cljs-tutorial/blob/master/tutorial/step2.md)
