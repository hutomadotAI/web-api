/** Test that WEBHOOK URI item is allowed. **/

QUnit.test("WEBHOOK URL with http and www is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com','webhook');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("WEBHOOK URL with http and without www is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://hutoma.com','webhook');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("WEBHOOK URL with http protocol is valid", function (assert) {
    var allowed = validation.isInputInvalid('https://www.hutoma.com','webhook');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("WEBHOOK URL with a path is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages','webhook');
    assert.ok(allowed == false, "Passed");
});

QUnit.test("URL with a path to file is valid", function (assert) {
    var allowed = validation.isInputInvalid('http://www.hutoma.com/pages/login.php','webhook');
    assert.ok(allowed == false, "Passed");
});



/** Test that WEBHOOK URI item is denied. **/

QUnit.test("WEBHOOK URL empty is not valid", function (assert) {
    var allowed = validation.isInputInvalid('','webhook');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("WEBHOOK URL composed by single word is not valid", function (assert) {
    var allowed = validation.isInputInvalid('hutoma','webhook');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("WEBHOOK URL without top-level domain is invalid", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma','webhook');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("WEBHOOK URL without http is not valid", function (assert) {
    var allowed = validation.isInputInvalid('www.hutoma.com','webhook');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("WEBHOOK URL without http and www is not valid", function (assert) {
    var allowed = validation.isInputInvalid('hutoma.com','webhook');
    assert.ok(allowed == true, "Passed");
});

QUnit.test("WEBHOOK URL with different protocol by http and https is not valid", function (assert) {
    var allowed = validation.isInputInvalid('abc://www.hutoma.com','webhook');
    assert.ok(allowed == true, "Passed");
});