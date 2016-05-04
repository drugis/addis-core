var testUrl = process.env.ADDIS_TEST_URL ? process.env.ADDIS_TEST_URL : 'https://addis-test.drugis.org';

module.exports = function(browser, url){
    browser
      .url(url)
      .waitForElementVisible('body', 50000)
      .click('button[type="submit"]')
      .waitForElementVisible('body', 50000)
      .assert.containsText('h2', 'Sign in with your Google Account')
      .pause(1000)
      .setValue('input[type=email]', 'addistestuser1@gmail.com')
      .click('input[type="submit"]')
      .pause(1000)
      .setValue('input[type=password]', 'speciaalvoordejenkins')
      .click('#signIn');
      if (testUrl.substr(0, 16) === 'http://localhost') {
          browser.pause(3000) // wait for submit button to become active (thanks for keeping us safe google)
              .click('#submit_approve_access');
      }
      return browser;
};
