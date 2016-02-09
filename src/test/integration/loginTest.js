var login = require('./util/login.js');

// test the login

module.exports = {
  "Addis login test" : function (browser) {
    login(browser, 'https://addis-test.drugis.org')
      .waitForElementVisible('#user-view-user-name', 50000)
      .pause(1000)
      .assert.containsText('#user-view-user-name', 'Ulrika Tester')
      .pause(1000)
      .end();
  }
};