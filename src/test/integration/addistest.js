var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';

module.exports = {
  'Demo test addis' : function (browser) {
    browser
      .url(testUrl)
      .waitForElementVisible('body', 1000)
      .pause(1000);
    if(testUrl === 'https://addis-test.drugis.org'){
      browser.assert.containsText('h1', 'test-addis.drugis.org')
    } else {
      browser.assert.containsText('h1', 'addis.drugis.org')
    }
    browser.end();
  }
};
