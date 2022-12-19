# Contributing

Our goal is to provide high-quality names that make intuitive sense. In our experience, this goal is best achieved
through contribution of original names, rather than copying names from other mappings projects.
While we believe that discussions relating to the names used by other mappings projects can be useful, those names
must stand up to scrutiny on their own - we won't accept names on the grounds that they're present in other mappings
projects. 

We recommend discussing your contribution with other members of the community - either directly in your pull request,
or in our other community spaces. We're always happy to help if you need us!

## Guide: Pull Requests

1. ### Look at the conventions
    Before you start changing or adding mappings, please have a look at the [naming conventions](/CONVENTIONS.md);
    you don't have to memorize them, and you can always review them at any time, but you should apply them when 
    possible. Be sure to talk to us about any questions you might have (either through an issue or on
    [Discord](https://discord.quiltmc.org/toolchain)).
2. ### Open your PR and wait for reviews
    Once you have forked QM and opened a pull request, you need to wait for people to review it. When you get reviews, 
    try to thoughtfully address any concerns other people have. If you get confused, be sure to ask questions!
3. ### Entering a Final Comment Period
    Once your PR has no "changes requested" reviews, the minimum number of approvals for its
    [triage category](#guide-triage-categories), and nobody has their review requested, it is eligible to enter a 
    Final Comment Period (FCP). An FCP is a last call for any reviewers to look at your PR before it is merged.
    The minimum length of your PR's FCP is determined by its triage category, but if any further changes are
    requested, the FCP might be lengthened, or if the concerns are significant, the FCP cancelled until the concerns
    are addressed and resolved.
4. ### Request a merge!
    Once the minimum time on the Final Comment Period has passed, and you have resolved any concerns reviewers have
    raised during that time, leave a comment on your PR requesting for it to be merged. A Mappings team member will
    take a final look over your PR, and, if everything looks good, merge it!

## Guide: Fixing Failing Builds
There are two main reasons the build may fail on your pull request:

1. ### Spellcheck Failure
    A spellcheck failure gives the error `entry name contains unknown/misspelled word: YourEntryName`. There are two ways to fix this:
    1. #### Your word is misspelled
        If your word is misspelled, you can simply fix the error.
    2. #### Your word is not misspelled, but the build fails anyway!
        This can happen in a few cases.  
        The most common is that the word is a valid word in the context of Minecraft, but is not in the normal english dictionary, such as "prismarine".
        In this case, you can add the word to [QM's Minecraft dictionary](buildSrc/src/main/resources/minecraft_specific_words.txt) under the corresponding category, which in this case would be `items`.  
        Another possibility is that you have written the word in British English, which is invalid since QM uses American English exclusively.
        Here, just change the word to its American English equivalent.  
      
    After performing any of these fixes, run `./gradlew mappingLint` to check that the error is fixed.
2. ### Javadoc Failure
   A javadoc failure will occur when you rename a class or method that a piece of javadoc points to.
   It gives the error `error: reference not found * {@link TheOldName}`.
   To fix this, you need to change the javadoc to point to the new name. Simple!
   You can open the javadoc editor in Enigma by right-clicking the entry and selecting the `Edit Javadoc` option, or by pressing `Ctrl + D` by default.
   Run `./gradlew build javadocJar` to check your new mapping. 

## Guide: Triage Categories

Triage categories ensure that important, but small PRs--like bugfixes--are merged quickly, while large changes--like
a naming refactor--are thoroughly reviewed before they are merged.

Aside from the main triage categories, QM also divides its PRs by the type of Minecraft version they target:
releases and snapshots, with the former having longer review times and aiming for more stability.

- Release PRs are those that target a release of Minecraft, such as `1.17`, `1.17.1` or `1.18`
- Snapshot PRs are those that target a snapshot of Minecraft, such as `21w44a`, `1.17.1-pre1` or `1.18-rc4`

## PR Policy definitions
Everything within this section is the definitions for the actual PR policy followed by the Mappings team, in accordance
with [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md)

To avoid conflicts and make the process faster, the FCP may be ended early when a new Minecraft version is released and
QM is updated.

### `T: new mappings`

**Description**: Used for pull requests that focus mainly on adding new mappings.

**Required Approvals**: 2
- At least 1 approval must come directly from a Mappings team member, and the other one can come from a Mappings
Triage member or another Mappings Team member.

**Final Comment Period**:
- Snapshots: 1 day
- Releases: 2 days

**Special cases**:
- If the PR adds new mappings needed by QSL, the Final Comment Period is shortened to 12 hours.

### `T: refactor`

**Description**: Used for pull request that focus mainly on changing the names of existing mappings.

**Required Approvals**: 3
- At least 2 approvals must come directly from a Mappings team member, and the other one can come from a Mappings
Triage member or another Mappings Team member.

**Final Comment Period**:
- Snapshots: 3 days
- Releases: 6 days

### `T: documentation`

**Description**: Used for pull requests that focus mainly on adding or changing documentation.

**Required Approvals**: 2
- At least 1 approval must come directly from a Mappings team member, and the other one can come from a Mappings
Triage member or another Mappings Team member.

**Final Comment Period**:
- Snapshots: 2 days
- Releases: 4 days

### `T: toolchain changes`

**Description**: Used for pull requests that focus mainly on changes to the QM toolchain, including the buildscript,
and/or tools used.

**Required Approvals**: 1
- At least 1 approval must come directly from a Mappings team member

**Final Comment Period**:
- Snapshots: 1 day
- Releases: 2 days

### Other
Trivial fixes that do not require review (e.g. typos) are exempt from this policy. Mappings team members should
double-check with other members of the team on Discord before pushing a commit or merging a PR without going
through this process.

PRs that do not fit under any of these categories but are not "trivial fixes" are merged at the consensus of the
Mappings team, using whatever criteria they determine to be appropriate.

*This is only a summary of QM's PR process and an explanation of QM-specific exceptions to it. For exact definitions
and more information, see [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md).*
