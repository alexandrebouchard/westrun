Summary [![Build Status](https://travis-ci.org/alexandrebouchard/westrun.png?branch=master)](https://travis-ci.org/alexandrebouchard/westrun)

-------

Westrun contains utilities to start jobs on westgrid (in particular, java jobs, but could be useful for other types of jobs as well). It could also be useful in other 
scientific cluster environments. See [alternative setups](#alt-setups) for additional details.

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

Finally, start your job using ``wrun-launch -template path/to/template -why Some description of why you ran these experiments``. Note that this creates copies of your source code, so feel free to keep editing the source code while the code is running on server (more precisely, as soon as the command wrun-launch is completed).


Getting results back
--------------------

Run ``wrun-sync`` when you want to sync up the contents of the experiments repository with the remote (to see results of experiments).

All the results are stored in ``results``. However, a more convenient way to access them is to look at ``plans/[name-of-plan]-results/latest/job-results/``, which will contain symlinks to the individual workers' result folders. You can also find in ``plans/[name-of-plan]-results/latest/executionInfo/inputs/templateFile`` a copy of the template used to launch this job (in general, westrun is organized so that you do not necessarily have to version control the experiments directory: things are saved and organized for you).

Once in a while, when no experiments are running, it is a good idea to erase unnecessary result directories and then run ``wrun-clean``, which will push these deletions to the server.

Note that large files can be added in ``.westrun/syncignore`` to avoid syncing issues.
For example, patterns such as ``/**/tmp`` can be used.

Analyzing results
-----------------

To search over experiments, start by typing ``wrun-search``. You will see what fields are available for search in the current experiments repository. For example, if you are want to see the git commit used to the experiments, use:

```
wrun-search -select folder_location,git_commit
```

As another example, say you want to keep only the executions where the code was ran clean (i.e. when there were not pending files in the repo). To do this, you could use:

```
wrun-search -select folder_location,git_commit,dirty_files -where "dirty_files='false'"
```

You can also use ``-selectAll`` to show all, and you can disable the header line with ``-showHeader false``.

Note that constraint and select can use any SQL syntax (they are just turned into select and where clauses respectively).

When using ``wrun-search`` in the above fashion, we can only access to the part of the execution that does not change across the execution (this is done for efficiency reason, so that it can be cached easily). To consolidate the results of the experiments, we will use ``wrun-search`` in conjunction with ``wrun-collect``. The command ``wrun-collect`` has the same arguments as ``wrun-search``, but in addition, it gives you the possibility of specifying output files relative to each execution folder. These are consolidated into one big database. 

Let us start with a simple example: monitoring the status of the runs associated with a certain plan. To print out the end times of all the runs created by a certain plan, we would just use:

```
wrun-collect -where "plan like '%'" -simpleFiles executionInfo/end-time.txt | wrun-search -pipe -select "folder_location,end_time"
```

Here, ``%`` will match any string, this could be replaced by a specific plan.
This will loop over the matched plans, and for each of these, add the contents of the file executionInfo/end-time.txt as a new column (the column name, end_time, is obtained by taking the file name, striping the extension, and replacing non-alphanumeric characters by underscores). This outputs to stdout a database created on the fly. To instruct wrun-search to obtain the database from stdin instead of from the default database, we add ``-pipe`` to wrun-search.

Since this type of queries is often useful in practice, a shortcut for the above command is available under ``wrun-status %`` (replace % by a plan for faster performance). See that file under the directory ``scripts`` to see an example of how to do more advanced selection and formatting using wrun-search and wrun-collect.

The following files can be handled by ``wrun-collect`` in each result directory:

- ``-csvFile``: comma separated file.
- ``-mapFiles``: tab separated key values, one per line. Accepts a list of ``mapFiles`` to collect.
- ``-simpleFiles``: a file where the key is the file name (extension-stripped, and all non-standard characters transformed to ``_``), and the value is the contents. Accepts a list of ``simpleFiles`` to collect.

Alternative setups <a name="alt-setups"></a>
-----------

The default configuration for seamlessly running and analyzing jobs with Westrun is to package Java projects with Gradle. Moreover, the Java jobs should be instrumented runs handled with ```briefj.run.Mains.instrumentedRun```. 

However, most of Westrun's functionality can be recovered by simply saving results to a specific directory and options to a specific file within that directory. Each job should store an options.map file in the directory ``results/all/@{individualExec.getName()}/executionInfo/`` and results should be saved to ``results/all/@{individualExec.getName()}/``. This is easily achieved by passing the system environment variable ``SPECIFIED_RESULT_FOLDER=results/all/@{individualExec.getName()}`` as in the draft template file. 

By passing ``SPECIFIED_RESULT_FOLDER``, Westrun can be used for different types of jobs (e.g. R or Python). To run on different scientific cluster environments (eg. without qsub systems) a simple alternative is to submit jobs via ``nohup job.sh &``. This is built-in via 
``wrun-launch -template path/to/template -why Some description of why you ran these experiments -noQsub``.

Furthermore, note that Westrun can be used locally as well. This may be useful in cases where it is difficult to package applications for different servers (eg. python dependencies) or for small jobs where Westrun's organization and analysis tools are still desired. To run locally enable ssh connections and initialize as ``wrun-init -sshRemoteHost localhost -codeRepository /path/to/code``. It is recommended that you setup passwordless local login ``cat ~/.ssh/id_rsa.pub | ssh localhost 'cat >> ~/.ssh/authorized_keys'``. If running locally use the ``-noQsub`` argument when launching new experiments.

