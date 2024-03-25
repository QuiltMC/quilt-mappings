const core = require('@actions/core');
const { context, getOctokit } = require('@actions/github');

const token = core.getInput('github_token', { required: true });
const octokit = getOctokit(token);
const github = octokit.rest;

// Regex that matches any snapshots, including -pre and -rc version numbers.
const snapshotRegex = new RegExp("\d\dw\d\d[a-e]|\d\.\d\d\.\d\d?-(?:pre|rc)\d")

// Any labels that are used in the code.
const fcpLabel = 'final-comment-period';
const tinyLabel = "s: tiny";
const smallLabel = "s: small";
const medLabel = "s: medium";
const largeLabel = "s: large";
const newLabel = "T: new";
const refactorLabel = "T: refactor";
const docLabel = "T: documentation";
const toolLabel = "T: toolchain";

var mappingsTeam;

const fcpStartCommentTemplate = `
:tada: The Final Comment Period has started! This PR will be merged in daysTillEnd days, unless there are any outstanding reviews at that time!

end-fcp: endFcpString
`
// Comment that gets posted when FCP has passed for a PR and it's good to merge.
const fcpDoneComment = "FCP for this PR has passed! A Quilt Mappings team member should merge this.";

// Get all members of Quilt's Mappings and Mappings Triage teams.
async function getMappingsTeam() {
    const mappingsTeamReq = await github.teams.listMembersInOrg({ org: "QuiltMC", team_slug: "mappings" });
    const triageTeamReq = await github.teams.listMembersInOrg({ org: "QuiltMC", team_slug: "mappings-triage" });

    // Merge both teams, removing duplicates
    mappingsTeam = mappingsTeamReq.map((member) => member.login);
    const triageTeam = triageTeamReq.map((member) => member.login);
    triageTeam.forEach((member) => (mappingsTeam.some((existing) => predicate(member, existing)) ? null : mappingsTeam.push(member)));
}

// Check the size of a PR and add the appropriate label.
async function addSizeLabel(size, num) {
    if (size < 50) {
        await github.issues.addLabels({ ...baseParameters, issue_number: num, labels: [tinyLabel] });
    } else if (size < 200) {
        await github.issues.addLabels({ ...baseParameters, issue_number: num, labels: [smallLabel] });
    } else if (size < 700) {
        await github.issues.addLabels({ ...baseParameters, issue_number: num, labels: [medLabel] });
    } else {
        await github.issues.addLabels({ ...baseParameters, issue_number: num, labels: [largeLabel] });
    };
}

// Calcuate the number of days a PR should be in FCP.
// Returns that as an int to use in checkFCPTime()
// If it returns 0, it's ineligible for FCP and the caller should error out.
// See CONTRIBUTING.md for how these are defined.
async function calcFCPDate(pull) {
    // Get new labels, since if a size label was just added, it's not in the previous
    // view of the labels.
    labelReq = await github.issues.listLabelsOnIssue({ ...baseParameters, issue_number: pull.number });
    labels = labelReq.map((label) => label.name);
    reviews = await github.pulls.listReviews({ ...baseParameters, pull_number: pull.number });
    approvals = reviews.map((review) => review.state == "APPROVED");
    team_approve = approvals.map((review) => mappingsTeam.includes(review.user.login));

    // Immediately error out if there's 0 or 1 reviews.
    if (approvals.length < 2) {
        core.warning(`PR ${pull.number} doesn't have enough reviews! Ineligible for FCP!`);
        return 0;
    }

    // New Mappings or Documentation
    // Since these have the same logic, no need to duplicate the code.
    if (labels.includes(newLabel) || labels.includes(docLabel)) {
        // Ensure at least 1 review is from Mappings team,
        // and that there's 2+ reviews in total.
        if (team_approve.length >= 1 && approvals.length >= 2) {
            core.info(`PR ${pull.number} matches approval count threshold for FCP!`);
            // Check if performed against a snapshot branch.
            // If true, then FCP is 1 day as long as there are at least 3 approvals.
            if (snapshotRegex.test(pull.base.ref) && approvals.length >= 3) {
                core.info(`PR ${pull.number} is eligible for 1 day FCP!`);
                return 1;
            } else {
                // Since it matches both conditions and isn't a snapshot, then we have a 2 day FCP.
                return 2;
            }
        } else if (team_approve.length == 0) {
            core.warning(`PR ${pull.number} doesn't have any team member reviews! Ineligible for FCP!`);
            return 0;
        }
    }
    // Mapping Refactors
    else if (labels.includes(refactorLabel)) {
        // Ensure at least 2 reviews are from Mappings team,
        // and that there's 3+ reviews in total.
        if (team_approve.length >= 2 && approvals.length >= 3) {
            core.info(`PR ${pull.number} matches approval count threshold for FCP!`);
            // Check refactor size and determine length.
            // Tiny or Small: 2 days
            if (labels.includes(tinyLabel) || labels.includes(smallLabel)) {
                return 2;
                // Medium: 3 days
            } else if (labels.includes(medLabel)) {
                return 3;
                // Large: 4 days
            } else if (labels.includes(largeLabel)) {
                return 4;
            } else {
                core.warning(`Unable to determine size of PR ${pull.number}! Unable to calculate FCP!`);
                return 0;
            }
        } else if (team_approve.length < 2) {
            core.warning(`PR ${pull.number} doesn't have enough team member reviews! Ineligible for FCP!`);
            return 0;
        }
    }
    // Toolchain Changes
    else if (labels.includes(toolLabel)) {
        // Ensure at least 2 reviews from Mappings team,
        if (team_approve.length >= 2) {
            core.info(`PR ${pull.number} matches approval count threshold for FCP!`);
            return 2;
        } else if (team_approve.length < 2) {
            core.warning(`PR ${pull.number} doesn't have enough team member reviews! Ineligible for FCP!`);
            return 0;
        }
    }
    // No type label, error out.
    else {
        core.error(`PR ${pull.number} has no type label, unable to determine FCP!`);
        return 0;
    }
}

// Check if time for FCP has elapsed. If it has, return 0.
// If it hasn't, return 1.
// If there's an error in a called function, return 2.
async function checkFCPTime(comment, pull) {
    commentTime = new Date(Date.parse(comment.created_at));
    core.info(`FCP Comment Time for PR ${pull.number}: ${commentTime}`);
    fcpFinalTime = new Date(commentTime);
    fcpDate = await calcFCPDate()
    // If it's 0, we got an error on the previous step
    // No need to continue.
    if (fcpDate == 0) {
        return 2;
    }
    fcpFinalTime.setDate(fcpFinalTime.getDate() + fcpDate);
    fcpFinalTime.setHours(12);
    fcpFinalTime.setMinutes(0);
    fcpFinalTime.setSeconds(0);
    core.info(`FCP Time for PR ${pull.number}: ${fcpFinalTime}`);
    if (Date.now() >= fcpFinalTime) {
        core.info(`FCP for PR ${pull.number} has passed, notifying team.`);
        try {
            await github.issues.createComment({ ...baseParameters, issue_number: pull.number, body: fcpDoneComment });
            return 0;
        } catch (error) {
            core.error(`Unable to post FCP comment for PR ${pull.number}! Please merge this PR!`)
        }
    } else {
        core.info(`FCP has not passed for PR ${pull.number}, not notifying team.`);
        return 1;
    }
}

// Check that the FCP comment is made by a Mappings team member.
// If it is, return 0
// If there is an FCP comment made by a non-team member, return 1.
async function checkFCPComment(comment) {
    // Ensure start-fcp comment is made by someone on mappings team.
    if (mappingsTeam.includes(comment.user.login)) {
        core.info("FCP Label made by valid Mappings Team user");
        return 0;
    } else if (!mappingsTeam.includes(comment.user.login)) {
        core.warning(`FCP Comment Invalid: made by user ${comment.user.login}, who is not a member of the Mappings Team.`);
        return 1;
    }
}

async function main() {
    const repo = context.repo.repo;
    const owner = context.repo.owner;
    const baseParameters = { owner, repo };
    await getMappingsTeam();

    // When triggered on schedule, check all open FCP comments for when their FCP ends.
    if (context.eventName == "schedule") {
        const pullsReq = await github.pulls.list({ ...baseParameters, state: "open", sort: "created" });
        pulls = pullsReq.data;

        for (const pull of pulls) {
            core.info(`Processing PR ${pull.number}`);
            prCommentsReq = await github.issues.listComments({ ...baseParameters, issue_number: pull.number });
            prComments = prCommentsReq.data;
            labels = pull.labels.map((label) => label.name);

            // Check PR size and add appropriate label
            size = pull.additions + pull.deletions;
            await addSizeLabel(size, pull.number);

            // FCP Processing
            for (comment of prComments) {
                // Check FCP Status
                if (comment.body.includes("start-fcp")) {
                    commentStatus = await checkFCPComment(comment);
                    if (commentStatus == 1) {
                        return;
                    }
                    // Check the FCP time.
                    fcpTime = await checkFCPTime(comment, pull);
                    if (fcpTime == 2) {
                        core.warning(`Error Processing FCP comment for PR ${pull.number}, please check manually if FCP has passed!`)
                    }
                }
            }
        }
    } else if (context.eventName == "pull_request_target") {

    }
}

main().catch(error => {
    core.setFailed(error.message);
    core.error(error.stack);
});
