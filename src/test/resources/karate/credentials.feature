Feature: Credential Store API - POST /v1/credentials

  Background:
    * url baseUrl

  # ──────────────────────────────────────────────────
  # POSITIVE SCENARIOS
  # ──────────────────────────────────────────────────

  Scenario: Valid nric and uuid stores credential and returns 201
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And header X-Request-Id = 'karate-trace-001'
    And request { nric: 'S1234567D', uuid: '#(uuid)' }
    When method POST
    Then status 201
    And match response.status == 'stored'
    And match response.message == 'Credential saved successfully'

  Scenario: Request without X-Request-Id header is accepted
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: 'T9876543Z', uuid: '#(uuid)' }
    When method POST
    Then status 201
    And match response.status == 'stored'

  Scenario: Same nric with different uuid stores successfully
    * def uuid1 = java.util.UUID.randomUUID() + ''
    * def uuid2 = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '#(uuid1)' }
    When method POST
    Then status 201

    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '#(uuid2)' }
    When method POST
    Then status 201

  # ──────────────────────────────────────────────────
  # DUPLICATE UUID
  # ──────────────────────────────────────────────────

  Scenario: Duplicate uuid returns 409 conflict
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '#(uuid)' }
    When method POST
    Then status 201

    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '#(uuid)' }
    When method POST
    Then status 409
    And match response.status == 'conflict'
    And match response.message == '#notnull'

  # ──────────────────────────────────────────────────
  # NEGATIVE SCENARIOS
  # ──────────────────────────────────────────────────

  Scenario: Missing nric field returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { uuid: '#(uuid)' }
    When method POST
    Then status 400

  Scenario: Missing uuid field returns 400
    Given path '/v1/credentials'
    And request { nric: 'S1234567D' }
    When method POST
    Then status 400

  Scenario: Blank nric returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: '', uuid: '#(uuid)' }
    When method POST
    Then status 400

  Scenario: Blank uuid returns 400
    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '' }
    When method POST
    Then status 400

  Scenario: Empty body returns 400
    Given path '/v1/credentials'
    And request {}
    When method POST
    Then status 400

  # ──────────────────────────────────────────────────
  # SQL INJECTION ATTEMPTS
  # ──────────────────────────────────────────────────

  Scenario: SQL injection via single quote in nric returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: "' OR '1'='1", uuid: '#(uuid)' }
    When method POST
    Then status 400

  Scenario: SQL injection via UNION SELECT in nric returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: '1 UNION SELECT * FROM credential_records', uuid: '#(uuid)' }
    When method POST
    Then status 400

  Scenario: SQL injection via DROP TABLE in nric returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: "S1234567D; DROP TABLE credential_records--", uuid: '#(uuid)' }
    When method POST
    Then status 400

  Scenario: SQL injection via comment in uuid returns 400
    Given path '/v1/credentials'
    And request { nric: 'S1234567D', uuid: '550e8400-e29b-41d4-a716-446655440000 -- injected' }
    When method POST
    Then status 400

  Scenario: SQL injection via SLEEP function returns 400
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials'
    And request { nric: '1 AND SLEEP(5)', uuid: '#(uuid)' }
    When method POST
    Then status 400

  # ──────────────────────────────────────────────────
  # METHOD NOT ALLOWED
  # ──────────────────────────────────────────────────

  Scenario: GET on credentials endpoint returns 405
    Given path '/v1/credentials'
    When method GET
    Then status 405

  Scenario: DELETE on credentials endpoint returns 405
    Given path '/v1/credentials'
    When method DELETE
    Then status 405

  # ──────────────────────────────────────────────────
  # NOT FOUND
  # ──────────────────────────────────────────────────

  Scenario: Non-existent sub-path returns 404
    * def uuid = java.util.UUID.randomUUID() + ''
    Given path '/v1/credentials/nonexistent'
    And request { nric: 'S1234567D', uuid: '#(uuid)' }
    When method POST
    Then status 404