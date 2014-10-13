Summary [![Build Status](https://travis-ci.org/alexandrebouchard/westrun.png?branch=master)](https://travis-ci.org/alexandrebouchard/westrun)

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

- Requires: Java 8+, POSIX, git, Gradle 2.1+, rsync.
- Setup a password-less connection to the remote host.
- Clone this repo and compile using ``gradle installApp``.
- Add the folder ``scripts`` to your PATH variable. If you use mac, you can use TextEdit to add it by adding the following line:
  ``open -a TextEdit .bash_profile`` and copy this line ``export PATH=$PATH:/path/to/scripts`` to ``.bash_profile``.


**Strongly recommended:** to speedup builds, create or (add to)  the file ``~/.gradle/gradle.properties`` and add the following line: 
```
org.gradle.daemon=true
```
To create ``~/.gradle/gradle.properties``, open terminal and add the following lines:
```
touch ~/.gradle/gradle.properties
```


Creating an experiments repository
----------------------------------

- Create a folder that will contain your experiments, cd inside the folder
- Type ``wrun-init -sshRemoteHost bison.westgrid.ca -codeRepository /path/to/code``, where you should replace ``bison.westgrid.ca`` by the entry point of your westgrid cluster, and ``/path/to/code``, by the path to a git repository containing associated code.

These two steps will create some configurations in ``.westrun`` and some basic directory structure. The format in ``.westrun/config.json`` is pretty self-explanatory, in case you need to change the configuration later on.

If you are using a compiled language such as java, you will need to specify how your code is to be built. 

- Change directory to the root of your code repository
- Type ``wrun-add-build-command``. (This simply creates a file called ``.buildcommands.json``, which defines a list of commands and arguments to execute; by default it contains gradle commands (see below), but it can be modified to support build environments other than gradle.) Note that compilation is currently always done locally (i.e. before sending files off to server). You can use ``wrun-self-build`` from the root of the *code* repository to test your self-building script.

- *Gradle builds*: If the project is currently managed through gradle, the following lines must be added to the ```build.gradle``` file: 

```
apply plugin:'application'
mainClassName = "package.MainClass"
```

where ideally you should replace ``package.MainClass`` by a fully qualified name to the main java class point of entry of your program, but this is not strictly mandatory (this is just used to force gradle to download all the dependencies, you can still launch class files other than the one specified here).

Sending an experiment to westgrid
---------------------------------

The main task to send an experiment is to create a *template*. A template is simply a script in which you can add special macros that are resolved into a cross product. 

``cd -`` back to you experiments repository (note: all commands used in the rest of the tutorial can be invoke in any subdirectory of your experiments repository, not just the root). You can create an example of a draft template by typing ``wrun-template-draft -name nameOfTemplate`` where ```nameOfTemplate``` is replaced with a string describing the template. This will create a draft plan of execution in the folder ``plans``. Next, see the instructions and configurations in this file.

Optionally, once the template is ready, you can test it via ``wrun-test -template path/to/templateDrafts``, this will run on the server directly, bypassing qsub, running only the first item in the cross product and showing you the output dynamically so that you can see right away if the code can successfully start. **Kill (control-C) early** to avoid hugging server resources.

Finally, start your job using ``wrun-launch -template path/to/template -description Some description``. Note that this creates copies of your source code, so feel free to keep editing the source code while the code is running on server (more precisely, as soon as the command wrun-launch is completed).


Getting results back
--------------------

Run ``wrun-sync`` when you want to sync up the contents of the experiments repository with the remote (to see results of experiments).

All the results are stored in ``results``. However, a more convenient way to access them is to look at ``plans/[name-of-plan]-results/latest/job-results/``, which will contain symlinks to the individual workers' result folders. You can also find in ``plans/[name-of-plan]-results/latest/executionInfo/inputs/templateFile`` a copy of the template used to launch this job (in general, westrun is organized so that you do not necessarily have to version control the experiments directory: things are saved and organized for you).

Once in a while, when no experiments are running, it is a good idea to erase unnecessary result directories and then run ``wrun-clean``, which will push these deletions to the server.


Appendix
---------

Note that large files can be added in ```.westrun/syncignore`` to avoid syncing issues.

For example, patterns such as ```/**/tmp``` can be used.
