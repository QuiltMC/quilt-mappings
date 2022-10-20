/******/ (() => { // webpackBootstrap
/******/ 	var __webpack_modules__ = ({

/***/ 18:
/***/ ((module) => {

module.exports = eval("require")("@actions/core");


/***/ }),

/***/ 496:
/***/ ((module) => {

module.exports = eval("require")("@actions/github");


/***/ })

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __nccwpck_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			// no module.id needed
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		var threw = true;
/******/ 		try {
/******/ 			__webpack_modules__[moduleId](module, module.exports, __nccwpck_require__);
/******/ 			threw = false;
/******/ 		} finally {
/******/ 			if(threw) delete __webpack_module_cache__[moduleId];
/******/ 		}
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
/******/ 	/* webpack/runtime/compat */
/******/ 	
/******/ 	if (typeof __nccwpck_require__ !== 'undefined') __nccwpck_require__.ab = __dirname + "/";
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be isolated against other modules in the chunk.
(() => {
const core = __nccwpck_require__(18);
const { context, getOctokit } = __nccwpck_require__(496);

const updateLabel = 'update-base';
const outdatedLabel = 'outdated';
const releaseLabel = 'v: release';
const snapshotLabel = 'v: snapshot';

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
        core.setOutput('result', 'skipped');
        return;
    }

    labels.delete(updateLabel);

    const repoData = (await github.repos.get({ ...baseParameters })).data;
    const base = repoData.default_branch;

    const pullData = (await github.pulls.get({ ...baseParameters, pull_number })).data;
    if (pullData.base.ref == base) {
        await createComment('🚨 Target branch is already set to ' + base);
        core.setOutput('result', 'already-set');
    } else {
        if (pullData.maintainer_can_modify != true) {
            // Can't edit head branch, change base branch, send warning and add 'outdated' label
            await github.pulls.update({ ...baseParameters, pull_number, base });
            await createComment('🚨 Cannot merge base branch to the pull request, no permission\n\n'
                + '@' + pullData.user.login + ' please merge the base branch to the pull request manually '
                + 'or toggle "Allow edits by maintainers" to true');
            labels.add(outdatedLabel);
    
            core.setOutput('result', 'no-permission');
        } else {
            // Update base branch to repo default branch
            await github.pulls.update({ ...baseParameters, pull_number, base });
            await createComment('🚀 Target branch has been updated to ' + base);

            try {
                // Merge the base branch into the pull request branch
                await github.pulls.updateBranch({ ...baseParameters, pull_number });
                core.setOutput('result', 'success');
            } catch (error) {
                // 422 is returned when there is a merge conflict
                if (error.status === 422) {
                    await createComment('🚨 Please fix merge conflicts before this can be merged' );
                    labels.add(outdatedLabel);
                    core.setOutput('result', 'outdated');
                } else {
                    core.setOutput('result', 'error');
                    throw error;
                }
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

})();

module.exports = __webpack_exports__;
/******/ })()
;