Summary
-------

Westrun contains utilities to start jobs on westgrid (in particular, java jobs, but could be useful for other types of jobs as well). It could also be useful in other 
scientific cluster environments.

The basic idea is to setup one or several experiments repositories (each linked with a
code repository). From within each of these experiments repo, you can:

- Prepare cross product jobs (i.e. try all combinations of various command line arguments)
- Send these to a remote queue (westrun takes care of creating a copy of the code for each group of runs, synchronizing the contents of the directory to the remote, submitting to queue.
- Retrieve and organize results.

Installation
------------

- Requires: Java 6+, a UNIX environment, Gradle, rsync.
- Setup a password-less connection to the remote host.
- Clone this repo and compile using ``gradle installApp``.
- Add the folder ``scripts`` to your PATH variable. 

**Strongly recommended:** to speedup builds, create or (add to)  the file ``~/.gradle/gradle.properties`` and add the following line: 
```
org.gradle.daemon=true
```


Creating an experiments repository
--------

- Create a folder that will contain your experiments, cd inside the folder
- Type ``wrun-setup -sshRemoteHost bison.westgrid.ca -codeRepository /path/to/code``, where you should replace ``bison.westgrid.ca`` by the entry point of your westgrid cluster, and ``/path/to/code``, by the path to a git repository containing associated code.

These two steps will create some configurations in ``.westrun``. The format in ``.westrun/config.json`` is pretty self-explanatory, in case you need to change the configuration later on.

If you are using a compiled language, you will need to specify how your code is to be built. 

- Change directory to the root of your code repository
- Type ``wrun-add-build-command -commandName gradle -commandArguments installApp`` if you are using the gradle build system. Replace ``gradle`` and ``installApp`` by the command and argument of your choice if you are using another build system.
- If needed, you can use ``wrun-add-build-command`` several times with different commands and arguments if a sequence of commands need to be executed.

This simply creates a file called ``.buildcommands.json``, which can be easily modified to change configurations later on. In complex cases, have a look at it to make sure it is right (for example, that there are no duplicate entries and that the order is right). You can test your build with ``wrun-self-build`` called from the root of the code repository. Note that compilation is currently always done locally.


Sending an experiment to westgrid
----------

The main task to send an experiment is to create a *template*. A template is simply a script in which you can add special macros that are resolved into a cross product. 

``cd -`` back to you experiments repository. You can create an example of a draft template by typing ``wrun-create-draft -templateInit java``. This will create a draft execution in the folder ``run-template-drafts``. You can rename this file  See the remaining instructions and configurations in this file.

