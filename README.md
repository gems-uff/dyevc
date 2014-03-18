#About

Dye over Version Control (DyeVC) is an analogy to the usage of dye over cells to visualize the mitosis process. This way, we aim at visualizing how clones of Distributed Version Control Systems (DVCS) evolve and at supporting users on controlling this evolution.

DyeVC is a non-obtrusive application. It lies on the system tray bar, where alerts are shown whenever the local configured repositories are ahead or behind the remote repositories. Local repositories to be monitored can be registered through the application interface, and are stored as user preferences, independently of operating system. Information regarding monitoring is given in different levels of detail, from a high level topology overview, (showing all known clones of a system, their relationships and how advanced or delayed they are between each other), through the state of each tracked branch regarding their origin, until each commit in the repository, where one can see not only local commits, but all known commits in all nodes where dyevc is running or that are related to a node where dyevc is running.

This project was proposed by professor **Leonardo Murta** and first version was implemented by his M. S. student **Cristiano Cesario**, as part of his dissertation project in the Software Engineering postgraduate course at Universidade Federal Fluminense.

This is one the several research projects conducted by the **Software Maintenance and Evolution Group - GEMS** (Acronym taken from the Portuguese group name: Grupo de Evolução e Manutenção de Sofware). For more information regarding this and other research projects conducted by the group, refer to the [GEMS website](http://gems.ic.uff.br/).

#Team

* Leonardo Gresta Paulino Murta (joined in Aug 2012)
* Cristiano Cesario (joined in Aug 2012)
* Wallace Ribeiro (joined in Mar 2013)

#Download

DyeVC does not need to be formally installed. As it uses [Java Web Start technology](http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/), all you have to do is point your browser to [this link](http://www.ic.uff.br/~ccesario/DyeVC/DyeVC.jnlp) or click the button below and the application will automatically download and execute, creating a shortcut in your computer to ease future access.

[![Launch DyeVC](DyeVC/src/main/resources/LaunchDyeVCButton.png "Launch DyeVC")](http://www.ic.uff.br/~ccesario/DyeVC/DyeVC.jnlp)

#Documentation

* [Article presented at the 1st Brazilian Workshop on Software Visualization, Evolution and Maintenance](http://www.ic.uff.br/~ccesario/DyeVC_VEM2013.pdf)
* [Wiki](https://github.com/gems-uff/dyevc/wiki)

#Development

* [Source Code](https://github.com/gems-uff/dyevc)
* [Issue Tracking](https://github.com/gems-uff/dyevc/issues)

#Technologies

* [Java Web Start](http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/)
* [JUNG](http://jung.sourceforge.net/)
* [JGit](http://www.eclipse.org/jgit/)
* [Mongolab](https://mongolab.com/welcome/)

#Information Usage

DyeVC collects non-private information to be used solely for experimental purposes. No file contents are collected nor analyzed by the application. The following is a list of the information collected.

* For repositories:
    * Hostname where the repository is located.
    * Path where the repository is located.
    * Relations between repositories (push / pull list).
* For commits:
    * Date and time when the commit was done.
    * Name of committer.
    * Repositories where the commit is found.
    * Commit predecessors (parents).
    * Commit message.

#License

Copyright (c) 2013 Universidade Federal Fluminense (UFF)  
  
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  
  
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  
  
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
