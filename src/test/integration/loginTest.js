var login = require('./util/login.js');
var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';

module.exports = {
  "Addis login test" : function (browser) {
    login(browser, testUrl)
      .waitForElementVisible('#user-view-user-name', 50000)
      .pause(1000)
      .assert.containsText('#user-view-user-name', 'Ulrika Tester')
      .pause(1000)
      .end();
  }
};