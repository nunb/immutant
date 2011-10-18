Requirements
------------
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml


Dependencies
------------

Immutant depends on polyglot: https://github.com/projectodd/jboss-polyglot

Polyglot is published as a snapshot, so you may need to run mvn with the
-U option to check for updates if you run into build issues.

Building
--------

Install the project using the provided settings.xml:

    mvn -s support/settings.xml install

If you will be building the project often, you'll want to
create/modify your own ~/.m2/settings.xml file.

If you're a regular JBoss developer, see:

* http://community.jboss.org/wiki/MavenGettingStarted-Developers

Otherwise, see: 

* http://community.jboss.org/wiki/MavenGettingStarted-Users

Once your repositories are configured, simply type:

    mvn install


SLIME/SWANK Integration
-----------------

If you are using emacs, you can fire up swank via:
  
    mvn clojure:swank
    
Install slime from marmalade via the instructions at https://github.com/technomancy/swank-clojure
under 'Connecting with SLIME'. Install clojure-mode, and connect with:

    M-x slime-connect
