module.exports = function(browser, url){
    return browser
      .url(url)
      .waitForElementVisible('body', 1000)
      .click('button[type="submit"]')
      .pause(1000)
      .setValue('input[type=email]', ' addistestuser1@gmail.com')
      .setValue('input[type=password]', 'addistestuser123')
      .click('input[type="submit"]')
};