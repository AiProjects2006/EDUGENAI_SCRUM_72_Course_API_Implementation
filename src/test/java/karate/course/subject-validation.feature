Feature: Subject request validation

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Reject a subject create request with the name missing
    Given path 'api/v1/subjects'
    And request {}
    When method post
    Then status 400
    And match response == { success: false, message: 'Subject name is required', data: null }

  Scenario: Reject a subject create request with a blank name
    Given path 'api/v1/subjects'
    And request { name: ' ', description: 'Invalid request' }
    When method post
    Then status 400
    And match response == { success: false, message: 'Subject name is required', data: null }

  Scenario: Reject a subject update request with a blank name
    * def subjectName = 'Subject update validation ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId

    Given path 'api/v1/subjects', subjectId
    And request { name: ' ', description: 'Invalid update' }
    When method put
    Then status 400
    And match response == { success: false, message: 'Subject name is required', data: null }

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200

  Scenario: Reject null subject names for create and update
    Given path 'api/v1/subjects'
    And request { name: null }
    When method post
    Then status 400
    And match response == { success: false, message: 'Subject name is required', data: null }

    * def subjectName = 'Null subject update ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId

    Given path 'api/v1/subjects', subjectId
    And request { name: null }
    When method put
    Then status 400
    And match response == { success: false, message: 'Subject name is required', data: null }

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200
