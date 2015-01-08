TortoiseChecklist
=================

Client side hook for TortoiseSVN for automatic checks (like Checkstyle, FindBugs) and context-sensitive personal checklists (PSP)

## Features

### Seamless integration into the development process

Once configured in TortoiseSVN, TortoiseChecklist will be called at every commit, making sure no checks are forgotten. But it does so without disturbing the development process more than needed: If there are no violations or relevant checklist questions, no user interaction is required at all.

### Violations and questions

Checks in TortoiseChecklist can result in two different kinds of items: "Violations" and "Checklist questions". A violation is something that is (almost) certainly wrong and where a fix is mandatory. A checklist question will be used in cases where "you should have a look at that and check if it is a problem".

### Personal and global checks

There is a global configuration with checks relevant for all commiters in a repository. Additionaly, every commiter can have a personal checklist with checks that are specific to his common errors (see "context information" below) or to test incubating checks.

### Using context information for the checks

Long Checklists with lots of irrelevant questions tend to annoy developers. In TortoiseChecklist, there is a lot of context information that can be used to determine the most relevant questions: The changeset that is to be commited, the person who commits (everybody has different weaknesses), the commit comment, ...

### Javascript-based DSL for the checks

To configure the checks, an easy but powerful JavaScript based DSL is used. In the configuration file, it is specified which checks shall be applied to which files. Example:

    question('JAXB geprüft?').when(pathMatches('**/*.xsd'))
    checkstyle('Java-Checkstyle', 'checkstyle_src.xml').withFilesWhere(pathMatches('**/src/**/*.java'))
    checkstyle('Java-Testcase-Checkstyle', 'checkstyle_test.xml').withFilesWhere(pathMatches('**/test/**/*.java'))
    findbugs('(.+)/src/(.*)\\.java', 'bin').withFilesWhere(pathMatches('**/*.java'))

### Extensible by plugins

TortoiseChecklist (and its DSL) can be extended using Java's ServiceLoader mechanism. All of the basic functionality uses this plugin mechanism, ensuring external plugins are first class citizens. Have a look at the subpackages of 'de.tntinteractive.tortoisechecklist.plugins' for inspiration.

### Riskless introduction

Having checks to ensure high quality commits is important, but sometimes delivering a fix to the customer NOW is more important. There is no risk that TortoiseChecklist will be the reason for delays here: If there is a bug in a check or TortoiseChecklist itself or one of the checks, or if there is consensus to temporarily ignore a violation, the tool itself or single violations can be circumvented. This makes the introduction less risky than server-side commit hooks.

Icons in the GUI come from http://www.aha-soft.com
