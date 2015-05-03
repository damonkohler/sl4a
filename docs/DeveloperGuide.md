If you are interested in understanding the internals of Android Scripting Environment, building from source, or contributing ideas or modifications to the project, then this document is for you.

# The Scripting Layer for Android Community #

The SL4A community exists primarily through the [discussion group](http://groups.google.com/group/android-scripting), the issue tracker and, to a lesser extent, the source control repository. You are definitely encouraged to contribute to the discussion and you can also help us to keep the effectiveness of the group high by following and promoting the guidelines listed here. If you're looking for a place to start, the issue tracker has a [low-hanging fruit](http://code.google.com/p/android-scripting/issues/list?q=label:LowHangingFruit) label.

## Please Be Friendly ##

Showing courtesy and respect to others is a vital part of the Google culture, and we strongly encourage everyone participating in SL4A development to join us in accepting nothing less. Of course, being courteous is not the same as failing to constructively disagree with each other, but it does mean that we should be respectful of each other when enumerating the 42 technical reasons that a particular proposal may not be the best choice. There's never a reason to be antagonistic or dismissive toward anyone who is sincerely trying to contribute to a discussion.

Writing scripts on Android is a lot of fun. Let's keep it that way. Let's strive to be one of the friendliest communities in all of open source.

## Where to Discuss SL4A ##

As always, discuss SL4A in the official SL4A discussion group. You don't have to actually submit code in order to sign up. Your participation itself is a valuable contribution.

# Working with the Code #

If you want to get your hands dirty with the code inside SL4A, this is the section for you.

## Checking Out the Source from Mercurial ##

Checking out the SL4A source is most useful if you plan to tweak it yourself. You can check out the source for SL4A using a Mercurial client as you would for any other project hosted on Google Code. Please see the instruction on the source code access page for how to do it.

## Compiling from Source ##

See [Compiling SL4A](CompilingASE.md).

## Testing ##

Tests should be written for any new code, and changes should be verified to not break existing tests before they are submitted for review. To perform the tests, install SL4A to your phone or emulator, install Python, run the supplied test script, and verify that there are no failures.

# Contributing Code #

We are excited that SL4A is open source, and hope to get great patches from the community. Before you fire up your favorite IDE and begin hammering away at that new feature, though, please take the time to read this section and understand the process. While it seems rigorous, we want to keep a high standard of quality in the code base.

## Contributor License Agreements ##

You must sign a Contributor License Agreement (CLA) before we can accept any code. The CLA protects you and us.

  * If you are an individual writing original source code and you're sure you own the intellectual property, then you'll need to sign an [individual CLA](http://code.google.com/legal/individual-cla-v1.0.html).
  * If you work for a company that wants to allow you to contribute your work to SL4A, then you'll need to sign a [corporate CLA](http://code.google.com/legal/corporate-cla-v1.0.html).

Follow either of the two links above to access the appropriate CLA and instructions for how to sign and return it.

## Coding Style ##

To keep the source consistent, readable, diffable and easy to merge, we use a fairly rigid coding style, as defined by the [Android project style guide](http://source.android.com/source/code-style.html) (with a few modifications and additions listed below). All patches will be expected to conform to the style outlined here.

  * Use 2-space indents, not 4.
  * Avoid unnecessary vertical whitespace.
  * Todos should be formatted as ` // TODO(username): Write some code! `

SL4A combines code written in several languages. Code written for SL4A should conform to the style guidelines defined by the [google-styleguide](http://code.google.com/p/google-styleguide/) project.

## Submitting Patches ##

Please do submit code. Here's what you need to do:

Normally you should make your change against a server-side clone of trunk. Decide which code you want to submit. A submission should be a set of changes that addresses one issue in the ASE issue tracker. Please don't mix more than one logical change per submittal, because it makes the history hard to follow. If you want to make a change that doesn't have a corresponding issue in the issue tracker, please create one.

Also, coordinate with team members that are listed on the issue in question. This ensures that work isn't being duplicated and communicating your plan early also generally leads to better patches.

Ensure that your code adheres to the SL4A source code style.

Ensure that there are tests for your code.

The current members of the SL4A engineering team are the only committers at present. In the great tradition of eating one's own dogfood, we will be requiring each new SL4A engineering team member to earn the right to become a committer by following the procedures in this document, writing consistently great code, and demonstrating repeatedly that he or she truly gets the zen of SL4A.

# Release Process #

SL4A is currently released weekly on Thursday unless something is blocking the release. Release notes are published to [damonkohler.com](http://www.damonkohler.com/search/labels/ase). The source tree is tagged with rXX, where XX is the release number, at each release.

## Release Checklist ##

  1. Run AllTests suite against Cupcake.
  1. Check release notes to determine if any interpreter packages need to be updated. If so:
    1. Update interpreter version numbers.
    1. Create new archives for updated interpreters.
    1. Copy new archives to SD card.
    1. Verify new interpreter archives install successfully.
  1. Run Python integration test.
  1. Run Perl integration test.
  1. Increase version numbers in AndroidManifest.xml
  1. Run `hg tag rXX`.
  1. Export new APK.
  1. Rebuild template archives (ScriptForAndroidTemplate and InterpreterForAndroidTemplate).
  1. Upload new interpreter archives (if any).
  1. Deprecate old interpreter archives (if any).
  1. Upload new APK.
  1. Deprecate old APK.
  1. Update home page download link and QR code.
  1. Update online API documentation.
  1. Post release notes.
  1. Email user group.