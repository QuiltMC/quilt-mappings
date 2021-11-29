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

GitHub PR labels should be used to indicate which category a PR belongs to.

---

### New Mappings
**Required Approvals**: 3
- At least 2 from Mappings Triage and 1 from the Mappings Team.
- Approvals from the Mappings Team are counted as 2 from Mappings Triage, so 2 approvals from the Mappings Team is
  enough.

**Final Comment Period**: May vary depending on the complexity of the PR and the type of version it targets.
- Releases: 2 to 5 days
- Snapshots: 12 hours to 3 days, may be longer if the PR is large.

### Mapping Refactors
Typo fixes are not considered as refactor.

**Required Approvals**: 4
- At least 2 from Mappings Triage and 2 from the Mappings Team
- Approvals from the Mappings Team are counted as 2 from Mappings Triage, so 2 approvals from the Mappings Team is
  enough.

**Final Comment Period**: From 7 to 10 days

### Minor Toolchain Changes
Updates to dependencies, trivial fixes, and minor changes to the buildscript are all considered minor changes.

**Required Approvals**: 1
- Must be from the Mappings Team

**Final Comment Period**: 2 days

### Toolchain Changes
Gradle task rewrites and new features in the toolchain are considered toolchain changes.

**Required Approvals**: 2
- Must be from the Mappings Team

**Final Comment Period**: 5 days

### Major Toolchain Refactor
Refactors that require significant changes to the toolchain are considered major toolchain changes.

**Required Approvals**: 3
- Must be from the Mappings Team

**Final Comment Period**: 7 days
- If the PR contains a controversial change, the FCP may be extended beyond 7 days.

---

This is only a summary of the process. The exact rules are defined in [RFC 39](https://github.com/QuiltMC/rfcs/blob/master/structure/0039-pr-policy.md)

