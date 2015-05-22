module.exports = {
  'Demo test addis' : function (browser) {
    browser
      .url('https://addis-test.drugis.org')
      .waitForElementVisible('body', 1000)
      .pause(1000)
      .assert.containsText('h1', 'addis.drugis.org ')
      .end();
  }
};