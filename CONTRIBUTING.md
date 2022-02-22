# Contributing

Our goal is to provide high-quality names that make intuitive sense. In our experience, this goal is best achieved
through contribution of original names, rather than copying names from other mappings projects.
While we believe that discussions relating to the names used by other mappings projects can be useful, those names
must stand up to scrutiny on their own - we won't accept names on the grounds that they're present in other mappings
projects. 

We recommend discussing your contribution with other members of the community - either directly in your pull request,
or in our other community spaces. We're always happy to help if you need us!

Please have a look at the [naming conventions](/CONVENTIONS.md) before submitting mappings.

## Pull Request Process
To get a pull request merged into Quilt Mappings, it must get a certain number of approvals from the maintainers,
and then it will enter a Final Comment Period. If the pull request passes the final comment period without opposition,
the PR will be merged. Otherwise, the PR will return to being in review.

The exact number of reviews needed, and the length of the Final Comment Period, varies depending on the scope and
complexity of the pull request. The numbers for each category are listed below.

> **Minor changes such as typo fixes may bypass the process.**

### Classifications
PRs can be classified by the type of the changes (e.g. new mappings, toolchain updates, mappings refactor, documentation),
by the type of Minecraft version they target (`release` or `snapshot`), and by their size. This classification is
done by the Mappings Team and Mappings Triage, and is used to determine the number of reviews needed and the length of
the FCP. GitHub PR labels should be used to indicate which category a PR belongs to.

#### Change type
Note that for the required reviews, approvals from members of the Mappings Team may be considered as Triage reviews.

##### New mappings
- Small PR
  - **Required Approvals: 2**, at least 1 from the Mappings Team and 1 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 1 day
    - Releases: 3 days
- Large PR
  - **Required Approvals: 3**, at least 1 from the Mappings Team and 2 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 3 days
    - Releases: 7 days

- Special cases
  - New mappings needed for QSL
    - **Final Comment Period:**
      - Snapshots & releases: 12 hours

##### Mapping refactors
Typo fixes are *not* considered refactor changes.

- Small PR
  - **Required Approvals: 3**, at least 2 from the Mappings Team and 1 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 5 days
    - Releases: 7 days
- Large PR
  - **Required Approvals: 4**, at least 2 from the Mappings Team and 2 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 7 days
    - Releases: 10 days

##### Toolchain changes
Changes to the buildscript, build system, or other tools used in QM are considered toolchain changes.

- Small PR
  - **Required Approvals: 1**, from the Mappings Team
  - **Final Comment Period:**
    - Snapshots: 2 days
    - Releases: 4 days
- Large PR
  - **Required Approvals: 3**, from the Mappings Team
  - **Final Comment Period:**
    - Snapshots: 7 days
    - Releases: 10 days

##### Documentation
- Small PR
  - **Required Approvals: 2**, at least 1 from the Mappings Team and 1 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 3 days
    - Releases: 7 days
- Large PR
  - **Required Approvals: 3**, at least 1 from the Mappings Team and 2 from Mappings Triage
  - **Final Comment Period:**
    - Snapshots: 6 days
    - Releases: 10 days

#### Target version
Affects only the length of the FCP.
- Release PRs are those that target a release of Minecraft, such as `1.17`, `1.17.1` or `1.18`
- Snapshot PRs are those that target a snapshot of Minecraft, such as `21w44a`, `1.17.1-pre1` or `1.18-rc4`

#### Size
- Small PRs are those that are focused on a single mapping, mapping set, or toolchain feature, and are not large enough
  to warrant a separate PR for each of the changes.
  - Examples include: a PR mapping all constants in a class, documenting a few methods in class; cleaning up a few
    lines of code in the buildscript, or refactoring the mappings of some methods.
- Large PRs are those that are focused on a large number of mappings, mapping sets, or toolchain, and *could* be
  separated into multiple, smaller PRs.
  - Examples include: a PR to map all the unmapped classes, refactoring many mappings
    across different classes, or refactoring the buildscript in a major scale (which was the case in
    [#7](https://github.com/QuiltMC/quilt-mappings/pull/7)).

---

This is only a summary of the process. The exact rules are defined in [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md)

