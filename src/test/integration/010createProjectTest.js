'use strict';
/* globals process */
var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';
var login = require('./util/login');
var DatasetsPage = require('./pages/datasets');
var ProjectPage = require('./pages/project');


module.exports = {
  'create project from featured dataset': function(browser) {
    var datasetsPage = new DatasetsPage(browser);
    var projectPage = new ProjectPage(browser);

    login(browser, testUrl);

    datasetsPage.waitForPageToLoad();
    datasetsPage.createFeaturedDatasetProject('intergration-test-project', 'A project used for automated testing');

    projectPage.waitForPageToLoad();
    projectPage.addOutcome('ham', 'a test outcome');
    projectPage.addOutcome('nau', 'a second test outcome');
    projectPage.addIntervention('parox', 'a test intervention');
    projectPage.addIntervention('fluox', 'a second test intervention');
    // projectPage.addAnalysis('network', 'test network analysis title');
    // projectPage.addAnalysis('single', 'test single-study title');
    // projectPage.addAnalysis('benefit', 'test meta-benefit-risk title');

    browser.pause(600);
    datasetsPage.end();
  }
};
