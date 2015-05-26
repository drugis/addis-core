module.exports = function(browser, url){
    return browser
      .url(url)
      .waitForElementVisible('body', 50000)
      .click('button[type="submit"]')
      .waitForElementVisible('body', 50000)
      .assert.containsText('h2', 'Sign in with your Google Account')
      .pause(1000)
      .setValue('input[type=email]', 'addistestuser1@gmail.com')
      .setValue('input[type=password]', 'speciaalvoordejenkins')
      .click('input[type="submit"]')
};