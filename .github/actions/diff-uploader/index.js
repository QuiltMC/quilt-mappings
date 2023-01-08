const core = require('@actions/core');
const {context, getOctokit} = require('@actions/github');
const fs = require("fs");

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

    try {
        const diff_buffer = fs.readFileSync("target.diff");
        const diff = diff_buffer.toString();

        if (diff === "") {
            await comment("No difference between head and target.");
            core.setOutput("result", "skip");
        } else {
            let changed_lines = (diff.match(new RegExp("^\\+[^+]", "gm")) || []).length;
            let changed_files = (diff.match(new RegExp("^\\+\\+\\+", "gm")) || []).length;

            core.setOutput("result", "success");
            await comment(`With commit ${context.sha}, ${changed_files} file(s) were updated and ${changed_lines} line(s) were modified compared to the latest Quilt Mappings version. \n\n\<details\>\n\<summary\>View the diff here:\</summary\> \n\<br\>\n\n\`\`\`diff\n${diff}\`\`\`\n\</details\>`)
        }
    } catch (err) {
        console.error(err);
        await comment(`No diff file generated:\n${err}`);
        core.setOutput("result", "failed");
    }
}

main().catch(error => {
    core.setFailed(error.message);
    core.error(error.stack);
});
