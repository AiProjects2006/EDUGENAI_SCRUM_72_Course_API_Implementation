Feature: Course request validation

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Reject a create request with required fields missing
    Given path 'api/v1/courses'
    And request {}
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

  Scenario: Reject a create request with a blank title
    Given path 'api/v1/courses'
    And request
      """
      {
        "subjectId": "#(java.lang.System.nanoTime())",
        "creatorId": "#(java.lang.System.nanoTime())",
        "title": " ",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

  Scenario: Reject an update request with a blank title
    Given path 'api/v1/courses', java.lang.System.nanoTime()
    And request
      """
      {
        "title": "",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method put
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

  Scenario: Reject create requests when each required non-title field is missing
    * def subjectId = java.lang.System.nanoTime()
    * def creatorId = java.lang.System.nanoTime()
    Given path 'api/v1/courses'
    And request { creatorId: '#(creatorId)', title: 'Valid title', level: 'BEGINNER', status: 'DRAFT' }
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

    Given path 'api/v1/courses'
    And request { subjectId: '#(subjectId)', title: 'Valid title', level: 'BEGINNER', status: 'DRAFT' }
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

    Given path 'api/v1/courses'
    And request { subjectId: '#(subjectId)', creatorId: '#(creatorId)', title: 'Valid title', status: 'DRAFT' }
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

    Given path 'api/v1/courses'
    And request { subjectId: '#(subjectId)', creatorId: '#(creatorId)', title: 'Valid title', level: 'BEGINNER' }
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

  Scenario: Return not found when creating a course for a deleted subject
    * def subjectName = 'Deleted parent subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200

    Given path 'api/v1/courses'
    And request
      """
      {
        "subjectId": "#(subjectId)",
        "creatorId": "#(java.lang.System.nanoTime())",
        "title": "Course with missing subject",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method post
    Then status 404
    And match response == { success: false, message: 'Subject not found', data: null }

  Scenario: Course filters return empty lists for unused subject and creator values
    * def subjectName = 'Empty filter subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId
    * def creatorId = java.lang.System.nanoTime()

    Given path 'api/v1/courses/subject', subjectId
    When method get
    Then status 200
    And match response.success == true
    And match response.data == []

    Given path 'api/v1/courses/creator', creatorId
    When method get
    Then status 200
    And match response.success == true
    And match response.data == []

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200
