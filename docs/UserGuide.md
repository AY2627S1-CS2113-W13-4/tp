# User Guide

## Introduction

{Give a product intro}

## Quick Start

{Give steps to get started quickly}

1. Ensure that you have Java 25 or above installed.
1. Down the latest version of `Duke` from [here](http://link.to/duke).

## Features 

{Give detailed description of each feature}

### Adding a todo: `todo`
Adds a new item to the list of todo items.

Format: `todo n/TODO_NAME d/DEADLINE`

* The `DEADLINE` can be in a natural language format.
* The `TODO_NAME` cannot contain punctuation.  

Example of usage: 

`todo n/Write the rest of the User Guide d/next week`

`todo n/Refactor the User Guide to remove passive voice d/13/04/2020`

### Generating and viewing groceries

`generate-list` combines the ingredients of every meal in the active meal plan.
Repeated recipes contribute their ingredients each time they appear. Ingredients with exactly
matching names and units are combined; different units remain separate without conversion.

`view-list` displays the stored result with unchecked markers and the total number of item entries.
Both commands take no arguments. Generate the list before viewing it. Replacing the meal plan
clears the old grocery list, so run `generate-list` again afterwards. Lists last for the current session.

**Integration status:** The grocery commands are wired, but `generate-plan` is not yet implemented
in this checkout. They become usable with planned meals when the meal-plan generator supplies its result.
Until then, `generate-list` reports that there is no active meal plan.

Diagnostic logs go to standard error, separate from the normal command output.

## FAQ

**Q**: How do I transfer my data to another computer? 

**A**: {your answer here}

## Command Summary

{Give a 'cheat sheet' of commands here}

* Add todo `todo n/TODO_NAME d/DEADLINE`
