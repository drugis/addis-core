var login = require('./util/login.js');

// test the login

module.exports = {
  "Addis login test" : function (browser) {
    login(browser, 'https://addis-test.drugis.org')
      .waitForElementVisible('body', 5000)
      .pause(2000)
      .assert.containsText('h2', 'Projects')
      .end();
  }
};