var login = require('./util/login.js');

// test the login

module.exports = {
  "Addis login test" : function (browser) {
    login(browser, 'https://addis-test.drugis.org')
      .waitForElementVisible('h2.nested-view-header', 50000)
      .pause(1000)
      .source(function (result){
              // Source will be stored in result.value
              console.log(result.value);
          })
      .assert.containsText('#projects-page-header', 'Projects')
      .pause(1000)
      .end();
  }
};