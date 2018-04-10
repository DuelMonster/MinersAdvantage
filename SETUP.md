## Welcome to MinersAdvantage!

[Compiling](#compiling-minersadvantage) - For those that want the latest unreleased features.

[Contributing](#contributing) - For those that want to help out.

### Compiling MinersAdvantage
IMPORTANT: Please report any issues you have as there might be some problems with the documentation.
Also make sure you know EXACTLY what you're doing before proceeding!  We are not responsible if your computer crashes, becomes corrupted, etc. :see_no_evil:
***
Contents:
- [Setup Java](#setup-java)
- [Setup Gradle](#setup-gradle)
- [Setup Git](#setup-git)
- [Setup MinersAdvantage](#setup-minersadvantage)
- [Compile MinersAdvantage](#compile-minersadvantage)
- [Updating Your Repository](#updating-your-repository)
***
#### Setup Java
The Java JDK is used to compile MinersAdvantage.

1. Download and install the Java JDK.
	* [Download link](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  Scroll down, accept the `Oracle Binary Code License Agreement for Java SE`, and download the appropriate version for your OS (if you have a 64-bit OS, please download the 64-bit version).
	* Linux: Installation methods for certain popular flavors of Linux are listed below.  If your distribution is not listed, follow the instructions specific to your package manager or install it manually.
		* Gentoo: `emerge dev-java/oracle-jdk-bin`
		* Archlinux: `pacman -S jdk7-openjdk`
		* Ubuntu/Debian: `apt-get install openjdk-7-jdk`
		* Fedora: `yum install java-1.7.0-openjdk`
2. Windows: Set environment variables for the JDK.
    * Go to `Control Panel\System and Security\System`, and click on `Advanced System Settings` on the left-hand side.
    * Click on `Environment Variables`.
    * Under `System Variables`, click `New`.
    * For `Variable Name`, input `JAVA_HOME`.
    * For `Variable Value`, input something similar to `C:\Program Files\Java\jdk1.7.0_51` (or wherever your Java JDK installation is), and click `Ok`.
    * Scroll down to a variable named `Path`, and double-click on it.
    * Append `;%JAVA_HOME%\bin` EXACTLY AS SHOWN and click `Ok`.  Make sure the location is correct; double-check just to make sure.
3. Open up your command line and run `javac`.  If it spews out a bunch of possible options and the usage, then you're good to go.  If not, either try the steps again or Google a solution for your problem(s).

#### Setup Gradle
Gradle is used to execute the various build tasks when compiling MinersAdvantage.

1. Download and install Gradle.
	* [Windows/Mac download link](http://www.gradle.org/downloads).  You only need the binaries, but choose whatever flavor you want.
		* Unzip the package and put it wherever you want, eg `C:\Gradle`.
	* Linux: Installation methods for certain popular flavors of Linux are listed below.  If your distribution is not listed, follow the instructions specific to your package manager or install it manually [here](http://www.gradle.org/downloads).
		* Gentoo: `emerge dev-java/gradle-bin`
		* Archlinux: `pacman -S gradle`
		* Ubuntu/Debian: `apt-get install gradle`
		* Fedora: Install Gradle manually from its website (see above), as Fedora ships a "broken" version of Gradle.  Use `yum install gradle` only if you know what you're doing.
2. Windows: Set environment variables for Gradle.
	* Go back to `Environment Variables` and then create a new system variable.
	* For `Variable Name`, input `GRADLE_HOME`.
	* For `Variable Value`, input something similar to `C:\Gradle-1.11` (or wherever your Gradle installation is), and click `Ok`.
	* Scroll down to `Path` again, and append `;%GRADLE_HOME%\bin` EXACTLY AS SHOWN and click `Ok`.  Once again, double-check the location.
3. Open up your command line and run `gradle`.  If it says "Welcome to Gradle [version].", then you're good to go.  If not, either try the steps again or Google a solution for your problem(s)..

#### Setup Git
Git is used to clone MinersAdvantage and update your local copy.

1. Download and install Git [here](http://git-scm.com/download/).
	* *Optional*: Download and install a Git GUI client, such as Github for Windows/Mac, SmartGitHg, TortoiseGit, etc.  A nice list is available [here](http://git-scm.com/downloads/guis).

#### Setup MinersAdvantage
This section assumes that you're using the command-line version of Git.

1. Open up your command line.
2. Navigate to a place where you want to download MinersAdvantage's source (eg `C:\Github`) by executing `cd [folder location]`.  If choosing a location other than `C:\Github`, just remember that when following the instructions.
3. Execute `git clone https://github.com/DuelMonster/MinersAdvantage.git`.  This will download MinersAdvantage's source into an `MinersAdvantage` folder
such as `C:\Github\MinersAdvantage`.
4. Right now, you should have a directory that looks something like:

***
	Github
	\-MinersAdvantage
		\-MinersAdvantage's files (should have `build.gradle`)
***

#### Compile MinersAdvantage
1. Execute `gradle setupCiWorkspace` in order to set up Forge and download the necessary libraries to build MinersAdvantage.  This might take some time, please be patient :watch:.
	* You will generally only have to do this when the Forge version in `build.properties` changes.
2. Execute `gradle build`. If you did everything right, `BUILD SUCCESSFUL` will be displayed after it finishes.  This should be relatively quick.
    * If you see `BUILD FAILED`, check the error output (it should be right around `BUILD FAILED`), fix everything (if possible), and try again.
3. Navigate to `C:\Github\MinersAdvantage\build\libs`.
    *  You should see a `.jar` file named `MinersAdvantage-x.y.z-#.jar`, where x.y.z is the Minecraft version number and # is the mod version.
		* NOTE: `null` means that you are missing a `build_number` value in `build.properties` or that your CI environment is set up incorrectly-it is totally safe to ignore.
4. Copy the jar into your Minecraft mods folder, and you are done!

#### Updating Your Repository
In order to get the most up-to-date builds, you'll have to periodically update your local repository.

1. Open up your command line.
2. Navigate to wherever you cloned MinersAdvantage in the console.
3. Make sure you have not made any changes to the local repository, or else there might be issues with Git.
	* If there are local changes, try reverting them to the status that they were when you last updated your repository by executing `git reset HEAD --hard`.
4. Execute `git pull master`.  This pulls all commits from the official repository that do not yet exist on your local repository and updates it.

### Contributing
***
#### Submitting a PR
So you found a :bug: in the code?  Think you can make it more efficient :dash:?  Want to help in general?  Great!

1. If you haven't already, create a Github account.
2. Click the `Fork` icon located at the top-right of this page (below your username).
3. Make the changes that you want to and commit them.
	* If you're making changes locally, you'll have to execute `git commit -a` and `git push` in your command line.
4. Click `Pull Request` at the right-hand side of the gray bar directly below your fork's name.
5. Click `Click to create a pull request for this comparison`, enter your PR's title, and create a detailed description telling DuelMonster what you changed.
6. Click `Send pull request`, and wait for feedback! :boom:

#### Creating an Issue
Experienced a crash :bomb:?  Have a suggestion?  Found a :bug:?  Create an issue now!

1. Make sure your issue hasn't already been answered or fixed by [searching for it](https://github.com/DuelMonster/MinersAdvantage/search?q=&type=Issues).  Also think about whether your issue is a valid one before submitting it.
	* Please do not open an issue to ask a question-that is for [DuelMonster's Twitter](https://twitter.com/DuelMonster/) or the [forums](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2372329-minersadvantage-making-mining-easier).
2. Go to [the issues page](https://github.com/DuelMonster/MinersAdvantage/issues).
3. Click `New Issue` right below `Star` and `Fork`.
4. Enter your Issue's title (something that summarizes your issue), and then create a detailed description ("Hey DuelMonster, could you add/change xxx?" or "Hey, found a problem with ...").
	* If you are reporting a bug report from an unofficial version, make sure you include the following:
		* Commit SHA (usually located in a changelog or the jar name itself)
		* ForgeModLoader log
		* Server log if applicable
		* Detailed description of the bug and pictures if applicable
5. Click `Submit new issue`, and wait for feedback! :boom:
