import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';

const yaml = readFileSync(process.argv[2] ?? new URL('../.github/workflows/requested-pr58-device-test.yml', import.meta.url), 'utf8');
const script = yaml.split('          script: |\n')[1].split('\n').map(line => line.slice(12)).join('\n');
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const execute = new AsyncFunction('github', 'context', 'core', 'Date', script);
const source = 'ba9656f0e22980af94883783986367d7bcdf545b';
const minimum = Date.parse('2026-10-10T22:56:06Z');
const names = ['Project boundaries and CI scope', 'Build, test, lint and APK verification',
  'Room SQLCipher and Compose instrumentation', 'Android verification'];
const defaults = () => ({ time: minimum, pull: { state: 'open', merged: false, draft: true, head: { sha: source } },
  refs: [source, source], workflow: { id: 7 },
  run: { id: 38089034166, workflow_id: 7, event: 'pull_request', head_sha: source, status: 'completed', conclusion: 'success' },
  jobs: names.map(name => ({ name, status: 'completed', conclusion: 'success' })), existing: [] });

async function check(name, mutate, expectedError, expectedDispatch = 0) {
  const state = defaults(); mutate(state);
  let reads = 0; const dispatches = [];
  const listJobs = () => {}; const listRuns = () => {};
  const github = { rest: { pulls: { get: async () => ({ data: state.pull }) },
    git: { getRef: async () => ({ data: { object: { sha: state.refs[Math.min(reads++, state.refs.length - 1)] } } }) },
    actions: { getWorkflow: async () => ({ data: state.workflow }), getWorkflowRun: async () => ({ data: state.run }),
      listJobsForWorkflowRun: listJobs, listWorkflowRuns: listRuns,
      createWorkflowDispatch: async args => { dispatches.push(args); } } },
    paginate: async method => method === listJobs ? state.jobs : state.existing };
  let error;
  try { await execute(github, { repo: { owner: 'Perrystack9', repo: 'Fahrerabrechnung_Android_0.4.1' } },
    { info: () => {} }, { now: () => state.time, parse: Date.parse }); } catch (caught) { error = caught; }
  if (expectedError) assert.match(error?.message ?? '', expectedError, name);
  else assert.equal(error, undefined, name);
  assert.equal(dispatches.length, expectedDispatch, name);
  if (expectedDispatch) assert.deepEqual(dispatches[0], { owner: 'Perrystack9', repo: 'Fahrerabrechnung_Android_0.4.1',
    workflow_id: 'device-test.yml', ref: 'feature/card-master-file-import',
    inputs: { source_sha: source, include_baseline: 'false' } });
  console.log(name + ': passed');
}

await check('Before the authorized minimum', s => { s.time = minimum - 1; }, /minimum/);
await check('Exactly at the authorized minimum', () => {}, undefined, 1);
await check('Later successful verification', s => { s.time = minimum + 60000; }, undefined, 1);
await check('Changed PR source', s => { s.pull.head.sha = 'other'; }, /open draft/);
await check('Closed PR', s => { s.pull.state = 'closed'; }, /open draft/);
await check('Merged PR', s => { s.pull.merged = true; }, /open draft/);
await check('Changed current branch', s => { s.refs[0] = 'other'; }, /differs/);
await check('Changed branch during verification', s => { s.refs[1] = 'other'; }, /changed during/);
await check('Wrong workflow', s => { s.run.workflow_id = 8; }, /Android verification/);
await check('Wrong CI source', s => { s.run.head_sha = 'other'; }, /Android verification/);
await check('Incomplete CI', s => { s.run.status = 'in_progress'; }, /Android verification/);
await check('Failed CI', s => { s.run.conclusion = 'failure'; }, /Android verification/);
await check('Missing instrumentation', s => { s.jobs.splice(2, 1); }, /All four/);
await check('Skipped instrumentation', s => { s.jobs[2].conclusion = 'skipped'; }, /All four/);
await check('Incomplete instrumentation', s => { s.jobs[2].status = 'in_progress'; }, /All four/);
await check('Existing package request', s => { s.existing = [{ id: 1234 }]; }, undefined, 0);
