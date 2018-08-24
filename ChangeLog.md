Down port from 1.11.2 branch.
fix: unable to use block harvesting features when swapping between Overworld and Nether without logging out and back in again.
fix: NullPointerException in Lumbination caused when chopping a tree on dedicated server.
refactor: Separated config sub categories into their own classes.
refactor: Added version number to the config file. New configs are version 2.0.
refactor: Converted item and block blacklists from json format to simple string list.
refactor: Added code to convert pre-version 2.0 config files to conform to the new version.