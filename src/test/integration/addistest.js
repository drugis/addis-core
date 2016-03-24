var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';

module.exports = {
  'Demo test addis' : function (browser) {
    browser
      .url(testUrl)
      .waitForElementVisible('body', 1000)
      .source(function (result){
              // Source will be stored in result.value
              console.log('source: ');
              console.log(result.value);
          })
      .pause(5000)
      .assert.containsText('h1', 'addis.drugis.org')
      .end();
  }
};
