# Step 5

This concludes the tutorial.  The game is minimally functional but definitely playable.  There's lots of room for improvement however:

### Make things look better

I'm not a graphic designer or a UX designer.  I'm just a general-purpose software developer with some UI implementation experience.  I'm bad with color palettes and fonts.  An obvious next step is to improve the visuals.

### Fix some minor bugs/usability issues

* The cards are selectable (you can drag over them and highlight the images).  Disable this.
* Buttons have selectable text, too.
* If you move the mouse too much while clicking it registers as a drag.  Fix this.

### Implement a hint system

Add a "hint" button that finds a set and highlights one of the cards in it.  Advanced: make a second click highlight a second card of the same set.

You could also use this to notify the player that there are no sets if they've been staring at it for a long time.

### Make a scoring system

Ideas:

* Make a timer and report the time at the end
 * Make a leaderboard.  You could store best times in local storage.
* Award points based on how fast the player spotted the set, possibly weighted by difficulty (only one thing different is easy, all 4 aspects being different is hardest (IMO))
 * Penalize incorrect sets and calling for more cards when there's a set on the board

### Do the blind last card thing

Some advanced players like to deal out the very last card face down.  With some tricky logic you can figure out what it must be.

### Advanced: Animate when cards move

When you deal out more than 12 cards and then find a set, the cards necessarily get reordered a bit to fill in.  Animate this movement.

### Super advanced: Make it multiplayer

Yeah...
