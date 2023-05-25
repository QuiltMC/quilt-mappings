const core = require('@actions/core');
const { summary } = require('@actions/core/lib/summary');
const {context, getOctokit} = require('@actions/github');
const fs = require("fs");

const maxDiffCommentLength = 64000;
const maxDiffComments = 3;

function splitDiff(diff, depth) {
    if (diff.length <= maxDiffCommentLength) {
        return [diff];
    }

    let lastDiffLiteralIndex = diff.lastIndexOf("\ndiff", maxDiffCommentLength) + 1;

    let resultDiff;
    let remainingDiff;
    if (lastDiffLiteralIndex == 0) {
        let index = diff.lastIndexOf("\n", maxDiffCommentLength) + 1;
        resultDiff = diff.substring(0, index) + "\nThe diff for this file is not complete!";
        remainingDiff = "Continuation of the last diff...\n" + diff.substring(index);
    } else {
        resultDiff = diff.substring(0, lastDiffLiteralIndex);
        remainingDiff = diff.substring(lastDiffLiteralIndex);
    }

    if (depth < maxDiffComments - 1) {
        return [resultDiff].concat(splitDiff(remainingDiff, depth + 1));
    } else {
        return [resultDiff + "\n\nThe remaining diff is too long!"];
    }
}

async function commentDiff(summary, diff, doComment) {
    if (diff.length > maxDiffCommentLength) {
        let diffs = splitDiff(diff, 0);
        let l = diffs.length;
        for (let i = 0; i < l; i++) {
            let iDiff = diffs[i];
            let pref = i == 0 ? summary + "\n\n" : "";

            // Formatting :pain:
            await doComment(
`${pref}\<details\>
\<summary\>View the diff here (${i + 1}/${l}):\</summary\>
\<br\>

\`\`\`diff
${iDiff}
\`\`\`
\</details\>`
);
        }
    } else {

        await doComment(
`${summary}

\<details\>
\<summary\>View the diff here:\</summary\>
\<br\>

\`\`\`diff
${diff}
\`\`\`
\</details\>`
);
    }
}

async function main() {
    const token = core.getInput('github-token', {required: true});
    const octokit = getOctokit(token);
    const github = octokit.rest;
    const repo = context.repo.repo;

    const owner = context.repo.owner;
    const baseParameters = {owner, repo};
    let comment = async function (comment) {
        return console.log(comment);
    }

    const targetSrcExists = core.getInput('target-src-exists', {required: true}) == 'true';

    if (context.eventName === "push") {
        comment = async function (comment) {
            return github.repos.createCommitComment({
                ...baseParameters,
                commit_sha: context.sha,
                body: comment
            });
        }
    } else if (context.eventName === "pull_request") {
        // TODO: is there a way to make a pr comment from a fork action?
        // comment = async function (comment) {
        //     return github.issues.createComment({
        //         ...baseParameters,
        //         issue_number: context.payload.number,
        //         body: comment
        //     });
        // }
    }
    if (!targetSrcExists) {
        await comment(`No diff file generated.`);
        core.setOutput("result", "skip");
        return;
    }

    try {
        const diff_buffer = fs.readFileSync("target.diff");
        const diff = diff_buffer.toString();

        if (diff === "") {
            await comment("No difference between head and target.");
            core.setOutput("result", "skip");
        } else {
            let added_lines = (diff.match(new RegExp("^\\+[^+]", "gm")) || []).length;
            let removed_lines = (diff.match(new RegExp("^-[^-]", "gm")) || []).length;
            let modified_files = (diff.match(new RegExp("^\\+\\+\\+", "gm")) || []).length;

            core.setOutput("result", "success");
            await commentDiff(`With commit ${context.sha}, ${modified_files} file(s) were updated with ${added_lines} line(s) added and ${removed_lines} removed compared to the latest Quilt Mappings version.`, diff, comment);
        }
    } catch (err) {
        console.error(err);
        await comment(`Error reading diff:\n${err}`);
        core.setOutput("result", "fail");
    }
}

main().catch(error => {
    core.setFailed(error.message);
    core.error(error.stack);
});
