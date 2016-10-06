# cljs-tutorial

This is a tutorial for React using the dmohs/cljs-react wrapper.  It assumes basic knowledge of ClojureScript and React.  If you're shaky on ClojureScript, see [here](https://github.com/MattPutnam/Cadenza-CLJS/blob/master/clojurescript_intro.md).  If you're unfamiliar with React, see [here](https://github.com/MattPutnam/Cadenza-CLJS/blob/master/react_intro.md).

## The project: Online Set game

We'll explore React/CLJS by making a web version of the card game Set.  Set is a game for any number of players using a deck of 81 cards.  Each card is unique, and has some number of symbols on it.  The cards vary by the type of symbol (oval, diamond, or squiggle), color (red, green, or purple), fill pattern (open, filled, or striped), and number (1, 2, or 3).  There's one of each combination, and 3<sup>4</sup> = 81.

12 cards are initially dealt out, and players try to spot combinations of 3 cards where each of the four properties is either all the same, or all different.  For some examples and the complete rules, see the official website [here](http://www.setgame.com/sites/default/files/instructions/SET%20INSTRUCTIONS%20-%20ENGLISH.pdf).

In this tutorial, we'll create a solitaire version.

## Getting started

Follow the instructions [here](https://github.com/dmohs/cljs-react-template) to create a new React/CLJS project.  Make sure you can view the application in your browser, and that you can run the hot reloader.  Check out the Devcards mode if you want, but we're not going to use it for this tutorial, so instead run `lein with-profile +figwheel do clean, resource, figwheel` to run the application normally.

Editor recommendations:

* Eclipse with the Counterclockwise plugin
* IntelliJ with the Cursive plugin
* Light Table

Each has its own set of limitations, unfortunately there's not a single IDE that really nails it yet.

Once you're set up, go to [step 1](https://github.com/MattPutnam/cljs-tutorial/blob/master/tutorial/step1.md).
