'use strict';
/* globals process */
var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';
var login = require('./util/login');

var DatasetsPage = require('./pages/datasets');
var ProjectsPage = require('./pages/projects');
var ProjectPage = require('./pages/project');
var NmaPage = require('./pages/networkMetaAnalysis');

module.exports = {
  'Warn on overlap test': function(browser) {
    var datasetsPage = new DatasetsPage(browser);
    var projectsPage = new ProjectsPage(browser);
    var projectPage = new ProjectPage(browser);
    var nmaPage = new NmaPage(browser);


    login(browser, testUrl);

    datasetsPage.waitForPageToLoad();
    datasetsPage.selectProjectsTab();

    projectsPage.waitForPageToLoad();
    projectsPage.selectProjectByName('overlapping intervention intergration-test-project');
    projectPage.addAnalysis('network', 'test network analysis title');

    browser.pause(300);
    browser.assert.urlContains('nma');

    nmaPage.waitForPageToLoad();
    nmaPage.checkInterventionOverlap();

    browser.pause(600);
    datasetsPage.end();
  }
};
