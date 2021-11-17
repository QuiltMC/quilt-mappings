import * as core from '@actions/core';
import { context, getOctokit } from '@actions/github';

const updateLabel = 'update-base';
const outdatedLabel = 'outdated';
const releaseLabel = 'release';
const snapshotLabel = 'snapshot';

async function main() {
    const token = core.getInput('github-token', { required: true });
    const octokit = getOctokit(token);
    const github = octokit.rest;

    const pull_number = core.getInput('pull-number', { required: true });

    const repo = context.repo.repo;
    const owner = context.repo.owner;
    const baseParameters = { owner, repo };

    async function createComment(comment) {
        return github.issues.createComment({ ...baseParameters, issue_number: pull_number, body: comment });
    }

    const labelsResponse = await github.issues.listLabelsOnIssue({ ...baseParameters, issue_number: pull_number });
    const labels = new Set(labelsResponse.data.map(label => label.name));

    if (!labels.has(updateLabel)) {
        core.info('Nothing to do, the pr ' + pull_number + ' does not have the label ' + updateLabel);
        return;
    }

    labels.delete(updateLabel);

    const repoData = (await github.repos.get({ ...baseParameters })).data;
    const base = repoData.default_branch;

    const pullData = (await github.pulls.get({ ...baseParameters, pull_number })).data;
    if (pullData.base.ref == base) {
        await createComment('🚨 Target branch is already set to ' + base);
    } else {
        await github.pulls.update({ ...baseParameters, pull_number, base });
        await createComment('🚀 Target branch has been updated to ' + base);

        try {
            await github.pulls.updateBranch({ ...baseParameters, pull_number });
        } catch (error) {
            // 422 is returned when there is a merge conflict
            if (error.status === 422) {
                await createComment('🚨 Please fix merge conflicts before this can be merged' );
                labels.add(outdatedLabel);
            } else {
                throw error;
            }
        }

        // Pre releases have '-', snapshots have 'w'
        const isSnapshot = base.includes('-') || base.includes('w');
        if (isSnapshot) {
            labels.delete(releaseLabel);
            labels.add(snapshotLabel);
        } else if (isSnapshot) {
            labels.delete(snapshotLabel);
            labels.add(releaseLabel);
        }
    }

    const labelsArray = [...labels];
    await github.issues.setLabels({ ...baseParameters, issue_number: pull_number, labels: labelsArray });
}

main().catch(error => {
    core.setFailed(error.message);
    core.error(error.stack);
});
