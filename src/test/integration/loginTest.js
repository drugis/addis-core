var login = require('./util/login.js');

// test the login

module.exports = {
  "Addis login test" : function (browser) {
    login(browser, 'https://addis-test.drugis.org')
      .waitForElementVisible('body', 15000)
      .pause(5000)
      .assert.containsText('h2', 'Projects')
      .pause(5000)
      .end();
  }
};