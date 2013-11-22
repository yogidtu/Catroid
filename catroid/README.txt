: Batch script for deleting not needed files/directories for the LiveWallpaper
:
: %~dp0  - Path to directory in which the script was started
: ATTRIB - change atributes of files/directories
: +/-h   - Add/Remove the hidden atribute to/from files
: @echo  - print output message
: functions
: - deleteAllFiles - deletes all files in a directory
: - deleteFolderAndSubfolders - Takes a String with the path to the directory
:                               which should be deleted and deletes this directory
:                               and everything in it.
: The Script works so, that you need to "whitelist" the files which shouldn't be
: deleted from a certain accessed folder and then deleting every other file in 
: that directory. The whitelisting works in that way, that you need to first
: hide the files you don't want to be deleted, then delete everything in this 
: directory with the function 'deleteAllFiles' and then unhide the whitelisted 
: files again.
: For deleting a whole directory with all contents in it simply call the function
: 'deleteFolderAndSubfolder'
:
: Script needs to be held up to date by adding files which shouldn't be deleted
: and by deleting not needed directories