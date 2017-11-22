/** Test that WEBHOOK URI item is allowed. **/

QUnit.test("WEBHOOK URL with http and www is valid", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.hutoma.com','webhook'));
});

QUnit.test("WEBHOOK URL with http and without www is valid", function (assert) {
    assert.notOk(validation.isInputInvalid('http://hutoma.com','webhook'));
});

QUnit.test("WEBHOOK URL with http protocol is valid", function (assert) {
    assert.notOk(validation.isInputInvalid('https://www.hutoma.com','webhook'));
});

QUnit.test("WEBHOOK URL with a path is valid", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.hutoma.com/pages','webhook'));
});

QUnit.test("WEBHOOK URL with a path to file is valid", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.hutoma.com/pages/login.php','webhook'));
});

QUnit.test("WEBHOOK URL with http protocol and subdomain", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.aaa.hutoma.com','webhook'));
});

QUnit.test("WEBHOOK URL with http protocol and subdomains", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.aaa.bbb.hutoma.com','webhook'));
});

QUnit.test("WEBHOOK URL with no .com domains", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.hutoma.co.uk','webhook'));
});

QUnit.test("WEBHOOK URL supports IP addresses", function (assert) {
    assert.notOk(validation.isInputInvalid('http://192.168.0.1','webhook'));
});

QUnit.test("WEBHOOK URL supports URLs with port", function (assert) {
    assert.notOk(validation.isInputInvalid('http://www.aaa.com:1234','webhook'));
});

/** Test that WEBHOOK URI item is denied. **/

QUnit.test("WEBHOOK URL empty is not valid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});

QUnit.test("WEBHOOK URL composed by single word is not valid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});

QUnit.test("WEBHOOK URL without top-level domain is invalid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});

QUnit.test("WEBHOOK URL without http is not valid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});

QUnit.test("WEBHOOK URL without http and www is not valid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});

QUnit.test("WEBHOOK URL with different protocol by http and https is not valid", function (assert) {
    assert.ok(validation.isInputInvalid('','webhook'), "Passed");
});