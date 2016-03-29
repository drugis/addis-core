'use strict';
/* globals process */
var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';
var login = require('./util/login');
var DatasetsPage = require('./pages/datasets');


module.exports = {
  'create project from featured dataset': function(browser) {
    var datasetsPage = new DatasetsPage(browser);

    login(browser, testUrl);

    datasetsPage.waitForPageToLoad();
    datasetsPage.createFeaturedDatasetProject('intergration-test-project', 'A project used for automated testing');

    browser.pause(300);
    datasetsPage.end();
  }
};
