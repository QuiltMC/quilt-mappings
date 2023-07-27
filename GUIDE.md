# Editing mappings

## Starting up

After running a mappings task (either `mappings` or `mappingsUnpicked` via Gradle), you'll be greeted with this imposing Swing GUI:
![An image of enigma's main window, devoid of life. The "deobfuscated classes", "obfuscated classes", and "structure" dockers are open on the sides, and nothing is displayed in the centre of the screen.](https://github.com/ix0rai/quilt-mappings/assets/66223394/fc569c2c-7b6e-4d22-9199-dd9b52afd8ca)

The first thing you want to draw your attention to is the left of the screen, where you'll find the "Obfuscated Classes" and "Deobfuscated Classes" [dockers](#docker-guide).
Obfuscated Classes will contain classes where the top-level name remains unmapped, and "Deobfuscated Classes" the mapped ones.
Alternatively, you can use the merged "Classes" docker to view all classes at once, by pressing the button on the far left.
Dockers can be resized and their titles dragged around, so you can find the best setup for you!

All three dockers on the left serve the same purpose: showing you the classes in the project, just like you'd see in your IDE.
Each class has a little icon to show how completely it's mapped: a green check for done, a yellow dot for in progress, and a red x for untouched.
Double-click on packages and classes to open them. You can also right-click to see a context menu with some shortcuts to help you map faster!
When you open a class, it will be decompiled and the source code shown in the main view in the center.
You'll immediately notice a couple of things:
1. Some names, or *tokens*, are highlighted in red, green, or grey. Red means a name isn't mapped, green means it *is* mapped, and grey means it's been
   automatically mapped by our [enigma plugin](https://github.com/QuiltMC/quilt-enigma-plugin). Don't be afraid of editing green or grey names,
   there's always something better to find!
2. The file is in a tab: Enigma supports having multiple files decompiled and ready to access at once. Nice!
3. The "structure" docker on the right has updated! We'll explain how to use that soon.

## Naming

The process of naming boils down to four steps:
1. Read the relevant code. The first step in coming up with a name should always be reading and brainstorming your own names.
   For every type of token, you can use the "show calls" tool in the right-click menu to find usages if you're lacking in code to assess.
2. Once you've applied your new name, reread some code! Taking a look at the code with the name you invented will help you get a good idea of the quality of your name.
3. Cross-reference your name. QM mappers have access to Yarn and Mojmap to compare their names against, using tools like [Linkie](https://linkie.shedaniel.me/mappings)
   and [WagYourTail's mapping viewer](https://wagyourtail.xyz/Projects/MinecraftMappingViewer/App). If any other name contradicts yours, triple-check your name to make
   sure you haven't made a mistake! After checking the other names, hybridize, copy, or keep your original name to find the best possible option.
4. Consider Javadoc. If your name is unclear, and you can't improve it without making it too wordy, throw in some Javadoc to make sure modders know
   what they're looking at.

### Tips: naming fields

With fields, "show calls" is your best friend. You'll want to run through many of these before finalising your decision.
Constants will often appear to have no usages, which is misleading: the compilation has inlined its value instead of using a reference.
Use `ctrl + f` to find what you're looking for, and reference the Mojang name since Yarn will often fail to map these.

### Tips: naming classes

Class names should come with plenty of deliberation, given that they're the most visible of all.
Make sure to use the "show inheritance" and "show implementations" tools on right-click to make sure the name you're choosing is appropriately broad or narrow.
To use the example from the [conventions](CONVENTIONS.md), if a block class is only used to disable spawning, but has only one usage and no inheritors, it shouldn't be named `NoSpawningBlock`, it should be named after its usage: `BedrockBlock`.
When considering which package to put a class in, either use an existing one or try to ensure there are 3+ classes that fit the name when creating a new package.

### Tips: naming methods

With methods, the whole world of the context menu is open to you. Make sure to use all its options to sort through the references!
Assume that the user knows the return type and the parameters when coming up with a name.

### Tips: naming parameters

Parameters can be tough to name, since Mojmap doesn't ship their names for us to reference. Be wary of differences between Yarn and QM making their name incompatible:
if Yarn has a different name for the parameter's class, you can't be inspired by that name and must come up with your own.
Be sure to look at every reference in its method to make sure you understand it!

## Docker Guide

In Enigma, a docker is a panel, docked on either the left or right, that displays information.
Enigma has 9 built-in dockers to help you map: Classes, Deobfuscated Classes, Obfuscated Classes, Collab, Notifications, Structure, Inheritance, Implementations, and Calls.

### Classes

The Classes docker displays all classes found in the project. It serves the purpose of both the Deobfuscated Classes and Obfuscated Classes panel at
the same time!

### Obfuscated Classes

The Obfuscated Classes docker shows all classes whose *top level* class name is unmapped. This means they can contain mapped tokens, but still lack
the name of the containing class. Very useful for finding targets to map!

### Deobfuscated Classes

The Deobfuscated Classes dockers displays classes with those that are missing a top-level name filtered out.

### Collab

The Collab docker shows off Enigma's server feature: you can start a server to map together with your friends!
When you're offline, it simply shows buttons to either set up or join a server.
Once a server has been started, it shows a list of currently connected users as well as a chat box that can be used to collaborate.

### Notifications

The Notifications docker displays a history of notifications that enigma has sent you. This can include server chat, name collisions, project opens,
name warnings, etc.!

### Structure

The Structure docker is highly useful for assessing how a class works. It provides a condensed view of the class, with a few filter options
to help you find unmapped items and examine the class. The entries in the tree can be double-clicked to navigate to them.

### Inheritance

When you use "show inheritance" in the context menu, this docker creates a tree showing you what it's found.
The entries in the tree can be double-clicked to navigate to them.

### Calls

Similarly to the Inheritance docker, the Calls docker shows a tree of found calls when you use the "show calls" tool.
The entries in the tree can be double-clicked to navigate to them.

### Implementations

The third of the trio, the Implementations docker shows a tree of found implementations after clicking the "show implementations" tool.
The entries in the tree can be double-clicked to navigate to them.

## Tools

Enigma has a few tools to help you map faster, which we haven't yet talked about. These include:

### Stats

The Stats tool shows you a breakdown of how far along you are with mapping your project. It's found under `File -> Mapping Stats`
in the menu bar, and supports filtering by package to find out how well covered your *specific* bit of the project is.

Additionally, you'll see a "Generate Diagram" button in the bottom left corner. Click the checkboxes next to each token type, and
they'll be added to a diagram that shows mapping stats per-package. Clicking the button opens the diagram in your web browser, and you can
click each package to see it and its children closer.

### Search

Pressing `ctrl + shift + f` will open a search tool, where you can find tokens that match your given query.
It supports fields, methods, and classes, with checkboxes to mix and match what you're searching for.
Classes are given priority, then methods, then fields.

### Different decompilers

Enigma supports decompilation with a variety of different decompilers, including CFR, Vineflower, Procyon and raw bytecode.
You can change the decompiler used in the menu bar, under `Decompiler`.
We recommend to stick with Vineflower as your primary decompiler, but you can swap to others if it fails to decompile or to
see if they produce more readable code for you!

### Keybinds

Enigma supports customising every keybind in its user interface! There's also support for assigning the same action
to multiple keys. Go to `File -> Configure Keybinds` in the menu bar to change them. There are also many keybinds
that don't come with an assignment by default, so be sure to explore!

### Themes

Enigma supports customising its theme, with a few built-in options. Yes, this includes a dark mode.
You can change the theme in the menu bar, under `Theme`.