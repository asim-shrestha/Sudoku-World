# Final project user stories:
- User story: As a traveller, I would like to preload large sets of words before a trip so that I will have a constant stream of new words to practice with.
    - Given: that the user has a stable internet connection and is within the "Online Sets" section of the set editor menu
    - When: the user holds down a set and then clicks the download button
    - Then: the word set will be locally saved to their phone and the user is free to use it in their own game.<br/><br/>
    - Given: that the user is in the "Online Sets" catagory of the "Set Builder" menu
    - When: the user enters something in the search bar
    - Then: online sets that match the query will display.<br/><br/>
    - Given: that the user has downloaded one or more online sets
    - When: the user is in the "My Sets" catagory of the "Set Builder" menu
    - Then: the downloaded sets will display with a cloud icon, indicating that it was download.<br/><br/>


- User story: As a competitor, I would like to challenge my friends to complete sudoku boards the fastest to see who has learned a new language better.
    - Given: that two users have a stable internet connection and is connected to google play games
    - When: the users press play in multiplayer mode
    - Then: they will me match-made against each other and must race to finish their given board the fastest<br/><br/>
    - Given: that the user is hosting a match
    - When: the user enters multiplayer mode
    - Then: he will be able to choose the settings for the game such as difficulty and board size<br/><br/>
    - Given: that the user is in a multiplayer match
    - When: the user reaches a certain quota of filled cells
    - Then: A power up will appear that the user can use to make solving the puzzle first easier for him<br/><br/>


- User story: As a language learner, I would like to create my own word sets to challenge myself with.
    - Given: that the user has created his own word pairs within the "Set Builder" menu and is currently in the "My Sets" catagory
    - When: the user press the plus button at the bottom right of the screen
    - Then: The user is able to create his own word set with the word pairs he has created and use them in game<br/><br/>
    - Given: that the user is in the "My Word Pairs" catagory of the set builder menu
    - When: the user presses the "+" button at the bottom right of the screen
    - Then: the "Add Word Pair" menu will pop up, allowing the user to create their own word pair<br/><br/>


- User story: As a social learner, I would like to be able to upload the sets that I create so that my friends may try them out as well.
    - Given: that the user has a stable internet connection and has created a custom word set and is in the "My Sets" catagory of the "Set Builder" menu
    - When: the user performs a long press on the set they wish to upload and click "Upload"
    - Then: the set will appear in the "Online Sets" catagory of the "Set Builder" many where users may download it to their respective device.<br/><br/>


- User story: As a user, I would like to play mutiple different puzzles so that I always face a new challenge.
    - Given: that the user has pressed play within the "New Game" menu
    - When: the game is loading
    - Then: a valid, single solution sudoku puzzle will be generated using a custom backtracking search algorithm.<br/><br/>


- User Story: As a sudoku player, I would like to be able to play multiple concurrent games so that when I get stuck on one hard puzzle, I can reset myself with a different puzzle and come back to the original puzzle with a clear head.
- User Story: As a user, I would like to be able to continue where I left off if I decide to stop playing midway through a game.
    - Given: that the user is within the main menu and has previously played a game and has not deleted their save file
    - When: the player presses the continue button
    - Then: the user is able to swipe through their previous save games and select which one they would like to play <br/><br/>
 

- User Story: As a language learner, I would like to have statistics of my game playing so that I may guage my language learning levels
    - Given: that the user is in the menu
    - When: a new game is started or an old game is continued
    - Then: there will be a timer under the sudoku board which keeps track of how long a puzzle has been played for.<br/><br/>
    - Given: that the user is playing a sudoku puzzle and has completed it properly
    - When: the user presses check answer
    - Then: a menu with statistics such as total time taken and number of incorrect cells would display<br/><br/>


- User Story: As a user, I would like some relaxing music to be playing in order to help my concentration.
    - Given: that the user has their volume turned up and that they have enabled backgroud music within the settings menu
    - When: the user begins playing a puzzle
    - Then: there will be a soft, slow music playing in the background<br/><br/>


- User Story: As a user, I would like appropriate sounds to play in response to the actions I am taking
     - Given: that the user has their volume turned up and that they have enabled sounds within the settings menu
     - When: the user performs actions such as button clicks
     - Then: a sound will play which informs the user that their button click went through, or their button click was correct / incorrect<br/><br/>


- User Story: As a music listener, I would like to be able to turn off the the in game sounds and music so that I may listen to my music from other apps open on my phone
    - Given: that the user is within the settings menu
    - When: the user DISABLES "Background Music" and "Sounds"
    - Then: no sounds will be played from the game.<br/><br/>


- User Story: As a user, I would like to change settings while playing a puzzle to quicky see what settings feel the best to play in.
    - Given: that the user is in the puzzle screen
    - When: the user presses the settings icon at the top right of the screen
    - Then: a settings menu will appear, enabling the user to change desired settings<br/><br/>
    

- User Story: As a language learner, I would like to be able to learn up to 4 different languages.
    - Given: that the user is in the set builder
    - When: the user wants to make a new word set, the user can add word pairs of the desired language
    - Then: the user is able to create a worded said in either english, spanish, french, or russian<br/><br/>


# User stories of past iterations:
- User Story: As a user I would like an interface in which I can navigate and utilize in order to start games with various options.
    - Given: that the user is within the main menu
    - When: the user clicks play
    - Then: a "New Game" menu will open up where the user can modify the game settings.<br/><br/>


- User Story: As a player, I would like to be able to play a variety of modes in order to first learn how to play and then progressively challenge myself more and more.
    - Given: that the user is within the new game menu and has selected a difficulty of "Hard" and has selected a board size of "9x9"
    - When: the user presses play
    - Then: there will be less than 35 pre-placed sudoku tiles.<br/><br/>
    - Given: that the user is within the new game menu and has selected "Display native words in initial cells" and has selected an appropriate word set
    - When: the user presses play
    - Then: any sudoku cell with preplaced values will have those values displayed in the "native language" of the word set.<br/><br/>
    - Given: that the user has selected 4x4 mode in the "New Game" menu and has selected an appropriately sized set
    - When: the user presses play
    - Then: he will be greeted with a 4x4 sudoku puzzle.<br/><br/>
    - Given: that the user is playing a 6x6 sudoku puzzle
    - When: the user is in the puzzle screen
    - Then: there will be 6 buttons the user can use to place values in cells<br/><br/>


- User Story: As a sudoku player, I would like to have incorrectly placed cells display so that I don't waste my time playing with incorrect cells
    - Given: that the user has enabled "Hints" in the settings menu and is currently playing a game
    - When: the user places an incorrect value within a cell
    - Then: the incorrect cell will have a red highlight until it is cleared or a correct value is placed<br/><br/>
 

- User Story: As a player, I would like to be able to edit my grid in case I input an incorrect value in the grid cell.
    - Given: that the user has placed a value in an unlocked cell and that the cell is currently being selected
    - When: the user touches the clear cell button
    - Then: the cell is clearned and no value appears within the cell<br/><br/>
    - Given: that the user is playing a sudoku puzzle and has selected an unlocked cell
    - When: the user presses any of the buttons at the bottom of the screen
    - Then: the value corresponding to the button will be placed in the unlocked cell<br/><br/>
    

- User Story: As a player, I would like to be able to check if I solved the sudoku puzzle correctly.
    - Given: that the user has placed the correct values into every cell
    - When: the user presses the "Check Answer" button
    - Then: a toast saying “Congratulations, You’ve Won!”  will display.<br/><br/>


- User Story: As a transiter, I would like to be able to play the game in landscape mode for when i ride the bus.
    - Given: That the user as screen rotation enabled within their phone and is playing a puzzle
    - When: the user rotates their device
    - Then: The layout will rotate to match their orientation, and view objects will resize and reposition to best fit the screen.<br/><br/>


- User Story: As a language learner, I would like to know the translation of a word in its language pair in case I forget while I am playing.
    - Given: that the user has DISABLED listening comprehension mode within the settings menu and is currently playing a puzzle
    - When: the user performs a long press on a pre-filled cell or a button
    - Then: a toast containing the translation cell's or button's word will appear in its language pair at the bottom of the screen.<br/><br/>


- User Story: As a language learner, I would like to improve my comprehension of of the native and foreign words i am using.
    - Given: that the user has ENABLED listening comprehension mode within the settings menu and that their volume is turned up.
    - When: the user performs a long press on a pre-filled cell or a button
    - Then: the word contained within the cell or button will be read outloud to the user.<br/><br/> 
    

- User Story: As a user, I would like to use this app in various devices.
    - Given: that the user is using a device with a large screen such as a tablet
    - When: the user loads up a game
    - Then: the sudoku grid along with the font that displays cell values will resize to best fit the device.<br/><br/>


- User story: As a language learner, I would like to be able to pick the words I use in game so that I can decide which words I want to learn in a non-native language.
    - Given: that the user is playing in native or foreign mode and has currently selected a set within the "My Sets" category of the "Set Builder" menu
    - When: the user presses the checkmark button, selecting the set and then presses play within the "New Game" menu
    - Then: the values in the cells and buttons of the new game reflect the word set they have chosen<br/><br/>


- User Story: As a language learner, I would like to work with custom wordlists that I may import myself.
    - Given: that the user has a stable internet connection and is within the "Online Sets" catagory of the change sets menu
    - When: the user long clicks on a set and presses the download button
    - Then: the set is downloaded and the user is able to select the set in the "My Sets" catagory and play puzzles with it<br/><br/>

    
- User story: As a user, I would like to know which words I struggle with the most so that I may practice them more.
    - Given: that the user has gotten at least 12 unique words wrong before
    - When: the user is in the "My Sets" catagory of the "Set Builder" menu
    - Then: A set containing the users most incorrect words will appear<br/><br/>
