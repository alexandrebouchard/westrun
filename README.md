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

- Requires: Java 6+, POSIX, git, Gradle, rsync.
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
touch ``~/.gradle/gradle.properties``
```


Creating an experiments repository
--------

- Create a folder that will contain your experiments, cd inside the folder
- Type ``wrun-init -sshRemoteHost bison.westgrid.ca -codeRepository /path/to/code``, where you should replace ``bison.westgrid.ca`` by the entry point of your westgrid cluster, and ``/path/to/code``, by the path to a git repository containing associated code.

These two steps will create some configurations in ``.westrun`` and some basic directory structure. The format in ``.westrun/config.json`` is pretty self-explanatory, in case you need to change the configuration later on.

If you are using a compiled language such as java, you will need to specify how your code is to be built. 

- Change directory to the root of your code repository
- Type ``wrun-add-build-command``. (This simply creates a file called ``.buildcommands.json``, which defines a list of commands and arguments to execute; by default it contains gradle commands (see below), but it can be modified to support build environments other than gradle.) Note that compilation is currently always done locally (i.e. before sending files off to server). You can use ``wrun-self-build`` from the root of the *code* repository to test your self-building script.

- *Gradle builds*: If the project is currently managed through gradle, the following lines must be added to the ```build.gradle``` file: 

```
apply plugin:'application'
mainClassName = "[mainClass].Main"
```

Sending an experiment to westgrid
----------

The main task to send an experiment is to create a *template*. A template is simply a script in which you can add special macros that are resolved into a cross product. 

``cd -`` back to you experiments repository (note: all commands used in the rest of the tutorial can be invoke in any subdirectory of your experiments repository, not just the root). You can create an example of a draft template by typing ``wrun-template-draft``. This will create a draft execution in the folder ``templates``. See the instructions and configurations in this file.

Optionally, once the template is ready, you can test it via ``wrun-test -template path/to/templateDrafts``, this will run on the server directly, bypassing qsub, running only the first item in the cross product and showing you the output dynamically so that you can see right away if the code can successfully start. **Kill (control-C) early** to avoid hugging server resources.

Finally, start your job using ``wrun-launch -template path/to/template -description Some description``. Run ``wrun-sync`` when you want to sync up the contents of the experiments repository with the remote (to see results of experiments).

Once in a while, when no experiments are running, it is a good idea to run ``wrun-clean``, which will remove unnecessary files and save disk space.
